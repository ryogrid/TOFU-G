/* ************************************************************** *
 *                                                                *
 * Copyright (c) 2005, Ryo Kanbayashi, All rights reserved.       *
 *                                                                *
 *                                                                *
 * This software is distributed under the modified BSD License.   *
 * ************************************************************** */

import java.util.Date;

// �Đڑ��̂��߂̑҂����ԂȂǂ��Ǘ�����N���X
public class ReConnectWaiter {
	private static final int MUST_WAIT_COUNT = 5;
	private static final int DEFAULT_WAIT_TIME = 100;
	private static int waitTime = 100;   //�~���b
	private static int waitCount = 0;
	private static volatile boolean intteruptWait = false;  //wait

	//�����ŕێ����Ă��鎞�ԕ�Wait����
	public static void waitForDoing() {
		Date d = new Date();
		long waitStart = d.getTime();
		
		//�ҋ@�̒��f���߂�������΁A�w�肳�ꂽ���ԑҋ@���s��
		while((((d = new Date()).getTime()-waitStart)<= waitTime)&&(intteruptWait != true)){
			try {
				Thread.sleep(waitTime);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		increaceWaitTime();          //�����wait�̂��߂ɒl��ύX
		waitCount++;
	}

	//�����ŕێ����Ă���҂����Ԃ����Z�b�g����AisWaitable()���Ă�ł��痘�p���邱��
	public static void restAll() {
		waitTime = DEFAULT_WAIT_TIME;         //�P�b�Ƀ��Z�b�g
		waitCount = 0;
	}
	
	//�҂����Ԃ����̒i�K�ֈڍs����
	public static void increaceWaitTime(){
		waitTime = waitTime * 2; 
	}
	
	//�҂����ł��邩�ǂ�����Ԃ��i�K��񐔑ҋ@���J��Ԃ����̂ł͂Ȃ��ł��낤����)
	public static boolean isWaitable(){
		if(waitCount >= MUST_WAIT_COUNT){
			return false;
		}else{
			return true;
		}
	}
	
	//�ҋ@���甲����悤�w������
	public static void requestIntterupt(){
		intteruptWait = true;
	}
}