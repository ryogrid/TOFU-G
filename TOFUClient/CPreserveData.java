/* ************************************************************** *
 *                                                                *
 * Copyright (c) 2005, Ryo Kanbayashi, All rights reserved.       *
 *                                                                *
 *                                                                *
 * This software is distributed under the modified BSD License.   *
 * ************************************************************** */

import java.security.*;
import java.io.*;
import java.util.*;
import java.nio.ByteBuffer;

//クライアントの元でファイルに保存すべき情報を統括するクラス
//readOutAll()を呼ぶ時には、保持していると予想されるクラスファイルは事前にロードしておくこと
public class CPreserveData implements Serializable {
	private static final long serialVersionUID = 1L;
	final String FILE_NAME = "CPreserveData.dat";
	volatile private transient ArrayList projectThreads = new ArrayList();   //プロジェクトごとのスレッドを複数保持
	volatile private transient ListIterator proThreIterator = projectThreads.listIterator();     //projectThreadsのiterator
	private LinkedHashMap sInfos = new LinkedHashMap();
	private transient Iterator sInfosIterator = (sInfos.values()).iterator();     //sInfosのiterator
	//DataOfEachProjcetを複数保持 key:サーバのシグニチャ　値 : DataOfEachProject
	public ClientInfo cInfo = new ClientInfo();
	public SecurityPolicies sePolicy = new SecurityPolicies(); //セキュリティーポリシーに関するクラス
	int ConnectFailCount = 0; //接続処理の失敗した回数を保持
	boolean endableFlag = false; //終了してもよいか
	boolean nowConnectingFlag = false; //現在接続状態かどうか
	boolean alreadyInit = false;       //初期設定を行ってあるかどうか
	
	public final int PRIORITYHIGH = 8;
	public final int PRIORITYMIDDLE = 5;
	public final int PRIORITYLAW = 2;
	int priorityLevel = 2;              //処理優先度を表す値

	public CPreserveData(){
		initProThreIterator();     //Iteratorを初期化
		initSInfosIterator();
	}

