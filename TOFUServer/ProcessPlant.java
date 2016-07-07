/* ************************************************************** *
 *                                                                *
 * Copyright (c) 2005, Ryo Kanbayashi, All rights reserved.       *
 *                                                                *
 *                                                                *
 * This software is distributed under the modified BSD License.   *
 * ************************************************************** */

import java.io.Serializable;

//�Ԃ��ꂽ�W���u�ɑ΂��āi����āj���炩�̏������s���N���X�̃C���^�[�t�F�[�X
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
	
	//�Ǝ��̃R���\�[����println
	public void printlnToConsole(String str){
		String tmp = str;
		tmp.concat("\0");      //�I�[������������
		ancestor.sGUI.ta.setText(tmp);
	}
	
	//�Ǝ��̃R���\�[����print
	public void printToConsole(String str){
		ancestor.sGUI.ta.setText(str);
	}
	
	//�Ăяo�������Z�b�g�i�R���X�g���N�^�Ŏw�肳�ꂽ���̂�null�������͐������Ȃ��ꍇ)
	public void setAncester(ServerMain ancestor){
		this.ancestor = ancestor;
	}
	
	//���炩�̏������s��(�Ǝ��̏�������������j
	public abstract void doSomeThing(CulculateData food);
	
	
}
