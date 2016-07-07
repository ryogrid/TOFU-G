/* ************************************************************** *
 *                                                                *
 * Copyright (c) 2005, Ryo Kanbayashi, All rights reserved.       *
 *                                                                *
 *                                                                *
 * This software is distributed under the modified BSD License.   *
 * ************************************************************** */

import java.util.Calendar;
import java.util.Iterator;

//定期的にファイルへの保存を行うスレッド
public class DataPreserver implements Runnable {
	private int checkIntervalMin = 10;        //チェックを行うインターバル。デフォルトは一時間
	private Calendar beforeCheckDate = null;       //前回チェックを行った日時
	private volatile boolean stopRequest = false;      //このスレッドが終了すべき時はtrueに
	
	public DataPreserver(){
		this.beforeCheckDate = Calendar.getInstance(); 
	}
	
	public DataPreserver(int checkInterval){
		this();
		this.checkIntervalMin = checkInterval;
	}
	
	public void run() {
	int debugNum=0;		
		while(stopRequest == false){
			//比較の時点からインターバル分引いた時間が前回実行時の日時より後の場合。
			//つまり、インターバル以上の時間が経過していた場合

			if(isProgressed(beforeCheckDate,Calendar.getInstance(),checkIntervalMin) == true){
				System.out.println("dataPreserverで" + debugNum++ + "回目のコール!!");
			    ServerMain.sDataToFile.preserveAll();        //ファイルへ保存
				this.beforeCheckDate = Calendar.getInstance();
			}else{     //比較すべき時間が経過していない場合sleep
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	//beforeからafterまでにInterval分の時間が経過したのかを返す。
	public boolean isProgressed(Calendar before,Calendar after,int intervalMin){
		after.add(Calendar.MINUTE,-1* intervalMin);
		if(before.after(after)==false){
			after.add(Calendar.MINUTE,intervalMin);   //元に戻す
			return true;
		}else{
			after.add(Calendar.MINUTE,intervalMin);   //元に戻す
			return false;
		}
	}
	
	//処理を停止するよう指示する
	public void requestIntterupt(){
		stopRequest = true;
	}
}
