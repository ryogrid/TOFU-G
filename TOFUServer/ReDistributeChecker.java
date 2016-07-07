/* ************************************************************** *
 *                                                                *
 * Copyright (c) 2005, Ryo Kanbayashi, All rights reserved.       *
 *                                                                *
 *                                                                *
 * This software is distributed under the modified BSD License.   *
 * ************************************************************** */

import java.util.Calendar;
import java.util.Iterator;

//ジョブの配布されてからの時間をチェックし、管理するスレッド
public class ReDistributeChecker implements Runnable {
	private int waitDays = 3;    //再配布の判断するまでの日数。デフォルトは３日。
	private int checkIntervalMin = 60;        //チェックを行うインターバル。デフォルトは一時間
	private Calendar beforeCheckDate = null;       //前回チェックを行った日時
	private volatile boolean stopRequest = false;      //このスレッドが終了すべき時はtrueに
	
	public ReDistributeChecker(){
		this.beforeCheckDate = Calendar.getInstance(); 
	}
	
	public ReDistributeChecker(int waitDays,int checkInterval){
		this();
		this.waitDays = waitDays;
		this.checkIntervalMin = checkInterval;
	}
	
	public void run() {
		while(stopRequest == false){
			//比較の時点からインターバル分引いた時間が前回のチェック時の日時より後の場合。
			//つまり、インターバル以上の時間が経過していた場合
//			if(isProgressed(beforeCheckDate,Calendar.getInstance(),checkIntervalMin) == true){
			if(isProgressed(beforeCheckDate,Calendar.getInstance(),1) == true){
			    synchronized(ServerMain.sDataToFile.jManager.distributeJobQueue){
					synchronized(ServerMain.sDataToFile.jManager.waitingBackJobs){
						Iterator queueIterator =ServerMain.sDataToFile.jManager.getWaitingQIterator();
						Calendar checkUse = Calendar.getInstance();
					
						while(queueIterator.hasNext()){
							ServerLogWraper tmp = (ServerLogWraper) queueIterator.next();
							Calendar distributedDate = (tmp.getWrapped()).getDistributeDate();
							
							//再配布すべき時間が経過してしまっている場合
					//		if(isProgressed(distributedDate,checkUse,waitDays*1440)==true){
							if(isProgressed(distributedDate,checkUse,2)==true){
								int addJobCount = tmp.getRedundancy() - tmp.getProgress();
								CulculateData addJob = tmp.getWrapped();
								for(int i=0;i<addJobCount;i++){   //追加配布の量分だけ配布待ちキューに追加
									ServerMain.sDataToFile.jManager.addReJobToQueue(addJob);
								}
							}else{    //まだ経過していなければ
								//とりあえず何もしない
							}
							
						}
					}
			    }
				this.beforeCheckDate = Calendar.getInstance(); 
			}else{     //比較すべき時間が経過していない場合sleep
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	//beforeからafterまでにInterval分の時間が経過したのかを返す。
	public boolean isProgressed(Calendar before,Calendar after,int intervalMin){
		after.add(Calendar.MINUTE,-1* intervalMin);
		if(before.after(after)==false){
			after.add(Calendar.MINUTE,intervalMin);   //元に戻す
			return true;
		}else{
			after.add(Calendar.MINUTE,intervalMin);   //元に戻す
			return false;
		}
	}
	
	//処理を停止するよう指示する
	public void requestIntterupt(){
		stopRequest = true;
	}
	
}
