/* ************************************************************** *
 *                                                                *
 * Copyright (c) 2005, Ryo Kanbayashi, All rights reserved.       *
 *                                                                *
 *                                                                *
 * This software is distributed under the modified BSD License.   *
 * ************************************************************** */

import java.io.*;
import java.security.*;
import java.util.*;

public class SPreserveData implements Serializable{
	private static final long serialVersionUID = 1L;
	final String FILE_NAME = "SPreserveData.dat";
	String messageToClients = null;                 //クライアントへ送信するためのメッセージ

    /**
     * 
     * @uml.property name="svrInfo"
     * @uml.associationEnd multiplicity="(1 1)"
     */
    ServerInfo svrInfo = new ServerInfo(); //プロジェクトそれぞれのシグニチャ(クライアントへ送信する事を意識）

    /**
     * 
     * @uml.property name="clientDB"
     * @uml.associationEnd multiplicity="(1 1)"
     */
    volatile ClientDatabase clientDB = new ClientDatabase(); //クライアントのデータを統括、操作するクラス

    /**
     * 
     * @uml.property name="resultDB"
     * @uml.associationEnd multiplicity="(1 1)"
     */
    volatile ResultDatabaseImpl resultDB = new ResultDatabaseImpl(); //ユーザーへ与えるために計算結果を積んでいるクラス

    /**
     * 
     * @uml.property name="plocPlant"
     * @uml.associationEnd multiplicity="(1 1)"
     */
    volatile ProcessPlantImpl plocPlant = new ProcessPlantImpl(resultDB, null); //受信したデータになんらかの処理を行うクラス

    /**
     * 
     * @uml.property name="generater"
     * @uml.associationEnd multiplicity="(0 1)"
     */
    volatile DataGeneraterImpl generater = null; //計算データを生成するユーザー定義クラス

    /**
     * 
     * @uml.property name="jManager"
     * @uml.associationEnd multiplicity="(0 1)"
     */
    volatile JobManager jManager = null; //計算データの再配布や、完了を管理するクラス

	boolean alreadyInit = false;                     //初期設定が済んでいるか
	
	//以下にサーバが起動中に利用するpersonalなデータを保持
	int reDistributeDays = 1;        //再配布を決定するまでの日数
	int reDistributecheckMin = 60;        //再配布チェックスレッドのチェックの間隔
	
//	このクラスを丸ごとファイルへ書き込む
	  public void preserveAll(){

System.out.println("fMonitorの同期に入る前だよ");	      
	  	  ServerMain.fMonitor.checkCanDo();    //書き込みを行ってよいかチェック
System.out.println("fMonitorの同期を出て、synchronized(ServerMain.sDataToFile)に入るとこだよ");
//	  	  synchronized(ServerMain.sDataToFile){
//	  	    ServerMain.fMonitor.checkCanDo();    //書き込みを行ってよいかチェック
	  	      
			  File preserveFile = new File(FILE_NAME);
			  try {
//				  ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(preserveFile));
			      DebugObjectOutputStream out = new DebugObjectOutputStream(new FileOutputStream(preserveFile));
				  System.out.println("preserveAll:データ書き込み開始!!");
				  out.writeObject(this);
				  System.out.println("preserveAll:データ書き込み終了!!");
				  out.close();
			  } catch (FileNotFoundException e) {
				  e.printStackTrace();
			  } catch (IOException e) {
				  e.printStackTrace();
			  }
			  
//			  ServerMain.fMonitor.finishDoing();
//	  	}
System.out.println("PreserveAllの中にてfinishDoingの前で待機してます");			  
	    ServerMain.fMonitor.finishDoing();
System.out.println("fMonitorの同期脱出しました");	
	  }
	  
	//このクラスを丸ごとファイルから読み込みそれを返す
	public SPreserveData readOutAll(){
		File preserveFile = new File(FILE_NAME);

		try {
			ObjectInputStream in = new ObjectInputStream(new FileInputStream(preserveFile));
			SPreserveData temp = (SPreserveData) in.readObject();
			in.close();
			return temp;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
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
		buffer.append(d.getTime());       //システムタイムを文字列として追加
		MessageDigest md;
		try {
			md = MessageDigest.getInstance("SHA1");
			md.update(buffer.toString().getBytes());	//作成しておいた文字列で初期化
			return md.digest();			//ダイジェスト値を返す
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			return null;
		}
	}
}
