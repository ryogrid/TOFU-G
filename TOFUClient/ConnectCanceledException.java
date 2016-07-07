/* ************************************************************** *
 *                                                                *
 * Copyright (c) 2005, Ryo Kanbayashi, All rights reserved.       *
 *                                                                *
 *                                                                *
 * This software is distributed under the modified BSD License.   *
 * ************************************************************** */

public class ConnectCanceledException extends Exception {
	public ConnectCanceledException(){
		super("ユーザーによって接続処理がキャンセルされました");
	}
}
