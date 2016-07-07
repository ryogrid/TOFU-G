/* ************************************************************** *
 *                                                                *
 * Copyright (c) 2005, Ryo Kanbayashi, All rights reserved.       *
 *                                                                *
 *                                                                *
 * This software is distributed under the modified BSD License.   *
 * ************************************************************** */

import java.security.*;
import java.io.*;
import java.util.*;
import java.nio.ByteBuffer;

//�N���C�A���g�̌��Ńt�@�C���ɕۑ����ׂ����𓝊�����N���X
//readOutAll()���ĂԎ��ɂ́A�ێ����Ă���Ɨ\�z�����N���X�t�@�C���͎��O�Ƀ��[�h���Ă�������
public class CPreserveData implements Serializable {
	private static final long serialVersionUID = 1L;
	final String FILE_NAME = "CPreserveData.dat";
	volatile private transient ArrayList projectThreads = new ArrayList();   //�v���W�F�N�g���Ƃ̃X���b�h�𕡐��ێ�
	volatile private transient ListIterator proThreIterator = projectThreads.listIterator();     //projectThreads��iterator
	private LinkedHashMap sInfos = new LinkedHashMap();
	private transient Iterator sInfosIterator = (sInfos.values()).iterator();     //sInfos��iterator
	//DataOfEachProjcet�𕡐��ێ� key:�T�[�o�̃V�O�j�`���@�l : DataOfEachProject
	public ClientInfo cInfo = new ClientInfo();
	public SecurityPolicies sePolicy = new SecurityPolicies(); //�Z�L�����e�B�[�|���V�[�Ɋւ���N���X
	int ConnectFailCount = 0; //�ڑ������̎��s�����񐔂�ێ�
	boolean endableFlag = false; //�I�����Ă��悢��
	boolean nowConnectingFlag = false; //���ݐڑ���Ԃ��ǂ���
	boolean alreadyInit = false;       //�����ݒ���s���Ă��邩�ǂ���
	
	public final int PRIORITYHIGH = 8;
	public final int PRIORITYMIDDLE = 5;
	public final int PRIORITYLAW = 2;
	int priorityLevel = 2;              //�����D��x��\���l

	public CPreserveData(){
		initProThreIterator();     //Iterator��������
		initSInfosIterator();
	}

