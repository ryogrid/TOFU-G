/* ************************************************************** *
 *                                                                *
 * Copyright (c) 2005, Ryo Kanbayashi, All rights reserved.       *
 *                                                                *
 *                                                                *
 * This software is distributed under the modified BSD License.   *
 * ************************************************************** */

import java.io.*;
import java.net.*;
import java.util.Calendar;
import java.util.Date;
import java.util.zip.GZIPInputStream;

import javax.swing.JOptionPane;

//�v���W�F�N�g���ɐ�������A�f�[�^�擾�A�v�Z�A�v�Z���ʕԋp�̎菇���s���X���b�h
public class EachProThread extends Thread {
	private ClientMain ancestor;
	//	  ����U��ꂽ�v���W�F�N�g�̃f�[�^
	private DataOfEachProject assignedPro = new DataOfEachProject();
	public Culculater worker = null;
	private FileOutputStream fOutCulculater = null;
	private FileOutputStream fOutData = null;
	private ObjectInputStream in = null;
	private ObjectOutputStream out = null;
	private Socket sock = null;
	private volatile boolean stopRequest = false; //���̃X���b�h���I�����ׂ�����true��

	//�����ׂ��v���W�F�N�g�Ɋւ���f�[�^�ƁA�Ăяo�����̎Q�Ƃ�^����
	public EachProThread(DataOfEachProject handInPro, ClientMain ancestor) {
		this.assignedPro = handInPro;
		this.ancestor = ancestor;
	}

