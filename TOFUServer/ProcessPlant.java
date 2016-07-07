/* ************************************************************** *
 *                                                                *
 * Copyright (c) 2005, Ryo Kanbayashi, All rights reserved.       *
 *                                                                *
 *                                                                *
 * This software is distributed under the modified BSD License.   *
 * ************************************************************** */

import java.io.Serializable;

//返されたジョブに対して（よって）何らかの処理を行うクラスのインターフェース
public abstract class ProcessPlant implements Serializable {

/**
 * 
 * @uml.property name="resultDB"
 * @uml.associationEnd multiplicity="(1 1)"
 */
protected ResultDatabase resultDB = null;

/**
 * 
 * @uml.property name="ancestor"
 * @uml.associationEnd multiplicity="(1 1)"
 */
transient volatile protected ServerMain ancestor = null;

    
	
	public  ProcessPlant(ResultDatabase giveDB,ServerMain parent){
		resultDB = giveDB;
		ancestor = parent;
	}
	
	//独自のコンソールへprintln
	public void printlnToConsole(String str){
		String tmp = str;
		tmp.concat("\0");      //終端文字を加える
		ancestor.sGUI.ta.setText(tmp);
	}
	
	//独自のコンソールへprint
	public void printToConsole(String str){
		ancestor.sGUI.ta.setText(str);
	}
	
	//呼び出し元をセット（コンストラクタで指定されたものがnullもしくは正しくない場合)
	public void setAncester(ServerMain ancestor){
		this.ancestor = ancestor;
	}
	
	//何らかの処理を行う(独自の処理を実装せよ）
	public abstract void doSomeThing(CulculateData food);
	
	
}
