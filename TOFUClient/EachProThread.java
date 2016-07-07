/* ************************************************************** *
 *                                                                *
 * Copyright (c) 2005, Ryo Kanbayashi, All rights reserved.       *
 *                                                                *
 *                                                                *
 * This software is distributed under the modified BSD License.   *
 * ************************************************************** */

import java.io.*;
import java.net.*;
import java.util.Calendar;
import java.util.Date;
import java.util.zip.GZIPInputStream;

import javax.swing.JOptionPane;

//プロジェクト毎に生成され、データ取得、計算、計算結果返却の手順を行うスレッド
public class EachProThread extends Thread {
	private ClientMain ancestor;
	//	  割り振られたプロジェクトのデータ
	private DataOfEachProject assignedPro = new DataOfEachProject();
	public Culculater worker = null;
	private FileOutputStream fOutCulculater = null;
	private FileOutputStream fOutData = null;
	private ObjectInputStream in = null;
	private ObjectOutputStream out = null;
	private Socket sock = null;
	private volatile boolean stopRequest = false; //このスレッドが終了すべき時はtrueに

	//扱うべきプロジェクトに関するデータと、呼び出し元の参照を与える
	public EachProThread(DataOfEachProject handInPro, ClientMain ancestor) {
		this.assignedPro = handInPro;
		this.ancestor = ancestor;
	}

	public void run() {

		try {
			while (stopRequest != true) { //同じプロセスを永久に回す

				try {
					//次にジョブを受け取らなくてはならない場合
					if (assignedPro.nextShouldDoThing == assignedPro.SHOULDRECIVEJOB) {

						//計算データを保持していなければ(サーバからジョブを受けとっていない場合のため）
						if (assignedPro.culData == null) {
							DataOfEachProject tmpPro = getJob(assignedPro,
									ClientMain.cDataToFile.cInfo);
							assignedPro = tmpPro;
							assignedPro.variousState = assignedPro.HAVENEWJOB;
						}
						ancestor.cGUI.writeStateToTable(
								assignedPro.projectNumber, assignedPro); //プロジェクト情報を更新
						ancestor.cGUI.writeLogToTable(
								assignedPro.projectNumber, assignedPro); //計算開始時に書き込み
						assignedPro.nextShouldDoThing = assignedPro.SHOULDCULCULATE;
						ClientMain.cDataToFile.addDatOfEach(assignedPro); //データを更新

						//次に計算を行わなくてはならない場合
					} else if (assignedPro.nextShouldDoThing == assignedPro.SHOULDCULCULATE) {
						assignedPro.culMemo.setInterruption(false); //念のため、初期化しておく

						DataOfEachProject tmpPro = doCulculate(assignedPro);
						assignedPro =tmpPro;
						
						//計算が正常に終了した場合
						if (assignedPro.culMemo.getInterruption() == false) {
							assignedPro.variousState = assignedPro.COMPCUL;
							assignedPro.nextShouldDoThing = assignedPro.SHOULDBACKJOB;
						} else { //計算が正常に終了していない場合（中断された場合など）
							assignedPro.variousState = assignedPro.HAVEMIDDLEWAYJOB;
							assignedPro.nextShouldDoThing = assignedPro.SHOULDCULCULATE; //もう一度計算を行わなくてはならないと表明
						}

						ClientMain.cDataToFile.addDatOfEach(assignedPro); //データを更新

						//次に計算結果をサーバへ返さなくてはならない場合
					} else if (assignedPro.nextShouldDoThing == assignedPro.SHOULDBACKJOB) {
						DataOfEachProject tmpPro = backResult(assignedPro,
								ClientMain.cDataToFile.cInfo);
						assignedPro = tmpPro;
						
						ClientLogWraper tmp = assignedPro.pastJobDB
								.wrapByLogWraper(assignedPro.culData); //CulculateDataをラッピング
						assignedPro.pastJobDB.addOneLogWraper(tmp); //ログを保存
						assignedPro.variousState = assignedPro.AFTERBACK;

						ancestor.cGUI.writeStateToTable(
								assignedPro.projectNumber, assignedPro); //プロジェクト番号にしたがったプロジェクト情報をtableに表示
						ancestor.cGUI.writeLogToTable(
								assignedPro.projectNumber, assignedPro); //計算終了後に書き込み
						assignedPro.culData = null; //終了した計算タスクを消去しておく（新しいジョブを受け取るため）

						ClientMain.cDataToFile.addDatOfEach(assignedPro); //データを更新

						assignedPro.nextShouldDoThing = assignedPro.SHOULDRECIVEJOB;
					} else { //その他の場合stateに異常があるかもしれないので指定しなおす
						assignedPro.nextShouldDoThing = assignedPro.SHOULDCULCULATE;
					}

				} catch (IOException e1) { //接続失敗などソケット関係のエラー
				    e1.printStackTrace();
				    System.out.println("cDataToFile内のアドレスは:" + assignedPro.svrInfo.address.getHostAddress());
				    doReConnectProcess();
				}finally{
					if(sock != null){//念のため解放
				        try {
                            sock.close();
                        } catch (IOException e2) {
                            e2.printStackTrace();
                        }
				    }
				}
			}

		} catch (ConnectCanceledException e) { //接続が許可されなかった場合
			ancestor.cGUI.startStopEachItem[assignedPro.projectNumber - 1]
					.setEnabled(true);
			//このスレッドはこのまま終了してしまう
		} catch (FullConnectFailedException e1) {
			//規定回数接続を失敗してしまった場合の処理
			Object[] msg = { "サーバが停止しているか何らかの理由でサーバへ接続できません：" + assignedPro.svrInfo.projectNum +"の処理を停止します" };
			JOptionPane.showOptionDialog(ancestor.cGUI, msg, "警告",
					JOptionPane.PLAIN_MESSAGE, 0, null, null, null);
			ClientMain.cDataToFile.addDatOfEach(assignedPro);
			ClientMain.cDataToFile.removeProThre(assignedPro.projectNumber-1);  //処理スレッドを取り除く
			ancestor.cGUI.startStopEachItem[assignedPro.indexOfMenu].setText("START " + assignedPro.svrInfo.nickname + "!"); //STARTボタンにチェンジ
			
			ReConnectWaiter.restAll();	            //待ち時間や待機回数などをリセット
		} finally {
			assignedPro.culMemo.setInterruption(false); //次回起動時のために初期化しておく
			assignedPro.variousState = assignedPro.NOTSTART;

			ClientMain.cDataToFile.addDatOfEach(assignedPro); //プロジェクトのデータを確実に更新しておく
			ClientMain.cDataToFile.preserveAll();
		}
	}

