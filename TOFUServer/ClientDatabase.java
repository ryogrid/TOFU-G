/* ************************************************************** *
 *                                                                *
 * Copyright (c) 2005, Ryo Kanbayashi, All rights reserved.       *
 *                                                                *
 *                                                                *
 * This software is distributed under the modified BSD License.   *
 * ************************************************************** */

import java.io.*;
import java.util.*;

//クライアントのデータを全て保持し、操作するクラス
public class ClientDatabase implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 
     * @uml.property name="ancestor"
     * @uml.associationEnd multiplicity="(0 1)"
     */
    private ServerMain ancestor = null;

//    private volatile LinkedHashMap clientDatas = new LinkedHashMap(); //ClientInfoを複数保持するため
private volatile PersistentLinkedHashMap clientDatas = new PersistentLinkedHashMap(
    100,
    "./tmp/clientDatas.tmp"); //ClientInfoを複数保持するため

    /**
     * 
     * @uml.property name="clientDatasIterator"
     * @uml.associationEnd elementType="ClientInfo" multiplicity="(0 -1)"
     */
    private transient Iterator clientDatasIterator = null;


    public ClientDatabase() {
        clientDatasIterator = clientDatas.iterator(); //iteratorをセットしておく
    }

    //クライアントのデータのラッパーを一人分足す
    public void addClient(ClientInfo newClient) {
        synchronized(clientDatas){
            ServerMain.fMonitor.informChildMoving(); //書き込み禁止
        
        	try {
                clientDatas.put(byteArrayToString(newClient.signiture), newClient); //シグニチャをキーとしてデータを加える
            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException();
            }finally{
                ServerMain.fMonitor.cancelChildMoving(); //書き込み許可    
            }
        }
    }

    //クライアントのデータを更新する(実は足すのと同じ処理)
    public void renewClient(ClientInfo reClient) {
        synchronized (clientDatas) {
            ServerMain.fMonitor.informChildMoving(); //書き込み禁止
        
            try {
                clientDatas.put(byteArrayToString(reClient.signiture), reClient); //シグニチャをキーとしてデータを更新（上書き効果で）
            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException();
            }finally{
                ServerMain.fMonitor.cancelChildMoving(); //書き込み許可    
            }
        }
    }

    //一人分のクライアントデータをシグニチャ指定で得る
    public ClientInfo getOneInfo(byte[] signiture) {
        ClientInfo tmp;
        synchronized(clientDatas){
            ServerMain.fMonitor.informChildMoving(); //書き込み禁止
            try{
                tmp =  (ClientInfo) clientDatas.get(byteArrayToString(signiture));
            }finally{
                ServerMain.fMonitor.cancelChildMoving(); //書き込み許可    
            }
        }
        return tmp;
    }

    //一人分のクライアントデータをクライアントのシグニチャ指定で取り除く。取り除いたデータを返す。
    public ClientInfo delOneClient(byte[] signiture) {
        ClientInfo tmp;
        synchronized(clientDatas){
            ServerMain.fMonitor.informChildMoving(); //書き込み禁止
            try{
                tmp =  (ClientInfo) clientDatas.remove(byteArrayToString(signiture));    
            }finally{
                ServerMain.fMonitor.cancelChildMoving(); //書き込み許可    
            }
        }
        return tmp;
        
    }

    //データベースをリセットする
    public void resetDatabese() {
        synchronized(clientDatas){
            ServerMain.fMonitor.informChildMoving(); //書き込み禁止
            try{
                clientDatas = new PersistentLinkedHashMap(1000,"./tmp/clientDatas.tmp");
            }finally{
                ServerMain.fMonitor.cancelChildMoving(); //書き込み許可    
            }
        }
    }

/**
 * 
 * @uml.property name="clientDatas"
 */
//    //データの集合であるLinkedHashMapそのものを返す
//    public HashMap getClientDatas() {
//        synchronized(clientDatas){
//            return clientDatas;
//        }
//    }
//データの集合であるArrayListそのものをセット
public void setClientDatas(PersistentLinkedHashMap list) {
    synchronized (clientDatas) {
        ServerMain.fMonitor.informChildMoving(); //書き込み禁止
        try {
            clientDatas = list;
        } finally {
            ServerMain.fMonitor.cancelChildMoving(); //書き込み許可    
        }
    }
}

    //イテレータを初期化、最初の位置へ戻したい時に利用（意義に関して要検討）、コンストラクタで初期化はされている
    public synchronized void initCDatasIterator() {
        clientDatasIterator = clientDatas.iterator(); //iteratorをセット
    }

    //clientDatasIteratorのhasNext(); iteratorが初期化されているか注意
    public boolean clientDatasHasNext() {
        boolean tmp;
        synchronized(clientDatas){
            ServerMain.fMonitor.informChildMoving(); //書き込み禁止
            try{
                tmp = clientDatasIterator.hasNext();    
            }finally{
                ServerMain.fMonitor.cancelChildMoving(); //書き込み許可    
            }
        }
        return tmp;
    }

    //clinetDatasIteratorのnext(); iteratorが初期化されているか注意
    public ClientInfo clientDatasNext() {
        ClientInfo tmp = null;
        synchronized(clientDatas){
            ServerMain.fMonitor.informChildMoving(); //書き込み禁止
            
            try{
            	tmp = (ClientInfo) clientDatasIterator.next();
            }catch(Exception e){
                e.printStackTrace();
                System.out.println("clientDatasNext内でNullを返します");
                tmp = null;
            }finally{
                ServerMain.fMonitor.cancelChildMoving(); //書き込み許可    
            }
        }
        return tmp;
    }

    //クライアントの順位を得、その順位をＤＢの中のクラアントデータに書き込む
    //他のクライアントの順位も変更してテーブルに反映する
    //※ＤＢの中対象のクライアントデータを更新してから呼ぶこと
    public int getGradeOfClient(ServerMain ancestor, byte signiture[]) {
        ClientInfo target = getOneInfo(signiture);
        int oldGrade = target.tmpIncluded.rankOfWorker; //変更前の順位
        int newGrade = 1; //新しく設定する順位用変数
        initCDatasIterator();
        synchronized(clientDatas){
	        while (clientDatasHasNext()) {
	            ClientInfo tmp = clientDatasNext();
	            //対象のクライアントより上の者がいたら順位を加算
	            if ((tmp.AllWorkCounts > target.AllWorkCounts)) {
	                newGrade++;
	                //比較対象が対象のクライアントではなく、順位の逆転が起きる場合
	            } else if ((tmp.AllWorkCounts < target.AllWorkCounts)
	                    && (tmp.tmpIncluded.rankOfWorker <= oldGrade)
	                    && (byteArrayToString(target.signiture).equals(
	                            byteArrayToString(tmp.signiture)) == false)) {
	                tmp.tmpIncluded.rankOfWorker++;
	                ancestor.sGUI.clientTb.writeStateToTable(tmp); //テーブルに反映
	            }
	        }
        }
        target.tmpIncluded.rankOfWorker = newGrade; //DBの中の値を更新
        return newGrade;
    }

    //バイト配列をStringの文字列へ変換
    private String byteArrayToString(byte[] array) {
        StringBuffer tmp = new StringBuffer();
        for (int i = 0; i < array.length; i++) {
            tmp.append(array[i]);
        }
        String result = tmp.toString();
        return result;
    }

}