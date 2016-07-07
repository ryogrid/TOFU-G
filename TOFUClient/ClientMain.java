/* ************************************************************** *
 *                                                                *
 * Copyright (c) 2005, Ryo Kanbayashi, All rights reserved.       *
 *                                                                *
 *                                                                *
 * This software is distributed under the modified BSD License.   *
 * ************************************************************** */

import java.net.*;
import java.util.*;
import java.util.zip.GZIPInputStream;
import java.awt.Image;
import java.awt.Toolkit;
import java.io.*;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.swing.JOptionPane;

public class ClientMain {
    public static volatile FileMonitor fMonitor = new FileMonitor(); // ファイル保存の排他処理を行うモニター
    public static volatile CPreserveData cDataToFile = new CPreserveData(); // ファイルに保存すべきデータ

    private Socket sock = null;
    private ObjectInputStream in = null;
    private ObjectOutputStream out = null;
    private FileOutputStream fOutCulculater = null;
    private FileOutputStream fOutData = null;
    public ClientGui cGUI = null;
    public PermissionClassLoader myloader = new PermissionClassLoader(
            ClassLoader.getSystemClassLoader());

    public Thread dataPreserverThread = null; // DataPreserverを保持するためのスレッド
    public DataPreserver dataPreserver = null; // 定期的にデータを保存するスレッド

    public ClientMain() {

        startPrepare();
        cGUI = new ClientGui(this);
        while (ClientMain.cDataToFile.alreadyInit == false) { // 一度も初期設定を行っていない場合
            Object[] msg = { "まず初期設定を行って下さい" };
            JOptionPane.showOptionDialog(cGUI, msg, "警告",
                    JOptionPane.PLAIN_MESSAGE, 0, null, null, null);
            fillClientDat();
        }
        cGUI.informChangeToComp(); // ファイル中のフラグによって描画変更

        while (ClientMain.cDataToFile.sInfosHasNext()) { // タブの描画だけ行う
            DataOfEachProject assignedPro = (DataOfEachProject) ClientMain.cDataToFile
                    .sInfosNext();
            Toolkit toolkit = Toolkit.getDefaultToolkit();
            Image img = toolkit
                    .getImage("./ProjectFiles/" + assignedPro.svrInfo.nickname
                            + "/" + "ProjectPicture.jpg");
            cGUI.addProjectTabMenu(assignedPro.projectNumber, img, assignedPro); // タブを追加する
        }

        dataPreserver = new DataPreserver(1); // １分間隔で保存を行わせる
        dataPreserverThread = new Thread(dataPreserver);
        dataPreserverThread.start();

        // 自動計算が許可されている場合、アプリケーション起動時にすべてのプロジェクトの処理を開始する
        if (ClientMain.cDataToFile.sePolicy.autCulculate) {
            startAllThread();
            for (int j = 0; j < 10; j++) {
                if (cGUI.startStopEachItem[j] != null) {
                    int projectNumber = j + 1; // プロジェクト番号を表すために１つ値を増加
                    DataOfEachProject tmpPro = ClientMain.cDataToFile
                            .getDataOfEachByProNum(projectNumber);
                    cGUI.startStopEachItem[j].setText("STOP "
                            + tmpPro.svrInfo.nickname + "!"); // STOPボタンにチェンジ
                }
            }
        }
    }

    // すべてのプロジェクトのスレッドを開始する(開始してしまっているスレッドがあっても大丈夫)
    public void startAllThread() {

        int i = 0; // 現在何個目のプロジェクトかのカウンタ
        // 以下でプロジェクトごとに処理スレッドを起動し、タブへ描画を行う
        ClientMain.cDataToFile.initSInfosIterator();
        while (ClientMain.cDataToFile.sInfosHasNext()) { // プロジェクトデータの次の要素がある限り
            i++; // プロジェクト数カウンタを増加させる
            DataOfEachProject assignedPro = (DataOfEachProject) ClientMain.cDataToFile
                    .sInfosNext();

            startOneProThread(assignedPro); // プロジェクトのデータを与えてそのプロジェクトのスレッドを開始
        }
    }

