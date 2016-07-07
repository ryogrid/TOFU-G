/* ************************************************************** *
 *                                                                *
 * Copyright (c) 2005, Ryo Kanbayashi, All rights reserved.       *
 *                                                                *
 *                                                                *
 * This software is distributed under the modified BSD License.   *
 * ************************************************************** */

import java.io.*;
import java.net.*;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.swing.JOptionPane;

public class ServerMain {
	static public SPreserveData sDataToFile = new SPreserveData(); //�t�@�C���ɕۑ����ׂ��f�[�^��ێ�
	public ServerSocket svsock = null;
	public Socket sock = null;

    /**
     * 
     * @uml.property name="sGUI"
     * @uml.associationEnd inverse="ancestor:ServerGui" multiplicity="(1 1)"
     */
    public ServerGui sGUI = null;

	
	public Thread requestManagerThread = null; //RequestManager��ێ����邽�߂̃X���b�h

    /**
     * 
     * @uml.property name="requestManager"
     * @uml.associationEnd inverse="ancestor:RequestManager" multiplicity="(0 1)"
     */
    public RequestManager requestManager = null;

	
	public Thread reDisCheckerThread = null;   //ReDistributeChecker��ێ����邽�߂̃X���b�h

    /**
     * 
     * @uml.property name="reDisChecker"
     * @uml.associationEnd multiplicity="(0 1)"
     */
    public ReDistributeChecker reDisChecker = null; //�Ĕz�z�̃`�F�b�N���s��

	
	public Thread dataPreserverThread = null;  //DataPreserver��ێ����邽�߂̃X���b�h

    /**
     * 
     * @uml.property name="dataPreserver"
     * @uml.associationEnd multiplicity="(0 1)"
     */
    public DataPreserver dataPreserver = null; //����I�Ƀf�[�^��ۑ�����X���b�h

	static public volatile FileMonitor fMonitor = new FileMonitor();          //�t�@�C���ۑ��̔r���������s�����j�^�[
	
	public static void main(String[] args) {
		ServerMain sMain = new ServerMain();
	}

	public ServerMain() {

		sGUI = new ServerGui(this);
		//		�t�@�C������f�[�^��ǂݏo���đ������
		SPreserveData tmp = ServerMain.sDataToFile.readOutAll();
		if (tmp != null) { //�ǂݏo���Ɏ��s����null���Ԃ��Ă��ĂȂ����
			ServerMain.sDataToFile = tmp;
		}
		while (sDataToFile.alreadyInit == false) { //��x�������ݒ���s���Ă��Ȃ��ꍇ
			Object[] msg = { "�܂������ݒ���s���ĉ�����" };
			JOptionPane.showOptionDialog(sGUI, msg, "�x��",
					JOptionPane.PLAIN_MESSAGE, 0, null, null, null);
			fillServerDat();
		}
		startPrepare(); //�N�����̏������s���i�t�@�C������̓ǂݏo���Ȃǁj
	}

