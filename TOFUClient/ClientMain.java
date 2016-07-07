/* ************************************************************** *
 *                                                                *
 * Copyright (c) 2005, Ryo Kanbayashi, All rights reserved.       *
 *                                                                *
 *                                                                *
 * This software is distributed under the modified BSD License.   *
 * ************************************************************** */

import java.net.*;
import java.util.*;
import java.util.zip.GZIPInputStream;
import java.awt.Image;
import java.awt.Toolkit;
import java.io.*;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.swing.JOptionPane;

public class ClientMain {
    public static volatile FileMonitor fMonitor = new FileMonitor(); // �t�@�C���ۑ��̔r���������s�����j�^�[
    public static volatile CPreserveData cDataToFile = new CPreserveData(); // �t�@�C���ɕۑ����ׂ��f�[�^

    private Socket sock = null;
    private ObjectInputStream in = null;
    private ObjectOutputStream out = null;
    private FileOutputStream fOutCulculater = null;
    private FileOutputStream fOutData = null;
    public ClientGui cGUI = null;
    public PermissionClassLoader myloader = new PermissionClassLoader(
            ClassLoader.getSystemClassLoader());

    public Thread dataPreserverThread = null; // DataPreserver��ێ����邽�߂̃X���b�h
    public DataPreserver dataPreserver = null; // ����I�Ƀf�[�^��ۑ�����X���b�h

    public ClientMain() {

        startPrepare();
        cGUI = new ClientGui(this);
        while (ClientMain.cDataToFile.alreadyInit == false) { // ��x�������ݒ���s���Ă��Ȃ��ꍇ
            Object[] msg = { "�܂������ݒ���s���ĉ�����" };
            JOptionPane.showOptionDialog(cGUI, msg, "�x��",
                    JOptionPane.PLAIN_MESSAGE, 0, null, null, null);
            fillClientDat();
        }
        cGUI.informChangeToComp(); // �t�@�C�����̃t���O�ɂ���ĕ`��ύX

        while (ClientMain.cDataToFile.sInfosHasNext()) { // �^�u�̕`�悾���s��
            DataOfEachProject assignedPro = (DataOfEachProject) ClientMain.cDataToFile
                    .sInfosNext();
            Toolkit toolkit = Toolkit.getDefaultToolkit();
            Image img = toolkit
                    .getImage("./ProjectFiles/" + assignedPro.svrInfo.nickname
                            + "/" + "ProjectPicture.jpg");
            cGUI.addProjectTabMenu(assignedPro.projectNumber, img, assignedPro); // �^�u��ǉ�����
        }

        dataPreserver = new DataPreserver(1); // �P���Ԋu�ŕۑ����s�킹��
        dataPreserverThread = new Thread(dataPreserver);
        dataPreserverThread.start();

        // �����v�Z��������Ă���ꍇ�A�A�v���P�[�V�����N�����ɂ��ׂẴv���W�F�N�g�̏������J�n����
        if (ClientMain.cDataToFile.sePolicy.autCulculate) {
            startAllThread();
            for (int j = 0; j < 10; j++) {
                if (cGUI.startStopEachItem[j] != null) {
                    int projectNumber = j + 1; // �v���W�F�N�g�ԍ���\�����߂ɂP�l�𑝉�
                    DataOfEachProject tmpPro = ClientMain.cDataToFile
                            .getDataOfEachByProNum(projectNumber);
                    cGUI.startStopEachItem[j].setText("STOP "
                            + tmpPro.svrInfo.nickname + "!"); // STOP�{�^���Ƀ`�F���W
                }
            }
        }
    }

    // ���ׂẴv���W�F�N�g�̃X���b�h���J�n����(�J�n���Ă��܂��Ă���X���b�h�������Ă����v)
    public void startAllThread() {

        int i = 0; // ���݉��ڂ̃v���W�F�N�g���̃J�E���^
        // �ȉ��Ńv���W�F�N�g���Ƃɏ����X���b�h���N�����A�^�u�֕`����s��
        ClientMain.cDataToFile.initSInfosIterator();
        while (ClientMain.cDataToFile.sInfosHasNext()) { // �v���W�F�N�g�f�[�^�̎��̗v�f���������
            i++; // �v���W�F�N�g���J�E���^�𑝉�������
            DataOfEachProject assignedPro = (DataOfEachProject) ClientMain.cDataToFile
                    .sInfosNext();

            startOneProThread(assignedPro); // �v���W�F�N�g�̃f�[�^��^���Ă��̃v���W�F�N�g�̃X���b�h���J�n
        }
    }