    // 与えたプロジェクトのスレッドの処理スレッドを開始する（クラスロードなども行っている)
    public void startOneProThread(DataOfEachProject assignedPro) {

        if (assignedPro.variousState == assignedPro.NOTSTART) {
            assignedPro.variousState = assignedPro.AFTERINIT;

            eachProPrepare(assignedPro);

            EachProThread eachThre = new EachProThread(assignedPro, this);

            Toolkit toolkit = Toolkit.getDefaultToolkit();
            Image img = toolkit.getImage("./" + assignedPro.svrInfo.nickname
                    + "/" + "ProjectPicture.jpg");

            try { // ここで計算処理のクラスを代入しておく
                myloader.setPath("./ProjectFiles/"
                        + assignedPro.svrInfo.nickname + "/"); // 事前のpath設定忘れずに
                eachThre.worker = (Culculater) myloader.loadClass(
                        assignedPro.svrInfo.nickname + "Worker").newInstance();
                myloader.loadClass(assignedPro.svrInfo.nickname + "Data");
                myloader.loadClass(assignedPro.svrInfo.nickname + "Memo");
                if (assignedPro.culMemo == null) { // もし今回が初回で、ClinetMemoが保持されていない場合
                    assignedPro.culMemo = (ClientMemo) (myloader
                            .loadClass(assignedPro.svrInfo.nickname + "Memo")
                            .newInstance());
                }
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }

            eachThre.start();
            ClientMain.cDataToFile.addProThreByIndex(
                    assignedPro.projectNumber - 1, eachThre); // 配列として集めて保持しておく
        }
    }

    // 初期設定用ダイアログを表示する。実際に設定を行った場合１、行えなかった場合０を返す
    public boolean fillClientDat() {
        int okOrNo = JOptionPane.showConfirmDialog(cGUI,
                "初期設定を行ってよいですか？(ユーザー情報がリセットされます）", "確認",
                JOptionPane.YES_NO_OPTION);
        if (okOrNo == JOptionPane.OK_OPTION) {

            ClientMain.cDataToFile = new CPreserveData(); // 既存の内容をリセットする

            String strTmp; // 一旦記入された内容を保持（if文での利用のため）
            int intTmp; // 一旦記入された時の戻り値を保持
            strTmp = JOptionPane.showInputDialog(cGUI, "名前を記入して下さい", "初期設定",
                    JOptionPane.PLAIN_MESSAGE);
            if (strTmp != null) { // 取り消しを押されていなければ
                ClientMain.cDataToFile.cInfo.participantNum = strTmp;
            } else {
                return false;
            }

            strTmp = JOptionPane.showInputDialog(cGUI, "よろしければメールアドレスを記入して下さい",
                    "初期設定", JOptionPane.PLAIN_MESSAGE);
            if (strTmp != null) { // 取り消しを押されていなければ
                ClientMain.cDataToFile.cInfo.mailAddress = strTmp;
            } else {
                return false;
            }

            intTmp = JOptionPane.showConfirmDialog(cGUI, "サーバへの自動接続を許可しますか？",
                    "確認", JOptionPane.YES_NO_OPTION);
            if (intTmp == JOptionPane.YES_OPTION) {
                ClientMain.cDataToFile.sePolicy.autConnect = true;
            } else if (intTmp == JOptionPane.NO_OPTION) {
                ClientMain.cDataToFile.sePolicy.autConnect = false;
            }

            intTmp = JOptionPane.showConfirmDialog(cGUI, "自動計算を許可しますか？", "確認",
                    JOptionPane.YES_NO_OPTION);
            if (intTmp == JOptionPane.YES_OPTION) {
                ClientMain.cDataToFile.sePolicy.autCulculate = true;
            } else if (intTmp == JOptionPane.NO_OPTION) {
                ClientMain.cDataToFile.sePolicy.autCulculate = false;
            }
            // 名前がちゃんと記入されていれば
            if (ClientMain.cDataToFile.cInfo.participantNum != null) {
                // 乱数とシステムタイムからダイジェストを生成しシグニチャとする
                ClientMain.cDataToFile.cInfo.signiture = ClientMain.cDataToFile
                        .generateHash(String
                                .valueOf((long) (Math.random() * 100000000000000000L)));
                // clientアプリケーションを使い始めた日を記録する
                ClientMain.cDataToFile.cInfo.startUseDate = Calendar
                        .getInstance();
                ClientMain.cDataToFile.alreadyInit = true;
                cDataToFile.preserveAll();
                cGUI.informChangeToComp(); // フラグが変更されたので再描画
                return true;
            }
        }
        return false;
    }

