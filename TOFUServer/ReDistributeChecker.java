/* ************************************************************** *
 *                                                                *
 * Copyright (c) 2005, Ryo Kanbayashi, All rights reserved.       *
 *                                                                *
 *                                                                *
 * This software is distributed under the modified BSD License.   *
 * ************************************************************** */

import java.util.Calendar;
import java.util.Iterator;

//�W���u�̔z�z����Ă���̎��Ԃ��`�F�b�N���A�Ǘ�����X���b�h
public class ReDistributeChecker implements Runnable {
	private int waitDays = 3;    //�Ĕz�z�̔��f����܂ł̓����B�f�t�H���g�͂R���B
	private int checkIntervalMin = 60;        //�`�F�b�N���s���C���^�[�o���B�f�t�H���g�͈ꎞ��
	private Calendar beforeCheckDate = null;       //�O��`�F�b�N���s��������
	private volatile boolean stopRequest = false;      //���̃X���b�h���I�����ׂ�����true��
	
	public ReDistributeChecker(){
		this.beforeCheckDate = Calendar.getInstance(); 
	}
	
	public ReDistributeChecker(int waitDays,int checkInterval){
		this();
		this.waitDays = waitDays;
		this.checkIntervalMin = checkInterval;
	}
	
	public void run() {
		while(stopRequest == false){
			//��r�̎��_����C���^�[�o�������������Ԃ��O��̃`�F�b�N���̓�������̏ꍇ�B
			//�܂�A�C���^�[�o���ȏ�̎��Ԃ��o�߂��Ă����ꍇ
//			if(isProgressed(beforeCheckDate,Calendar.getInstance(),checkIntervalMin) == true){
			if(isProgressed(beforeCheckDate,Calendar.getInstance(),1) == true){
			    synchronized(ServerMain.sDataToFile.jManager.distributeJobQueue){
					synchronized(ServerMain.sDataToFile.jManager.waitingBackJobs){
						Iterator queueIterator =ServerMain.sDataToFile.jManager.getWaitingQIterator();
						Calendar checkUse = Calendar.getInstance();
					
						while(queueIterator.hasNext()){
							ServerLogWraper tmp = (ServerLogWraper) queueIterator.next();
							Calendar distributedDate = (tmp.getWrapped()).getDistributeDate();
							
							//�Ĕz�z���ׂ����Ԃ��o�߂��Ă��܂��Ă���ꍇ
					//		if(isProgressed(distributedDate,checkUse,waitDays*1440)==true){
							if(isProgressed(distributedDate,checkUse,2)==true){
								int addJobCount = tmp.getRedundancy() - tmp.getProgress();
								CulculateData addJob = tmp.getWrapped();
								for(int i=0;i<addJobCount;i++){   //�ǉ��z�z�̗ʕ������z�z�҂��L���[�ɒǉ�
									ServerMain.sDataToFile.jManager.addReJobToQueue(addJob);
								}
							}else{    //�܂��o�߂��Ă��Ȃ����
								//�Ƃ肠�����������Ȃ�
							}
							
						}
					}
			    }
				this.beforeCheckDate = Calendar.getInstance(); 
			}else{     //��r���ׂ����Ԃ��o�߂��Ă��Ȃ��ꍇsleep
				try {
					Thread.sleep(100);
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
