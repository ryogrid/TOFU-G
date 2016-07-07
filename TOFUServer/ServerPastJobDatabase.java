/* ************************************************************** *
 *                                                                *
 * Copyright (c) 2005, Ryo Kanbayashi, All rights reserved.       *
 *                                                                *
 *                                                                *
 * This software is distributed under the modified BSD License.   *
 * ************************************************************** */

import java.io.IOException;
import java.io.Serializable;
import java.util.*;

//�ߋ��ɍs����job�̌��ʂ�Logging���A���o�����肷��N���X�B�K�v�ȕ����͓K�X����
public class ServerPastJobDatabase implements Serializable {

/**
 * 
 * @uml.property name="pastDatas"
 * @uml.associationEnd multiplicity="(1 1)"
 */
//	private ArrayList pastDatas = new ArrayList();    //�ߋ��̃��O��ێ��iCulculateData���܂�LogWrapper��ێ��j
private PersistentVector pastDatas = new PersistentVector(
    100,
    "./tmp/pastDatas.tmp"); //�ߋ��̃��O��ێ��iCulculateData���܂�LogWrapper��ێ��j

	
	public void addOneLogWraper(ServerLogWraper result){
		synchronized (pastDatas) {
		    ServerMain.fMonitor.informChildMoving();
		    
		    try {
                pastDatas.add(result);
            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException();
            }finally{
                ServerMain.fMonitor.cancelChildMoving();    
            }
        }
	}
	
	public CulculateData getOneJobLog(int index){
	    ServerLogWraper tmp;
	    synchronized (pastDatas) {
		    ServerMain.fMonitor.informChildMoving();
		    try{
		        tmp = (ServerLogWraper) pastDatas.get(index);    
		    }finally{
		        ServerMain.fMonitor.cancelChildMoving();    
		    }
		}
	    
		return  tmp.getWrapped();
	}
	
	//index�w���LogWraper�����o��
	public ServerLogWraper getOneLogWraper(int index){
	    ServerLogWraper tmp;
	    synchronized (pastDatas) {
	        ServerMain.fMonitor.informChildMoving();
	        try{
	            tmp = (ServerLogWraper) pastDatas.get(index);    
	        }finally{
	            ServerMain.fMonitor.cancelChildMoving();    
	        }
	    }
        return  tmp;
	}
	
	//index�w���LogWraper����������
	public void delOneLogWraper(int index){
	    synchronized (pastDatas) {
	        ServerMain.fMonitor.informChildMoving();
	        try{
	            pastDatas.remove(index);    
	        }finally{
	            ServerMain.fMonitor.cancelChildMoving();    
	        }
	    }
	}
	
	//�ۑ�����Ă��郍�O�̐��𓾂�
	public int getLogCount(){
	    int tmp;
	    synchronized (pastDatas) {
	        ServerMain.fMonitor.informChildMoving();
	        try{
	            tmp = pastDatas.size();    
	        }finally{
	            ServerMain.fMonitor.cancelChildMoving();    
	        }
	    }
	    return tmp;
	}
	
//	//�v�Z���ʂ�ێ����Ă���ArrayList�̎Q�Ƃ�Ԃ�
//	public Vector getVector(){
//	    synchronized (pastDatas) {
//	        return pastDatas;
//	    }
//	}
	
	//CulculateData��LogWraper�Ń��b�s���O����B�����Ɋe��t���f�[�^���t��
	public ServerLogWraper wrapByLogWraper(CulculateData data){
		ServerLogWraper tmp = new ServerLogWraper(data);
		
		//�t���f�[�^��t������ꍇ�̃R�[�h���L�q����
		
		return tmp;
	}
	
	

	
}
