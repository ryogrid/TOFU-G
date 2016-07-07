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

//過去に行ったjobの結果をLoggingし、取り出したりする抽象クラス。必要な部分は適宜実装
public class ClientPastJobDatabase implements Serializable {
//	private ArrayList pastDatas = new ArrayList();    //過去のログを保持（CulculateDataを含むLogWrapperを保持）
    String nickName = null;

    private PersistentVector pastDatas = null;

    //pathを与える
    public ClientPastJobDatabase(String nickname,ClassLoader loader){
        pastDatas = new PersistentVector(100,"./tmp/pastDatas.tmp",loader, "./ProjectFiles/"
    			+ nickname + "/");    //過去のログを保持（CulculateDataを含むLogWrapperを保持）
    }
    
    public ClientPastJobDatabase(){
        pastDatas = new PersistentVector(100,"./tmp/pastDatas.tmp");
    }
    
    //PersistentVectorのためのloaderをリセットする。再起動後などに使用    
    public void resetLoader(ClassLoader loader){
        if(pastDatas != null){
            pastDatas.resetLoader(loader);
        }
    }
    
	public synchronized void addOneLogWraper(ClientLogWraper result){
		try {
            pastDatas.add(result);
        } catch (IOException e) {
            e.printStackTrace();
        }
	}
	
	public CulculateData getOneJobLog(int index){
		DataOfEachProject tmp = (DataOfEachProject) pastDatas.get(index);
		return  tmp.culData;
	}
	
	//index指定でLogWraperを取り出す
	public ClientLogWraper getOneLogWraper(int index){
		ClientLogWraper tmp = (ClientLogWraper) pastDatas.get(index);
		return  tmp;
	}
	
	//index指定でLogWraperを消去する
	public void delOneLogWraper(int index){
		pastDatas.remove(index);
	}
	
	//保存されているログの数を得る
	public int getLogCount(){
		return pastDatas.size();
	}
	
//	//計算結果を保持しているArrayListの参照を返す
//	public Vector getVector(){
//		return pastDatas;
//	}
	
	//CulculateDataをLogWraperでラッピングする。同時に各種付随データも付加
	public ClientLogWraper wrapByLogWraper(CulculateData data){
		ClientLogWraper tmp = new ClientLogWraper(data);
		
		//付随データを付加する場合のコードを記述せよ
		
		return tmp;
	}
	


	
}