	//このクラスを丸ごとファイルへ書き込む
	public synchronized void preserveAll() {
		
		synchronized(ClientMain.cDataToFile){
		    ClientMain.fMonitor.checkCanDo();
		    
			File preserveFile = new File(FILE_NAME);
			try {
				ObjectOutputStream out =
					new ObjectOutputStream(new FileOutputStream(preserveFile));
				out.writeObject(this);
				out.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			ClientMain.fMonitor.finishDoing();
		}
		
	}
	
	//現在参加しているプロジェクト数を得る
	public int getProjectCount(){
	    int tmp;
		synchronized(sInfos){
		    ClientMain.fMonitor.informChildMoving();
		    
			tmp =  sInfos.size();
			
			ClientMain.fMonitor.cancelChildMoving();
		}
		return tmp;
	}

	//このクラスを丸ごとファイルから読み込みそれを返す(失敗した場合nullを返す）
	//クラスロードの時に用いるクラスローダーとその探索pathを与える
	public synchronized CPreserveData readOutAll(ClassLoader loader,String[] path) {
		File preserveFile = new File(FILE_NAME);
		
			try {
				MyObjectInputStream in =
					new MyObjectInputStream(new FileInputStream(preserveFile),loader,path);
				CPreserveData temp = (CPreserveData) in.readObject();
				in.close();
				return temp;
			} catch (FileNotFoundException e) {
				return null;
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
				return null;
			}
	}

	//与えた文字列と、システムタイムでハッシュ値を算出(失敗した場合nullを返す)
	public byte[] generateHash(String str) {
		//		MessageDigestオブジェクトの初期化
		StringBuffer buffer = new StringBuffer(str);
		Date d = new Date();
		buffer.append(d.getTime()); //システムタイムを文字列として追加
		MessageDigest md;
		try {
			md = MessageDigest.getInstance("SHA1");
			md.update(buffer.toString().getBytes()); //作成しておいた文字列で初期化
			return md.digest(); //ダイジェスト値を返す
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			return null;
		}
	}

	//sInfosにDataOfEachProjectの要素を追加する　key :サーバのシグニチャ　値　:DataOfEachProject
	public void addDatOfEach(DataOfEachProject newPro) {
		synchronized(sInfos){
		    ClientMain.fMonitor.informChildMoving();
		    
			sInfos.put(byteArrayToString(newPro.svrInfo.signiture),newPro);
			
			ClientMain.fMonitor.cancelChildMoving();
		}
	}

	//シグニチャ指定で要素を削除、削除した値を返す
	public DataOfEachProject delDataOfEach(byte[] signiture) {
	    DataOfEachProject tmp;
		synchronized(sInfos){
		    ClientMain.fMonitor.informChildMoving();
		    
			tmp = (DataOfEachProject) sInfos.remove(byteArrayToString(signiture));
			
			ClientMain.fMonitor.cancelChildMoving();
		}
		return tmp;
	}

	//シグニチャ指定で要素を得る
	public  DataOfEachProject getDataOfEach(byte[] signiture) {
	    DataOfEachProject tmp;
	    synchronized(sInfos){
	        ClientMain.fMonitor.informChildMoving();
	        
	        tmp =(DataOfEachProject) sInfos.get(byteArrayToString(signiture));
	        
	        ClientMain.fMonitor.cancelChildMoving();
	    }
	    return tmp;
	}
	
	//プロジェクト番号でプロジェクトのデータを得る、見つからない場合はnullを返す
	public DataOfEachProject getDataOfEachByProNum(int number){
		
		synchronized(sInfosIterator){
		    ClientMain.fMonitor.informChildMoving();
		    
			initSInfosIterator();
			while(sInfosHasNext() == true){
				DataOfEachProject tmp = sInfosNext();
				if(tmp.projectNumber == number){  //探しているプロジェクト番号のプロジェクトがあったら
					ClientMain.fMonitor.cancelChildMoving();
				    return tmp;
				}
			}
			
			ClientMain.fMonitor.cancelChildMoving();
			return null;
		}
	}
	
	//イテレータを初期化、最初の位置へ戻したい時に利用（意義に関して要検討）、コンストラクタで初期化されていない時は使え
	public synchronized void initSInfosIterator(){
		synchronized(sInfos){
		    ClientMain.fMonitor.informChildMoving();
		    
			sInfosIterator = (sInfos.values()).iterator();      //iteratorをセット
			
			ClientMain.fMonitor.cancelChildMoving();
		}
	}
	
	//sInfosIteratorのhasNext();　iteratorが初期化されているか注意
	public boolean sInfosHasNext(){
		boolean tmp;
	    synchronized(sInfos){
		    ClientMain.fMonitor.informChildMoving();
		    
			tmp = sInfosIterator.hasNext();
			
			ClientMain.fMonitor.cancelChildMoving();
		}
	    return tmp;
	}
	
	//sInfosIteratorのnext(); iteratorが初期化されているか注意
	public DataOfEachProject sInfosNext(){
	    DataOfEachProject tmp;
		synchronized(sInfos){
		    ClientMain.fMonitor.informChildMoving();
		    
			tmp = (DataOfEachProject) sInfosIterator.next();
			
			ClientMain.fMonitor.cancelChildMoving();
		}
		return tmp;
	}

	//projectThreadsを初期化
	public synchronized void initProthre(){
			projectThreads = new ArrayList();
	}
	
	//イテレータを初期化、最初の位置へ戻したい時に利用（意義に関して要検討）、コンストラクタで初期化されていない時は使え
	public void initProThreIterator(){
		synchronized(projectThreads){	
	    	proThreIterator = projectThreads.listIterator();      //iteratorをセット
		}	
	}

	//proThreIteratorのhasNext();　iteratorが初期化されているか注意
	public boolean proThreHasNext(){
	    boolean tmp;
		synchronized(projectThreads){
		    ClientMain.fMonitor.informChildMoving();
		    
			tmp =proThreIterator.hasNext();
			
			ClientMain.fMonitor.cancelChildMoving();
		}
		return tmp;
	}
	
	//proThreIteratorのnext(); iteratorが初期化されているか注意
	public EachProThread proThreNext(){
	    EachProThread tmp;
		synchronized(projectThreads){
		    ClientMain.fMonitor.informChildMoving();
		    
			tmp = (EachProThread) proThreIterator.next();
			
			ClientMain.fMonitor.cancelChildMoving();
		}
		return tmp;
	}

	//プロジェクト処理のスレッドを追加する
	//*インデックスは「プロジェクト番号-1」を用いること
	public void addProThreByIndex(int index,EachProThread newer){
		synchronized(projectThreads){
		    ClientMain.fMonitor.informChildMoving();
		    
			projectThreads.add(index,newer);
			
			ClientMain.fMonitor.cancelChildMoving();
		}
	}
	
	//オブジェクト指定でプロジェクト処理のスレッドを取り除く
	public void removeProThre(EachProThread removed){
		synchronized(projectThreads){
		    ClientMain.fMonitor.informChildMoving();
		    
			projectThreads.remove(removed);
			
			ClientMain.fMonitor.cancelChildMoving();
		}
	}
	
	//インデックス指定でプロジェクト処理のスレッドを取り除く
	public void removeProThre(int number){
		synchronized(projectThreads){
		    ClientMain.fMonitor.informChildMoving();
		    
			projectThreads.remove(number);
			
			ClientMain.fMonitor.cancelChildMoving();
		}
	}
	
	//インデックス指定でプロジェクト処理のスレッドを得る
	public EachProThread getProThre(int number){
	    EachProThread tmp;
		synchronized(projectThreads){
		    ClientMain.fMonitor.informChildMoving();
		    
			tmp = (EachProThread) projectThreads.get(number);
			
			ClientMain.fMonitor.cancelChildMoving();
		}
		return tmp;
	}
	
	//バイト配列をStringの文字列へ変換
	private String byteArrayToString(byte[] array){
		StringBuffer tmp = new StringBuffer();
		for(int i = 0; i < array.length;i++){
			tmp.append(array[i]);
		}
		String result = tmp.toString();
		return result;
	}
	
	//プロジェクトデータを与えて、処理スレッドを返す
	public EachProThread getProThreByPro(DataOfEachProject need){
		synchronized(projectThreads){
		    ClientMain.fMonitor.informChildMoving();
		    
			initProThreIterator();
			while(proThreHasNext()){
				EachProThread thre = proThreNext();
				DataOfEachProject tmp = thre.getHavinProject();
				if(need.equals(tmp)){
				    ClientMain.fMonitor.cancelChildMoving();
					return thre;
				}
			}
			ClientMain.fMonitor.cancelChildMoving();
			return null;         //見つからない場合nullを返す
		}
	}
}
