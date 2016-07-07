/* ************************************************************** *
 *                                                                *
 * Copyright (c) 2005, Ryo Kanbayashi, All rights reserved.       *
 *                                                                *
 *                                                                *
 * This software is distributed under the modified BSD License.   *
 * ************************************************************** */

import java.util.Calendar;
import java.util.Iterator;

//����I�Ƀt�@�C���ւ̕ۑ����s���X���b�h
public class DataPreserver implements Runnable {
	private int checkIntervalMin = 10;        //�`�F�b�N���s���C���^�[�o���B�f�t�H���g�͈ꎞ��
	private Calendar beforeCheckDate = null;       //�O��`�F�b�N���s��������
	private volatile boolean stopRequest = false;      //���̃X���b�h���I�����ׂ�����true��
	
	public DataPreserver(){
		this.beforeCheckDate = Calendar.getInstance(); 
	}
	
	public DataPreserver(int checkInterval){
		this();
		this.checkIntervalMin = checkInterval;
	}
	
	public void run() {
	int debugNum=0;		
		while(stopRequest == false){
			//��r�̎��_����C���^�[�o�������������Ԃ��O����s���̓�������̏ꍇ�B
			//�܂�A�C���^�[�o���ȏ�̎��Ԃ��o�߂��Ă����ꍇ

			if(isProgressed(beforeCheckDate,Calendar.getInstance(),checkIntervalMin) == true){
				System.out.println("dataPreserver��" + debugNum++ + "��ڂ̃R�[��!!");
			    ServerMain.sDataToFile.preserveAll();        //�t�@�C���֕ۑ�
				this.beforeCheckDate = Calendar.getInstance();
			}else{     //��r���ׂ����Ԃ��o�߂��Ă��Ȃ��ꍇsleep
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	//before����after�܂ł�Interval���̎��Ԃ��o�߂����̂���Ԃ��B
	public boolean isProgressed(Calendar before,Calendar after,int intervalMin){
		after.add(Calendar.MINUTE,-1* intervalMin);
		if(before.after(after)==false){
			after.add(Calendar.MINUTE,intervalMin);   //���ɖ߂�
			return true;
		}else{
			after.add(Calendar.MINUTE,intervalMin);   //���ɖ߂�
			return false;
		}
	}
	
	//�������~����悤�w������
	public void requestIntterupt(){
		stopRequest = true;
	}
}
