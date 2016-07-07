/* ************************************************************** *
 *                                                                *
 * Copyright (c) 2005, Ryo Kanbayashi, All rights reserved.       *
 *                                                                *
 *                                                                *
 * This software is distributed under the modified BSD License.   *
 * ************************************************************** */

import java.io.*;
import java.security.*;
import java.util.*;

public class SPreserveData implements Serializable{
	private static final long serialVersionUID = 1L;
	final String FILE_NAME = "SPreserveData.dat";
	String messageToClients = null;                 //�N���C�A���g�֑��M���邽�߂̃��b�Z�[�W

    /**
     * 
     * @uml.property name="svrInfo"
     * @uml.associationEnd multiplicity="(1 1)"
     */
    ServerInfo svrInfo = new ServerInfo(); //�v���W�F�N�g���ꂼ��̃V�O�j�`��(�N���C�A���g�֑��M���鎖���ӎ��j

    /**
     * 
     * @uml.property name="clientDB"
     * @uml.associationEnd multiplicity="(1 1)"
     */
    volatile ClientDatabase clientDB = new ClientDatabase(); //�N���C�A���g�̃f�[�^�𓝊��A���삷��N���X

    /**
     * 
     * @uml.property name="resultDB"
     * @uml.associationEnd multiplicity="(1 1)"
     */
    volatile ResultDatabaseImpl resultDB = new ResultDatabaseImpl(); //���[�U�[�֗^���邽�߂Ɍv�Z���ʂ�ς�ł���N���X

    /**
     * 
     * @uml.property name="plocPlant"
     * @uml.associationEnd multiplicity="(1 1)"
     */
    volatile ProcessPlantImpl plocPlant = new ProcessPlantImpl(resultDB, null); //��M�����f�[�^�ɂȂ�炩�̏������s���N���X

    /**
     * 
     * @uml.property name="generater"
     * @uml.associationEnd multiplicity="(0 1)"
     */
    volatile DataGeneraterImpl generater = null; //�v�Z�f�[�^�𐶐����郆�[�U�[��`�N���X

    /**
     * 
     * @uml.property name="jManager"
     * @uml.associationEnd multiplicity="(0 1)"
     */
    volatile JobManager jManager = null; //�v�Z�f�[�^�̍Ĕz�z��A�������Ǘ�����N���X

	boolean alreadyInit = false;                     //�����ݒ肪�ς�ł��邩
	
	//�ȉ��ɃT�[�o���N�����ɗ��p����personal�ȃf�[�^��ێ�
	int reDistributeDays = 1;        //�Ĕz�z�����肷��܂ł̓���
	int reDistributecheckMin = 60;        //�Ĕz�z�`�F�b�N�X���b�h�̃`�F�b�N�̊Ԋu
	
//	���̃N���X���ۂ��ƃt�@�C���֏�������
	  public void preserveAll(){

System.out.println("fMonitor�̓����ɓ���O����");	      
	  	  ServerMain.fMonitor.checkCanDo();    //�������݂��s���Ă悢���`�F�b�N
System.out.println("fMonitor�̓������o�āAsynchronized(ServerMain.sDataToFile)�ɓ���Ƃ�����");
//	  	  synchronized(ServerMain.sDataToFile){
//	  	    ServerMain.fMonitor.checkCanDo();    //�������݂��s���Ă悢���`�F�b�N
	  	      
			  File preserveFile = new File(FILE_NAME);
			  try {
//				  ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(preserveFile));
			      DebugObjectOutputStream out = new DebugObjectOutputStream(new FileOutputStream(preserveFile));
				  System.out.println("preserveAll:�f�[�^�������݊J�n!!");
				  out.writeObject(this);
				  System.out.println("preserveAll:�f�[�^�������ݏI��!!");
				  out.close();
			  } catch (FileNotFoundException e) {
				  e.printStackTrace();
			  } catch (IOException e) {
				  e.printStackTrace();
			  }
			  
//			  ServerMain.fMonitor.finishDoing();
//	  	}
System.out.println("PreserveAll�̒��ɂ�finishDoing�̑O�őҋ@���Ă܂�");			  
	    ServerMain.fMonitor.finishDoing();
System.out.println("fMonitor�̓����E�o���܂���");	
	  }
	  
	//���̃N���X���ۂ��ƃt�@�C������ǂݍ��݂����Ԃ�
	public SPreserveData readOutAll(){
		File preserveFile = new File(FILE_NAME);

		try {
			ObjectInputStream in = new ObjectInputStream(new FileInputStream(preserveFile));
			SPreserveData temp = (SPreserveData) in.readObject();
			in.close();
			return temp;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
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
		buffer.append(d.getTime());       //�V�X�e���^�C���𕶎���Ƃ��Ēǉ�
		MessageDigest md;
		try {
			md = MessageDigest.getInstance("SHA1");
			md.update(buffer.toString().getBytes());	//�쐬���Ă�����������ŏ�����
			return md.digest();			//�_�C�W�F�X�g�l��Ԃ�
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			return null;
		}
	}
}