	//���̃N���X���ۂ��ƃt�@�C���֏�������
	public synchronized void preserveAll() {
		
		synchronized(ClientMain.cDataToFile){
		    ClientMain.fMonitor.checkCanDo();
		    
			File preserveFile = new File(FILE_NAME);
			try {
				ObjectOutputStream out =
					new ObjectOutputStream(new FileOutputStream(preserveFile));
				out.writeObject(this);
				out.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			ClientMain.fMonitor.finishDoing();
		}
		
	}
	
	//���ݎQ�����Ă���v���W�F�N�g���𓾂�
	public int getProjectCount(){
	    int tmp;
		synchronized(sInfos){
		    ClientMain.fMonitor.informChildMoving();
		    
			tmp =  sInfos.size();
			
			ClientMain.fMonitor.cancelChildMoving();
		}
		return tmp;
	}

	//���̃N���X���ۂ��ƃt�@�C������ǂݍ��݂����Ԃ�(���s�����ꍇnull��Ԃ��j
	//�N���X���[�h�̎��ɗp����N���X���[�_�[�Ƃ��̒T��path��^����
	public synchronized CPreserveData readOutAll(ClassLoader loader,String[] path) {
		File preserveFile = new File(FILE_NAME);
		
			try {
				MyObjectInputStream in =
					new MyObjectInputStream(new FileInputStream(preserveFile),loader,path);
				CPreserveData temp = (CPreserveData) in.readObject();
				in.close();
				return temp;
			} catch (FileNotFoundException e) {
				return null;
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
				return null;
			}
	}

	//�^����������ƁA�V�X�e���^�C���Ńn�b�V���l���Z�o(���s�����ꍇnull��Ԃ�)
	public byte[] generateHash(String str) {
		//		MessageDigest�I�u�W�F�N�g�̏�����
		StringBuffer buffer = new StringBuffer(str);
		Date d = new Date();
		buffer.append(d.getTime()); //�V�X�e���^�C���𕶎���Ƃ��Ēǉ�
		MessageDigest md;
		try {
			md = MessageDigest.getInstance("SHA1");
			md.update(buffer.toString().getBytes()); //�쐬���Ă�����������ŏ�����
			return md.digest(); //�_�C�W�F�X�g�l��Ԃ�
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			return null;
		}
	}

	//sInfos��DataOfEachProject�̗v�f��ǉ�����@key :�T�[�o�̃V�O�j�`���@�l�@:DataOfEachProject
	public void addDatOfEach(DataOfEachProject newPro) {
		synchronized(sInfos){
		    ClientMain.fMonitor.informChildMoving();
		    
			sInfos.put(byteArrayToString(newPro.svrInfo.signiture),newPro);
			
			ClientMain.fMonitor.cancelChildMoving();
		}
	}

	//�V�O�j�`���w��ŗv�f���폜�A�폜�����l��Ԃ�
	public DataOfEachProject delDataOfEach(byte[] signiture) {
	    DataOfEachProject tmp;
		synchronized(sInfos){
		    ClientMain.fMonitor.informChildMoving();
		    
			tmp = (DataOfEachProject) sInfos.remove(byteArrayToString(signiture));
			
			ClientMain.fMonitor.cancelChildMoving();
		}
		return tmp;
	}

	//�V�O�j�`���w��ŗv�f�𓾂�
	public  DataOfEachProject getDataOfEach(byte[] signiture) {
	    DataOfEachProject tmp;
	    synchronized(sInfos){
	        ClientMain.fMonitor.informChildMoving();
	        
	        tmp =(DataOfEachProject) sInfos.get(byteArrayToString(signiture));
	        
	        ClientMain.fMonitor.cancelChildMoving();
	    }
	    return tmp;
	}
	
	//�v���W�F�N�g�ԍ��Ńv���W�F�N�g�̃f�[�^�𓾂�A������Ȃ��ꍇ��null��Ԃ�
	public DataOfEachProject getDataOfEachByProNum(int number){
		
		synchronized(sInfosIterator){
		    ClientMain.fMonitor.informChildMoving();
		    
			initSInfosIterator();
			while(sInfosHasNext() == true){
				DataOfEachProject tmp = sInfosNext();
				if(tmp.projectNumber == number){  //�T���Ă���v���W�F�N�g�ԍ��̃v���W�F�N�g����������
					ClientMain.fMonitor.cancelChildMoving();
				    return tmp;
				}
			}
			
			ClientMain.fMonitor.cancelChildMoving();
			return null;
		}
	}
	
	//�C�e���[�^���������A�ŏ��̈ʒu�֖߂��������ɗ��p�i�Ӌ`�Ɋւ��ėv�����j�A�R���X�g���N�^�ŏ���������Ă��Ȃ����͎g��
	public synchronized void initSInfosIterator(){
		synchronized(sInfos){
		    ClientMain.fMonitor.informChildMoving();
		    
			sInfosIterator = (sInfos.values()).iterator();      //iterator���Z�b�g
			
			ClientMain.fMonitor.cancelChildMoving();
		}
	}
	
	//sInfosIterator��hasNext();�@iterator������������Ă��邩����
	public boolean sInfosHasNext(){
		boolean tmp;
	    synchronized(sInfos){
		    ClientMain.fMonitor.informChildMoving();
		    
			tmp = sInfosIterator.hasNext();
			
			ClientMain.fMonitor.cancelChildMoving();
		}
	    return tmp;
	}
	
	//sInfosIterator��next(); iterator������������Ă��邩����
	public DataOfEachProject sInfosNext(){
	    DataOfEachProject tmp;
		synchronized(sInfos){
		    ClientMain.fMonitor.informChildMoving();
		    
			tmp = (DataOfEachProject) sInfosIterator.next();
			
			ClientMain.fMonitor.cancelChildMoving();
		}
		return tmp;
	}

	//projectThreads��������
	public synchronized void initProthre(){
			projectThreads = new ArrayList();
	}
	
	//�C�e���[�^���������A�ŏ��̈ʒu�֖߂��������ɗ��p�i�Ӌ`�Ɋւ��ėv�����j�A�R���X�g���N�^�ŏ���������Ă��Ȃ����͎g��
	public void initProThreIterator(){
		synchronized(projectThreads){	
	    	proThreIterator = projectThreads.listIterator();      //iterator���Z�b�g
		}	
	}

	//proThreIterator��hasNext();�@iterator������������Ă��邩����
	public boolean proThreHasNext(){
	    boolean tmp;
		synchronized(projectThreads){
		    ClientMain.fMonitor.informChildMoving();
		    
			tmp =proThreIterator.hasNext();
			
			ClientMain.fMonitor.cancelChildMoving();
		}
		return tmp;
	}
	
	//proThreIterator��next(); iterator������������Ă��邩����
	public EachProThread proThreNext(){
	    EachProThread tmp;
		synchronized(projectThreads){
		    ClientMain.fMonitor.informChildMoving();
		    
			tmp = (EachProThread) proThreIterator.next();
			
			ClientMain.fMonitor.cancelChildMoving();
		}
		return tmp;
	}

	//�v���W�F�N�g�����̃X���b�h��ǉ�����
	//*�C���f�b�N�X�́u�v���W�F�N�g�ԍ�-1�v��p���邱��
	public void addProThreByIndex(int index,EachProThread newer){
		synchronized(projectThreads){
		    ClientMain.fMonitor.informChildMoving();
		    
			projectThreads.add(index,newer);
			
			ClientMain.fMonitor.cancelChildMoving();
		}
	}
	
	//�I�u�W�F�N�g�w��Ńv���W�F�N�g�����̃X���b�h����菜��
	public void removeProThre(EachProThread removed){
		synchronized(projectThreads){
		    ClientMain.fMonitor.informChildMoving();
		    
			projectThreads.remove(removed);
			
			ClientMain.fMonitor.cancelChildMoving();
		}
	}
	
	//�C���f�b�N�X�w��Ńv���W�F�N�g�����̃X���b�h����菜��
	public void removeProThre(int number){
		synchronized(projectThreads){
		    ClientMain.fMonitor.informChildMoving();
		    
			projectThreads.remove(number);
			
			ClientMain.fMonitor.cancelChildMoving();
		}
	}
	
	//�C���f�b�N�X�w��Ńv���W�F�N�g�����̃X���b�h�𓾂�
	public EachProThread getProThre(int number){
	    EachProThread tmp;
		synchronized(projectThreads){
		    ClientMain.fMonitor.informChildMoving();
		    
			tmp = (EachProThread) projectThreads.get(number);
			
			ClientMain.fMonitor.cancelChildMoving();
		}
		return tmp;
	}
	
	//�o�C�g�z���String�̕�����֕ϊ�
	private String byteArrayToString(byte[] array){
		StringBuffer tmp = new StringBuffer();
		for(int i = 0; i < array.length;i++){
			tmp.append(array[i]);
		}
		String result = tmp.toString();
		return result;
	}
	
	//�v���W�F�N�g�f�[�^��^���āA�����X���b�h��Ԃ�
	public EachProThread getProThreByPro(DataOfEachProject need){
		synchronized(projectThreads){
		    ClientMain.fMonitor.informChildMoving();
		    
			initProThreIterator();
			while(proThreHasNext()){
				EachProThread thre = proThreNext();
				DataOfEachProject tmp = thre.getHavinProject();
				if(need.equals(tmp)){
				    ClientMain.fMonitor.cancelChildMoving();
					return thre;
				}
			}
			ClientMain.fMonitor.cancelChildMoving();
			return null;         //������Ȃ��ꍇnull��Ԃ�
		}
	}
}
