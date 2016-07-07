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

//�^�X�N�̌��ʂ�store���A���o�����肷�钊�ۃN���X�B�K�v�ȕ����͓K�X����
//���ʂƂ͌����Ă�DataOfEachProject�����̂܂܂܂邲�ƕێ����Ă���
public abstract class ResultDatabase implements Serializable {

/**
 * 
 * @uml.property name="resultsDB"
 * @uml.associationEnd multiplicity="(1 1)"
 */
//	private ArrayList resultsDB = new ArrayList();    //�v�Z���ʂ�ۑ�����
private PersistentVector resultsDB = new PersistentVector(
    100,
    "./tmp/resultsDB.tmp"); //�v�Z���ʂ�ۑ�����

	
	public void addOneResult(DataOfEachProject result){
		synchronized(resultsDB){
		    ServerMain.fMonitor.informChildMoving();
		    
	    	try {
                resultsDB.add(result);
            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException();
            }finally{
                ServerMain.fMonitor.cancelChildMoving();    
            }
		}
	}
	
	public CulculateData getOneResult(int index){
		DataOfEachProject tmp;
	    synchronized(resultsDB){
	        ServerMain.fMonitor.informChildMoving();
	        try{
	            tmp = (DataOfEachProject) resultsDB.get(index);    
	        }finally{
	            ServerMain.fMonitor.cancelChildMoving();    
	        }
		}
		return  tmp.culData;
	}
	
	//index�w���DataOfEachProject�����o��
	public DataOfEachProject getOneDataOfEach(int index){
		DataOfEachProject tmp;
		synchronized (resultsDB) {
		    ServerMain.fMonitor.informChildMoving();
		    try{
		        tmp = (DataOfEachProject) resultsDB.get(index);    
		    }finally{
		        ServerMain.fMonitor.cancelChildMoving();    
		    }
        }
		return  tmp;
	}
	
	//�ۑ�����Ă���v�Z���ʂ̐��𓾂�
	public int getResultCount(){
	    int tmp;
	    synchronized(resultsDB){
	        ServerMain.fMonitor.informChildMoving();
	        try{
	            tmp = resultsDB.size();    
	        }finally{
	            ServerMain.fMonitor.cancelChildMoving();    
	        }
	    }
	    return tmp;
	}
	
//	//�v�Z���ʂ�ێ����Ă���ArrayList�̎Q�Ƃ�Ԃ�
//	public Vector getVector(){
//		synchronized(resultsDB){
//		    return resultsDB;
//		}
//	}
	
	//����̏����̗v�f�����o�����\�b�h���K�v�ȏꍇ�́A
	//���̃N���X���p�����ēK�X�������邱��
	
}
