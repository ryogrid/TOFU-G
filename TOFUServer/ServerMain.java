/* ************************************************************** *
 *                                                                *
 * Copyright (c) 2005, Ryo Kanbayashi, All rights reserved.       *
 *                                                                *
 *                                                                *
 * This software is distributed under the modified BSD License.   *
 * ************************************************************** */

import java.io.*;
import java.net.*;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.swing.JOptionPane;

public class ServerMain {
	static public SPreserveData sDataToFile = new SPreserveData(); //ファイルに保存すべきデータを保持
	public ServerSocket svsock = null;
	public Socket sock = null;

    /**
     * 
     * @uml.property name="sGUI"
     * @uml.associationEnd inverse="ancestor:ServerGui" multiplicity="(1 1)"
     */
    public ServerGui sGUI = null;

	
	public Thread requestManagerThread = null; //RequestManagerを保持するためのスレッド

    /**
     * 
     * @uml.property name="requestManager"
     * @uml.associationEnd inverse="ancestor:RequestManager" multiplicity="(0 1)"
     */
    public RequestManager requestManager = null;

	
	public Thread reDisCheckerThread = null;   //ReDistributeCheckerを保持するためのスレッド

    /**
     * 
     * @uml.property name="reDisChecker"
     * @uml.associationEnd multiplicity="(0 1)"
     */
    public ReDistributeChecker reDisChecker = null; //再配布のチェックを行う

	
	public Thread dataPreserverThread = null;  //DataPreserverを保持するためのスレッド

    /**
     * 
     * @uml.property name="dataPreserver"
     * @uml.associationEnd multiplicity="(0 1)"
     */
    public DataPreserver dataPreserver = null; //定期的にデータを保存するスレッド

	static public volatile FileMonitor fMonitor = new FileMonitor();          //ファイル保存の排他処理を行うモニター
	
	public static void main(String[] args) {
		ServerMain sMain = new ServerMain();
	}

	public ServerMain() {

		sGUI = new ServerGui(this);
		//		ファイルからデータを読み出して代入する
		SPreserveData tmp = ServerMain.sDataToFile.readOutAll();
		if (tmp != null) { //読み出しに失敗してnullが返ってきてなければ
			ServerMain.sDataToFile = tmp;
		}
		while (sDataToFile.alreadyInit == false) { //一度も初期設定を行っていない場合
			Object[] msg = { "まず初期設定を行って下さい" };
			JOptionPane.showOptionDialog(sGUI, msg, "警告",
					JOptionPane.PLAIN_MESSAGE, 0, null, null, null);
			fillServerDat();
		}
		startPrepare(); //起動時の処理を行う（ファイルからの読み出しなど）
	}

