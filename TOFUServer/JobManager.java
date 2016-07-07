/* ************************************************************** *
 *                                                                *
 * Copyright (c) 2005, Ryo Kanbayashi, All rights reserved.       *
 *                                                                *
 *                                                                *
 * This software is distributed under the modified BSD License.   *
 * ************************************************************** */

import java.io.IOException;
import java.io.Serializable;
import java.util.*;
import java.util.LinkedHashMap;
import java.util.LinkedList;

//ジョブの再配布など、ジョブ全体の管理を行うクラス
public class JobManager implements Serializable{
	private final int QUEUE_DANGER_SIZE = 3;       //ジョブの補給を行わなくてはならないキューのジョブ数
	private final int QUEUE_FILL_SIZE = 10;        //ジョブを補給する時にいくつまで埋めるか

    /**
     * 
     * @uml.property name="redundancyCount" multiplicity="(0 1)"
     */
    private int redundancyCount = 2; //冗長性のために、同じジョブをいくつ配布するか。デフォルトは２

    /**
     * 
     * @uml.property name="ancestor"
     * @uml.associationEnd multiplicity="(1 1)"
     */
    private transient ServerMain ancestor = null;

    /**
     * 
     * @uml.property name="generater"
     * @uml.associationEnd multiplicity="(1 1)"
     */
    private volatile DataGeneraterImpl generater; //ジョブの生成を請け負うクラス

    /**
     * 
     * @uml.property name="resultDB"
     * @uml.associationEnd multiplicity="(1 1)"
     */
    private volatile ResultDatabase resultDB = null; //完了したジョブを保持するクラス

/**
 * 
 * @uml.property name="distributeJobQueue"
 * @uml.associationEnd multiplicity="(1 1)"
 */
//	private volatile LinkedList distributeJobQueue = new LinkedList();     //配布待ちのジョブのキュー
public volatile PersistentLinkedList distributeJobQueue = new PersistentLinkedList(
    100,
    "./tmp/distributeJobQueue.tmp"); //配布待ちのジョブのキュー

/**
 * 
 * @uml.property name="waitingBackJobs"
 * @uml.associationEnd multiplicity="(1 1)"
 */
//	public volatile LinkedHashMap waitingBackJobs = new LinkedHashMap();   //配布されて、返ってくるのを待っているjobをServerLogWraperで保持
public volatile PersistentLinkedHashMap waitingBackJobs = new PersistentLinkedHashMap(
    100,
    "./tmp/waitingBackJobs.tmp"); //配布されて、返ってくるのを待っているjobをServerLogWraperで保持

    /**
     * 
     * @uml.property name="pastJobDB"
     * @uml.associationEnd multiplicity="(1 1)"
     */
    public volatile ServerPastJobDatabase pastJobDB = new ServerPastJobDatabase(); //過去ログのためのデータベース。ユーザと接触するのはresultDBの方

	
	public JobManager(ServerMain ancestor,DataGeneraterImpl generater,ResultDatabase database){
		this.ancestor = ancestor;
		this.generater = generater;
		this.resultDB = database;
		fillDistributeJobQ(QUEUE_FILL_SIZE);        //ジョブを規定数埋めて初期化
	}
	
	//DataGeneraterImplのgenerateDataメソッドのラッパーメソッド。
	//同じクライアントに同じＪｏｂは配布しない
	public synchronized CulculateData generateJob(byte[] clientSigniture){
		CulculateData newJob = removeOneJobFromQueue();
		
		//まだ返却待機キューにつまれていなければ
		if(isExistWaitingjob(newJob.getSigniture())!=true){
			newJob.setDistributeDate(Calendar.getInstance());    //配布した日時を記録しておく
			addNewWaitingJob(newJob);    //返却待機ジョブとして追加
//			配布したクライアントのシグニチャを記録しておく
			getWaitingJob(newJob.getSigniture()).addDistributer(clientSigniture);
			return newJob;
		}else{   //つまれていた場合
			
			//同じジョブが同じクライアントに以前配布されていた場合
			if(getWaitingJob(newJob.getSigniture()).checkFormerDistributer(clientSigniture)==true){
				addReJobToQueue(newJob); //取り出してしまったものを戻す
				CulculateData tmp = serchFreshJob(clientSigniture); //未配布のものを取り出す
				tmp.setDistributeDate(Calendar.getInstance());
				addNewWaitingJob(tmp);     //返却待機ジョブとして追加
//これ意味不明なことしてる予感				getWaitingJob(newJob.getSigniture()).addDistributer(clientSigniture);
				addDistributerToWaitingQ(tmp.getSigniture(),clientSigniture);
				return tmp;    
			}else{   //以前配布されていなければ、そのまま配布する
//				配布したクライアントのシグニチャを記録しておく
				getWaitingJob(newJob.getSigniture()).addDistributer(newJob.getSigniture());
				return newJob;
			}
		}
	}
	
