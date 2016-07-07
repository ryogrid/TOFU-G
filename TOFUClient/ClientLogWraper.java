/* ************************************************************** *
 *                                                                *
 * Copyright (c) 2005, Ryo Kanbayashi, All rights reserved.       *
 *                                                                *
 *                                                                *
 * This software is distributed under the modified BSD License.   *
 * ************************************************************** */

import java.io.Serializable;

//ログを保持する時に、計算データとは関わりのない、クライアントの情報を保持する場合があるかもしれない
//その場合このクラスにそれらのデータを保持する。要はCulculateDataのラッパー
public class ClientLogWraper implements Serializable {
	private CulculateData wrapped = null;       //ラッピングされる過去のjob
	
	//過去ログに保持したいデータがある場合は適宜フィールドに追加すること
	
	public CulculateData getWrapped() {
		return wrapped;
	}
	
	//CulculateDataを与えて新しいLogWraperを生成
	public ClientLogWraper(CulculateData wrapped) {
		this.wrapped = wrapped;
	}
	
	public void setWrapped(CulculateData wrapped) {
		this.wrapped = wrapped;
	}
}