	public void run() {

		try {
			while (stopRequest != true) { //�����v���Z�X���i�v�ɉ�

				try {
					//���ɃW���u���󂯎��Ȃ��Ă͂Ȃ�Ȃ��ꍇ
					if (assignedPro.nextShouldDoThing == assignedPro.SHOULDRECIVEJOB) {

						//�v�Z�f�[�^��ێ����Ă��Ȃ����(�T�[�o����W���u���󂯂Ƃ��Ă��Ȃ��ꍇ�̂��߁j
						if (assignedPro.culData == null) {
							DataOfEachProject tmpPro = getJob(assignedPro,
									ClientMain.cDataToFile.cInfo);
							assignedPro = tmpPro;
							assignedPro.variousState = assignedPro.HAVENEWJOB;
						}
						ancestor.cGUI.writeStateToTable(
								assignedPro.projectNumber, assignedPro); //�v���W�F�N�g�����X�V
						ancestor.cGUI.writeLogToTable(
								assignedPro.projectNumber, assignedPro); //�v�Z�J�n���ɏ�������
						assignedPro.nextShouldDoThing = assignedPro.SHOULDCULCULATE;
						ClientMain.cDataToFile.addDatOfEach(assignedPro); //�f�[�^���X�V

						//���Ɍv�Z���s��Ȃ��Ă͂Ȃ�Ȃ��ꍇ
					} else if (assignedPro.nextShouldDoThing == assignedPro.SHOULDCULCULATE) {
						assignedPro.culMemo.setInterruption(false); //�O�̂��߁A���������Ă���

						DataOfEachProject tmpPro = doCulculate(assignedPro);
						assignedPro =tmpPro;
						
						//�v�Z������ɏI�������ꍇ
						if (assignedPro.culMemo.getInterruption() == false) {
							assignedPro.variousState = assignedPro.COMPCUL;
							assignedPro.nextShouldDoThing = assignedPro.SHOULDBACKJOB;
						} else { //�v�Z������ɏI�����Ă��Ȃ��ꍇ�i���f���ꂽ�ꍇ�Ȃǁj
							assignedPro.variousState = assignedPro.HAVEMIDDLEWAYJOB;
							assignedPro.nextShouldDoThing = assignedPro.SHOULDCULCULATE; //������x�v�Z���s��Ȃ��Ă͂Ȃ�Ȃ��ƕ\��
						}

						ClientMain.cDataToFile.addDatOfEach(assignedPro); //�f�[�^���X�V

						//���Ɍv�Z���ʂ��T�[�o�֕Ԃ��Ȃ��Ă͂Ȃ�Ȃ��ꍇ
					} else if (assignedPro.nextShouldDoThing == assignedPro.SHOULDBACKJOB) {
						DataOfEachProject tmpPro = backResult(assignedPro,
								ClientMain.cDataToFile.cInfo);
						assignedPro = tmpPro;
						
						ClientLogWraper tmp = assignedPro.pastJobDB
								.wrapByLogWraper(assignedPro.culData); //CulculateData�����b�s���O
						assignedPro.pastJobDB.addOneLogWraper(tmp); //���O��ۑ�
						assignedPro.variousState = assignedPro.AFTERBACK;

						ancestor.cGUI.writeStateToTable(
								assignedPro.projectNumber, assignedPro); //�v���W�F�N�g�ԍ��ɂ����������v���W�F�N�g����table�ɕ\��
						ancestor.cGUI.writeLogToTable(
								assignedPro.projectNumber, assignedPro); //�v�Z�I����ɏ�������
						assignedPro.culData = null; //�I�������v�Z�^�X�N���������Ă����i�V�����W���u���󂯎�邽�߁j

						ClientMain.cDataToFile.addDatOfEach(assignedPro); //�f�[�^���X�V

						assignedPro.nextShouldDoThing = assignedPro.SHOULDRECIVEJOB;
					} else { //���̑��̏ꍇstate�Ɉُ킪���邩������Ȃ��̂Ŏw�肵�Ȃ���
						assignedPro.nextShouldDoThing = assignedPro.SHOULDCULCULATE;
					}

				} catch (IOException e1) { //�ڑ����s�Ȃǃ\�P�b�g�֌W�̃G���[
				    e1.printStackTrace();
				    System.out.println("cDataToFile���̃A�h���X��:" + assignedPro.svrInfo.address.getHostAddress());
				    doReConnectProcess();
				}finally{
					if(sock != null){//�O�̂��߉��
				        try {
                            sock.close();
                        } catch (IOException e2) {
                            e2.printStackTrace();
                        }
				    }
				}
			}

		} catch (ConnectCanceledException e) { //�ڑ���������Ȃ������ꍇ
			ancestor.cGUI.startStopEachItem[assignedPro.projectNumber - 1]
					.setEnabled(true);
			//���̃X���b�h�͂��̂܂܏I�����Ă��܂�
		} catch (FullConnectFailedException e1) {
			//�K��񐔐ڑ������s���Ă��܂����ꍇ�̏���
			Object[] msg = { "�T�[�o����~���Ă��邩���炩�̗��R�ŃT�[�o�֐ڑ��ł��܂���F" + assignedPro.svrInfo.projectNum +"�̏������~���܂�" };
			JOptionPane.showOptionDialog(ancestor.cGUI, msg, "�x��",
					JOptionPane.PLAIN_MESSAGE, 0, null, null, null);
			ClientMain.cDataToFile.addDatOfEach(assignedPro);
			ClientMain.cDataToFile.removeProThre(assignedPro.projectNumber-1);  //�����X���b�h����菜��
			ancestor.cGUI.startStopEachItem[assignedPro.indexOfMenu].setText("START " + assignedPro.svrInfo.nickname + "!"); //START�{�^���Ƀ`�F���W
			
			ReConnectWaiter.restAll();	            //�҂����Ԃ�ҋ@�񐔂Ȃǂ����Z�b�g
		} finally {
			assignedPro.culMemo.setInterruption(false); //����N�����̂��߂ɏ��������Ă���
			assignedPro.variousState = assignedPro.NOTSTART;

			ClientMain.cDataToFile.addDatOfEach(assignedPro); //�v���W�F�N�g�̃f�[�^���m���ɍX�V���Ă���
			ClientMain.cDataToFile.preserveAll();
		}
	}

