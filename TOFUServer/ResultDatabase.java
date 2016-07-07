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

//タスクの結果をstoreし、取り出したりする抽象クラス。必要な部分は適宜実装
//結果とは言ってもDataOfEachProjectをそのまままるごと保持しておく
public abstract class ResultDatabase implements Serializable {

/**
 * 
 * @uml.property name="resultsDB"
 * @uml.associationEnd multiplicity="(1 1)"
 */
//	private ArrayList resultsDB = new ArrayList();    //計算結果を保存する
private PersistentVector resultsDB = new PersistentVector(
    100,
    "./tmp/resultsDB.tmp"); //計算結果を保存する

	
	public void addOneResult(DataOfEachProject result){
		synchronized(resultsDB){
		    ServerMain.fMonitor.informChildMoving();
		    
	    	try {
                resultsDB.add(result);
            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException();
            }finally{
                ServerMain.fMonitor.cancelChildMoving();    
            }
		}
	}
	
	public CulculateData getOneResult(int index){
		DataOfEachProject tmp;
	    synchronized(resultsDB){
	        ServerMain.fMonitor.informChildMoving();
	        try{
	            tmp = (DataOfEachProject) resultsDB.get(index);    
	        }finally{
	            ServerMain.fMonitor.cancelChildMoving();    
	        }
		}
		return  tmp.culData;
	}
	
	//index指定でDataOfEachProjectを取り出す
	public DataOfEachProject getOneDataOfEach(int index){
		DataOfEachProject tmp;
		synchronized (resultsDB) {
		    ServerMain.fMonitor.informChildMoving();
		    try{
		        tmp = (DataOfEachProject) resultsDB.get(index);    
		    }finally{
		        ServerMain.fMonitor.cancelChildMoving();    
		    }
        }
		return  tmp;
	}
	
	//保存されている計算結果の数を得る
	public int getResultCount(){
	    int tmp;
	    synchronized(resultsDB){
	        ServerMain.fMonitor.informChildMoving();
	        try{
	            tmp = resultsDB.size();    
	        }finally{
	            ServerMain.fMonitor.cancelChildMoving();    
	        }
	    }
	    return tmp;
	}
	
//	//計算結果を保持しているArrayListの参照を返す
//	public Vector getVector(){
//		synchronized(resultsDB){
//		    return resultsDB;
//		}
//	}
	
	//特定の条件の要素を取り出すメソッドが必要な場合は、
	//このクラスを継承して適宜実装すること
	
}