    // 新規プロジェクト参加のダイアログを表示する(この中で実際に接続も行う）
    public void fillNewPro() throws ConnectCanceledException {

        int okOrNo = JOptionPane.showConfirmDialog(cGUI, "新規プロジェクト参加の設定をしますか？",
                "確認", JOptionPane.YES_NO_OPTION);
        if (okOrNo == JOptionPane.OK_OPTION) {
            String fillAddress = JOptionPane.showInputDialog(cGUI,
                    "新しく参加するプロジェクトのアドレスを記入して下さい", "new Project",
                    JOptionPane.PLAIN_MESSAGE);
            if (fillAddress != null) {
                try {
                    DataOfEachProject tmpDataOfEach = new DataOfEachProject();
                    tmpDataOfEach.svrInfo.address = InetAddress
                            .getByName(fillAddress);

                    String strTmp = JOptionPane.showInputDialog(cGUI,
                            "運営者へメッセージがあれば記入して下さい", "new Project",
                            JOptionPane.PLAIN_MESSAGE);
                    if (strTmp != null) { // 取り消しを押されていなければ
                        tmpDataOfEach.messageToServer = strTmp;
                    } else {
                        tmpDataOfEach.messageToServer = "";
                    }

                    tmpDataOfEach.joinDate = Calendar.getInstance();

                    // 同一のプロジェクトに参加する事にならないかチェック
                    boolean isExistSameProFlag = false; // 同一のプロジェクトがあったか
                    ClientMain.cDataToFile.initSInfosIterator();
                    while (ClientMain.cDataToFile.sInfosHasNext()) {
                        DataOfEachProject tmp = ClientMain.cDataToFile
                                .sInfosNext();
                        if (tmp.svrInfo.address
                                .equals(tmpDataOfEach.svrInfo.address)) {
                            // アドレスが一致するプロジェクトがあった場合
                            Object[] msg = { "同一のプロジェクトへ複数回参加登録を行う事はできません" };
                            JOptionPane.showOptionDialog(cGUI, msg, "警告",
                                    JOptionPane.PLAIN_MESSAGE, 0, null, null,
                                    null);
                            isExistSameProFlag = true;
                            break;
                        }
                    }

                    boolean alreadyFillFlag = false; // すでに規定数のプロジェクトに参加してしまっているか
                    if (ClientMain.cDataToFile.getProjectCount() >= 10) {
                        // アドレスが一致するプロジェクトがあった場合
                        Object[] msg = { "あなたはもう１０のプロジェクトに参加しています。これ以上は参加できません" };
                        JOptionPane.showOptionDialog(cGUI, msg, "警告",
                                JOptionPane.PLAIN_MESSAGE, 0, null, null, null);
                        alreadyFillFlag = true;
                    }

                    if ((isExistSameProFlag == false)
                            && (alreadyFillFlag == false)) { // 同一のプロジェクトが存在せずプロジェクト数が規定以内であれば
                        tmpDataOfEach = joinNewPro(tmpDataOfEach,
                                ClientMain.cDataToFile.cInfo);

                        boolean isExistSameNickname = false;
                        ClientMain.cDataToFile.initSInfosIterator();
                        // 同じニックネームのプロジェクトがないか走査する
                        while (ClientMain.cDataToFile.sInfosHasNext()) {
                            DataOfEachProject tmp = ClientMain.cDataToFile
                                    .sInfosNext();
                            // ニックネームの一致するプロジェクトがあった場合
                            if (tmp.svrInfo.nickname
                                    .equals(tmpDataOfEach.svrInfo.nickname)) {
                                Object[] msg = { "残念ながら同一のニックネームを使用するプロジェクトへは参加できません" };
                                JOptionPane.showOptionDialog(cGUI, msg, "警告",
                                        JOptionPane.PLAIN_MESSAGE, 0, null,
                                        null, null);
                                isExistSameNickname = true;
                                break;
                            }
                        }

                        // 同一のニックネームを持つプロジェクトが存在しなければ
                        if (isExistSameNickname == false) {
                            tmpDataOfEach.setPastJobDB(
                                    tmpDataOfEach.svrInfo.nickname,
                                    this.myloader); // pastJobDBの保存場所などをセットする

                            tmpDataOfEach.projectNumber = ClientMain.cDataToFile
                                    .getProjectCount() + 1; // 何個目のプロジェクトかを記録

                            ClientMain.cDataToFile.addDatOfEach(tmpDataOfEach);

                            Toolkit toolkit = Toolkit.getDefaultToolkit();
                            Image img = toolkit.getImage("./ProjectFiles/"
                                    + tmpDataOfEach.svrInfo.nickname + "/"
                                    + "ProjectPicture.jpg");

                            cGUI.addProjectTabMenu(ClientMain.cDataToFile
                                    .getProjectCount(), img, tmpDataOfEach);
                            ClientMain.cDataToFile.preserveAll(); // 新プロジェクトに参加したので保存
                            cGUI.informChangeToComp(); // ファイル中のフラグによって描画変更

                            tmpDataOfEach.nextShouldDoThing = tmpDataOfEach.SHOULDRECIVEJOB; // 処理する上で次にすべき事を指定しておく
                        }
                    }

                } catch (UnknownHostException e1) {
                    Object[] msg = { "アドレスが間違っているか、何らかの理由で解決できません" };
                    JOptionPane.showOptionDialog(cGUI, msg, "警告",
                            JOptionPane.PLAIN_MESSAGE, 0, null, null, null);
                } catch (IOException e2) { // ソケット関係の例外の場合
                    Object[] msg = { "サーバが停止しているか何らかの理由でサーバへ接続できませんでした" };
                    JOptionPane.showOptionDialog(cGUI, msg, "失敗",
                            JOptionPane.PLAIN_MESSAGE, 0, null, null, null);
                }
            }
        }
    }

