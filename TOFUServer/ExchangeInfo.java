/* ************************************************************** *
 *                                                                *
 * Copyright (c) 2005, Ryo Kanbayashi, All rights reserved.       *
 *                                                                *
 *                                                                *
 * This software is distributed under the modified BSD License.   *
 * ************************************************************** */

import java.io.Serializable;
public class ExchangeInfo implements Serializable {        //�T�[�o�N���C�A���g�ԂŌ��������f�[�^�̃N���X
	boolean backResult;       //���ʂ�Ԃ����߂̐ڑ����ǂ���
	boolean receiveCulData;     //�v�Z�f�[�^�����炤���߂Ȃ̂��ǂ���
	boolean initializeClient;   //�N���C�A���g�̏����n��p�ӂ��邽�߂̐ڑ����ǂ���
	boolean quiteProject;       //�N���C�A���g���v���W�F�N�g����E�ނ��邽�߂̐ڑ����ǂ���
	private static final long serialVersionUID = 1L;
}
