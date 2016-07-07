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

//過去に行ったjobの結果をLoggingし、取り出したりするクラス。必要な部分は適宜実装
public class ServerPastJobDatabase implements Serializable {

/**
 * 
 * @uml.property name="pastDatas"
 * @uml.associationEnd multiplicity="(1 1)"
 */
//	private ArrayList pastDatas = new ArrayList();    //過去のログを保持（CulculateDataを含むLogWrapperを保持）
private PersistentVector pastDatas = new PersistentVector(
    100,
    "./tmp/pastDatas.tmp"); //過去のログを保持（CulculateDataを含むLogWrapperを保持）

	
	public void addOneLogWraper(ServerLogWraper result){
		synchronized (pastDatas) {
		    ServerMain.fMonitor.informChildMoving();
		    
		    try {
                pastDatas.add(result);
            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException();
            }finally{
                ServerMain.fMonitor.cancelChildMoving();    
            }
        }
	}
	
	public CulculateData getOneJobLog(int index){
	    ServerLogWraper tmp;
	    synchronized (pastDatas) {
		    ServerMain.fMonitor.informChildMoving();
		    try{
		        tmp = (ServerLogWraper) pastDatas.get(index);    
		    }finally{
		        ServerMain.fMonitor.cancelChildMoving();    
		    }
		}
	    
		return  tmp.getWrapped();
	}
	
	//index指定でLogWraperを取り出す
	public ServerLogWraper getOneLogWraper(int index){
	    ServerLogWraper tmp;
	    synchronized (pastDatas) {
	        ServerMain.fMonitor.informChildMoving();
	        try{
	            tmp = (ServerLogWraper) pastDatas.get(index);    
	        }finally{
	            ServerMain.fMonitor.cancelChildMoving();    
	        }
	    }
        return  tmp;
	}
	
	//index指定でLogWraperを消去する
	public void delOneLogWraper(int index){
	    synchronized (pastDatas) {
	        ServerMain.fMonitor.informChildMoving();
	        try{
	            pastDatas.remove(index);    
	        }finally{
	            ServerMain.fMonitor.cancelChildMoving();    
	        }
	    }
	}
	
	//保存されているログの数を得る
	public int getLogCount(){
	    int tmp;
	    synchronized (pastDatas) {
	        ServerMain.fMonitor.informChildMoving();
	        try{
	            tmp = pastDatas.size();    
	        }finally{
	            ServerMain.fMonitor.cancelChildMoving();    
	        }
	    }
	    return tmp;
	}
	
//	//計算結果を保持しているArrayListの参照を返す
//	public Vector getVector(){
//	    synchronized (pastDatas) {
//	        return pastDatas;
//	    }
//	}
	
	//CulculateDataをLogWraperでラッピングする。同時に各種付随データも付加
	public ServerLogWraper wrapByLogWraper(CulculateData data){
		ServerLogWraper tmp = new ServerLogWraper(data);
		
		//付随データを付加する場合のコードを記述せよ
		
		return tmp;
	}
	
	

	
}
