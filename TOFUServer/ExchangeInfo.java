/* ************************************************************** *
 *                                                                *
 * Copyright (c) 2005, Ryo Kanbayashi, All rights reserved.       *
 *                                                                *
 *                                                                *
 * This software is distributed under the modified BSD License.   *
 * ************************************************************** */

import java.io.Serializable;
public class ExchangeInfo implements Serializable {        //サーバクライアント間で交換されるデータのクラス
	boolean backResult;       //結果を返すための接続かどうか
	boolean receiveCulData;     //計算データをもらうためなのかどうか
	boolean initializeClient;   //クライアントの処理系を用意するための接続かどうか
	boolean quiteProject;       //クライアントがプロジェクトから脱退するための接続かどうか
	private static final long serialVersionUID = 1L;
}
