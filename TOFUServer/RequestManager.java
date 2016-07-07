/* ************************************************************** *
 *                                                                *
 * Copyright (c) 2005, Ryo Kanbayashi, All rights reserved.       *
 *                                                                *
 *                                                                *
 * This software is distributed under the modified BSD License.   *
 * ************************************************************** */

import java.io.IOException;
import java.net.*;
import java.net.ServerSocket;
import java.net.Socket;

public class RequestManager implements Runnable {
private final int TIME_OUT_SECONDS = 120;   //クライアントへのデータ送信でのタイムアウト値

/**
 * 
 * @uml.property name="ancestor"
 * @uml.associationEnd inverse="requestManager:ServerMain" multiplicity="(1 1)"
 */
private ServerMain ancestor = null;

private ServerSocket svsock = null;
private Thread threads[];                //RequestProcessorを内包するスレッドを保持

/**
 * 
 * @uml.property name="requestProcessors"
 * @uml.associationEnd multiplicity="(0 -1)"
 */
private RequestProcessor requestProcessors[]; //RequestProcessorを保持する

final int THREAD_NUMBER = 10;          //スレッドプールに用意するスレッド数
private volatile boolean stopRequest = false;      //このスレッドが終了すべき時はtrueに

	public RequestManager(ServerSocket socket,ServerMain parent){
		svsock = socket;
		ancestor = parent;
	}
	public void run() {
		
		requestProcessors = new RequestProcessor[THREAD_NUMBER];
		threads = new Thread[THREAD_NUMBER];
		for(int i = 0; i < THREAD_NUMBER; i++){     //クライアントの要求を処理するスレッドを複数生成
			requestProcessors[i] = new RequestProcessor(ancestor,i);
			threads[i] = new Thread(requestProcessors[i]);
			threads[i].start();
		}
		
		try {
			svsock = ServerMain.makeSSLServerSocket(2525);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		try{
			while(stopRequest == false){    //acceptしたコネクションをスレッドプールへ
				try{
					Socket request = svsock.accept();
					request.setSoTimeout(TIME_OUT_SECONDS * 120);//タイムアウトを設定
					RequestProcessor.processRequest(request);
				}catch(IOException e){
					e.printStackTrace();
				}
			}
		}finally{
System.out.println("コネクションの振り分けのところのループ脱出");		    
		}
	}
	
	//このスレッドの終了を要請する
	public void requestStop(){
		stopRequest = true;
		
		//各リクエスト処理スレッドに停止命令を出し、停止するまで待機する
		for(int i = 0;i<THREAD_NUMBER;i++){
			if(requestProcessors[i] != null){
				requestProcessors[i].requestStop();
				RequestProcessor.finNotify();   //待ち状態になっているスレッドを解放
				while(threads[i].isAlive() == true){
					try {
						Thread.sleep(50);
						//実際に終了するまで無限ループ
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}
		
//		コネクションがプールされているか、どれかのスレッドがリクエストの対応をしている間は待機する
	    while((RequestProcessor.pool.isEmpty() == false)||(RequestProcessor.nowWaitThreadCount != 0)){		    
			try {
				Thread.sleep(50);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
		}
	    
		try {
			svsock.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}
}