    // �^�����v���W�F�N�g�̃X���b�h�̏����X���b�h���J�n����i�N���X���[�h�Ȃǂ��s���Ă���)
    public void startOneProThread(DataOfEachProject assignedPro) {

        if (assignedPro.variousState == assignedPro.NOTSTART) {
            assignedPro.variousState = assignedPro.AFTERINIT;

            eachProPrepare(assignedPro);

            EachProThread eachThre = new EachProThread(assignedPro, this);

            Toolkit toolkit = Toolkit.getDefaultToolkit();
            Image img = toolkit.getImage("./" + assignedPro.svrInfo.nickname
                    + "/" + "ProjectPicture.jpg");

            try { // �����Ōv�Z�����̃N���X�������Ă���
                myloader.setPath("./ProjectFiles/"
                        + assignedPro.svrInfo.nickname + "/"); // ���O��path�ݒ�Y�ꂸ��
                eachThre.worker = (Culculater) myloader.loadClass(
                        assignedPro.svrInfo.nickname + "Worker").newInstance();
                myloader.loadClass(assignedPro.svrInfo.nickname + "Data");
                myloader.loadClass(assignedPro.svrInfo.nickname + "Memo");
                if (assignedPro.culMemo == null) { // �������񂪏���ŁAClinetMemo���ێ�����Ă��Ȃ��ꍇ
                    assignedPro.culMemo = (ClientMemo) (myloader
                            .loadClass(assignedPro.svrInfo.nickname + "Memo")
                            .newInstance());
                }
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }

            eachThre.start();
            ClientMain.cDataToFile.addProThreByIndex(
                    assignedPro.projectNumber - 1, eachThre); // �z��Ƃ��ďW�߂ĕێ����Ă���
        }
    }

    // �����ݒ�p�_�C�A���O��\������B���ۂɐݒ���s�����ꍇ�P�A�s���Ȃ������ꍇ�O��Ԃ�
    public boolean fillClientDat() {
        int okOrNo = JOptionPane.showConfirmDialog(cGUI,
                "�����ݒ���s���Ă悢�ł����H(���[�U�[��񂪃��Z�b�g����܂��j", "�m�F",
                JOptionPane.YES_NO_OPTION);
        if (okOrNo == JOptionPane.OK_OPTION) {

            ClientMain.cDataToFile = new CPreserveData(); // �����̓��e�����Z�b�g����

            String strTmp; // ��U�L�����ꂽ���e��ێ��iif���ł̗��p�̂��߁j
            int intTmp; // ��U�L�����ꂽ���̖߂�l��ێ�
            strTmp = JOptionPane.showInputDialog(cGUI, "���O���L�����ĉ�����", "�����ݒ�",
                    JOptionPane.PLAIN_MESSAGE);
            if (strTmp != null) { // ��������������Ă��Ȃ����
                ClientMain.cDataToFile.cInfo.participantNum = strTmp;
            } else {
                return false;
            }

            strTmp = JOptionPane.showInputDialog(cGUI, "��낵����΃��[���A�h���X���L�����ĉ�����",
                    "�����ݒ�", JOptionPane.PLAIN_MESSAGE);
            if (strTmp != null) { // ��������������Ă��Ȃ����
                ClientMain.cDataToFile.cInfo.mailAddress = strTmp;
            } else {
                return false;
            }

            intTmp = JOptionPane.showConfirmDialog(cGUI, "�T�[�o�ւ̎����ڑ��������܂����H",
                    "�m�F", JOptionPane.YES_NO_OPTION);
            if (intTmp == JOptionPane.YES_OPTION) {
                ClientMain.cDataToFile.sePolicy.autConnect = true;
            } else if (intTmp == JOptionPane.NO_OPTION) {
                ClientMain.cDataToFile.sePolicy.autConnect = false;
            }

            intTmp = JOptionPane.showConfirmDialog(cGUI, "�����v�Z�������܂����H", "�m�F",
                    JOptionPane.YES_NO_OPTION);
            if (intTmp == JOptionPane.YES_OPTION) {
                ClientMain.cDataToFile.sePolicy.autCulculate = true;
            } else if (intTmp == JOptionPane.NO_OPTION) {
                ClientMain.cDataToFile.sePolicy.autCulculate = false;
            }
            // ���O�������ƋL������Ă����
            if (ClientMain.cDataToFile.cInfo.participantNum != null) {
                // �����ƃV�X�e���^�C������_�C�W�F�X�g�𐶐����V�O�j�`���Ƃ���
                ClientMain.cDataToFile.cInfo.signiture = ClientMain.cDataToFile
                        .generateHash(String
                                .valueOf((long) (Math.random() * 100000000000000000L)));
                // client�A�v���P�[�V�������g���n�߂������L�^����
                ClientMain.cDataToFile.cInfo.startUseDate = Calendar
                        .getInstance();
                ClientMain.cDataToFile.alreadyInit = true;
                cDataToFile.preserveAll();
                cGUI.informChangeToComp(); // �t���O���ύX���ꂽ�̂ōĕ`��
                return true;
            }
        }
        return false;
    }

