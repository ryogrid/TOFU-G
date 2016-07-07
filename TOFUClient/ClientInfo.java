/* ************************************************************** *
 *                                                                *
 * Copyright (c) 2005, Ryo Kanbayashi, All rights reserved.       *
 *                                                                *
 *                                                                *
 * This software is distributed under the modified BSD License.   *
 * ************************************************************** */

import java.io.*;
import java.util.Calendar;
import java.util.Date;
//クライアントに関するデータ（サーバへ送信する事を意識）
public class ClientInfo implements Serializable{
	private static final long serialVersionUID = 1L;
	String participantNum = null;               //参加者の名前
	int AllWorkCounts = 0;						//すべてのプロジェクトでのワーク数
	volatile double AllWorkingTime = 0;                   //すべてのプロジェクトでの計算時間（秒）
	float clientVersion = 0F;           //クライアントアプリケーションのver
	Calendar startUseDate = null;          //クライアントアプリケーションをいつ利用し始めたのか
	String mailAddress = null;           //参加者のメールアドレス
	byte signiture[] = null;             //クライアントを一意に識別する値（ハッシュ？）
	DataOfEachProject tmpIncluded = null;  //サーバへ送信する時・サーバ内でだけ利用
}