	//再接続までの待機処理を行う（実際の接続はしないで待機の管理）
	private void doReConnectProcess() throws FullConnectFailedException {

		if (ReConnectWaiter.isWaitable() == true) { //規定回数waitしていない場合
			ReConnectWaiter.waitForDoing(); //待機
			//待機後にそのまま処理ループへ戻る
		} else {
			
			throw new FullConnectFailedException();
		}
	}

	//計算結果をサーバへ返す(targetの内容を変更して返す）
	//引数 target : 各々のプロジェクト関する情報（対象プロジェクト） myself : クライアント自信の情報
	public DataOfEachProject backResult(DataOfEachProject target,
			ClientInfo myself) throws ConnectCanceledException, IOException {

		try {

			//自動接続が許可されていない場合、許可の確認を行う
			if (ClientMain.cDataToFile.sePolicy.autConnect == false) {
				if (ancestor.checkConnectable(target) == false) {
					throw new ConnectCanceledException();
				}
			}

			InetAddress svrAddress = target.svrInfo.address; //アドレスを取り出す
			//			sock = new Socket(svrAddress, 2525);
			//sock = ClientMain.makeSSLClientSocket(svrAddress,2525);
			sock = ClientMain.makeSSLClientSocket(InetAddress.getByAddress(svrAddress.getAddress()),2525);
			
			out = new ObjectOutputStream(new DataOutputStream(sock
					.getOutputStream()));

			ExchangeInfo sendInfo = new ExchangeInfo();
			sendInfo.backResult = true; //計算結果の返却だという事を表明
			DataContainer sendContainer = new DataContainer();

			target.workCounts++; //処理したＷｏｒｋ数を加算
			myself.AllWorkCounts++; //全体のワーク数も加算しておく

			target.culData.setCompleteDate(Calendar.getInstance()); //計算終了日時を記録しておく

			ClientInfo forSendCInfo = extractForSendData(myself,target);
			sendContainer.setDeriverdCInfo(forSendCInfo);
			out.writeObject(sendInfo);

			out.writeObject(sendContainer); //クライアントからの情報を送信

			out.flush();
			

			//			ソケットでのデータ読み取り用
			in = new MyObjectInputStream(new DataInputStream(sock
					.getInputStream()), ancestor.myloader, "./ProjectFiles/"
					+ target.svrInfo.nickname + "/");

			ExchangeInfo receiveExInfo = (ExchangeInfo) in.readObject();
			FileInformations fInfos = (FileInformations) in.readObject();
			extractFileToLocal(fInfos, in); //ファイルをすべてローカルに保存
			DataContainer receiveContainer = (DataContainer) in.readObject();

			sock.close();

			//return shelter.comebackData((receiveContainer.getDeriverdCInfo()).tmpIncluded);     //退避していたデータも加えて返す
			return backFromReceiveData(target,(receiveContainer.getDeriverdCInfo()).tmpIncluded);    //受信したデータ内のデータを加えて返す

		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			return null;
		}finally{
		    if(sock != null){
		        sock.close();
		    }
		}
	}