	//配布した時にジョブを返却待機キューに新規追加するためのメソッド
	//上書きになる場合は何もしないので注意。更新する場合は別のメソッドを利用すること
	//※ジョブにシグニチャをセットしてから使用すること
	private void addNewWaitingJob(CulculateData job){
		if(waitingBackJobs.containsKey(job.getSigniture())==false){    //まだそのジョブが含まれていなかった場合
			synchronized(waitingBackJobs){
			    ServerMain.fMonitor.informChildMoving();
			    
				try {
                    waitingBackJobs.put(byteArrayToString(job.getSigniture()),new ServerLogWraper(job,redundancyCount));
                } catch (IOException e) {
                    e.printStackTrace();
                    throw new RuntimeException();
                }finally{
                    ServerMain.fMonitor.cancelChildMoving();    
                }
			}
		}else{    //すでにそのＪｏｂが含まれていた場合
			//何もしない
		}
	}
	
	//指定したジョブが待ち状態としてキューにつまれているか
	private boolean isExistWaitingjob(byte[] signiture){
		boolean tmp;
	    synchronized(waitingBackJobs){
		    ServerMain.fMonitor.informChildMoving();
		    try{
		        tmp = waitingBackJobs.containsKey(byteArrayToString(signiture));    
		    }finally{
		        ServerMain.fMonitor.cancelChildMoving();    
		    }
	    }
	    return tmp;
	}
	
	//配布した時にジョブを返却待機キューに追加するためのメソッド
	//※ジョブにシグニチャをセットしてから使用すること
	private void addWaitingJob(ServerLogWraper job){
		synchronized(waitingBackJobs){
		    ServerMain.fMonitor.informChildMoving();
		    
			try {
                waitingBackJobs.put(byteArrayToString(job.getWrapped().getSigniture()),job);
            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException();
            }finally{
                ServerMain.fMonitor.cancelChildMoving();    
            }
		}
	}
	
	//配布待ちキューからjobをひとつ取り出し、キューの中の残りジョブ数が少なくなった場合補充する
	private CulculateData removeOneJobFromQueue(){
		
	    CulculateData job;
	    synchronized(distributeJobQueue){
		    ServerMain.fMonitor.informChildMoving();
			try{
//				ジョブが少なくなっている場合補給する
				if(distributeJobQueue.size() < QUEUE_DANGER_SIZE){
					fillDistributeJobQ(QUEUE_FILL_SIZE);
				}
				
				job = (CulculateData) distributeJobQueue.getFirst();
				distributeJobQueue.remove(job);      //getFirstは得るだけで取り除かないので取り除く
			}finally{
			    ServerMain.fMonitor.cancelChildMoving();    
			}
		}
		return job;
	}
	
	private ServerLogWraper removeWaitingJob(byte[] signiture){
		ServerLogWraper tmp;
	    synchronized(waitingBackJobs){
		    ServerMain.fMonitor.informChildMoving();
		    try{
		        tmp =  (ServerLogWraper) waitingBackJobs.remove(byteArrayToString(signiture));    
		    }finally{
		        ServerMain.fMonitor.cancelChildMoving();    
		    }
	    }
	    return tmp;
	}
	
//	バイト配列をStringの文字列へ変換
	private String byteArrayToString(byte[] array) {
		StringBuffer tmp = new StringBuffer();
		for (int i = 0; i < array.length; i++) {
			tmp.append(array[i]);
		}
		String result = tmp.toString();
		return result;
	}
	
