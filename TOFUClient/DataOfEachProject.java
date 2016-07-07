/* ************************************************************** *
 *                                                                *
 * Copyright (c) 2005, Ryo Kanbayashi, All rights reserved.       *
 *                                                                *
 *                                                                *
 * This software is distributed under the modified BSD License.   *
 * ************************************************************** */

import java.io.*;
import java.util.Calendar;
//プロジェクト単位で分割可能な変数について保持
//プロジェクトにおいてロードしなくてはいけないクラスがカレントpathにない場合は最初にsetPastJobDBを使うこと
public class DataOfEachProject implements Serializable {
	private static final long serialVersionUID = 1L;
	
	//以下、状態を表現するための定数
	final int NOTSTART = 0;   //まだ処理スレッドも開始していない
	final int AFTERINIT = 1; //スレッドが生成された直後(他のどの状態でもない）
	final int HAVENEWJOB = 2; //未処理のｊｏｂを保有
	final int HAVEPROGRESSJOB = 3;    //一度は処理を行った途中結果のｊｏｂを保有
	final int NOWCUL = 4; //現在計算処理を行っている
	final int COMPCUL = 5; //計算が終了したＪｏｂを保有（返却待機）
	final int NOWCONNECT = 6; //現在サーバに接続中
	final int AFTERBACK = 7;     //ジョブを返却した後の状態
	final int HAVEMIDDLEWAYJOB = 8;        //途中まで計算が行われたjobを持っている
	int variousState = 0; //クライアントの現在の状態を表す値、上記の各種定数を利用
	
	CulculateData culData = null;   //このプロジェクトでの計算タスク;
	ClientPastJobDatabase pastJobDB = null;     //過去のログを保持するクラス
	int workCounts = 0;             //このプロジェクトに関しての計算回数
	volatile String messageToServer = null;         //サーバへのメッセージ
	volatile String messageFromServer = null;                 //サーバからのメッセージ
	boolean culComplete = false;         //このプロジェクトに関して計算は終了しているか
	int rankOfWorker = 0;                //サーバに接続した時点での順位
	double workSeconds = 0;               //このプロジェクトに関しての総起動時間
	Calendar joinDate = null;             //このプロジェクトに参加した日時
	ServerInfo svrInfo = new ServerInfo();          //プロジェクトそれぞれのシグニチャ
	volatile ClientMemo culMemo = null;                     //サーバ側がクライアントに保持させておこうとしているデータ
	int projectNumber = 0; //何個目のプロジェクトかを保持
	int indexOfMenu = 0;   //メニューバーのどこに配置されているかを保持
	
	final int SHOULDCULCULATE = 1;    //次に計算をしなくてはならない
	final int SHOULDRECIVEJOB = 2;    //次に計算データを得なくてはならない
	final int SHOULDBACKJOB = 3;      //次に計算結果を返さなくてはならない
	int nextShouldDoThing = 0;       //クライアントが処理をする上で次に何の処理を行えばよいか、定数を使って表す
	
	//ファイル保存用のpathを与える、pastJobDB使用前に必ず呼ぶこと
	public void setPastJobDB(String nickname,ClassLoader loader){
	    pastJobDB = new ClientPastJobDatabase(nickname,loader);
	}
	
	public DataOfEachProject(){
	    pastJobDB = new ClientPastJobDatabase();
	}
	
	public void resetLoader(ClassLoader loader){
	    if(pastJobDB!=null){
	        pastJobDB.resetLoader(loader);
	    }
	}
}
