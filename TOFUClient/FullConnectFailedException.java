/* ************************************************************** *
 *                                                                *
 * Copyright (c) 2005, Ryo Kanbayashi, All rights reserved.       *
 *                                                                *
 *                                                                *
 * This software is distributed under the modified BSD License.   *
 * ************************************************************** */

//規定回数接続を失敗した時にthrowされる例外クラス
public class FullConnectFailedException extends Exception {
	public FullConnectFailedException(){
		super("規定回数サーバへの接続を失敗しました");
	}
}