	//指定された数以上まで（正確にその値になることは保証しない）配布待ちジョブのキューを埋める
	private void fillDistributeJobQ(int count){
		synchronized(distributeJobQueue){
		    ServerMain.fMonitor.informChildMoving();
		    try{
				//指定した数になるまで末尾にジョブを加える（シグニチャなどの各種プロパティは取り出す時に付加）
				while(distributeJobQueue.size() < count){
					CulculateData tmp = generater.generateData();
					//乱数とシステムタイムでシグニチャを生成し設定
					tmp.setSigniture(ServerMain.sDataToFile.generateHash(String.valueOf((long)(Math.random()*100000000000000000L))));    //jobのシグニチャを記録しておく
					
					//同じジョブを指定された数だけ追加
					for(int i=0;i<redundancyCount;i++){
						try {
	                        distributeJobQueue.addLast(tmp);
	                    } catch (IOException e) {
	                        e.printStackTrace();
	                    }
					}
				}
		    }finally{
		        ServerMain.fMonitor.cancelChildMoving();    
		    }
		}
	}
	
	//再配布のジョブを配布待ちキューの始めに追加する
	public void addReJobToQueue(CulculateData reJob){
		synchronized(distributeJobQueue){
		    ServerMain.fMonitor.informChildMoving();
		    
			try {
                distributeJobQueue.addFirst(reJob);
            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException();
            }finally{
                ServerMain.fMonitor.cancelChildMoving();    
            }
		}
	}
	
	//ファイルに保存していた物を再び利用する時に参照を再設定するためのメソッド
	public void setReference(ServerMain ancestor,DataGeneraterImpl generater,ResultDatabase database){
		this.ancestor = ancestor;
		this.generater = generater;
		this.resultDB = database;
	}
	
	private ServerLogWraper getWaitingJob(byte[] signiture){
		Object tmp;
		synchronized(waitingBackJobs){
		    ServerMain.fMonitor.informChildMoving();
		    try{
		        tmp = waitingBackJobs.get(byteArrayToString(signiture));
		    }finally{
		        ServerMain.fMonitor.cancelChildMoving();    
		    }
		}
		
		if(tmp!=null){
			return (ServerLogWraper) tmp;
		}else{
			return null;
		}
	}
	
	//返却されたjobについて処理を行う
	//内部のtmpIncludedにデータを保持したClientInfoを渡すこと
	public void processCulculatedJob(CulculateData receiveJob,ClientInfo cInfo){
		
	    //計算結果の信頼性が確認された場合(checkの内部で処理を行っているので注意)
		if(culculatedJobCheck(receiveJob,cInfo)){
			ServerLogWraper tmp = removeWaitingJob(receiveJob.getSigniture());
		    if(tmp != null){   //ちゃんと取り除けた、つまり、異常ではないjobだった場合（余剰の計算結果とかじゃないという事でもある）
				removeAllSameJobsFromDisQ(receiveJob.getSigniture());       //同じジョブはすべて配布待ちキューから取り除いておく
				
				ancestor.sGUI.jobTb.writeJobResut(tmp);           //jobの結果を表に追加
				pastJobDB.addOneLogWraper(tmp);
				resultDB.addOneResult(cInfo.tmpIncluded);   //計算結果(その他もろもろ含）を格納
		    }
			
			ServerMain.sDataToFile.plocPlant.doSomeThing(receiveJob);      //定められた何らかの処理を行う
		}else{ //まだ信頼性が確認されない場合
		    //ここでは特に何もしない
		}
	}
	
	//クライアントによって返された計算結果をチェックし、内部で処理を行う。戻り値=完了としてよいかどうか
	public boolean culculatedJobCheck(CulculateData receiveJob,ClientInfo cInfo){
		ServerLogWraper waiting =  getWaitingJob(receiveJob.getSigniture());
		
		if(waiting != null){
			try {
				boolean checkResult = waiting.checkCompleted(receiveJob,new ContributerProperty(cInfo,receiveJob.getDistributeDate(),receiveJob.getCompleteDate()));
//				if(checkResult == true){   //信頼性が確かめられた場合返却待機キューから取り除く
//					removeWaitingJob(receiveJob.getSigniture());
//				}
				
				return checkResult; 
			} catch (FindillegalResultException e) {
				//計算結果が食い違ってしまった場合の処理
				ServerLogWraper tmp = removeWaitingJob(receiveJob.getSigniture());     //返却待機キューから取り除く
				addReJobToQueue(tmp.getWrapped());   //内部に保持されているまっさらなデータを配布待ちキューに再配布として追加する
				return false;
			}
		}else{     //戻ってきたＪｏｂが待機状態でない場合（すでに完了とされてしまっているという事）
			return true;
		}
	}
	