    // �V�K�v���W�F�N�g�Q���̃_�C�A���O��\������(���̒��Ŏ��ۂɐڑ����s���j
    public void fillNewPro() throws ConnectCanceledException {

        int okOrNo = JOptionPane.showConfirmDialog(cGUI, "�V�K�v���W�F�N�g�Q���̐ݒ�����܂����H",
                "�m�F", JOptionPane.YES_NO_OPTION);
        if (okOrNo == JOptionPane.OK_OPTION) {
            String fillAddress = JOptionPane.showInputDialog(cGUI,
                    "�V�����Q������v���W�F�N�g�̃A�h���X���L�����ĉ�����", "new Project",
                    JOptionPane.PLAIN_MESSAGE);
            if (fillAddress != null) {
                try {
                    DataOfEachProject tmpDataOfEach = new DataOfEachProject();
                    tmpDataOfEach.svrInfo.address = InetAddress
                            .getByName(fillAddress);

                    String strTmp = JOptionPane.showInputDialog(cGUI,
                            "�^�c�҂փ��b�Z�[�W������΋L�����ĉ�����", "new Project",
                            JOptionPane.PLAIN_MESSAGE);
                    if (strTmp != null) { // ��������������Ă��Ȃ����
                        tmpDataOfEach.messageToServer = strTmp;
                    } else {
                        tmpDataOfEach.messageToServer = "";
                    }

                    tmpDataOfEach.joinDate = Calendar.getInstance();

                    // ����̃v���W�F�N�g�ɎQ�����鎖�ɂȂ�Ȃ����`�F�b�N
                    boolean isExistSameProFlag = false; // ����̃v���W�F�N�g����������
                    ClientMain.cDataToFile.initSInfosIterator();
                    while (ClientMain.cDataToFile.sInfosHasNext()) {
                        DataOfEachProject tmp = ClientMain.cDataToFile
                                .sInfosNext();
                        if (tmp.svrInfo.address
                                .equals(tmpDataOfEach.svrInfo.address)) {
                            // �A�h���X����v����v���W�F�N�g���������ꍇ
                            Object[] msg = { "����̃v���W�F�N�g�֕�����Q���o�^���s�����͂ł��܂���" };
                            JOptionPane.showOptionDialog(cGUI, msg, "�x��",
                                    JOptionPane.PLAIN_MESSAGE, 0, null, null,
                                    null);
                            isExistSameProFlag = true;
                            break;
                        }
                    }

                    boolean alreadyFillFlag = false; // ���łɋK�萔�̃v���W�F�N�g�ɎQ�����Ă��܂��Ă��邩
                    if (ClientMain.cDataToFile.getProjectCount() >= 10) {
                        // �A�h���X����v����v���W�F�N�g���������ꍇ
                        Object[] msg = { "���Ȃ��͂����P�O�̃v���W�F�N�g�ɎQ�����Ă��܂��B����ȏ�͎Q���ł��܂���" };
                        JOptionPane.showOptionDialog(cGUI, msg, "�x��",
                                JOptionPane.PLAIN_MESSAGE, 0, null, null, null);
                        alreadyFillFlag = true;
                    }

                    if ((isExistSameProFlag == false)
                            && (alreadyFillFlag == false)) { // ����̃v���W�F�N�g�����݂����v���W�F�N�g�����K��ȓ��ł����
                        tmpDataOfEach = joinNewPro(tmpDataOfEach,
                                ClientMain.cDataToFile.cInfo);

                        boolean isExistSameNickname = false;
                        ClientMain.cDataToFile.initSInfosIterator();
                        // �����j�b�N�l�[���̃v���W�F�N�g���Ȃ�����������
                        while (ClientMain.cDataToFile.sInfosHasNext()) {
                            DataOfEachProject tmp = ClientMain.cDataToFile
                                    .sInfosNext();
                            // �j�b�N�l�[���̈�v����v���W�F�N�g���������ꍇ
                            if (tmp.svrInfo.nickname
                                    .equals(tmpDataOfEach.svrInfo.nickname)) {
                                Object[] msg = { "�c�O�Ȃ��瓯��̃j�b�N�l�[�����g�p����v���W�F�N�g�ւ͎Q���ł��܂���" };
                                JOptionPane.showOptionDialog(cGUI, msg, "�x��",
                                        JOptionPane.PLAIN_MESSAGE, 0, null,
                                        null, null);
                                isExistSameNickname = true;
                                break;
                            }
                        }

                        // ����̃j�b�N�l�[�������v���W�F�N�g�����݂��Ȃ����
                        if (isExistSameNickname == false) {
                            tmpDataOfEach.setPastJobDB(
                                    tmpDataOfEach.svrInfo.nickname,
                                    this.myloader); // pastJobDB�̕ۑ��ꏊ�Ȃǂ��Z�b�g����

                            tmpDataOfEach.projectNumber = ClientMain.cDataToFile
                                    .getProjectCount() + 1; // ���ڂ̃v���W�F�N�g�����L�^

                            ClientMain.cDataToFile.addDatOfEach(tmpDataOfEach);

                            Toolkit toolkit = Toolkit.getDefaultToolkit();
                            Image img = toolkit.getImage("./ProjectFiles/"
                                    + tmpDataOfEach.svrInfo.nickname + "/"
                                    + "ProjectPicture.jpg");

                            cGUI.addProjectTabMenu(ClientMain.cDataToFile
                                    .getProjectCount(), img, tmpDataOfEach);
                            ClientMain.cDataToFile.preserveAll(); // �V�v���W�F�N�g�ɎQ�������̂ŕۑ�
                            cGUI.informChangeToComp(); // �t�@�C�����̃t���O�ɂ���ĕ`��ύX

                            tmpDataOfEach.nextShouldDoThing = tmpDataOfEach.SHOULDRECIVEJOB; // ���������Ŏ��ɂ��ׂ������w�肵�Ă���
                        }
                    }

                } catch (UnknownHostException e1) {
                    Object[] msg = { "�A�h���X���Ԉ���Ă��邩�A���炩�̗��R�ŉ����ł��܂���" };
                    JOptionPane.showOptionDialog(cGUI, msg, "�x��",
                            JOptionPane.PLAIN_MESSAGE, 0, null, null, null);
                } catch (IOException e2) { // �\�P�b�g�֌W�̗�O�̏ꍇ
                    Object[] msg = { "�T�[�o����~���Ă��邩���炩�̗��R�ŃT�[�o�֐ڑ��ł��܂���ł���" };
                    JOptionPane.showOptionDialog(cGUI, msg, "���s",
                            JOptionPane.PLAIN_MESSAGE, 0, null, null, null);
                }
            }
        }
    }

