/* ************************************************************** *
 *                                                                *
 * Copyright (c) 2005, Ryo Kanbayashi, All rights reserved.       *
 *                                                                *
 *                                                                *
 * This software is distributed under the modified BSD License.   *
 * ************************************************************** */

import java.io.*;
import java.net.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.*;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.GZIPOutputStream;

public class RequestProcessor implements Runnable {
    public static volatile int nowWaitThreadCount = 0; //���A�����̃X���b�h���ҋ@���Ă��邩
    public static List pool = new LinkedList(); //�X���b�h�v�[��
    private int processorNumber = 0; //�e�X���b�h�ɐU���鎯�ʔԍ�

    /**
     * 
     * @uml.property name="ancestor"
     * @uml.associationEnd multiplicity="(1 1)"
     */
    private ServerMain ancestor = null;

    /**
     * 
     * @uml.property name="sendFiles"
     * @uml.associationEnd elementType="java.io.File" multiplicity="(0 -1)"
     */
    private ArrayList sendFiles = null; //�����[�g�֑��M���邽�߂̃t�@�C���̏���ێ�����

    private volatile boolean stopRequest = false; //���̃X���b�h���I�����ׂ�����true��

    public RequestProcessor(ServerMain parent, int number) {
        ancestor = parent;
        this.processorNumber = number;
    }

