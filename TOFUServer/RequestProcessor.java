/* ************************************************************** *
 *                                                                *
 * Copyright (c) 2005, Ryo Kanbayashi, All rights reserved.       *
 *                                                                *
 *                                                                *
 * This software is distributed under the modified BSD License.   *
 * ************************************************************** */

import java.io.*;
import java.net.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.*;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.GZIPOutputStream;

public class RequestProcessor implements Runnable {
    public static volatile int nowWaitThreadCount = 0; //今、いくつのスレッドが待機しているか
    public static List pool = new LinkedList(); //スレッドプール
    private int processorNumber = 0; //各スレッドに振られる識別番号

    /**
     * 
     * @uml.property name="ancestor"
     * @uml.associationEnd multiplicity="(1 1)"
     */
    private ServerMain ancestor = null;

    /**
     * 
     * @uml.property name="sendFiles"
     * @uml.associationEnd elementType="java.io.File" multiplicity="(0 -1)"
     */
    private ArrayList sendFiles = null; //リモートへ送信するためのファイルの情報を保持する

    private volatile boolean stopRequest = false; //このスレッドが終了すべき時はtrueに

    public RequestProcessor(ServerMain parent, int number) {
        ancestor = parent;
        this.processorNumber = number;
    }

    public void run() {
//        makeLogFile(processorNumber);

        sendFiles = getClassJarFiles(); //DistributeFilesフォルダのclassファイルを含むArrayListを得る
        sendFiles.add(new File("ProjectPicture.jpg"));

        FileInformations fInfos = new FileInformations();
        for (int i = 0; i < sendFiles.size(); i++) { //sendFilesに含まれるファイル情報を追加して行く
            fInfos.addFileInformation((File) sendFiles.get(i));
        }

        end: while (stopRequest == false) {
            try {
                Socket connection;
                synchronized (pool) { //コネクションを扱うスレッドプール
                    while (pool.isEmpty()) {
                        try {
//                            writeLogFile(processorNumber, "プールへ帰還");
                            nowWaitThreadCount++;
                            System.out.println("現在のプールの中のスレッド数＝"
                                    + nowWaitThreadCount);

                            pool.wait();
                        } catch (InterruptedException e) {

                        }
                        nowWaitThreadCount--;
                        if (stopRequest == true) { //終了の指示が出ていた場合
                            break end;
                        }
                    }
                    connection = (Socket) pool.remove(0);
                }

                ObjectInputStream in = new ObjectInputStream(
                        new DataInputStream(connection.getInputStream()));

                ExchangeInfo receiveInfo = (ExchangeInfo) in.readObject();

                if (receiveInfo.initializeClient == true) {
                    //クライアントの初期化を行うための接続の場合
                    //送られてきたコンテナに各種データを追加して返送する
                    DataContainer receiveContainer = (DataContainer) in
                            .readObject();
                    ClientInfo receiveCInfo = receiveContainer
                            .getDeriverdCInfo(); //クライアントの情報を取り出しておく
                    ServerMain.sDataToFile.clientDB.addClient(receiveCInfo); //内部のシグニチャをキーとして、データを追加
                    int grade = ServerMain.sDataToFile.clientDB
                            .getGradeOfClient(ancestor, receiveCInfo.signiture);
                    receiveCInfo.tmpIncluded.rankOfWorker = grade;
                    receiveCInfo.tmpIncluded.messageFromServer = ServerMain.sDataToFile.messageToClients; //メッセージを変更

                    synchronized (ServerMain.sDataToFile.svrInfo) { //プロジェクト総参加人数を加算
                        ServerMain.sDataToFile.svrInfo.workerCounts++;
                    }

                    InetAddress receivedAddress = (receiveContainer.getDeriverdCInfo()).tmpIncluded.svrInfo.address;
                    try {
                        //サーバ情報を追加
                        (receiveContainer.getDeriverdCInfo()).tmpIncluded.svrInfo = (ServerInfo) ServerMain.sDataToFile.svrInfo.clone(); //コンテナ中のデータにサーバ情報追加
                    } catch (CloneNotSupportedException e1) {
                        e1.printStackTrace();
                    }
                    (receiveContainer.getDeriverdCInfo()).tmpIncluded.svrInfo.address = receivedAddress;
                    
                    synchronized (ancestor.sGUI.clientTb) {
                        ancestor.sGUI.clientTb
                                .writeStateToTable(receiveContainer
                                        .getDeriverdCInfo()); //表へクライアント情報を追加
                    }
                    System.out
                            .println(receiveCInfo.participantNum + "へデータ送信開始");
                    ObjectOutputStream out = new ObjectOutputStream(
                            new DataOutputStream(connection.getOutputStream()));

                    //				ファイルの情報を書き込んでおく（実体はまだ）
                    ExchangeInfo sendExInfo = new ExchangeInfo();
                    out.writeObject(sendExInfo); //空だが一応送信

                    out.writeObject(fInfos);
                    out.writeObject(receiveContainer); //データを追加しておいたコンテナを返送

                    for (int i = 0; i < sendFiles.size(); i++) { //sendFilesに含まれるファイル情報を書き出す
                        writeFileToStream((File) sendFiles.get(i), out); //ファイル本体を書き込み
                    }

                    out.flush();
                    connection.close();
                    System.out
                            .println(receiveCInfo.participantNum + "へデータ送信完了");
                } else if (receiveInfo.receiveCulData == true) { //新しいjobを受け取るための接続の場合
//                    writeLogFile(processorNumber, "データ読み込み開始:receiveCulData");
                    DataContainer receiveContainer = (DataContainer) in
                            .readObject();

                    ClientInfo receiveCInfo = receiveContainer
                            .getDeriverdCInfo(); //クライアントの情報を取り出しておく
                    ServerMain.sDataToFile.clientDB.renewClient(receiveCInfo);
                    //内部のシグニチャをキーとして、データを更新
                    int grade = ServerMain.sDataToFile.clientDB
                            .getGradeOfClient(ancestor, receiveCInfo.signiture);
                    receiveCInfo.tmpIncluded.rankOfWorker = grade;
                    receiveCInfo.tmpIncluded.messageFromServer = ServerMain.sDataToFile.messageToClients; //メッセージを変更

                    InetAddress receivedAddress = (receiveContainer.getDeriverdCInfo()).tmpIncluded.svrInfo.address;
                    try {
                        //サーバ情報を追加
                        (receiveContainer.getDeriverdCInfo()).tmpIncluded.svrInfo = (ServerInfo) ServerMain.sDataToFile.svrInfo.clone(); //コンテナ中のデータにサーバ情報追加
                    } catch (CloneNotSupportedException e1) {
                        e1.printStackTrace();
                    }
                    (receiveContainer.getDeriverdCInfo()).tmpIncluded.svrInfo.address = receivedAddress;
                    
                    System.out.println(receiveCInfo.participantNum
                            + "へデータ送信開始:receiveCalData");
//                    writeLogFile(processorNumber, "ブロック1開始:receiveCalData");
                    ObjectOutputStream out = new ObjectOutputStream(
                            new DataOutputStream(connection.getOutputStream()));

                    ExchangeInfo sendInfo = new ExchangeInfo();
//                    writeLogFile(processorNumber, "ブロック2開始:receiveCalData");
                    out.writeObject(sendInfo); //空だが一応送信しておく
                    FileInformations noFInfos = new FileInformations();
                    //送信するファイルはないが書き込んでおく
//                    writeLogFile(processorNumber, "ブロック3開始:receiveCalData");
                    try{
	                    out.writeObject(noFInfos);
	
	                    CulculateData tmp;
	
	                    tmp = ServerMain.sDataToFile.jManager
	                            .generateJob((receiveContainer.getDeriverdCInfo()).signiture); //ユーザーの実装したDataGeneraterから計算データを得る
	                    
	                    (receiveContainer.getDeriverdCInfo()).tmpIncluded.culData = tmp;
                    }catch(Exception e){
                        e.printStackTrace();
//                        PrintStream tmp = new PrintStream(getLogFileOutStream(processorNumber));
//                        e.printStackTrace(tmp);
//                        tmp.flush();
//                        tmp.close();
//                        writeLogFile(processorNumber,"ブロック３で何らかのエラーが発生、ファイルへ出力しました");
                    }
                    
//                    writeLogFile(processorNumber, "ブロック4開始:receiveCalData");
                    out.writeObject(receiveContainer); //受信したコンテナ（各種データ追加済み）を返送
//                    writeLogFile(processorNumber, "ブロック5開始:receiveCalData");
                    out.flush();
//                    writeLogFile(processorNumber, "ブロック6開始:receiveCalData");

                    connection.close();
//                    writeLogFile(processorNumber, receiveCInfo.participantNum
//                            + "へデータ送信完了&ブロック６完了:receiveCalData");

                } else if (receiveInfo.backResult == true) { //結果の返送だった場合
//                    writeLogFile(processorNumber, "データ読み込み開始:backResult");
                    DataContainer receiveContainer = (DataContainer) in
                            .readObject();
                    ClientInfo receiveCInfo = receiveContainer
                            .getDeriverdCInfo(); //クライアントの情報を取り出しておく

                    ServerMain.sDataToFile.clientDB.renewClient(receiveCInfo); //内部のシグニチャをキーとして、データを更新
                    ancestor.sGUI.clientTb.writeStateToTable(receiveContainer
                            .getDeriverdCInfo()); //表のクライアント情報を更新
                    (receiveContainer.getDeriverdCInfo()).tmpIncluded.messageFromServer = ServerMain.sDataToFile.messageToClients; //メッセージを変更

                    CulculateData receiveResult = (receiveContainer
                            .getDeriverdCInfo()).tmpIncluded.culData;

                    ServerMain.sDataToFile.jManager.processCulculatedJob(
                            receiveResult, receiveContainer.getDeriverdCInfo());
//                    writeLogFile(processorNumber, "ブロック1開始:backResult");
                    ObjectOutputStream out = new ObjectOutputStream(
                            new DataOutputStream(connection.getOutputStream()));

                    ExchangeInfo sendInfo = new ExchangeInfo();
//                    writeLogFile(processorNumber, "ブロック2開始:backResult");
                    out.writeObject(sendInfo); //空だが一応送信しておく
                    FileInformations noFInfos = new FileInformations();
                    //送信するファイルはないが書き込んでおく
//                    writeLogFile(processorNumber, "ブロック3開始:backResult");
                    out.writeObject(noFInfos);
//                    writeLogFile(processorNumber, "ブロック4開始:backResult");
                    out.writeObject(receiveContainer); //受信したコンテナ（各種データ追加済み）を返送
//                    writeLogFile(processorNumber, "ブロック5開始:backResult");
                    out.flush();
//                    writeLogFile(processorNumber, "ブロック6開始:backResult");

                    connection.close();
//                    writeLogFile(processorNumber, receiveCInfo.participantNum
//                            + "へデータ送信完了&ブロック６完了:backResult");
                } else if (receiveInfo.quiteProject == true) { //クライアントからの脱退処理のための接続だった場合
//                    writeLogFile(processorNumber, "データ読み込み開始:quiteProject");
                    //送られてきたコンテナを得る
                    DataContainer receiveContainer = (DataContainer) in
                            .readObject();

                    ClientInfo receiveCInfo = receiveContainer
                            .getDeriverdCInfo(); //クライアントの情報を取り出しておく

                    ServerMain.sDataToFile.clientDB
                            .delOneClient(receiveCInfo.signiture); //クライアントのデータをデータベースから取り除く

                    //プロジェクト総参加人数を減らし、脱退人数を増やす
                    ServerMain.sDataToFile.svrInfo.workerCounts--;
                    ServerMain.sDataToFile.svrInfo.quiteMenberCount++;

                    synchronized (ancestor.sGUI.clientTb) {
                        ancestor.sGUI.clientTb
                                .removeStateFromTable(receiveContainer
                                        .getDeriverdCInfo()); //表からクライアント情報を削除
                    }
//                    writeLogFile(processorNumber, "ブロック1開始:quiteProject");
                    ObjectOutputStream out = new ObjectOutputStream(
                            new DataOutputStream(connection.getOutputStream()));

                    //				ファイルの情報を書き込んでおく（実体はまだ）
                    ExchangeInfo sendExInfo = new ExchangeInfo();
//                    writeLogFile(processorNumber, "ブロック2開始:quiteProject");
                    out.writeObject(sendExInfo); //空だが一応送信
//                    writeLogFile(processorNumber, "ブロック3開始:quiteProject");
                    out.writeObject(fInfos);
//                    writeLogFile(processorNumber, "ブロック4開始:quiteProject");
                    out.writeObject(receiveContainer); //データを追加しておいたコンテナを返送
//                    writeLogFile(processorNumber, "ブロック5開始:quiteProject");
                    out.flush();
//                    writeLogFile(processorNumber, "ブロック6開始:quiteProject");
                    connection.close();
//                    writeLogFile(processorNumber, receiveCInfo.participantNum
//                            + "へデータ送信完了&ブロック６完了:quiteProject");

                    ServerMain.sDataToFile.preserveAll();
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } finally {
//                writeLogFile(processorNumber, "何らかのエラーでFinallyブロックへ");
            }
        }
    }

    //スレッドプールを実装している関数、requestの対応をさせる
    public static void processRequest(Socket request) {

        synchronized (pool) {
            pool.add(pool.size(), request);
            pool.notifyAll();
        }
    }

    //引数のFileをOutputStreamへ書き込む
    private void writeFileToStream(File file, OutputStream stream) {
        try {
            FileInputStream in = new FileInputStream(file);
            int i;
            while ((i = in.read()) != -1) {
                stream.write(i);
            }
            in.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //DistributeFilesの中にあるclassファイルとjarファイルのFileオブジェクトを含んだArrayListを返す
    private ArrayList getClassJarFiles() {

        ArrayList tmp = new ArrayList();
        File distributeFilesDir = new File("./DistributeFiles");
        File[] distributeFiles = distributeFilesDir.listFiles(new FileFilter() { //拡張子がclassのファイルのlistを得る
                    public boolean accept(File pathname) {
                        String name = pathname.getName();
                        String fiveTale = name.substring(name.length() - 5); //後ろから５文字を切り出す
                        String threeTale = name.substring(name.length() - 3); //後ろから３文字切り出す
                        if ((fiveTale.equals("class") == true)
                                || (threeTale.equals("jar") == true)) { //拡張子がclassかjarであれば
                            return true;
                        } else {
                            return false;
                        }
                    }
                });
        for (int i = 0; i < distributeFiles.length; i++) {
            tmp.add(distributeFiles[i]);
        }

        return tmp;
    }

    //このスレッドの終了を要請する
    public void requestStop() {
        stopRequest = true;
    }

    //とりあえず、すべてのスレッドを待ち状態から開放する
    public static void finNotify() {
        synchronized (pool) {
            pool.notifyAll();
        }
    }

//    //デバッグ用のメソッド、スレッドナンバーを渡してログ出力用のファイルを生成
//    private void makeLogFile(int number) {
//        String strNum = String.valueOf(number);
//        File logFile = new File("./" + strNum + "Log.txt");
//        try {
//            logFile.createNewFile();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    //	デバッグ用のメソッド、与えたスレッド番号に対応するログファイルにテキストを出力
//    private void writeLogFile(int number, String str) {
//        String strNum = String.valueOf(number);
//        File logFile = new File("./" + strNum + "Log.txt");
//
//        FileWriter logFileOut = null;
//        try {
//            logFileOut = new FileWriter(logFile, true);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        try {
//            logFileOut.write(str + "\n");
//            logFileOut.flush();
//            if (logFileOut != null) {
//                logFileOut.close();
//            }
//        } catch (IOException e1) {
//            e1.printStackTrace();
//        }
//    }
//    
//    private FileOutputStream getLogFileOutStream(int number){
//        String strNum = String.valueOf(number);
//        File logFile = new File("./" + strNum + "Log.txt");
//
//        FileOutputStream logFileOut = null;
//        try {
//            logFileOut = new FileOutputStream(logFile);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        
//        return logFileOut;
//    }

}