	//�Đڑ��܂ł̑ҋ@�������s���i���ۂ̐ڑ��͂��Ȃ��őҋ@�̊Ǘ��j
	private void doReConnectProcess() throws FullConnectFailedException {

		if (ReConnectWaiter.isWaitable() == true) { //�K���wait���Ă��Ȃ��ꍇ
			ReConnectWaiter.waitForDoing(); //�ҋ@
			//�ҋ@��ɂ��̂܂܏������[�v�֖߂�
		} else {
			
			throw new FullConnectFailedException();
		}
	}

	//�v�Z���ʂ��T�[�o�֕Ԃ�(target�̓��e��ύX���ĕԂ��j
	//���� target : �e�X�̃v���W�F�N�g�ւ�����i�Ώۃv���W�F�N�g�j myself : �N���C�A���g���M�̏��
	public DataOfEachProject backResult(DataOfEachProject target,
			ClientInfo myself) throws ConnectCanceledException, IOException {

		try {

			//�����ڑ���������Ă��Ȃ��ꍇ�A���̊m�F���s��
			if (ClientMain.cDataToFile.sePolicy.autConnect == false) {
				if (ancestor.checkConnectable(target) == false) {
					throw new ConnectCanceledException();
				}
			}

			InetAddress svrAddress = target.svrInfo.address; //�A�h���X�����o��
			//			sock = new Socket(svrAddress, 2525);
			//sock = ClientMain.makeSSLClientSocket(svrAddress,2525);
			sock = ClientMain.makeSSLClientSocket(InetAddress.getByAddress(svrAddress.getAddress()),2525);
			
			out = new ObjectOutputStream(new DataOutputStream(sock
					.getOutputStream()));

			ExchangeInfo sendInfo = new ExchangeInfo();
			sendInfo.backResult = true; //�v�Z���ʂ̕ԋp���Ƃ�������\��
			DataContainer sendContainer = new DataContainer();

			target.workCounts++; //���������v�������������Z
			myself.AllWorkCounts++; //�S�̂̃��[�N�������Z���Ă���

			target.culData.setCompleteDate(Calendar.getInstance()); //�v�Z�I���������L�^���Ă���

			ClientInfo forSendCInfo = extractForSendData(myself,target);
			sendContainer.setDeriverdCInfo(forSendCInfo);
			out.writeObject(sendInfo);

			out.writeObject(sendContainer); //�N���C�A���g����̏��𑗐M

			out.flush();
			

			//			�\�P�b�g�ł̃f�[�^�ǂݎ��p
			in = new MyObjectInputStream(new DataInputStream(sock
					.getInputStream()), ancestor.myloader, "./ProjectFiles/"
					+ target.svrInfo.nickname + "/");

			ExchangeInfo receiveExInfo = (ExchangeInfo) in.readObject();
			FileInformations fInfos = (FileInformations) in.readObject();
			extractFileToLocal(fInfos, in); //�t�@�C�������ׂă��[�J���ɕۑ�
			DataContainer receiveContainer = (DataContainer) in.readObject();

			sock.close();

			//return shelter.comebackData((receiveContainer.getDeriverdCInfo()).tmpIncluded);     //�ޔ����Ă����f�[�^�������ĕԂ�
			return backFromReceiveData(target,(receiveContainer.getDeriverdCInfo()).tmpIncluded);    //��M�����f�[�^���̃f�[�^�������ĕԂ�

		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			return null;
		}finally{
		    if(sock != null){
		        sock.close();
		    }
		}
	}