    public void run() {
//        makeLogFile(processorNumber);

        sendFiles = getClassJarFiles(); //DistributeFiles�t�H���_��class�t�@�C�����܂�ArrayList�𓾂�
        sendFiles.add(new File("ProjectPicture.jpg"));

        FileInformations fInfos = new FileInformations();
        for (int i = 0; i < sendFiles.size(); i++) { //sendFiles�Ɋ܂܂��t�@�C������ǉ����čs��
            fInfos.addFileInformation((File) sendFiles.get(i));
        }

        end: while (stopRequest == false) {
            try {
                Socket connection;
                synchronized (pool) { //�R�l�N�V�����������X���b�h�v�[��
                    while (pool.isEmpty()) {
                        try {
//                            writeLogFile(processorNumber, "�v�[���֋A��");
                            nowWaitThreadCount++;
                            System.out.println("���݂̃v�[���̒��̃X���b�h����"
                                    + nowWaitThreadCount);

                            pool.wait();
                        } catch (InterruptedException e) {

                        }
                        nowWaitThreadCount--;
                        if (stopRequest == true) { //�I���̎w�����o�Ă����ꍇ
                            break end;
                        }
                    }
                    connection = (Socket) pool.remove(0);
                }

                ObjectInputStream in = new ObjectInputStream(
                        new DataInputStream(connection.getInputStream()));

                ExchangeInfo receiveInfo = (ExchangeInfo) in.readObject();

                if (receiveInfo.initializeClient == true) {
                    //�N���C�A���g�̏��������s�����߂̐ڑ��̏ꍇ
                    //�����Ă����R���e�i�Ɋe��f�[�^��ǉ����ĕԑ�����
                    DataContainer receiveContainer = (DataContainer) in
                            .readObject();
                    ClientInfo receiveCInfo = receiveContainer
                            .getDeriverdCInfo(); //�N���C�A���g�̏������o���Ă���
                    ServerMain.sDataToFile.clientDB.addClient(receiveCInfo); //�����̃V�O�j�`�����L�[�Ƃ��āA�f�[�^��ǉ�
                    int grade = ServerMain.sDataToFile.clientDB
                            .getGradeOfClient(ancestor, receiveCInfo.signiture);
                    receiveCInfo.tmpIncluded.rankOfWorker = grade;
                    receiveCInfo.tmpIncluded.messageFromServer = ServerMain.sDataToFile.messageToClients; //���b�Z�[�W��ύX

                    synchronized (ServerMain.sDataToFile.svrInfo) { //�v���W�F�N�g���Q���l�������Z
                        ServerMain.sDataToFile.svrInfo.workerCounts++;
                    }

                    InetAddress receivedAddress = (receiveContainer.getDeriverdCInfo()).tmpIncluded.svrInfo.address;
                    try {
                        //�T�[�o����ǉ�
                        (receiveContainer.getDeriverdCInfo()).tmpIncluded.svrInfo = (ServerInfo) ServerMain.sDataToFile.svrInfo.clone(); //�R���e�i���̃f�[�^�ɃT�[�o���ǉ�
                    } catch (CloneNotSupportedException e1) {
                        e1.printStackTrace();
                    }
                    (receiveContainer.getDeriverdCInfo()).tmpIncluded.svrInfo.address = receivedAddress;
                    
                    synchronized (ancestor.sGUI.clientTb) {
                        ancestor.sGUI.clientTb
                                .writeStateToTable(receiveContainer
                                        .getDeriverdCInfo()); //�\�փN���C�A���g����ǉ�
                    }
                    System.out
                            .println(receiveCInfo.participantNum + "�փf�[�^���M�J�n");
                    ObjectOutputStream out = new ObjectOutputStream(
                            new DataOutputStream(connection.getOutputStream()));

                    //				�t�@�C���̏�����������ł����i���̂͂܂��j
                    ExchangeInfo sendExInfo = new ExchangeInfo();
                    out.writeObject(sendExInfo); //�󂾂��ꉞ���M

                    out.writeObject(fInfos);
                    out.writeObject(receiveContainer); //�f�[�^��ǉ����Ă������R���e�i��ԑ�

                    for (int i = 0; i < sendFiles.size(); i++) { //sendFiles�Ɋ܂܂��t�@�C�����������o��
                        writeFileToStream((File) sendFiles.get(i), out); //�t�@�C���{�̂���������
                    }

                    out.flush();
                    connection.close();
                    System.out
                            .println(receiveCInfo.participantNum + "�փf�[�^���M����");
                } else if (receiveInfo.receiveCulData == true) { //�V����job���󂯎�邽�߂̐ڑ��̏ꍇ
//                    writeLogFile(processorNumber, "�f�[�^�ǂݍ��݊J�n:receiveCulData");
                    DataContainer receiveContainer = (DataContainer) in
                            .readObject();

                    ClientInfo receiveCInfo = receiveContainer
                            .getDeriverdCInfo(); //�N���C�A���g�̏������o���Ă���
                    ServerMain.sDataToFile.clientDB.renewClient(receiveCInfo);
                    //�����̃V�O�j�`�����L�[�Ƃ��āA�f�[�^���X�V
                    int grade = ServerMain.sDataToFile.clientDB
                            .getGradeOfClient(ancestor, receiveCInfo.signiture);
                    receiveCInfo.tmpIncluded.rankOfWorker = grade;
                    receiveCInfo.tmpIncluded.messageFromServer = ServerMain.sDataToFile.messageToClients; //���b�Z�[�W��ύX

                    InetAddress receivedAddress = (receiveContainer.getDeriverdCInfo()).tmpIncluded.svrInfo.address;
                    try {
                        //�T�[�o����ǉ�
                        (receiveContainer.getDeriverdCInfo()).tmpIncluded.svrInfo = (ServerInfo) ServerMain.sDataToFile.svrInfo.clone(); //�R���e�i���̃f�[�^�ɃT�[�o���ǉ�
                    } catch (CloneNotSupportedException e1) {
                        e1.printStackTrace();
                    }
                    (receiveContainer.getDeriverdCInfo()).tmpIncluded.svrInfo.address = receivedAddress;
                    
                    System.out.println(receiveCInfo.participantNum
                            + "�փf�[�^���M�J�n:receiveCalData");
//                    writeLogFile(processorNumber, "�u���b�N1�J�n:receiveCalData");
                    ObjectOutputStream out = new ObjectOutputStream(
                            new DataOutputStream(connection.getOutputStream()));

                    ExchangeInfo sendInfo = new ExchangeInfo();
//                    writeLogFile(processorNumber, "�u���b�N2�J�n:receiveCalData");
                    out.writeObject(sendInfo); //�󂾂��ꉞ���M���Ă���
                    FileInformations noFInfos = new FileInformations();
                    //���M����t�@�C���͂Ȃ�����������ł���
//                    writeLogFile(processorNumber, "�u���b�N3�J�n:receiveCalData");
                    try{
	                    out.writeObject(noFInfos);
	
	                    CulculateData tmp;
	
	                    tmp = ServerMain.sDataToFile.jManager
	                            .generateJob((receiveContainer.getDeriverdCInfo()).signiture); //���[�U�[�̎�������DataGenerater����v�Z�f�[�^�𓾂�
	                    
	                    (receiveContainer.getDeriverdCInfo()).tmpIncluded.culData = tmp;
                    }catch(Exception e){
                        e.printStackTrace();
//                        PrintStream tmp = new PrintStream(getLogFileOutStream(processorNumber));
//                        e.printStackTrace(tmp);
//                        tmp.flush();
//                        tmp.close();
//                        writeLogFile(processorNumber,"�u���b�N�R�ŉ��炩�̃G���[�������A�t�@�C���֏o�͂��܂���");
                    }
                    
//                    writeLogFile(processorNumber, "�u���b�N4�J�n:receiveCalData");
                    out.writeObject(receiveContainer); //��M�����R���e�i�i�e��f�[�^�ǉ��ς݁j��ԑ�
//                    writeLogFile(processorNumber, "�u���b�N5�J�n:receiveCalData");
                    out.flush();
//                    writeLogFile(processorNumber, "�u���b�N6�J�n:receiveCalData");

                    connection.close();
//                    writeLogFile(processorNumber, receiveCInfo.participantNum
//                            + "�փf�[�^���M����&�u���b�N�U����:receiveCalData");

                } else if (receiveInfo.backResult == true) { //���ʂ̕ԑ��������ꍇ
//                    writeLogFile(processorNumber, "�f�[�^�ǂݍ��݊J�n:backResult");
                    DataContainer receiveContainer = (DataContainer) in
                            .readObject();
                    ClientInfo receiveCInfo = receiveContainer
                            .getDeriverdCInfo(); //�N���C�A���g�̏������o���Ă���

                    ServerMain.sDataToFile.clientDB.renewClient(receiveCInfo); //�����̃V�O�j�`�����L�[�Ƃ��āA�f�[�^���X�V
                    ancestor.sGUI.clientTb.writeStateToTable(receiveContainer
                            .getDeriverdCInfo()); //�\�̃N���C�A���g�����X�V
                    (receiveContainer.getDeriverdCInfo()).tmpIncluded.messageFromServer = ServerMain.sDataToFile.messageToClients; //���b�Z�[�W��ύX

                    CulculateData receiveResult = (receiveContainer
                            .getDeriverdCInfo()).tmpIncluded.culData;

                    ServerMain.sDataToFile.jManager.processCulculatedJob(
                            receiveResult, receiveContainer.getDeriverdCInfo());
//                    writeLogFile(processorNumber, "�u���b�N1�J�n:backResult");
                    ObjectOutputStream out = new ObjectOutputStream(
                            new DataOutputStream(connection.getOutputStream()));

                    ExchangeInfo sendInfo = new ExchangeInfo();
//                    writeLogFile(processorNumber, "�u���b�N2�J�n:backResult");
                    out.writeObject(sendInfo); //�󂾂��ꉞ���M���Ă���
                    FileInformations noFInfos = new FileInformations();
                    //���M����t�@�C���͂Ȃ�����������ł���
//                    writeLogFile(processorNumber, "�u���b�N3�J�n:backResult");
                    out.writeObject(noFInfos);
//                    writeLogFile(processorNumber, "�u���b�N4�J�n:backResult");
                    out.writeObject(receiveContainer); //��M�����R���e�i�i�e��f�[�^�ǉ��ς݁j��ԑ�
//                    writeLogFile(processorNumber, "�u���b�N5�J�n:backResult");
                    out.flush();
//                    writeLogFile(processorNumber, "�u���b�N6�J�n:backResult");

                    connection.close();
//                    writeLogFile(processorNumber, receiveCInfo.participantNum
//                            + "�փf�[�^���M����&�u���b�N�U����:backResult");
                } else if (receiveInfo.quiteProject == true) { //�N���C�A���g����̒E�ޏ����̂��߂̐ڑ��������ꍇ
//                    writeLogFile(processorNumber, "�f�[�^�ǂݍ��݊J�n:quiteProject");
                    //�����Ă����R���e�i�𓾂�
                    DataContainer receiveContainer = (DataContainer) in
                            .readObject();

                    ClientInfo receiveCInfo = receiveContainer
                            .getDeriverdCInfo(); //�N���C�A���g�̏������o���Ă���

                    ServerMain.sDataToFile.clientDB
                            .delOneClient(receiveCInfo.signiture); //�N���C�A���g�̃f�[�^���f�[�^�x�[�X�����菜��

                    //�v���W�F�N�g���Q���l�������炵�A�E�ސl���𑝂₷
                    ServerMain.sDataToFile.svrInfo.workerCounts--;
                    ServerMain.sDataToFile.svrInfo.quiteMenberCount++;

                    synchronized (ancestor.sGUI.clientTb) {
                        ancestor.sGUI.clientTb
                                .removeStateFromTable(receiveContainer
                                        .getDeriverdCInfo()); //�\����N���C�A���g�����폜
                    }
//                    writeLogFile(processorNumber, "�u���b�N1�J�n:quiteProject");
                    ObjectOutputStream out = new ObjectOutputStream(
                            new DataOutputStream(connection.getOutputStream()));

                    //				�t�@�C���̏�����������ł����i���̂͂܂��j
                    ExchangeInfo sendExInfo = new ExchangeInfo();
//                    writeLogFile(processorNumber, "�u���b�N2�J�n:quiteProject");
                    out.writeObject(sendExInfo); //�󂾂��ꉞ���M
//                    writeLogFile(processorNumber, "�u���b�N3�J�n:quiteProject");
                    out.writeObject(fInfos);
//                    writeLogFile(processorNumber, "�u���b�N4�J�n:quiteProject");
                    out.writeObject(receiveContainer); //�f�[�^��ǉ����Ă������R���e�i��ԑ�
//                    writeLogFile(processorNumber, "�u���b�N5�J�n:quiteProject");
                    out.flush();
//                    writeLogFile(processorNumber, "�u���b�N6�J�n:quiteProject");
                    connection.close();
//                    writeLogFile(processorNumber, receiveCInfo.participantNum
//                            + "�փf�[�^���M����&�u���b�N�U����:quiteProject");

                    ServerMain.sDataToFile.preserveAll();
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } finally {
//                writeLogFile(processorNumber, "���炩�̃G���[��Finally�u���b�N��");
            }
        }
    }

    //�X���b�h�v�[�����������Ă���֐��Arequest�̑Ή���������
    public static void processRequest(Socket request) {

        synchronized (pool) {
            pool.add(pool.size(), request);
            pool.notifyAll();
        }
    }

    //������File��OutputStream�֏�������
    private void writeFileToStream(File file, OutputStream stream) {
        try {
            FileInputStream in = new FileInputStream(file);
            int i;
            while ((i = in.read()) != -1) {
                stream.write(i);
            }
            in.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //DistributeFiles�̒��ɂ���class�t�@�C����jar�t�@�C����File�I�u�W�F�N�g���܂�ArrayList��Ԃ�
    private ArrayList getClassJarFiles() {

        ArrayList tmp = new ArrayList();
        File distributeFilesDir = new File("./DistributeFiles");
        File[] distributeFiles = distributeFilesDir.listFiles(new FileFilter() { //�g���q��class�̃t�@�C����list�𓾂�
                    public boolean accept(File pathname) {
                        String name = pathname.getName();
                        String fiveTale = name.substring(name.length() - 5); //��납��T������؂�o��
                        String threeTale = name.substring(name.length() - 3); //��납��R�����؂�o��
                        if ((fiveTale.equals("class") == true)
                                || (threeTale.equals("jar") == true)) { //�g���q��class��jar�ł����
                            return true;
                        } else {
                            return false;
                        }
                    }
                });
        for (int i = 0; i < distributeFiles.length; i++) {
            tmp.add(distributeFiles[i]);
        }

        return tmp;
    }

    //���̃X���b�h�̏I����v������
    public void requestStop() {
        stopRequest = true;
    }

    //�Ƃ肠�����A���ׂẴX���b�h��҂���Ԃ���J������
    public static void finNotify() {
        synchronized (pool) {
            pool.notifyAll();
        }
    }

//    //�f�o�b�O�p�̃��\�b�h�A�X���b�h�i���o�[��n���ă��O�o�͗p�̃t�@�C���𐶐�
//    private void makeLogFile(int number) {
//        String strNum = String.valueOf(number);
//        File logFile = new File("./" + strNum + "Log.txt");
//        try {
//            logFile.createNewFile();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    //	�f�o�b�O�p�̃��\�b�h�A�^�����X���b�h�ԍ��ɑΉ����郍�O�t�@�C���Ƀe�L�X�g���o��
//    private void writeLogFile(int number, String str) {
//        String strNum = String.valueOf(number);
//        File logFile = new File("./" + strNum + "Log.txt");
//
//        FileWriter logFileOut = null;
//        try {
//            logFileOut = new FileWriter(logFile, true);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        try {
//            logFileOut.write(str + "\n");
//            logFileOut.flush();
//            if (logFileOut != null) {
//                logFileOut.close();
//            }
//        } catch (IOException e1) {
//            e1.printStackTrace();
//        }
//    }
//    
//    private FileOutputStream getLogFileOutStream(int number){
//        String strNum = String.valueOf(number);
//        File logFile = new File("./" + strNum + "Log.txt");
//
//        FileOutputStream logFileOut = null;
//        try {
//            logFileOut = new FileOutputStream(logFile);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        
//        return logFileOut;
//    }

}