    public static void main(String[] args) {
        ClientMain cMain = new ClientMain();
    }

    // スレッド開始時のための各種処理を行う
    public void startPrepare() {
        loadJarFileURLs();
        loadProjectFiles(); // プロジェクトからクラスファイルを得ている場合ロードしておく
        String proFilesPaths[] = getProFilePathsFromDir(); // プロジェクトファイルのpathを示す文字列群を得る
        CPreserveData tmp = ClientMain.cDataToFile.readOutAll(myloader,
                proFilesPaths);

        // ファイルからデータを読み出して代入する
        if (tmp != null) { // 読み出しに失敗してnullが返ってきてなければ
            ClientMain.cDataToFile = tmp;
            ClientMain.cDataToFile.initProthre(); // シリアライズされていないので初期化しておく
            ClientMain.cDataToFile.initProThreIterator(); // Iteratorはシリアライズしていないのでここで代入しておく
            ClientMain.cDataToFile.initSInfosIterator();
        }

        // 処理優先度を変更
        if ((ClientMain.cDataToFile.priorityLevel > 0)
                && (ClientMain.cDataToFile.priorityLevel <= 10)) {
            setPriority(ClientMain.cDataToFile.priorityLevel);
        } else {
            ClientMain.cDataToFile.priorityLevel = ClientMain.cDataToFile.PRIORITYLAW;
        }

    }