	//�^�����v���W�F�N�g�ɂ��Čv�Z���s��(dealPro�̓��e��ύX���ĕԂ��j
	//���� dealPro : �e�X�̃v���W�F�N�g�ւ�����i�Ώۃv���W�F�N�g
	public DataOfEachProject doCulculate(DataOfEachProject dealPro) {

		Date d = new Date();
		double culculateStart = d.getTime(); //�v�Z���Ԃ��v�Z���邽�߂̕ϐ�

		worker.culculate(dealPro.culData, dealPro.culMemo); //�v�Z���ʂ͎󂯂�CulculateData�Ɏ󂯂�
		String infoFromWorker = worker.getString(); //worker�̕Ԃ����𕶎����
		ancestor.cGUI.ta[dealPro.projectNumber - 1].setText(infoFromWorker);

		d = new Date();
		double culculateEnd = d.getTime();
		dealPro.culData
				.setNecessaryTime((double) ((culculateEnd - culculateStart) / 100)); //����job�Ɋւ��Ăǂꂾ�����Ԃ������������L�^
		dealPro.workSeconds += (double) ((culculateEnd - culculateStart) / 100); //�v�Z�ɂ����������Ԃ����Z
		ClientMain.cDataToFile.cInfo.AllWorkingTime += (double) ((culculateEnd - culculateStart) / 100); //�S�̂̌v�Z���Ԃɂ����Z���Ă���

		
		//���f���߂��o�邱�ƂȂ��v�Z���I�����Ă���Όv�Z�����ƌ��Ȃ��A�t���O�𗧂Ă�
		if (dealPro.culMemo.getInterruption() != true) {
			dealPro.culData.setCulculateComplete(true); //�v�Z���I�����Ă��鎖��\��
		}

		return dealPro;
	}