	//対象のクライアントに対して未配布のジョブを配布待ちキューから探し抜き出す。
	//末尾まで行ってしまった場合追加補充も行ってしまう
	private CulculateData serchFreshJob(byte[] signiture){
	    CulculateData tmpData;
	    synchronized(distributeJobQueue){
		    ServerMain.fMonitor.informChildMoving();
		    try{
				for(int i=0;i<distributeJobQueue.size();i++){
					ServerLogWraper tmp = getWaitingJob(((CulculateData)distributeJobQueue.get(i)).getSigniture());
					
					if(tmp!=null){  //返却待機キューに含まれていてそれがちゃんと返ってきた場合
					    if(tmp.checkFormerDistributer(signiture)==false){  //対象に対して新鮮なＪｏｂがあった場合それを返す
					        return tmp.getWrapped();
						}
					}else{    //返却待機キューに含まれていないという事は、未配布という事なのでそれを返す
					    return (CulculateData) distributeJobQueue.remove(i);
					}
				}
				//配布待機キューの中に未配布のジョブがなかった場合
				int nowQueueCount = distributeJobQueue.size();
				fillDistributeJobQ(nowQueueCount+1);
				tmpData = (CulculateData) distributeJobQueue.get(nowQueueCount+1-1);   //追加したジョブの先頭の物を配布する
		    }finally{
		        ServerMain.fMonitor.cancelChildMoving();    
		    }
		}
	    return tmpData;
	}
	
	//配布待機キューの中で指定した計算データ（シグニチャ）を持つものに配布クライアントを追加する
	//引数　jobSigniture:ジョブのシグニチャ clientSigniture:クライアントのシグニチャ
	private void addDistributerToWaitingQ(byte jobSigniture[],byte[] clientSigniture ){
	    ServerLogWraper tmp = getWaitingJob(jobSigniture);
		if(tmp != null){
			tmp.addDistributer(clientSigniture);
		}
	}

    /**
     * 
     * @uml.property name="redundancyCount"
     */
    public int getRedundancyCount() {
        return redundancyCount;
    }

    /**
     * 
     * @uml.property name="redundancyCount"
     */
    public void setRedundancyCount(int redundancyCount) {
        this.redundancyCount = redundancyCount;
    }

	
	public Iterator getWaitingQIterator(){
//		Collection tmp = waitingBackJobs.values();
//		return tmp.iterator();
	    return ((PersistentLinkedHashMap)waitingBackJobs).iterator();
	}
	
	public Iterator getDistributeQIterator(){
		return distributeJobQueue.iterator();
	}
	
	//同じシグニチャを持つjobを配布待ちキューからすべて取り除く
	public void removeAllSameJobsFromDisQ(byte signiture[]){
		synchronized(distributeJobQueue){
		    ServerMain.fMonitor.informChildMoving();
		    try{
				Iterator serchIterator = getDistributeQIterator();
				String strSigniture = byteArrayToString(signiture);
				ArrayList removeJobs = new ArrayList();     //除去するデータのインデックスを保持しておく（後にまとめて除去するため)
				while(serchIterator.hasNext()){  //配布待ちキューを走査して同じシグニチャを持つ物を取り除く
					CulculateData tmp = (CulculateData) serchIterator.next();
					if(byteArrayToString(tmp.getSigniture()).equals(strSigniture)){
						removeJobs.add(tmp);   //まとめて除去するためにストックしておく
					}
				}
				
				serchIterator = getDistributeQIterator();   //イテレーターを再び先頭へ
				//一致したものをまとめて除去していく
				Iterator removeJobsIterator = removeJobs.iterator();
				while(removeJobsIterator.hasNext()){
					distributeJobQueue.remove(removeJobsIterator.next());
				}
		    }finally{
		        ServerMain.fMonitor.cancelChildMoving();    
		    }
		}
	}
}
