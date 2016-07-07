/* ************************************************************** *
 *                                                                *
 * Copyright (c) 2005, Ryo Kanbayashi, All rights reserved.       *
 *                                                                *
 *                                                                *
 * This software is distributed under the modified BSD License.   *
 * ************************************************************** */

import java.util.Date;

// 再接続のための待ち時間などを管理するクラス
public class ReConnectWaiter {
	private static final int MUST_WAIT_COUNT = 5;
	private static final int DEFAULT_WAIT_TIME = 100;
	private static int waitTime = 100;   //ミリ秒
	private static int waitCount = 0;
	private static volatile boolean intteruptWait = false;  //wait

	//内部で保持している時間分Waitする
	public static void waitForDoing() {
		Date d = new Date();
		long waitStart = d.getTime();
		
		//待機の中断命令が無ければ、指定された時間待機を行う
		while((((d = new Date()).getTime()-waitStart)<= waitTime)&&(intteruptWait != true)){
			try {
				Thread.sleep(waitTime);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		increaceWaitTime();          //次回のwaitのために値を変更
		waitCount++;
	}

	//内部で保持している待ち時間をリセットする、isWaitable()を呼んでから利用すること
	public static void restAll() {
		waitTime = DEFAULT_WAIT_TIME;         //１秒にリセット
		waitCount = 0;
	}
	
	//待ち時間を次の段階へ移行する
	public static void increaceWaitTime(){
		waitTime = waitTime * 2; 
	}
	
	//待つ事ができるかどうかを返す（規定回数待機を繰り返したのではないであろうかと)
	public static boolean isWaitable(){
		if(waitCount >= MUST_WAIT_COUNT){
			return false;
		}else{
			return true;
		}
	}
	
	//待機から抜けるよう指示する
	public static void requestIntterupt(){
		intteruptWait = true;
	}
}