	public void fin() { //終了時の処理
		
		if (reDisChecker != null) {    //再配布チェックスレッドの停止処理
			reDisChecker.requestIntterupt();
			while (reDisCheckerThread.isAlive() == true) {
				try {
					Thread.sleep(50);
					//本当に終了するまで待機
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		
		if (dataPreserver != null) {    //定期保存スレッドの停止処理
			dataPreserver.requestIntterupt();
			while (dataPreserverThread.isAlive() == true) {
				try {
					Thread.sleep(50);
					//本当に終了するまで待機
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
System.out.println("各処理スレッドの停止に入るよ");		
		if (requestManager != null) {
			requestManager.requestStop();
			while (requestManagerThread.isAlive() == true) {
				try {
					Thread.sleep(50);
					//本当に終了するまで待機
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
System.out.println("データの保存まできたよ");		
		ServerMain.sDataToFile.preserveAll();
	}

	//初期設定を行う。記入に成功した場合はtrue、失敗した場合はfalseを返す
	public boolean fillServerDat() {
	    
		int okOrNo = JOptionPane.showConfirmDialog(sGUI,
				"初期設定を行ってよいですか？(情報がリセットされます）", "確認", JOptionPane.YES_NO_OPTION);
		if (okOrNo == JOptionPane.OK_OPTION) {
		    ServerMain.sDataToFile = new SPreserveData();
			String strTmp; //一旦記入された内容を保持（if文での利用のため）
			int intTmp; //一旦記入された時の戻り値を保持

			strTmp = JOptionPane.showInputDialog(sGUI, "プロジェクト名を記入して下さい",
					"初期設定", JOptionPane.PLAIN_MESSAGE);
			if (strTmp != null) { //取り消しを押されていなければ(上書き防止)
				ServerMain.sDataToFile.svrInfo.projectNum = strTmp;
			}else{
			    return false;
			}

			strTmp = JOptionPane.showInputDialog(sGUI,
					"５文字以内のアルファベットでプロジェクト名を省略して下さい\n例:cae,SETI", "初期設定",
					JOptionPane.PLAIN_MESSAGE);
			if (strTmp != null) { //取り消しを押されていなければ(上書き防止)
				ServerMain.sDataToFile.svrInfo.nickname = strTmp;
			}else{
			    return false;
			}

			strTmp = JOptionPane.showInputDialog(sGUI, "運営者の名前を記入して下さい",
					"初期設定", JOptionPane.PLAIN_MESSAGE);
			if (strTmp != null) { //取り消しを押されていなければ(上書き防止)
				ServerMain.sDataToFile.svrInfo.managerNum = strTmp;
			}else{
			    return false;
			}

			strTmp = JOptionPane.showInputDialog(sGUI,
					"あるのであればＷＥＢのアドレスを記入して下さい", "初期設定",
					JOptionPane.PLAIN_MESSAGE);
			if (strTmp != null) { //取り消しを押されていなければ(上書き防止)
				ServerMain.sDataToFile.svrInfo.addresOfHP = strTmp;
			}else{
			    return false;
			}

			strTmp = JOptionPane.showInputDialog(sGUI,
					"公開してよければE-Mailアドレスを記入して下さい", "初期設定",
					JOptionPane.PLAIN_MESSAGE);
			if (strTmp != null) { //取り消しを押されていなければ(上書き防止)
				ServerMain.sDataToFile.svrInfo.eMailAddress = strTmp;
			}else{
			    return false;
			}
			
			strTmp = (String) JOptionPane.showInputDialog(sGUI,
					"配布後に何日以上経過した場合にjobの再配布を行いますか？", "初期設定",
					JOptionPane.PLAIN_MESSAGE,null,null,String.valueOf(1));
			if (strTmp != null) { //取り消しを押されていなければ(上書き防止)
				ServerMain.sDataToFile.reDistributeDays = Integer.parseInt(strTmp);
			}else{
			    return false;
			}
			
			strTmp =JOptionPane.showInputDialog(sGUI,
					"参加者へメッセージがあれば記入して下さい（後で変更可能）",
					"初期設定",JOptionPane.PLAIN_MESSAGE);
			if (strTmp != null) { //取り消しを押されていなければ(上書き防止)
				ServerMain.sDataToFile.messageToClients = strTmp;
			}else{
			    return false;
			}

			//プロジェクト名と運営者名がちゃんと記入されていれば
			if ((ServerMain.sDataToFile.svrInfo.managerNum != null)
					&& (ServerMain.sDataToFile.svrInfo.managerNum != null)) {
				//プロジェクト名からダイジェストを生成しシグニチャとする
				ServerMain.sDataToFile.svrInfo.signiture = ServerMain.sDataToFile
						.generateHash(ServerMain.sDataToFile.svrInfo.projectNum);

				ServerMain.sDataToFile.alreadyInit = true;
				sDataToFile.preserveAll();
				return true;
			}
		}
		return false;
	}

	public void startPrepare() {
		if (ServerMain.sDataToFile.generater == null) { //もしまだオブジェクトを用意していなかった場合、用意する
			ServerMain.sDataToFile.generater = new DataGeneraterImpl();
			ServerMain.sDataToFile.jManager = new JobManager(this,ServerMain.sDataToFile.generater,ServerMain.sDataToFile.resultDB);
		}

		ServerMain.sDataToFile.clientDB.initCDatasIterator();
		while (ServerMain.sDataToFile.clientDB.clientDatasHasNext()) { //クライアントのデータを表へ表示
			sGUI.clientTb.writeStateToTable(ServerMain.sDataToFile.clientDB
					.clientDatasNext());
		}

		ServerMain.sDataToFile.plocPlant.setAncester(this);    //初期化しておく
		ServerMain.sDataToFile.jManager.setReference(this,ServerMain.sDataToFile.generater,ServerMain.sDataToFile.resultDB);  //参照を初期化
		
		for (int i = 0; i < sDataToFile.jManager.pastJobDB.getLogCount(); i++) { //jobのresultを表へ表示
			ServerLogWraper log = (ServerLogWraper) ServerMain.sDataToFile.jManager.pastJobDB.getOneLogWraper(i);
		    sGUI.jobTb.writeJobResut(log);
		}

	}

	static SSLServerSocket makeSSLServerSocket(int portno)
			throws java.io.IOException {
		SSLServerSocket ss;
		SSLServerSocketFactory ssf = (SSLServerSocketFactory) SSLServerSocketFactory
				.getDefault();
		ss = (SSLServerSocket) ssf.createServerSocket(portno);
		String cipherSuites[] = { "SSL_DH_anon_EXPORT_WITH_DES40_CBC_SHA" };
		ss.setEnabledCipherSuites(cipherSuites);

		return (ss);
	}

}