	//与えたプロジェクトについて計算を行う(dealProの内容を変更して返す）
	//引数 dealPro : 各々のプロジェクト関する情報（対象プロジェクト
	public DataOfEachProject doCulculate(DataOfEachProject dealPro) {

		Date d = new Date();
		double culculateStart = d.getTime(); //計算時間を計算するための変数

		worker.culculate(dealPro.culData, dealPro.culMemo); //計算結果は受けたCulculateDataに受ける
		String infoFromWorker = worker.getString(); //workerの返す情報を文字列に
		ancestor.cGUI.ta[dealPro.projectNumber - 1].setText(infoFromWorker);

		d = new Date();
		double culculateEnd = d.getTime();
		dealPro.culData
				.setNecessaryTime((double) ((culculateEnd - culculateStart) / 100)); //このjobに関してどれだけ時間がかかったか記録
		dealPro.workSeconds += (double) ((culculateEnd - culculateStart) / 100); //計算にかかった時間を加算
		ClientMain.cDataToFile.cInfo.AllWorkingTime += (double) ((culculateEnd - culculateStart) / 100); //全体の計算時間にも加算しておく

		
		//中断命令が出ることなく計算が終了していれば計算完了と見なし、フラグを立てる
		if (dealPro.culMemo.getInterruption() != true) {
			dealPro.culData.setCulculateComplete(true); //計算が終了している事を表明
		}

		return dealPro;
	}