    public void fin() { // 終了時の処理
        if (dataPreserver != null) { // 定期保存スレッドの停止処理
            dataPreserver.requestIntterupt();
            while (dataPreserverThread.isAlive() == true) {
                try {
                    Thread.sleep(50);
                    // 本当に終了するまで待機
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        ClientMain.cDataToFile.initProThreIterator();
        while (ClientMain.cDataToFile.proThreHasNext()) {
            EachProThread tmp = (EachProThread) ClientMain.cDataToFile
                    .proThreNext();
            tmp.requestStop();
            while (tmp.isAlive() == true) {
                try {
                    // 本当に終了するまで無限ループ
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        // 次回起動時のためにstateを初期化しておく
        ClientMain.cDataToFile.initSInfosIterator();
        while (ClientMain.cDataToFile.sInfosHasNext()) {
            DataOfEachProject tmp = ClientMain.cDataToFile.sInfosNext();
            tmp.variousState = tmp.NOTSTART;
        }

        ClientMain.cDataToFile.preserveAll(); // ファイルに保存する
    }

    // プロジェクトへの新規参加を行う(targetの内容を変更して返す）
    // 引数 target : 各々のプロジェクト関する情報（対象プロジェクト） myself : クライアント自信の情報
    // アドレスは設定してからtargetを渡すように注意
    // job、クラスロードは別。ここではファイルとサーバ情報受け取りのみ、一度だけ呼べばよい
    public DataOfEachProject joinNewPro(DataOfEachProject target,
            ClientInfo myself) throws ConnectCanceledException, IOException {
        try {

            // 自動接続が許可されていない場合、許可の確認を行う
            if (ClientMain.cDataToFile.sePolicy.autConnect == false) {
                if (checkConnectable(target) == false) {
                    throw new ConnectCanceledException();
                }
            }

            InetAddress svrAddress = target.svrInfo.address; // アドレスを取り出す

            // sock = ClientMain.makeSSLClientSocket(svrAddress, 2525);
            sock = ClientMain.makeSSLClientSocket(InetAddress
                    .getByAddress(svrAddress.getAddress()), 2525);

            out = new ObjectOutputStream(new DataOutputStream(sock
                    .getOutputStream()));
            ExchangeInfo sendInfo = new ExchangeInfo();
            sendInfo.initializeClient = true; // クライアントの初期化のための接続だと表明
            DataContainer sendContainer = new DataContainer();
            myself.tmpIncluded = target; // 一時的な保持スペースに押し込める
            sendContainer.setDeriverdCInfo(myself);
            out.writeObject(sendInfo);
            out.writeObject(sendContainer); // クライアントからの情報を送信
            out.flush();

            in = new MyObjectInputStream(new DataInputStream(sock
                    .getInputStream()), myloader);
            // ソケットでのデータ読み取り用

            ExchangeInfo receiveExInfo = (ExchangeInfo) in.readObject();
            FileInformations fInfos = (FileInformations) in.readObject();

            DataContainer receiveContainer = (DataContainer) in.readObject();

            File newDir = new File(
                    "./ProjectFiles/"
                            + receiveContainer.getDeriverdCInfo().tmpIncluded.svrInfo.nickname);
            newDir.mkdir(); //
            extractFileToLocal(
                    fInfos,
                    in,
                    "./ProjectFiles/"
                            + receiveContainer.getDeriverdCInfo().tmpIncluded.svrInfo.nickname
                            + "/"); // ファイルをすべてローカルに保存
            
            loadJarFileURLs();     //jarのURLを全て設定し直す
            
            myself.tmpIncluded = null; // 一時的に使用していたスペースをクリア（参照元を変更しないため）
            sock.close();

            // サーバ情報を取り出して返す
            return (receiveContainer.getDeriverdCInfo()).tmpIncluded;

        } catch (ClassNotFoundException e) {
            // 何もなかったものとしてメソッドを終了する
            return null;
        } finally {
            if (sock != null) {
                sock.close();
            }
        }
    }

    // 指定したプロジェクトの脱退処理を行う
    public DataOfEachProject quitOneProject(DataOfEachProject target,
            ClientInfo myself) throws ConnectCanceledException, IOException {

        // 自動接続が許可されていない場合、許可の確認を行う
        if (ClientMain.cDataToFile.sePolicy.autConnect == false) {
            if (checkConnectable(target) == false) {
                throw new ConnectCanceledException();
            }
        }

        InetAddress svrAddress = target.svrInfo.address; // アドレスを取り出す
        // sock = new Socket(svrAddress, 2525);
        try {
            // sock = ClientMain.makeSSLClientSocket(svrAddress, 2525);
            sock = ClientMain.makeSSLClientSocket(InetAddress
                    .getByAddress(svrAddress.getAddress()), 2525);

            out = new ObjectOutputStream(new DataOutputStream(sock
                    .getOutputStream()));
            ExchangeInfo sendInfo = new ExchangeInfo();
            sendInfo.quiteProject = true; // プロジェクト脱退のための接続だと表明
            DataContainer sendContainer = new DataContainer();
            myself.tmpIncluded = target; // 一時的な保持スペースに押し込める
            sendContainer.setDeriverdCInfo(myself);
            out.writeObject(sendInfo);
            out.writeObject(sendContainer); // クライアントからの情報を送信
            out.flush();

            in = new MyObjectInputStream(new DataInputStream(sock
                    .getInputStream()), myloader);
            // ソケットでのデータ読み取り用

            ExchangeInfo receiveExInfo = (ExchangeInfo) in.readObject();
            FileInformations fInfos = (FileInformations) in.readObject();

            DataContainer receiveContainer = (DataContainer) in.readObject();

            myself.tmpIncluded = null; // 一時的に使用していたスペースをクリア（参照元を変更しないため）
            sock.close();

            // サーバ情報を取り出して返す
            return (receiveContainer.getDeriverdCInfo()).tmpIncluded;

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        } finally {
            sock.close();
        }
    }

    // 後のオブジェクト受信との順序を変更しないように注意
    // コンテナの中からファイルの情報を抽出して、ストリームからローカルのディレクトリに展開
    // "./XXX/"という形式でpathの文字列を与える
    private void extractFileToLocal(FileInformations info, InputStream in,
            String path) {

        long fileLength = 0; // 読み込むファイルのlength
        long readedByte;
        for (int i = 0; (fileLength = info.fileLengths[i]) != -1; i++) {
            try {
                FileOutputStream out;
                if ((info.fileNames[i]
                        .substring(info.fileNames[i].length() - 3))
                        .equals("jar")) { // 拡張子がjarの場合
                    out = new FileOutputStream("./ProjectUsingJars/"
                            + info.fileNames[i]);
                } else {
                    out = new FileOutputStream(path + info.fileNames[i]);
                }
                readedByte = 0; // 読み込み済みのバイト数
                while (readedByte != fileLength) {
                    // 指定された長さで読み込む
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

    // プロジェクトファイルをProjectFilesの中から検索しloadするメソッド
    private void loadProjectFiles() {
        File projectFilesDir = new File("./ProjectFiles");
        File eachProjectDir[] = projectFilesDir.listFiles();

        for (int i = 0; i < eachProjectDir.length; i++) { // ディレクトリの中のファイルを探索し、ロードする
            File eachClassFileName[] = eachProjectDir[i]
                    .listFiles(new FileFilter() { // 拡張子がclassのファイルのlistを得る
                        public boolean accept(File pathname) {
                            String name = pathname.getName();
                            String fiveTale = name.substring(name.length() - 5); // 後ろから５文字を切り出す
                            if ((fiveTale.equals("class") == true)) { // 拡張子がclassであれば
                                return true;
                            } else {
                                return false;
                            }
                        }
                    });

            for (int j = 0; j < eachClassFileName.length; j++) {
                try {
                    myloader.setPath("./ProjectFiles/"
                            + eachProjectDir[i].getName() + "/"); // 事前のpath設定忘れずに
                    myloader
                            .loadClass(eachClassFileName[j].getName()
                                    .substring(
                                            0,
                                            eachClassFileName[j].getName()
                                                    .length() - 6)); // クラスファイルをロード
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // クラスローダーにjarFileのpathを認識させる
    //以前の内容は捨てて、全て設定し直す
    private void loadJarFileURLs() {
        File jarURLs[] = (new File("./ProjectUsingJars"))
                .listFiles(new FileFilter() { // 拡張子がclassのファイルのlistを得る
                    public boolean accept(File pathname) {
                        String name = pathname.getName();
                        String fiveTale = name.substring(name.length() - 3); // 後ろから3文字を切り出す
                        if ((fiveTale.equals("jar") == true)) { // 拡張子がclassであれば
                            return true;
                        } else {
                            return false;
                        }
                    }
                });
        
        myloader.initURL();
        //クラスローダーに設定
        if(jarURLs!=null){
            for(int i=0;i<jarURLs.length;i++){
                try {
                    myloader.addURL(jarURLs[i].toURL());
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // 与えられたプロジェクトのディレクトリーをまるごと消去する
    public void removeProjectFiles(DataOfEachProject pro) {
        String path = "./ProjectFiles/" + pro.svrInfo.nickname;
        File targetDir = new File(path);
        File includedFiles[] = targetDir.listFiles();

        // 対象のディレクトリ内のファイルを全て消去
        for (int i = 0; i < includedFiles.length; i++) {
            includedFiles[i].delete();
        }

        targetDir.delete();
    }

    // ProjectFilesディレクトリ内のすべてのディレクトリ・ファイルを削除する
    public void removeAllProjectFiles() {
        String path = "./ProjectFiles";
        File targetDir = new File(path);
        File eachProjectDirs[] = targetDir.listFiles();

        // ディレクトリ内のファイルを全て消去
        for (int i = 0; i < eachProjectDirs.length; i++) {
            File eachProjectFiles[] = eachProjectDirs[i].listFiles();

            for (int j = 0; j < eachProjectFiles.length; j++) {
                eachProjectFiles[j].delete();
            }
            eachProjectDirs[i].delete(); // ディレクトリ本体を削除
        }
    }

    // ディレクトリの構成からそれぞれのプロジェクトのファイルがあるディレクトリのpathたちを得る
    private String[] getProFilePathsFromDir() {
        File projectFilesDir = new File("./ProjectFiles");
        String eachProjectDir[] = projectFilesDir.list();

        for (int i = 0; i < eachProjectDir.length; i++) {
            eachProjectDir[i] = "./ProjectFiles/" + eachProjectDir[i];
        }

        return eachProjectDir;
    }

    static synchronized SSLSocket makeSSLClientSocket(InetAddress server,
            int portno) throws IOException {

        try {
            SSLSocket ss;
            SSLSocketFactory ssf = (SSLSocketFactory) SSLSocketFactory
                    .getDefault();
            System.out.println(server.getHostAddress() + "へのコネクションを張るよう試みます");
            Socket s = new Socket(server, portno);

            ss = (SSLSocket) ssf.createSocket(s, server.getHostAddress(),
                    portno, true);
            String cipherSuites[] = { "SSL_DH_anon_EXPORT_WITH_DES40_CBC_SHA" };
            ss.setEnabledCipherSuites(cipherSuites);

            return (ss);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("例外直後のmakeSSLClientScoketでのアドレス="
                    + server.getHostAddress());
            throw e;
        }
    }

    // 処理を開始しているプロジェクトの処理を中止させる（開始していなくてもＯＫとする）
    public void stopOneThread(DataOfEachProject stopped) {

        // 処理を開始している場合
        if (stopped.variousState != stopped.NOTSTART) {
            EachProThread tmp = ClientMain.cDataToFile.getProThreByPro(stopped);
            tmp.requestStop();
            while (tmp.isAlive() == true) {
                try {
                    // 本当に終了するまで無限ループ
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            stopped.variousState = stopped.NOTSTART; // 未開始状態だと表明
            ClientMain.cDataToFile.addDatOfEach(stopped);
        }
    }

    // ユーザーに対してサーバへの接続を行ってよいか確認する。（自動接続が許可されていない場合）
    public boolean checkConnectable(DataOfEachProject project) {
        int okOrNo = JOptionPane
                .showConfirmDialog(
                        cGUI,
                        ((project.svrInfo.projectNum != null) ? (project.svrInfo.projectNum)
                                : ("新規"))
                                + "のサーバ("
                                + project.svrInfo.address.getHostAddress()
                                + ")への接続を許可しますか？", "接続許可",
                        JOptionPane.YES_NO_OPTION);

        if (okOrNo == JOptionPane.OK_OPTION) {
            return true;
        } else {
            return false;
        }
    }

    // 全てのプロジェクトスレッドを終了する(開始していないプロジェクトがあっても大丈夫）
    public void stopAllThread() {

        ClientMain.cDataToFile.initSInfosIterator();
        while (ClientMain.cDataToFile.sInfosHasNext()) { // 順次終了していく
            stopOneThread(ClientMain.cDataToFile.sInfosNext());
        }
        ClientMain.cDataToFile.initProthre();
    }

    // メインスレッド(とそれに呼び出されたスレッド）の優先度を変更する事でアプリケーションの優先度を変更する
    public void setPriority(int level) {
        Thread nowThread = Thread.currentThread();
        nowThread.setPriority(level);

        ClientMain.cDataToFile.initProThreIterator();
        while (ClientMain.cDataToFile.proThreHasNext()) {
            Thread tmp = ClientMain.cDataToFile.proThreNext();
            tmp.setPriority(level);
        }
    }

    // 各プロジェクトのスレッドを開始する時におこなう初期化などの処理
    public void eachProPrepare(DataOfEachProject proData) {
        if (proData.pastJobDB != null) {
            proData.resetLoader(this.myloader);
        }
    }
}