    public static void main(String[] args) {
        ClientMain cMain = new ClientMain();
    }

    // �X���b�h�J�n���̂��߂̊e�폈�����s��
    public void startPrepare() {
        loadJarFileURLs();
        loadProjectFiles(); // �v���W�F�N�g����N���X�t�@�C���𓾂Ă���ꍇ���[�h���Ă���
        String proFilesPaths[] = getProFilePathsFromDir(); // �v���W�F�N�g�t�@�C����path������������Q�𓾂�
        CPreserveData tmp = ClientMain.cDataToFile.readOutAll(myloader,
                proFilesPaths);

        // �t�@�C������f�[�^��ǂݏo���đ������
        if (tmp != null) { // �ǂݏo���Ɏ��s����null���Ԃ��Ă��ĂȂ����
            ClientMain.cDataToFile = tmp;
            ClientMain.cDataToFile.initProthre(); // �V���A���C�Y����Ă��Ȃ��̂ŏ��������Ă���
            ClientMain.cDataToFile.initProThreIterator(); // Iterator�̓V���A���C�Y���Ă��Ȃ��̂ł����ő�����Ă���
            ClientMain.cDataToFile.initSInfosIterator();
        }

        // �����D��x��ύX
        if ((ClientMain.cDataToFile.priorityLevel > 0)
                && (ClientMain.cDataToFile.priorityLevel <= 10)) {
            setPriority(ClientMain.cDataToFile.priorityLevel);
        } else {
            ClientMain.cDataToFile.priorityLevel = ClientMain.cDataToFile.PRIORITYLAW;
        }

    }

