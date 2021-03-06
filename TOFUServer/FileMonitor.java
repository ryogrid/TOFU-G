/* ************************************************************** *
 *                                                                *
 * Copyright (c) 2005, Ryo Kanbayashi, All rights reserved.       *
 *                                                                *
 *                                                                *
 * This software is distributed under the modified BSD License.   *
 * ************************************************************** */

//ファイル保存の排他処理を行うためのモニター
//保存するクラスの内部のデータが変更されている場合にロックする
public class FileMonitor {
    volatile int nowMovingCount = 0;        //現在、変更を加えているスレッド数
    volatile boolean nowWriting = false;   //現在ファイルの書き込みを行っているか
//    volatile boolean nowWaiting = false;   //現在ファイルの書き込みを待機しているか
    volatile int nowWaitingCount = 0;      //現在ファイルの書き込みの待機しているスレッド数
    volatile Object monitor = new Object();
    
    //自分が動作しているとして、カウンタを増加する。
    //書き込みを行っている場合には、waitする
    //このメソッドを使用した場合、処理終了後にかならずcancelChildMovingをよぶこと
    public void informChildMoving(){
        System.out.println("informChildMovingの中でmonitorのロックを得るためにブロック");
        synchronized(monitor){
            
            //書き込みのチャンスで、書き込み待機されている場合、モニターを譲るために待機
            while((nowMovingCount==0)&&(nowWaitingCount != 0)){
                try {
                    System.out.println("ファイルの書き込みを優先するためにinformChildMoving内で譲りブロックします");
                    monitor.notifyAll();
                    monitor.wait(100);
                } catch (InterruptedException e) {
                    
                }
            }
            
            while(nowWriting == true){  //	ファイルの書き込みを行っている場合、書き込みが終わるまで待機
                try {
                    System.out.println("ファイル書き込み中のためにinformChildMoving内でブロック");
                    monitor.wait();
                } catch (InterruptedException e) {

                }
            }
            nowMovingCount = nowMovingCount+1;
System.out.println("informによって nowMovingCount =" + nowMovingCount);            
        }
    }
    
    
    //追加した自分のカウンタを取り除く
    //informChildMovingを使用した場合、処理終了後にかならずこのメソッドを使用すること
    public void cancelChildMoving(){
        System.out.println("cancelChildMovingの中でmonitorのロックを得るためにブロック");
        synchronized(monitor){
            nowMovingCount = nowMovingCount-1;
System.out.println("cancelによって nowMovingCount=" + nowMovingCount);
            if(nowMovingCount == 0){   //ひとつのスレッドも処理を行ってない場合
                monitor.notifyAll();   //書き込み待ちで待機しているスレッドを解放
            }
            
//            if(nowWriting == true){   //書き込みを行っている最中に処理を終えるスレッドがいるはずないので
//                System.out.println("cancelChiledMovingで同期エラー！！");
//                throw new RuntimeException();
//            }
        }
    }
    
    //処理を行ってよいかチェックする
    //処理終了後にfinishDoing()を必ず使用する事
    public void checkCanDo(){
        System.out.println("checkCanDoの中でmonitorのロックを得るためにブロック");
        synchronized(monitor){
            while((nowMovingCount != 0)||(nowWriting == true) ){
				try {
System.out.println("nowMovingCount=" + nowMovingCount + "なので書き込み待機します");				    
				    System.out.println("checkCanDoによってnowWaitingCount=" + nowWaitingCount);
					nowWaitingCount = nowWaitingCount + 1;
					monitor.wait();
					System.out.println("checkCanDo内のブロックひとまず突破");
				} catch (InterruptedException e) {
					
				}
				System.out.println("checkCanDoによってnowWaitingCount=" + nowWaitingCount);
				nowWaitingCount = nowWaitingCount-1 ; //待機状態を抜けたと表明
            }
            nowWriting = true;  //書き込みを行っていると表明
        }
    }
    
    //排他的におこなわなければならない処理が終了した事を表明する
    //checkCanDo()を用いたら必ずこちらを終了時に使用すること
    public void finishDoing(){
        System.out.println("finishDoing内でmonitorのロックを得るためにブロックしています");
        synchronized(monitor){
            nowWriting = false;   //書き込みは終了したと表明
//            nowMovingCount = 0;    //念のため０にしておく
            monitor.notifyAll();   //書き込み中に待機していスレッド達を解放する
        }
    }
}
