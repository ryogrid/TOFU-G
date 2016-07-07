/* ************************************************************** *
 *                                                                *
 * Copyright (c) 2005, Ryo Kanbayashi, All rights reserved.       *
 *                                                                *
 *                                                                *
 * This software is distributed under the modified BSD License.   *
 * ************************************************************** */

import java.io.IOException;
import java.net.*;
import java.net.ServerSocket;
import java.net.Socket;

public class RequestManager implements Runnable {
private final int TIME_OUT_SECONDS = 120;   //�N���C�A���g�ւ̃f�[�^���M�ł̃^�C���A�E�g�l

/**
 * 
 * @uml.property name="ancestor"
 * @uml.associationEnd inverse="requestManager:ServerMain" multiplicity="(1 1)"
 */
private ServerMain ancestor = null;

private ServerSocket svsock = null;
private Thread threads[];                //RequestProcessor������X���b�h��ێ�

/**
 * 
 * @uml.property name="requestProcessors"
 * @uml.associationEnd multiplicity="(0 -1)"
 */
private RequestProcessor requestProcessors[]; //RequestProcessor��ێ�����

final int THREAD_NUMBER = 10;          //�X���b�h�v�[���ɗp�ӂ���X���b�h��
private volatile boolean stopRequest = false;      //���̃X���b�h���I�����ׂ�����true��

	public RequestManager(ServerSocket socket,ServerMain parent){
		svsock = socket;
		ancestor = parent;
	}
	public void run() {
		
		requestProcessors = new RequestProcessor[THREAD_NUMBER];
		threads = new Thread[THREAD_NUMBER];
		for(int i = 0; i < THREAD_NUMBER; i++){     //�N���C�A���g�̗v������������X���b�h�𕡐�����
			requestProcessors[i] = new RequestProcessor(ancestor,i);
			threads[i] = new Thread(requestProcessors[i]);
			threads[i].start();
		}
		
		try {
			svsock = ServerMain.makeSSLServerSocket(2525);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		try{
			while(stopRequest == false){    //accept�����R�l�N�V�������X���b�h�v�[����
				try{
					Socket request = svsock.accept();
					request.setSoTimeout(TIME_OUT_SECONDS * 120);//�^�C���A�E�g��ݒ�
					RequestProcessor.processRequest(request);
				}catch(IOException e){
					e.printStackTrace();
				}
			}
		}finally{
System.out.println("�R�l�N�V�����̐U�蕪���̂Ƃ���̃��[�v�E�o");		    
		}
	}
	
	//���̃X���b�h�̏I����v������
	public void requestStop(){
		stopRequest = true;
		
		//�e���N�G�X�g�����X���b�h�ɒ�~���߂��o���A��~����܂őҋ@����
		for(int i = 0;i<THREAD_NUMBER;i++){
			if(requestProcessors[i] != null){
				requestProcessors[i].requestStop();
				RequestProcessor.finNotify();   //�҂���ԂɂȂ��Ă���X���b�h�����
				while(threads[i].isAlive() == true){
					try {
						Thread.sleep(50);
						//���ۂɏI������܂Ŗ������[�v
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}
		
//		�R�l�N�V�������v�[������Ă��邩�A�ǂꂩ�̃X���b�h�����N�G�X�g�̑Ή������Ă���Ԃ͑ҋ@����
	    while((RequestProcessor.pool.isEmpty() == false)||(RequestProcessor.nowWaitThreadCount != 0)){		    
			try {
				Thread.sleep(50);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
		}
	    
		try {
			svsock.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}
}