    public void fin() { // �I�����̏���
        if (dataPreserver != null) { // ����ۑ��X���b�h�̒�~����
            dataPreserver.requestIntterupt();
            while (dataPreserverThread.isAlive() == true) {
                try {
                    Thread.sleep(50);
                    // �{���ɏI������܂őҋ@
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        ClientMain.cDataToFile.initProThreIterator();
        while (ClientMain.cDataToFile.proThreHasNext()) {
            EachProThread tmp = (EachProThread) ClientMain.cDataToFile
                    .proThreNext();
            tmp.requestStop();
            while (tmp.isAlive() == true) {
                try {
                    // �{���ɏI������܂Ŗ������[�v
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        // ����N�����̂��߂�state�����������Ă���
        ClientMain.cDataToFile.initSInfosIterator();
        while (ClientMain.cDataToFile.sInfosHasNext()) {
            DataOfEachProject tmp = ClientMain.cDataToFile.sInfosNext();
            tmp.variousState = tmp.NOTSTART;
        }

        ClientMain.cDataToFile.preserveAll(); // �t�@�C���ɕۑ�����
    }

    // �v���W�F�N�g�ւ̐V�K�Q�����s��(target�̓��e��ύX���ĕԂ��j
    // ���� target : �e�X�̃v���W�F�N�g�ւ�����i�Ώۃv���W�F�N�g�j myself : �N���C�A���g���M�̏��
    // �A�h���X�͐ݒ肵�Ă���target��n���悤�ɒ���
    // job�A�N���X���[�h�͕ʁB�����ł̓t�@�C���ƃT�[�o���󂯎��̂݁A��x�����Ăׂ΂悢
    public DataOfEachProject joinNewPro(DataOfEachProject target,
            ClientInfo myself) throws ConnectCanceledException, IOException {
        try {

            // �����ڑ���������Ă��Ȃ��ꍇ�A���̊m�F���s��
            if (ClientMain.cDataToFile.sePolicy.autConnect == false) {
                if (checkConnectable(target) == false) {
                    throw new ConnectCanceledException();
                }
            }

            InetAddress svrAddress = target.svrInfo.address; // �A�h���X�����o��

            // sock = ClientMain.makeSSLClientSocket(svrAddress, 2525);
            sock = ClientMain.makeSSLClientSocket(InetAddress
                    .getByAddress(svrAddress.getAddress()), 2525);

            out = new ObjectOutputStream(new DataOutputStream(sock
                    .getOutputStream()));
            ExchangeInfo sendInfo = new ExchangeInfo();
            sendInfo.initializeClient = true; // �N���C�A���g�̏������̂��߂̐ڑ����ƕ\��
            DataContainer sendContainer = new DataContainer();
            myself.tmpIncluded = target; // �ꎞ�I�ȕێ��X�y�[�X�ɉ������߂�
            sendContainer.setDeriverdCInfo(myself);
            out.writeObject(sendInfo);
            out.writeObject(sendContainer); // �N���C�A���g����̏��𑗐M
            out.flush();

            in = new MyObjectInputStream(new DataInputStream(sock
                    .getInputStream()), myloader);
            // �\�P�b�g�ł̃f�[�^�ǂݎ��p

            ExchangeInfo receiveExInfo = (ExchangeInfo) in.readObject();
            FileInformations fInfos = (FileInformations) in.readObject();

            DataContainer receiveContainer = (DataContainer) in.readObject();

            File newDir = new File(
                    "./ProjectFiles/"
                            + receiveContainer.getDeriverdCInfo().tmpIncluded.svrInfo.nickname);
            newDir.mkdir(); //
            extractFileToLocal(
                    fInfos,
                    in,
                    "./ProjectFiles/"
                            + receiveContainer.getDeriverdCInfo().tmpIncluded.svrInfo.nickname
                            + "/"); // �t�@�C�������ׂă��[�J���ɕۑ�
            
            loadJarFileURLs();     //jar��URL��S�Đݒ肵����
            
            myself.tmpIncluded = null; // �ꎞ�I�Ɏg�p���Ă����X�y�[�X���N���A�i�Q�ƌ���ύX���Ȃ����߁j
            sock.close();

            // �T�[�o�������o���ĕԂ�
            return (receiveContainer.getDeriverdCInfo()).tmpIncluded;

        } catch (ClassNotFoundException e) {
            // �����Ȃ��������̂Ƃ��ă��\�b�h���I������
            return null;
        } finally {
            if (sock != null) {
                sock.close();
            }
        }
    }

    // �w�肵���v���W�F�N�g�̒E�ޏ������s��
    public DataOfEachProject quitOneProject(DataOfEachProject target,
            ClientInfo myself) throws ConnectCanceledException, IOException {

        // �����ڑ���������Ă��Ȃ��ꍇ�A���̊m�F���s��
        if (ClientMain.cDataToFile.sePolicy.autConnect == false) {
            if (checkConnectable(target) == false) {
                throw new ConnectCanceledException();
            }
        }

        InetAddress svrAddress = target.svrInfo.address; // �A�h���X�����o��
        // sock = new Socket(svrAddress, 2525);
        try {
            // sock = ClientMain.makeSSLClientSocket(svrAddress, 2525);
            sock = ClientMain.makeSSLClientSocket(InetAddress
                    .getByAddress(svrAddress.getAddress()), 2525);

            out = new ObjectOutputStream(new DataOutputStream(sock
                    .getOutputStream()));
            ExchangeInfo sendInfo = new ExchangeInfo();
            sendInfo.quiteProject = true; // �v���W�F�N�g�E�ނ̂��߂̐ڑ����ƕ\��
            DataContainer sendContainer = new DataContainer();
            myself.tmpIncluded = target; // �ꎞ�I�ȕێ��X�y�[�X�ɉ������߂�
            sendContainer.setDeriverdCInfo(myself);
            out.writeObject(sendInfo);
            out.writeObject(sendContainer); // �N���C�A���g����̏��𑗐M
            out.flush();

            in = new MyObjectInputStream(new DataInputStream(sock
                    .getInputStream()), myloader);
            // �\�P�b�g�ł̃f�[�^�ǂݎ��p

            ExchangeInfo receiveExInfo = (ExchangeInfo) in.readObject();
            FileInformations fInfos = (FileInformations) in.readObject();

            DataContainer receiveContainer = (DataContainer) in.readObject();

            myself.tmpIncluded = null; // �ꎞ�I�Ɏg�p���Ă����X�y�[�X���N���A�i�Q�ƌ���ύX���Ȃ����߁j
            sock.close();

            // �T�[�o�������o���ĕԂ�
            return (receiveContainer.getDeriverdCInfo()).tmpIncluded;

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        } finally {
            sock.close();
        }
    }

    // ��̃I�u�W�F�N�g��M�Ƃ̏�����ύX���Ȃ��悤�ɒ���
    // �R���e�i�̒�����t�@�C���̏��𒊏o���āA�X�g���[�����烍�[�J���̃f�B���N�g���ɓW�J
    // "./XXX/"�Ƃ����`����path�̕������^����
    private void extractFileToLocal(FileInformations info, InputStream in,
            String path) {

        long fileLength = 0; // �ǂݍ��ރt�@�C����length
        long readedByte;
        for (int i = 0; (fileLength = info.fileLengths[i]) != -1; i++) {
            try {
                FileOutputStream out;
                if ((info.fileNames[i]
                        .substring(info.fileNames[i].length() - 3))
                        .equals("jar")) { // �g���q��jar�̏ꍇ
                    out = new FileOutputStream("./ProjectUsingJars/"
                            + info.fileNames[i]);
                } else {
                    out = new FileOutputStream(path + info.fileNames[i]);
                }
                readedByte = 0; // �ǂݍ��ݍς݂̃o�C�g��
                while (readedByte != fileLength) {
                    // �w�肳�ꂽ�����œǂݍ���
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

    // �v���W�F�N�g�t�@�C����ProjectFiles�̒����猟����load���郁�\�b�h
    private void loadProjectFiles() {
        File projectFilesDir = new File("./ProjectFiles");
        File eachProjectDir[] = projectFilesDir.listFiles();

        for (int i = 0; i < eachProjectDir.length; i++) { // �f�B���N�g���̒��̃t�@�C����T�����A���[�h����
            File eachClassFileName[] = eachProjectDir[i]
                    .listFiles(new FileFilter() { // �g���q��class�̃t�@�C����list�𓾂�
                        public boolean accept(File pathname) {
                            String name = pathname.getName();
                            String fiveTale = name.substring(name.length() - 5); // ��납��T������؂�o��
                            if ((fiveTale.equals("class") == true)) { // �g���q��class�ł����
                                return true;
                            } else {
                                return false;
                            }
                        }
                    });

            for (int j = 0; j < eachClassFileName.length; j++) {
                try {
                    myloader.setPath("./ProjectFiles/"
                            + eachProjectDir[i].getName() + "/"); // ���O��path�ݒ�Y�ꂸ��
                    myloader
                            .loadClass(eachClassFileName[j].getName()
                                    .substring(
                                            0,
                                            eachClassFileName[j].getName()
                                                    .length() - 6)); // �N���X�t�@�C�������[�h
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // �N���X���[�_�[��jarFile��path��F��������
    //�ȑO�̓��e�͎̂ĂāA�S�Đݒ肵����
    private void loadJarFileURLs() {
        File jarURLs[] = (new File("./ProjectUsingJars"))
                .listFiles(new FileFilter() { // �g���q��class�̃t�@�C����list�𓾂�
                    public boolean accept(File pathname) {
                        String name = pathname.getName();
                        String fiveTale = name.substring(name.length() - 3); // ��납��3������؂�o��
                        if ((fiveTale.equals("jar") == true)) { // �g���q��class�ł����
                            return true;
                        } else {
                            return false;
                        }
                    }
                });
        
        myloader.initURL();
        //�N���X���[�_�[�ɐݒ�
        if(jarURLs!=null){
            for(int i=0;i<jarURLs.length;i++){
                try {
                    myloader.addURL(jarURLs[i].toURL());
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // �^����ꂽ�v���W�F�N�g�̃f�B���N�g���[���܂邲�Ə�������
    public void removeProjectFiles(DataOfEachProject pro) {
        String path = "./ProjectFiles/" + pro.svrInfo.nickname;
        File targetDir = new File(path);
        File includedFiles[] = targetDir.listFiles();

        // �Ώۂ̃f�B���N�g�����̃t�@�C����S�ď���
        for (int i = 0; i < includedFiles.length; i++) {
            includedFiles[i].delete();
        }

        targetDir.delete();
    }

    // ProjectFiles�f�B���N�g�����̂��ׂẴf�B���N�g���E�t�@�C�����폜����
    public void removeAllProjectFiles() {
        String path = "./ProjectFiles";
        File targetDir = new File(path);
        File eachProjectDirs[] = targetDir.listFiles();

        // �f�B���N�g�����̃t�@�C����S�ď���
        for (int i = 0; i < eachProjectDirs.length; i++) {
            File eachProjectFiles[] = eachProjectDirs[i].listFiles();

            for (int j = 0; j < eachProjectFiles.length; j++) {
                eachProjectFiles[j].delete();
            }
            eachProjectDirs[i].delete(); // �f�B���N�g���{�̂��폜
        }
    }

    // �f�B���N�g���̍\�����炻�ꂼ��̃v���W�F�N�g�̃t�@�C��������f�B���N�g����path�����𓾂�
    private String[] getProFilePathsFromDir() {
        File projectFilesDir = new File("./ProjectFiles");
        String eachProjectDir[] = projectFilesDir.list();

        for (int i = 0; i < eachProjectDir.length; i++) {
            eachProjectDir[i] = "./ProjectFiles/" + eachProjectDir[i];
        }

        return eachProjectDir;
    }

    static synchronized SSLSocket makeSSLClientSocket(InetAddress server,
            int portno) throws IOException {

        try {
            SSLSocket ss;
            SSLSocketFactory ssf = (SSLSocketFactory) SSLSocketFactory
                    .getDefault();
            System.out.println(server.getHostAddress() + "�ւ̃R�l�N�V�����𒣂�悤���݂܂�");
            Socket s = new Socket(server, portno);

            ss = (SSLSocket) ssf.createSocket(s, server.getHostAddress(),
                    portno, true);
            String cipherSuites[] = { "SSL_DH_anon_EXPORT_WITH_DES40_CBC_SHA" };
            ss.setEnabledCipherSuites(cipherSuites);

            return (ss);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("��O�����makeSSLClientScoket�ł̃A�h���X="
                    + server.getHostAddress());
            throw e;
        }
    }

    // �������J�n���Ă���v���W�F�N�g�̏����𒆎~������i�J�n���Ă��Ȃ��Ă��n�j�Ƃ���j
    public void stopOneThread(DataOfEachProject stopped) {

        // �������J�n���Ă���ꍇ
        if (stopped.variousState != stopped.NOTSTART) {
            EachProThread tmp = ClientMain.cDataToFile.getProThreByPro(stopped);
            tmp.requestStop();
            while (tmp.isAlive() == true) {
                try {
                    // �{���ɏI������܂Ŗ������[�v
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            stopped.variousState = stopped.NOTSTART; // ���J�n��Ԃ��ƕ\��
            ClientMain.cDataToFile.addDatOfEach(stopped);
        }
    }

    // ���[�U�[�ɑ΂��ăT�[�o�ւ̐ڑ����s���Ă悢���m�F����B�i�����ڑ���������Ă��Ȃ��ꍇ�j
    public boolean checkConnectable(DataOfEachProject project) {
        int okOrNo = JOptionPane
                .showConfirmDialog(
                        cGUI,
                        ((project.svrInfo.projectNum != null) ? (project.svrInfo.projectNum)
                                : ("�V�K"))
                                + "�̃T�[�o("
                                + project.svrInfo.address.getHostAddress()
                                + ")�ւ̐ڑ��������܂����H", "�ڑ�����",
                        JOptionPane.YES_NO_OPTION);

        if (okOrNo == JOptionPane.OK_OPTION) {
            return true;
        } else {
            return false;
        }
    }

    // �S�Ẵv���W�F�N�g�X���b�h���I������(�J�n���Ă��Ȃ��v���W�F�N�g�������Ă����v�j
    public void stopAllThread() {

        ClientMain.cDataToFile.initSInfosIterator();
        while (ClientMain.cDataToFile.sInfosHasNext()) { // �����I�����Ă���
            stopOneThread(ClientMain.cDataToFile.sInfosNext());
        }
        ClientMain.cDataToFile.initProthre();
    }

    // ���C���X���b�h(�Ƃ���ɌĂяo���ꂽ�X���b�h�j�̗D��x��ύX���鎖�ŃA�v���P�[�V�����̗D��x��ύX����
    public void setPriority(int level) {
        Thread nowThread = Thread.currentThread();
        nowThread.setPriority(level);

        ClientMain.cDataToFile.initProThreIterator();
        while (ClientMain.cDataToFile.proThreHasNext()) {
            Thread tmp = ClientMain.cDataToFile.proThreNext();
            tmp.setPriority(level);
        }
    }

    // �e�v���W�F�N�g�̃X���b�h���J�n���鎞�ɂ����Ȃ��������Ȃǂ̏���
    public void eachProPrepare(DataOfEachProject proData) {
        if (proData.pastJobDB != null) {
            proData.resetLoader(this.myloader);
        }
    }
}