	public void fin() { //�I�����̏���
		
		if (reDisChecker != null) {    //�Ĕz�z�`�F�b�N�X���b�h�̒�~����
			reDisChecker.requestIntterupt();
			while (reDisCheckerThread.isAlive() == true) {
				try {
					Thread.sleep(50);
					//�{���ɏI������܂őҋ@
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		
		if (dataPreserver != null) {    //����ۑ��X���b�h�̒�~����
			dataPreserver.requestIntterupt();
			while (dataPreserverThread.isAlive() == true) {
				try {
					Thread.sleep(50);
					//�{���ɏI������܂őҋ@
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
System.out.println("�e�����X���b�h�̒�~�ɓ����");		
		if (requestManager != null) {
			requestManager.requestStop();
			while (requestManagerThread.isAlive() == true) {
				try {
					Thread.sleep(50);
					//�{���ɏI������܂őҋ@
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
System.out.println("�f�[�^�̕ۑ��܂ł�����");		
		ServerMain.sDataToFile.preserveAll();
	}

	//�����ݒ���s���B�L���ɐ��������ꍇ��true�A���s�����ꍇ��false��Ԃ�
	public boolean fillServerDat() {
	    
		int okOrNo = JOptionPane.showConfirmDialog(sGUI,
				"�����ݒ���s���Ă悢�ł����H(��񂪃��Z�b�g����܂��j", "�m�F", JOptionPane.YES_NO_OPTION);
		if (okOrNo == JOptionPane.OK_OPTION) {
		    ServerMain.sDataToFile = new SPreserveData();
			String strTmp; //��U�L�����ꂽ���e��ێ��iif���ł̗��p�̂��߁j
			int intTmp; //��U�L�����ꂽ���̖߂�l��ێ�

			strTmp = JOptionPane.showInputDialog(sGUI, "�v���W�F�N�g�����L�����ĉ�����",
					"�����ݒ�", JOptionPane.PLAIN_MESSAGE);
			if (strTmp != null) { //��������������Ă��Ȃ����(�㏑���h�~)
				ServerMain.sDataToFile.svrInfo.projectNum = strTmp;
			}else{
			    return false;
			}

			strTmp = JOptionPane.showInputDialog(sGUI,
					"�T�����ȓ��̃A���t�@�x�b�g�Ńv���W�F�N�g�����ȗ����ĉ�����\n��:cae,SETI", "�����ݒ�",
					JOptionPane.PLAIN_MESSAGE);
			if (strTmp != null) { //��������������Ă��Ȃ����(�㏑���h�~)
				ServerMain.sDataToFile.svrInfo.nickname = strTmp;
			}else{
			    return false;
			}

			strTmp = JOptionPane.showInputDialog(sGUI, "�^�c�҂̖��O���L�����ĉ�����",
					"�����ݒ�", JOptionPane.PLAIN_MESSAGE);
			if (strTmp != null) { //��������������Ă��Ȃ����(�㏑���h�~)
				ServerMain.sDataToFile.svrInfo.managerNum = strTmp;
			}else{
			    return false;
			}

			strTmp = JOptionPane.showInputDialog(sGUI,
					"����̂ł���΂v�d�a�̃A�h���X���L�����ĉ�����", "�����ݒ�",
					JOptionPane.PLAIN_MESSAGE);
			if (strTmp != null) { //��������������Ă��Ȃ����(�㏑���h�~)
				ServerMain.sDataToFile.svrInfo.addresOfHP = strTmp;
			}else{
			    return false;
			}

			strTmp = JOptionPane.showInputDialog(sGUI,
					"���J���Ă悯���E-Mail�A�h���X���L�����ĉ�����", "�����ݒ�",
					JOptionPane.PLAIN_MESSAGE);
			if (strTmp != null) { //��������������Ă��Ȃ����(�㏑���h�~)
				ServerMain.sDataToFile.svrInfo.eMailAddress = strTmp;
			}else{
			    return false;
			}
			
			strTmp = (String) JOptionPane.showInputDialog(sGUI,
					"�z�z��ɉ����ȏ�o�߂����ꍇ��job�̍Ĕz�z���s���܂����H", "�����ݒ�",
					JOptionPane.PLAIN_MESSAGE,null,null,String.valueOf(1));
			if (strTmp != null) { //��������������Ă��Ȃ����(�㏑���h�~)
				ServerMain.sDataToFile.reDistributeDays = Integer.parseInt(strTmp);
			}else{
			    return false;
			}
			
			strTmp =JOptionPane.showInputDialog(sGUI,
					"�Q���҂փ��b�Z�[�W������΋L�����ĉ������i��ŕύX�\�j",
					"�����ݒ�",JOptionPane.PLAIN_MESSAGE);
			if (strTmp != null) { //��������������Ă��Ȃ����(�㏑���h�~)
				ServerMain.sDataToFile.messageToClients = strTmp;
			}else{
			    return false;
			}

			//�v���W�F�N�g���Ɖ^�c�Җ��������ƋL������Ă����
			if ((ServerMain.sDataToFile.svrInfo.managerNum != null)
					&& (ServerMain.sDataToFile.svrInfo.managerNum != null)) {
				//�v���W�F�N�g������_�C�W�F�X�g�𐶐����V�O�j�`���Ƃ���
				ServerMain.sDataToFile.svrInfo.signiture = ServerMain.sDataToFile
						.generateHash(ServerMain.sDataToFile.svrInfo.projectNum);

				ServerMain.sDataToFile.alreadyInit = true;
				sDataToFile.preserveAll();
				return true;
			}
		}
		return false;
	}

	public void startPrepare() {
		if (ServerMain.sDataToFile.generater == null) { //�����܂��I�u�W�F�N�g��p�ӂ��Ă��Ȃ������ꍇ�A�p�ӂ���
			ServerMain.sDataToFile.generater = new DataGeneraterImpl();
			ServerMain.sDataToFile.jManager = new JobManager(this,ServerMain.sDataToFile.generater,ServerMain.sDataToFile.resultDB);
		}

		ServerMain.sDataToFile.clientDB.initCDatasIterator();
		while (ServerMain.sDataToFile.clientDB.clientDatasHasNext()) { //�N���C�A���g�̃f�[�^��\�֕\��
			sGUI.clientTb.writeStateToTable(ServerMain.sDataToFile.clientDB
					.clientDatasNext());
		}

		ServerMain.sDataToFile.plocPlant.setAncester(this);    //���������Ă���
		ServerMain.sDataToFile.jManager.setReference(this,ServerMain.sDataToFile.generater,ServerMain.sDataToFile.resultDB);  //�Q�Ƃ�������
		
		for (int i = 0; i < sDataToFile.jManager.pastJobDB.getLogCount(); i++) { //job��result��\�֕\��
			ServerLogWraper log = (ServerLogWraper) ServerMain.sDataToFile.jManager.pastJobDB.getOneLogWraper(i);
		    sGUI.jobTb.writeJobResut(log);
		}

	}

	static SSLServerSocket makeSSLServerSocket(int portno)
			throws java.io.IOException {
		SSLServerSocket ss;
		SSLServerSocketFactory ssf = (SSLServerSocketFactory) SSLServerSocketFactory
				.getDefault();
		ss = (SSLServerSocket) ssf.createServerSocket(portno);
		String cipherSuites[] = { "SSL_DH_anon_EXPORT_WITH_DES40_CBC_SHA" };
		ss.setEnabledCipherSuites(cipherSuites);

		return (ss);
	}

}