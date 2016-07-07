/* ***************************************************************
 *                                                                                                         �@�@�@*
 * Copyright (c) 2005, Ryo Kanbayashi, All rights reserved.                                 *
 *                                                                                                                *
 *                                                                                                                *
 * This software is distributed under the modified BSD License.                          *
 * ***************************************************************/

import java.io.*;
import java.net.*;

//�T�[�o���ꂼ�ꂪ�N���C�A���g�̂��߂ɕێ����Ă���f�[�^
public class ServerInfo implements Serializable,Cloneable{
	private static final long serialVersionUID = 1L;
	InetAddress address = null;	//�T�[�o�̃A�h���X��ێ�
	String projectNum = null;      //�v���W�F�N�g�̖��O
	String managerNum = null;      //�^�c�҂̖��O
	String addresOfHP = null;      //�z�[���y�[�W�̃A�h���X�i����ꍇ�j
	int workerCounts = 0;               //���̃v���W�F�N�g�̑��Q���l��
	int quiteMenberCount =0;            //��x�Q���������E�ނ��Ă��܂����l��
	byte signiture[] = null;       //�T�[�o����ӂɎ��ʂ���l�i�n�b�V���H�j
	String nickname = null;         //��蕶���ȓ��̃v���W�F�N�g���̗��́A�N���X�t�@�C���E�f�B���N�g�����Ƃ��ė��p
	public String eMailAddress;    //�^�c�҂̃��[���A�h���X
	
	//�V�����[�R�s�[
	public Object clone() throws CloneNotSupportedException{
	    return super.clone();
	}
}
