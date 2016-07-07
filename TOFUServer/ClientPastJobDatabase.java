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

//�ߋ��ɍs����job�̌��ʂ�Logging���A���o�����肷�钊�ۃN���X�B�K�v�ȕ����͓K�X����
public class ClientPastJobDatabase implements Serializable {
//	private ArrayList pastDatas = new ArrayList();    //�ߋ��̃��O��ێ��iCulculateData���܂�LogWrapper��ێ��j
    String nickName = null;

    private PersistentVector pastDatas = null;

    //path��^����
    public ClientPastJobDatabase(String nickname,ClassLoader loader){
        pastDatas = new PersistentVector(100,"./tmp/pastDatas.tmp",loader, "./ProjectFiles/"
    			+ nickname + "/");    //�ߋ��̃��O��ێ��iCulculateData���܂�LogWrapper��ێ��j
    }
    
    public ClientPastJobDatabase(){
        pastDatas = new PersistentVector(100,"./tmp/pastDatas.tmp");
    }
    
    //PersistentVector�̂��߂�loader�����Z�b�g����B�ċN����ȂǂɎg�p    
    public void resetLoader(ClassLoader loader){
        if(pastDatas != null){
            pastDatas.resetLoader(loader);
        }
    }
    
	public synchronized void addOneLogWraper(ClientLogWraper result){
		try {
            pastDatas.add(result);
        } catch (IOException e) {
            e.printStackTrace();
        }
	}
	
	public CulculateData getOneJobLog(int index){
		DataOfEachProject tmp = (DataOfEachProject) pastDatas.get(index);
		return  tmp.culData;
	}
	
	//index�w���LogWraper�����o��
	public ClientLogWraper getOneLogWraper(int index){
		ClientLogWraper tmp = (ClientLogWraper) pastDatas.get(index);
		return  tmp;
	}
	
	//index�w���LogWraper����������
	public void delOneLogWraper(int index){
		pastDatas.remove(index);
	}
	
	//�ۑ�����Ă��郍�O�̐��𓾂�
	public int getLogCount(){
		return pastDatas.size();
	}
	
//	//�v�Z���ʂ�ێ����Ă���ArrayList�̎Q�Ƃ�Ԃ�
//	public Vector getVector(){
//		return pastDatas;
//	}
	
	//CulculateData��LogWraper�Ń��b�s���O����B�����Ɋe��t���f�[�^���t��
	public ClientLogWraper wrapByLogWraper(CulculateData data){
		ClientLogWraper tmp = new ClientLogWraper(data);
		
		//�t���f�[�^��t������ꍇ�̃R�[�h���L�q����
		
		return tmp;
	}
	


	
}
