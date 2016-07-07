/* ************************************************************** *
 *                                                                *
 * Copyright (c) 2005, Ryo Kanbayashi, All rights reserved.       *
 *                                                                *
 *                                                                *
 * This software is distributed under the modified BSD License.   *
 * ************************************************************** */

import java.io.*;
import java.util.*;

//�N���C�A���g�̃f�[�^��S�ĕێ����A���삷��N���X
public class ClientDatabase implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 
     * @uml.property name="ancestor"
     * @uml.associationEnd multiplicity="(0 1)"
     */
    private ServerMain ancestor = null;

//    private volatile LinkedHashMap clientDatas = new LinkedHashMap(); //ClientInfo�𕡐��ێ����邽��
private volatile PersistentLinkedHashMap clientDatas = new PersistentLinkedHashMap(
    100,
    "./tmp/clientDatas.tmp"); //ClientInfo�𕡐��ێ����邽��

    /**
     * 
     * @uml.property name="clientDatasIterator"
     * @uml.associationEnd elementType="ClientInfo" multiplicity="(0 -1)"
     */
    private transient Iterator clientDatasIterator = null;


    public ClientDatabase() {
        clientDatasIterator = clientDatas.iterator(); //iterator���Z�b�g���Ă���
    }

    //�N���C�A���g�̃f�[�^�̃��b�p�[����l������
    public void addClient(ClientInfo newClient) {
        synchronized(clientDatas){
            ServerMain.fMonitor.informChildMoving(); //�������݋֎~
        
        	try {
                clientDatas.put(byteArrayToString(newClient.signiture), newClient); //�V�O�j�`�����L�[�Ƃ��ăf�[�^��������
            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException();
            }finally{
                ServerMain.fMonitor.cancelChildMoving(); //�������݋���    
            }
        }
    }

    //�N���C�A���g�̃f�[�^���X�V����(���͑����̂Ɠ�������)
    public void renewClient(ClientInfo reClient) {
        synchronized (clientDatas) {
            ServerMain.fMonitor.informChildMoving(); //�������݋֎~
        
            try {
                clientDatas.put(byteArrayToString(reClient.signiture), reClient); //�V�O�j�`�����L�[�Ƃ��ăf�[�^���X�V�i�㏑�����ʂŁj
            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException();
            }finally{
                ServerMain.fMonitor.cancelChildMoving(); //�������݋���    
            }
        }
    }

    //��l���̃N���C�A���g�f�[�^���V�O�j�`���w��œ���
    public ClientInfo getOneInfo(byte[] signiture) {
        ClientInfo tmp;
        synchronized(clientDatas){
            ServerMain.fMonitor.informChildMoving(); //�������݋֎~
            try{
                tmp =  (ClientInfo) clientDatas.get(byteArrayToString(signiture));
            }finally{
                ServerMain.fMonitor.cancelChildMoving(); //�������݋���    
            }
        }
        return tmp;
    }

    //��l���̃N���C�A���g�f�[�^���N���C�A���g�̃V�O�j�`���w��Ŏ�菜���B��菜�����f�[�^��Ԃ��B
    public ClientInfo delOneClient(byte[] signiture) {
        ClientInfo tmp;
        synchronized(clientDatas){
            ServerMain.fMonitor.informChildMoving(); //�������݋֎~
            try{
                tmp =  (ClientInfo) clientDatas.remove(byteArrayToString(signiture));    
            }finally{
                ServerMain.fMonitor.cancelChildMoving(); //�������݋���    
            }
        }
        return tmp;
        
    }

    //�f�[�^�x�[�X�����Z�b�g����
    public void resetDatabese() {
        synchronized(clientDatas){
            ServerMain.fMonitor.informChildMoving(); //�������݋֎~
            try{
                clientDatas = new PersistentLinkedHashMap(1000,"./tmp/clientDatas.tmp");
            }finally{
                ServerMain.fMonitor.cancelChildMoving(); //�������݋���    
            }
        }
    }

/**
 * 
 * @uml.property name="clientDatas"
 */
