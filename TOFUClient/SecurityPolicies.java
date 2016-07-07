/* ************************************************************** *
 *                                                                *
 * Copyright (c) 2005, Ryo Kanbayashi, All rights reserved.       *
 *                                                                *
 *                                                                *
 * This software is distributed under the modified BSD License.   *
 * ************************************************************** */

import java.io.*;
public class SecurityPolicies implements Serializable{
	private static final long serialVersionUID = 1L;
	boolean autConnect = false;    //自動接続を行ってもよいか
	boolean autCulculate = false;   //自動で計算を行ってもよいか
	File preservePath = null;        //ファイルを保存するパスを保持
}
