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
	private HashMap deliverdObjs = new HashMap();       //�ǉ��̃I�u�W�F�N�g��ێ�
	
//	private CulculateData deriverdCulData = null;
//	private Culculater deriverdCulculater= null;        //�t�@�C���ő��M���Ă��邽�ߖ��g�p
	
	private ClientInfo deriverdCInfo = null;            //����M�������ŗ��p(������DataOfEachProject���i�[���鎖��Y�ꂸ�Ɂj
	
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
	
//	deriverdObjs�փI�u�W�F�N�g��ǉ�
	void addObject(Object obj, String name){
	//������
	}
	
//	DeriverdObjs�����菜���ƂƂ��ɂ��̃I�u�W�F�N�g��Ԃ�	
	Object removeObject(String objName){    
		return null;     //������
	}

	public ClientInfo getDeriverdCInfo() {
		return deriverdCInfo;
	}

	public void setDeriverdCInfo(ClientInfo info) {
		deriverdCInfo = info;
	}

}
