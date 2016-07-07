/* ************************************************************** *
 *                                                                *
 * Copyright (c) 2005, Ryo Kanbayashi, All rights reserved.       *
 *                                                                *
 *                                                                *
 * This software is distributed under the modified BSD License.   *
 * ************************************************************** */

import java.util.HashMap;
import java.io.*;

public class DataContainer implements Serializable{
	private HashMap deliverdObjs = new HashMap();       //追加のオブジェクトを保持
	
//	private CulculateData deriverdCulData = null;
//	private Culculater deriverdCulculater= null;        //ファイルで送信しているため未使用
	
	private ClientInfo deriverdCInfo = null;            //送受信側両方で利用(内部にDataOfEachProjectを格納する事を忘れずに）
	
	private static final long serialVersionUID = 1L;
	
//	public Culculater getDeriverdCulculater() {
//		return deriverdCulculater;
//	}
//
//
//	public CulculateData getDeriverdCulData() {
//		return deriverdCulData;
//	}
//
//
//	public void setDeriverdCulculater(Culculater culculater) {
//		deriverdCulculater = culculater;
//	}
//
//
//	public void setDeriverdCulData(CulculateData data) {
//		deriverdCulData = data;
//	}
	
//	deriverdObjsへオブジェクトを追加
	void addObject(Object obj, String name){
	//未実装
	}
	
//	DeriverdObjsから取り除くとともにそのオブジェクトを返す	
	Object removeObject(String objName){    
		return null;     //未実装
	}

	public ClientInfo getDeriverdCInfo() {
		return deriverdCInfo;
	}

	public void setDeriverdCInfo(ClientInfo info) {
		deriverdCInfo = info;
	}

}
