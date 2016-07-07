/* ************************************************************** *
 *                                                                *
 * Copyright (c) 2005, Ryo Kanbayashi, All rights reserved.       *
 *                                                                *
 *                                                                *
 * This software is distributed under the modified BSD License.   *
 * ************************************************************** */

import java.io.*;
import java.util.Calendar;
import java.util.Date;
//�N���C�A���g�Ɋւ���f�[�^�i�T�[�o�֑��M���鎖���ӎ��j
public class ClientInfo implements Serializable{
	private static final long serialVersionUID = 1L;
	String participantNum = null;               //�Q���҂̖��O
	int AllWorkCounts = 0;						//���ׂẴv���W�F�N�g�ł̃��[�N��
	volatile double AllWorkingTime = 0;                   //���ׂẴv���W�F�N�g�ł̌v�Z���ԁi�b�j
	float clientVersion = 0F;           //�N���C�A���g�A�v���P�[�V������ver
	Calendar startUseDate = null;          //�N���C�A���g�A�v���P�[�V�����������p���n�߂��̂�
	String mailAddress = null;           //�Q���҂̃��[���A�h���X
	byte signiture[] = null;             //�N���C�A���g����ӂɎ��ʂ���l�i�n�b�V���H�j
	DataOfEachProject tmpIncluded = null;  //�T�[�o�֑��M���鎞�E�T�[�o���ł������p
}
