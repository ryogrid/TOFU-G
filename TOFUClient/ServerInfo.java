/* ***************************************************************
 *                                                                                                         　　　*
 * Copyright (c) 2005, Ryo Kanbayashi, All rights reserved.                                 *
 *                                                                                                                *
 *                                                                                                                *
 * This software is distributed under the modified BSD License.                          *
 * ***************************************************************/

import java.io.*;
import java.net.*;

//サーバそれぞれがクライアントのために保持しているデータ
public class ServerInfo implements Serializable,Cloneable{
	private static final long serialVersionUID = 1L;
	InetAddress address = null;	//サーバのアドレスを保持
	String projectNum = null;      //プロジェクトの名前
	String managerNum = null;      //運営者の名前
	String addresOfHP = null;      //ホームページのアドレス（ある場合）
	int workerCounts = 0;               //このプロジェクトの総参加人数
	int quiteMenberCount =0;            //一度参加したが脱退してしまった人数
	byte signiture[] = null;       //サーバを一意に識別する値（ハッシュ？）
	String nickname = null;         //一定文字以内のプロジェクト名の略称、クラスファイル・ディレクトリ名として利用
	public String eMailAddress;    //運営者のメールアドレス
	
	//シャローコピー
	public Object clone() throws CloneNotSupportedException{
	    return super.clone();
	}
}