//    //�f�[�^�̏W���ł���LinkedHashMap���̂��̂�Ԃ�
//    public HashMap getClientDatas() {
//        synchronized(clientDatas){
//            return clientDatas;
//        }
//    }
//�f�[�^�̏W���ł���ArrayList���̂��̂��Z�b�g
public void setClientDatas(PersistentLinkedHashMap list) {
    synchronized (clientDatas) {
        ServerMain.fMonitor.informChildMoving(); //�������݋֎~
        try {
            clientDatas = list;
        } finally {
            ServerMain.fMonitor.cancelChildMoving(); //�������݋���    
        }
    }
}

    //�C�e���[�^���������A�ŏ��̈ʒu�֖߂��������ɗ��p�i�Ӌ`�Ɋւ��ėv�����j�A�R���X�g���N�^�ŏ������͂���Ă���
    public synchronized void initCDatasIterator() {
        clientDatasIterator = clientDatas.iterator(); //iterator���Z�b�g
    }

    //clientDatasIterator��hasNext(); iterator������������Ă��邩����
    public boolean clientDatasHasNext() {
        boolean tmp;
        synchronized(clientDatas){
            ServerMain.fMonitor.informChildMoving(); //�������݋֎~
            try{
                tmp = clientDatasIterator.hasNext();    
            }finally{
                ServerMain.fMonitor.cancelChildMoving(); //�������݋���    
            }
        }
        return tmp;
    }

    //clinetDatasIterator��next(); iterator������������Ă��邩����
    public ClientInfo clientDatasNext() {
        ClientInfo tmp = null;
        synchronized(clientDatas){
            ServerMain.fMonitor.informChildMoving(); //�������݋֎~
            
            try{
            	tmp = (ClientInfo) clientDatasIterator.next();
            }catch(Exception e){
                e.printStackTrace();
                System.out.println("clientDatasNext����Null��Ԃ��܂�");
                tmp = null;
            }finally{
                ServerMain.fMonitor.cancelChildMoving(); //�������݋���    
            }
        }
        return tmp;
    }

    //�N���C�A���g�̏��ʂ𓾁A���̏��ʂ��c�a�̒��̃N���A���g�f�[�^�ɏ�������
    //���̃N���C�A���g�̏��ʂ��ύX���ăe�[�u���ɔ��f����
    //���c�a�̒��Ώۂ̃N���C�A���g�f�[�^���X�V���Ă���ĂԂ���
    public int getGradeOfClient(ServerMain ancestor, byte signiture[]) {
        ClientInfo target = getOneInfo(signiture);
        int oldGrade = target.tmpIncluded.rankOfWorker; //�ύX�O�̏���
        int newGrade = 1; //�V�����ݒ肷�鏇�ʗp�ϐ�
        initCDatasIterator();
        synchronized(clientDatas){
	        while (clientDatasHasNext()) {
	            ClientInfo tmp = clientDatasNext();
	            //�Ώۂ̃N���C�A���g����̎҂������珇�ʂ����Z
	            if ((tmp.AllWorkCounts > target.AllWorkCounts)) {
	                newGrade++;
	                //��r�Ώۂ��Ώۂ̃N���C�A���g�ł͂Ȃ��A���ʂ̋t�]���N����ꍇ
	            } else if ((tmp.AllWorkCounts < target.AllWorkCounts)
	                    && (tmp.tmpIncluded.rankOfWorker <= oldGrade)
	                    && (byteArrayToString(target.signiture).equals(
	                            byteArrayToString(tmp.signiture)) == false)) {
	                tmp.tmpIncluded.rankOfWorker++;
	                ancestor.sGUI.clientTb.writeStateToTable(tmp); //�e�[�u���ɔ��f
	            }
	        }
        }
        target.tmpIncluded.rankOfWorker = newGrade; //DB�̒��̒l���X�V
        return newGrade;
    }

    //�o�C�g�z���String�̕�����֕ϊ�
    private String byteArrayToString(byte[] array) {
        StringBuffer tmp = new StringBuffer();
        for (int i = 0; i < array.length; i++) {
            tmp.append(array[i]);
        }
        String result = tmp.toString();
        return result;
    }

}