	//コンテナの中からファイルの情報を抽出して、ストリームからローカルのディレクトリに展開
	private void extractFileToLocal(FileInformations info, InputStream in) {

		long fileLength = 0; //読み込むファイルのlength
		long readedByte;
		for (int i = 0; (fileLength = info.fileLengths[i]) != -1; i++) {
			try {
				FileOutputStream out = new FileOutputStream(info.fileNames[i]);
				readedByte = 0; //読み込み済みのバイト数
				while (readedByte != fileLength) {
					//指定された長さで読み込む
					out.write(in.read());
					readedByte++;
				}
				out.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	//新しいジョブを受け取る(targetの内容を変更して返す）
	//引数 target : 各々のプロジェクトに関する情報（対象プロジェクト） myself : クライアント自身の情報
	public DataOfEachProject getJob(DataOfEachProject target, ClientInfo myself)
			throws ConnectCanceledException, IOException {
		try {

			//			自動接続が許可されていない場合、許可の確認を行う
			if (ClientMain.cDataToFile.sePolicy.autConnect == false) {
				if (ancestor.checkConnectable(target) == false) {
					throw new ConnectCanceledException();
				}
			}

			InetAddress svrAddress = target.svrInfo.address;
			//			sock = new Socket(svrAddress, 2525);
//			sock = ClientMain.makeSSLClientSocket(svrAddress,2525);
			sock = ClientMain.makeSSLClientSocket(InetAddress.getByAddress(svrAddress.getAddress()),2525);

			out = new ObjectOutputStream(new DataOutputStream(sock
					.getOutputStream()));

			ExchangeInfo sendInfo = new ExchangeInfo();
			sendInfo.receiveCulData = true; //新しいジョブを受け取るためだと表明
			out.writeObject(sendInfo);
			DataContainer sendContainer = new DataContainer();
			
			ClientInfo forSendCInfo = extractForSendData(myself,target);
			
			sendContainer.setDeriverdCInfo(forSendCInfo);

			out.writeObject(sendContainer); //クライアントからの情報を送信（ここでは空）
			out.flush();

			in = new MyObjectInputStream(new DataInputStream(sock
					.getInputStream()), ancestor.myloader, "./ProjectFiles/"
					+ target.svrInfo.nickname + "/");
			//ソケットでのデータ読み取り用

			ExchangeInfo receiveExInfo = (ExchangeInfo) in.readObject();
			FileInformations fInfos = (FileInformations) in.readObject();
			extractFileToLocal(fInfos, in); //ファイルをすべてローカルに保存
			DataContainer receiveContainer = (DataContainer) in.readObject();
//			return shelter.comebackData((receiveContainer.getDeriverdCInfo()).tmpIncluded); //退避していたデータも加えて返す
			sock.close();
			return backFromReceiveData(target,(receiveContainer.getDeriverdCInfo()).tmpIncluded);    //受信したデータ内のデータを加えて返す
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			return null;
		}finally{
		    if(sock != null){
		        sock.close();
		    }
		}

	}

	//このスレッドの終了を要請する
	public void requestStop() {
		stopRequest = true;
		assignedPro.culMemo.setInterruption(true); //計算をしている場合中断してくれるよう要請するフラグを立てる
		ReConnectWaiter.requestIntterupt();
	}

	//内部で保持しているプロジェクトを返す
	public DataOfEachProject getHavinProject() {
		return assignedPro;
	}
	
	//サーバへ送信するために必要なデータだけを抽出したDataOfEachProjectを含んだClientInfoを作りだすメソッド
	//※サーバで利用するデータが変更されたり加えられた場合、このメソッドも変更されなくてはならない
	//※ディープコピーではないので注意せよ
	private ClientInfo extractForSendData(ClientInfo info,DataOfEachProject eachPro){
	    DataOfEachProject copiedEachPro = new DataOfEachProject();
	    copiedEachPro.culData = eachPro.culData;
	    copiedEachPro.workCounts = eachPro.workCounts;
	    copiedEachPro.workSeconds = eachPro.workSeconds;
    	copiedEachPro.messageToServer = eachPro.messageToServer;
	    copiedEachPro.messageFromServer = eachPro.messageFromServer;
	    copiedEachPro.rankOfWorker = eachPro.rankOfWorker;
	    copiedEachPro.joinDate = eachPro.joinDate;
	    copiedEachPro.svrInfo = eachPro.svrInfo;
	    copiedEachPro.pastJobDB = null;
	    ClientInfo copiedClientInfo = new ClientInfo();
	    copiedClientInfo.AllWorkCounts = info.AllWorkCounts;
	    copiedClientInfo.AllWorkingTime = info.AllWorkingTime;
	    copiedClientInfo.clientVersion = info.clientVersion;
	    copiedClientInfo.mailAddress = info.mailAddress;
	    copiedClientInfo.participantNum = info.participantNum;
	    copiedClientInfo.signiture = info.signiture;
	    copiedClientInfo.startUseDate = info.startUseDate;
	    copiedClientInfo.tmpIncluded = copiedEachPro;
	    return copiedClientInfo;
	}
	
	//元のDataOfEachProjectに受信したものからデータを加え、それを返す
	//extractForSendDataを利用した場合、このメソッドも使用すること
	private DataOfEachProject backFromReceiveData(DataOfEachProject target,DataOfEachProject received){
	    target.culData = received.culData;
	    target.workCounts = received.workCounts;
        target.messageFromServer = received.messageFromServer;
	    target.rankOfWorker = received.rankOfWorker;
	    target.joinDate = received.joinDate;
	    target.svrInfo = received.svrInfo;
	    return target;
	}

}