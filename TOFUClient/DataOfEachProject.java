/* ************************************************************** *
 *                                                                *
 * Copyright (c) 2005, Ryo Kanbayashi, All rights reserved.       *
 *                                                                *
 *                                                                *
 * This software is distributed under the modified BSD License.   *
 * ************************************************************** */

import java.io.*;
import java.util.Calendar;
//�v���W�F�N�g�P�ʂŕ����\�ȕϐ��ɂ��ĕێ�
//�v���W�F�N�g�ɂ����ă��[�h���Ȃ��Ă͂����Ȃ��N���X���J�����gpath�ɂȂ��ꍇ�͍ŏ���setPastJobDB���g������
public class DataOfEachProject implements Serializable {
	private static final long serialVersionUID = 1L;
	
	//�ȉ��A��Ԃ�\�����邽�߂̒萔
	final int NOTSTART = 0;   //�܂������X���b�h���J�n���Ă��Ȃ�
	final int AFTERINIT = 1; //�X���b�h���������ꂽ����(���̂ǂ̏�Ԃł��Ȃ��j
	final int HAVENEWJOB = 2; //�������̂�������ۗL
	final int HAVEPROGRESSJOB = 3;    //��x�͏������s�����r�����ʂ̂�������ۗL
	final int NOWCUL = 4; //���݌v�Z�������s���Ă���
	final int COMPCUL = 5; //�v�Z���I�������i������ۗL�i�ԋp�ҋ@�j
	final int NOWCONNECT = 6; //���݃T�[�o�ɐڑ���
	final int AFTERBACK = 7;     //�W���u��ԋp������̏��
	final int HAVEMIDDLEWAYJOB = 8;        //�r���܂Ōv�Z���s��ꂽjob�������Ă���
	int variousState = 0; //�N���C�A���g�̌��݂̏�Ԃ�\���l�A��L�̊e��萔�𗘗p
	
	CulculateData culData = null;   //���̃v���W�F�N�g�ł̌v�Z�^�X�N;
	ClientPastJobDatabase pastJobDB = null;     //�ߋ��̃��O��ێ�����N���X
	int workCounts = 0;             //���̃v���W�F�N�g�Ɋւ��Ă̌v�Z��
	volatile String messageToServer = null;         //�T�[�o�ւ̃��b�Z�[�W
	volatile String messageFromServer = null;                 //�T�[�o����̃��b�Z�[�W
	boolean culComplete = false;         //���̃v���W�F�N�g�Ɋւ��Čv�Z�͏I�����Ă��邩
	int rankOfWorker = 0;                //�T�[�o�ɐڑ��������_�ł̏���
	double workSeconds = 0;               //���̃v���W�F�N�g�Ɋւ��Ă̑��N������
	Calendar joinDate = null;             //���̃v���W�F�N�g�ɎQ����������
	ServerInfo svrInfo = new ServerInfo();          //�v���W�F�N�g���ꂼ��̃V�O�j�`��
	volatile ClientMemo culMemo = null;                     //�T�[�o�����N���C�A���g�ɕێ������Ă������Ƃ��Ă���f�[�^
	int projectNumber = 0; //���ڂ̃v���W�F�N�g����ێ�
	int indexOfMenu = 0;   //���j���[�o�[�̂ǂ��ɔz�u����Ă��邩��ێ�
	
	final int SHOULDCULCULATE = 1;    //���Ɍv�Z�����Ȃ��Ă͂Ȃ�Ȃ�
	final int SHOULDRECIVEJOB = 2;    //���Ɍv�Z�f�[�^�𓾂Ȃ��Ă͂Ȃ�Ȃ�
	final int SHOULDBACKJOB = 3;      //���Ɍv�Z���ʂ�Ԃ��Ȃ��Ă͂Ȃ�Ȃ�
	int nextShouldDoThing = 0;       //�N���C�A���g�������������Ŏ��ɉ��̏������s���΂悢���A�萔���g���ĕ\��
	
	//�t�@�C���ۑ��p��path��^����ApastJobDB�g�p�O�ɕK���ĂԂ���
	public void setPastJobDB(String nickname,ClassLoader loader){
	    pastJobDB = new ClientPastJobDatabase(nickname,loader);
	}
	
	public DataOfEachProject(){
	    pastJobDB = new ClientPastJobDatabase();
	}
	
	public void resetLoader(ClassLoader loader){
	    if(pastJobDB!=null){
	        pastJobDB.resetLoader(loader);
	    }
	}
}
