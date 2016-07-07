/* ************************************************************** *
 *                                                                *
 * Copyright (c) 2005, Ryo Kanbayashi, All rights reserved.       *
 *                                                                *
 *                                                                *
 * This software is distributed under the modified BSD License.   *
 * ************************************************************** */

import java.io.Serializable;

//計算を行うためのデータを生成するクラス、ユーザーが実装
public abstract class DataGenerater implements Serializable {

	//	新しいCulculateDataを生成して返す
	public CulculateData generateData(){return null;};
	
}