	//�R���e�i�̒�����t�@�C���̏��𒊏o���āA�X�g���[�����烍�[�J���̃f�B���N�g���ɓW�J
	private void extractFileToLocal(FileInformations info, InputStream in) {

		long fileLength = 0; //�ǂݍ��ރt�@�C����length
		long readedByte;
		for (int i = 0; (fileLength = info.fileLengths[i]) != -1; i++) {
			try {
				FileOutputStream out = new FileOutputStream(info.fileNames[i]);
				readedByte = 0; //�ǂݍ��ݍς݂̃o�C�g��
				while (readedByte != fileLength) {
					//�w�肳�ꂽ�����œǂݍ���
					out.write(in.read());
					readedByte++;
				}
				out.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	//�V�����W���u���󂯎��(target�̓��e��ύX���ĕԂ��j
	//���� target : �e�X�̃v���W�F�N�g�Ɋւ�����i�Ώۃv���W�F�N�g�j myself : �N���C�A���g���g�̏��
	public DataOfEachProject getJob(DataOfEachProject target, ClientInfo myself)
			throws ConnectCanceledException, IOException {
		try {

			//			�����ڑ���������Ă��Ȃ��ꍇ�A���̊m�F���s��
			if (ClientMain.cDataToFile.sePolicy.autConnect == false) {
				if (ancestor.checkConnectable(target) == false) {
					throw new ConnectCanceledException();
				}
			}

			InetAddress svrAddress = target.svrInfo.address;
			//			sock = new Socket(svrAddress, 2525);
//			sock = ClientMain.makeSSLClientSocket(svrAddress,2525);
			sock = ClientMain.makeSSLClientSocket(InetAddress.getByAddress(svrAddress.getAddress()),2525);

			out = new ObjectOutputStream(new DataOutputStream(sock
					.getOutputStream()));

			ExchangeInfo sendInfo = new ExchangeInfo();
			sendInfo.receiveCulData = true; //�V�����W���u���󂯎�邽�߂��ƕ\��
			out.writeObject(sendInfo);
			DataContainer sendContainer = new DataContainer();
			
			ClientInfo forSendCInfo = extractForSendData(myself,target);
			
			sendContainer.setDeriverdCInfo(forSendCInfo);

			out.writeObject(sendContainer); //�N���C�A���g����̏��𑗐M�i�����ł͋�j
			out.flush();

			in = new MyObjectInputStream(new DataInputStream(sock
					.getInputStream()), ancestor.myloader, "./ProjectFiles/"
					+ target.svrInfo.nickname + "/");
			//�\�P�b�g�ł̃f�[�^�ǂݎ��p

			ExchangeInfo receiveExInfo = (ExchangeInfo) in.readObject();
			FileInformations fInfos = (FileInformations) in.readObject();
			extractFileToLocal(fInfos, in); //�t�@�C�������ׂă��[�J���ɕۑ�
			DataContainer receiveContainer = (DataContainer) in.readObject();
//			return shelter.comebackData((receiveContainer.getDeriverdCInfo()).tmpIncluded); //�ޔ����Ă����f�[�^�������ĕԂ�
			sock.close();
			return backFromReceiveData(target,(receiveContainer.getDeriverdCInfo()).tmpIncluded);    //��M�����f�[�^���̃f�[�^�������ĕԂ�
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			return null;
		}finally{
		    if(sock != null){
		        sock.close();
		    }
		}

	}

	//���̃X���b�h�̏I����v������
	public void requestStop() {
		stopRequest = true;
		assignedPro.culMemo.setInterruption(true); //�v�Z�����Ă���ꍇ���f���Ă����悤�v������t���O�𗧂Ă�
		ReConnectWaiter.requestIntterupt();
	}

	//�����ŕێ����Ă���v���W�F�N�g��Ԃ�
	public DataOfEachProject getHavinProject() {
		return assignedPro;
	}
	
	//�T�[�o�֑��M���邽�߂ɕK�v�ȃf�[�^�����𒊏o����DataOfEachProject���܂�ClientInfo����肾�����\�b�h
	//���T�[�o�ŗ��p����f�[�^���ύX���ꂽ�������ꂽ�ꍇ�A���̃��\�b�h���ύX����Ȃ��Ă͂Ȃ�Ȃ�
	//���f�B�[�v�R�s�[�ł͂Ȃ��̂Œ��ӂ���
	private ClientInfo extractForSendData(ClientInfo info,DataOfEachProject eachPro){
	    DataOfEachProject copiedEachPro = new DataOfEachProject();
	    copiedEachPro.culData = eachPro.culData;
	    copiedEachPro.workCounts = eachPro.workCounts;
	    copiedEachPro.workSeconds = eachPro.workSeconds;
    	copiedEachPro.messageToServer = eachPro.messageToServer;
	    copiedEachPro.messageFromServer = eachPro.messageFromServer;
	    copiedEachPro.rankOfWorker = eachPro.rankOfWorker;
	    copiedEachPro.joinDate = eachPro.joinDate;
	    copiedEachPro.svrInfo = eachPro.svrInfo;
	    copiedEachPro.pastJobDB = null;
	    ClientInfo copiedClientInfo = new ClientInfo();
	    copiedClientInfo.AllWorkCounts = info.AllWorkCounts;
	    copiedClientInfo.AllWorkingTime = info.AllWorkingTime;
	    copiedClientInfo.clientVersion = info.clientVersion;
	    copiedClientInfo.mailAddress = info.mailAddress;
	    copiedClientInfo.participantNum = info.participantNum;
	    copiedClientInfo.signiture = info.signiture;
	    copiedClientInfo.startUseDate = info.startUseDate;
	    copiedClientInfo.tmpIncluded = copiedEachPro;
	    return copiedClientInfo;
	}
	
	//����DataOfEachProject�Ɏ�M�������̂���f�[�^�������A�����Ԃ�
	//extractForSendData�𗘗p�����ꍇ�A���̃��\�b�h���g�p���邱��
	private DataOfEachProject backFromReceiveData(DataOfEachProject target,DataOfEachProject received){
	    target.culData = received.culData;
	    target.workCounts = received.workCounts;
        target.messageFromServer = received.messageFromServer;
	    target.rankOfWorker = received.rankOfWorker;
	    target.joinDate = received.joinDate;
	    target.svrInfo = received.svrInfo;
	    return target;
	}

}