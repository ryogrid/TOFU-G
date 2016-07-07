/* ************************************************************** *
 *                                                                *
 * Copyright (c) 2005, Ryo Kanbayashi, All rights reserved.       *
 *                                                                *
 *                                                                *
 * This software is distributed under the modified BSD License.   *
 * ************************************************************** */

import java.awt.event.*;
import java.io.IOException;

import javax.swing.JOptionPane;

class CWindowEvents extends WindowAdapter implements ActionListener,
		ItemListener {
	public ClientMain ancestor = null;

	public CWindowEvents(ClientMain parent) {
		ancestor = parent;
	}

	public void windowClosing(WindowEvent e) {
		ancestor.fin();
		System.exit(0);
	}

	public void actionPerformed(ActionEvent e) {

		try {
			
			if ((e.getSource()) == ancestor.cGUI.initItem) { //�����ݒ�̃{�^�����������ꍇ
			    int okOrNo = JOptionPane.showConfirmDialog(ancestor.cGUI,
						"�S�f�[�^�����Z�b�g����܂����A�i�߂܂����H ���L����I�����܂�", "�m�F",
						JOptionPane.YES_NO_OPTION);
				if (okOrNo == JOptionPane.OK_OPTION) {
				    ancestor.stopAllThread();   //�Ƃ肠�����A���ׂĂ̏����X���b�h���~����
				    for (int j = 0; j < 10; j++) {  //�f�t�h�ɂ��ύX��`����
						if (ancestor.cGUI.startStopEachItem[j] != null) {
							int projectNumber = j + 1; //�v���W�F�N�g�ԍ���\�����߂ɂP�l�𑝉�
							DataOfEachProject tmpPro = ClientMain.cDataToFile
									.getDataOfEachByProNum(projectNumber);
							ancestor.cGUI.startStopEachItem[j].setText("START " + tmpPro.svrInfo.nickname + "!"); //START�{�^���Ƀ`�F���W
						}
					}
				    
				    ancestor.dataPreserver.requestIntterupt();   //�f�[�^�ۑ��X���b�h����~
				    while(ancestor.dataPreserverThread.isAlive() == true){  //�f�[�^�ۑ��X���b�h���I������܂őҋ@
				        try {
                            Thread.sleep(50);
                        } catch (InterruptedException e2) {
                            e2.printStackTrace();
                        }
				    }
				    
				    boolean isFilled = ancestor.fillClientDat(); //�����ݒ�_�C�A���O��\��
				    if(isFilled == true){ //�L�������������ꍇ
				        ancestor.removeAllProjectFiles();
				        System.exit(0);
				    }
				}
			} else if (e.getSource() == ancestor.cGUI.newProItem) { //new project
				// �������ݎ�
				ancestor.fillNewPro(); //new project �_�C�A���O��\��
			} else if ((e.getSource()) == ancestor.cGUI.startAllItem) { //GO AllProject�{�^�����������ꍇ
				ancestor.startAllThread();
				for (int j = 0; j < 10; j++) {
					if (ancestor.cGUI.startStopEachItem[j] != null) {
						int projectNumber = j + 1; //�v���W�F�N�g�ԍ���\�����߂ɂP�l�𑝉�
						DataOfEachProject tmpPro = ClientMain.cDataToFile
								.getDataOfEachByProNum(projectNumber);
						ancestor.cGUI.startStopEachItem[j].setText("STOP " + tmpPro.svrInfo.nickname + "!"); //STOP�{�^���Ƀ`�F���W
					}
				}
			} else if((e.getSource()== ancestor.cGUI.stopAllItem)){   //STOP AllProject�{�^�����������ꍇ
				ancestor.stopAllThread();
				for (int j = 0; j < 10; j++) {
					if (ancestor.cGUI.startStopEachItem[j] != null) {
						int projectNumber = j + 1; //�v���W�F�N�g�ԍ���\�����߂ɂP�l�𑝉�
						DataOfEachProject tmpPro = ClientMain.cDataToFile
								.getDataOfEachByProNum(projectNumber);
						ancestor.cGUI.startStopEachItem[j].setText("START " + tmpPro.svrInfo.nickname + "!"); //START�{�^���Ƀ`�F���W
					}
				}
			} else if (isElement(ancestor.cGUI.quitProItem, e.getSource()) != -1) { //�C�x���g�\�[�X��findQuitProItem�̗v�f�ł���ꍇ
					//�C�x���g�\�[�X��quiteProItem�̗v�f�̂ǂꂩ�������ꍇ
					//���̃u���b�N�ł��ꂼ��̃v���W�F�N�g����E�ނ��鏈��������
				int okOrNo = JOptionPane.showConfirmDialog(ancestor.cGUI,
						"�v���W�F�N�g����E�ނ��܂����H�@���o�^����������܂�", "�m�F",
						JOptionPane.YES_NO_OPTION);
				
				if (okOrNo == JOptionPane.OK_OPTION) {
					try {
						int index = isElement(ancestor.cGUI.quitProItem, e.getSource());
						DataOfEachProject tmp = ClientMain.cDataToFile
								.getDataOfEachByProNum(index + 1);
						ancestor.stopOneThread(tmp); //���łɏ������J�n���Ă��܂��Ă���ꍇ�͒�~
						ancestor.quitOneProject(tmp, ClientMain.cDataToFile.cInfo); //�E�ޏ������T�[�o�ɑ΂��čs��
						ancestor.cGUI.removeProjectTab(tmp.projectNumber); //�v���W�F�N�g�̃^�u����菜��
						ancestor.cGUI.projectMenu
								.remove(ancestor.cGUI.quitProItem[tmp.projectNumber - 1]);
						ancestor.cGUI.quitProItem[tmp.projectNumber - 1] = null;
						ancestor.cGUI.processingMenu
								.remove(ancestor.cGUI.startStopEachItem[tmp.projectNumber - 1]);
						ancestor.cGUI.startStopEachItem[tmp.projectNumber - 1] = null;
						ClientMain.cDataToFile.delDataOfEach(tmp.svrInfo.signiture); //�v���W�F�N�g�f�[�^����菜��
						ancestor.removeProjectFiles(tmp); //�v���W�F�N�g�̃t�@�C�����܂ރf�B���N�g��������

						ClientMain.cDataToFile.preserveAll();
					} catch (IOException e2) {
						//�ڑ��Ɏ��s�����ꍇ�̏���
						Object[] msg = { "�T�[�o����~���Ă��邩���炩�̗��R�ŃT�[�o�֐ڑ��ł��܂���ł���" };
						JOptionPane.showOptionDialog(ancestor.cGUI, msg, "���s",
								JOptionPane.PLAIN_MESSAGE, 0, null, null, null);
					}
				}
				
			} else if ((isElement(ancestor.cGUI.startStopEachItem, e.getSource())) != -1) {
				//�C�x���g�\�[�X��startStopEachItem�̗v�f�̂ǂꂩ�������ꍇ
				//���̃u���b�N�ł��ꂼ��̃v���W�F�N�g�̏������X�^�[�gor�X�g�b�v����
				
				int index = isElement(ancestor.cGUI.startStopEachItem, e.getSource());
				int projectNumber = index + 1; //�v���W�F�N�g�ԍ���\�����߂ɂP�l�𑝉�
				DataOfEachProject tmpPro = ClientMain.cDataToFile
						.getDataOfEachByProNum(projectNumber);
				
				//�v���W�F�N�g�����J�n�̏ꍇStart�{�^���Ƃ��ĉ����ꂽ�Ɣ��f
				if(tmpPro.variousState == tmpPro.NOTSTART){
					ancestor.startOneProThread(tmpPro);
					ancestor.cGUI.startStopEachItem[index].setText("STOP " + tmpPro.svrInfo.nickname + "!"); //STOP�{�^���Ƀ`�F���W
				}else{     //�v���W�F�N�g���J�n���Ă���ꍇstop�{�^���Ƃ��ĉ����ꂽ�Ɣ��f
					ancestor.stopOneThread(tmpPro);
					ClientMain.cDataToFile.removeProThre(tmpPro.projectNumber-1);  //�����X���b�h����菜��
					ancestor.cGUI.startStopEachItem[index].setText("START " + tmpPro.svrInfo.nickname + "!"); //START�{�^���Ƀ`�F���W
				}
			}else if((e.getSource()) == ancestor.cGUI.messageItem){    //���b�Z�[�W�ύX���������܂ꂽ�ꍇ
				changeMessage();	//���b�Z�[�W�ύX�̂��߂̃_�C�A���O��\������
			}
		} catch (ConnectCanceledException e1) {       //�ڑ���������Ȃ������ꍇ
			//���������Ƀ��\�b�h���I������
		}
	}

	public void itemStateChanged(ItemEvent e) {

		//��ԕ\���̕\���A��\����ύX����
		if ((e.getSource().equals(ancestor.cGUI.stateShowItem) == true)) {
			if (e.getStateChange() == ItemEvent.SELECTED) {
				for (int i = 0; i < 10; i++) {
					if (ancestor.cGUI.useForAttach[i] != null) {
						ancestor.cGUI.jobLogPane[i].setVisible(false);
						ancestor.cGUI.proInfoPane[i].setVisible(false);
						ancestor.cGUI.sp[i].setVisible(false);
					} else {
						break;
					}
				}
			} else if (e.getStateChange() == ItemEvent.DESELECTED) {
				for (int i = 0; i < 10; i++) {
					if (ancestor.cGUI.useForAttach[i] != null) {
						ancestor.cGUI.jobLogPane[i].setVisible(true);
						ancestor.cGUI.proInfoPane[i].setVisible(true);
						ancestor.cGUI.sp[i].setVisible(true);
					} else {
						break;
					}
				}
			}
		} else if (e.getSource().equals(ancestor.cGUI.autoCulculate)) { //�����v�Z�̃`�F�b�N���ύX���ꂽ�ꍇ
			if (e.getStateChange() == ItemEvent.SELECTED) {
				ClientMain.cDataToFile.sePolicy.autCulculate = true;
			} else if (e.getStateChange() == ItemEvent.DESELECTED) {
				ClientMain.cDataToFile.sePolicy.autCulculate = false;
			}
		} else if (e.getSource().equals(ancestor.cGUI.autoConnect)) { //�����ڑ��̃`�F�b�N���ύX���ꂽ�ꍇ
			if (e.getStateChange() == ItemEvent.SELECTED) {
				ClientMain.cDataToFile.sePolicy.autConnect = true;
			} else if (e.getStateChange() == ItemEvent.DESELECTED) {
				ClientMain.cDataToFile.sePolicy.autConnect = false;
			}
		}else if(e.getSource().equals(ancestor.cGUI.highPriorityItem)){      //�D��x�̕ύX�̏ꍇ
			if (e.getStateChange() == ItemEvent.SELECTED) {
				ancestor.cGUI.highPriorityItem.setSelected(true);
				ancestor.cGUI.middlePriorityItem.setSelected(false);
				ancestor.cGUI.lowPriorityItem.setSelected(false);
				ancestor.setPriority(8);
				
				ClientMain.cDataToFile.priorityLevel = ClientMain.cDataToFile.PRIORITYHIGH;
				ClientMain.cDataToFile.preserveAll();
			}
		}else if(e.getSource().equals(ancestor.cGUI.middlePriorityItem)){
			if (e.getStateChange() == ItemEvent.SELECTED) {
				ancestor.cGUI.highPriorityItem.setSelected(false);
				ancestor.cGUI.middlePriorityItem.setSelected(true);
				ancestor.cGUI.lowPriorityItem.setSelected(false);
				ancestor.setPriority(5);
				
				ClientMain.cDataToFile.priorityLevel = ClientMain.cDataToFile.PRIORITYMIDDLE;
				ClientMain.cDataToFile.preserveAll();
			}
			
		}else if(e.getSource().equals(ancestor.cGUI.lowPriorityItem)){
			if (e.getStateChange() == ItemEvent.SELECTED) {
				ancestor.cGUI.highPriorityItem.setSelected(false);
				ancestor.cGUI.middlePriorityItem.setSelected(false);
				ancestor.cGUI.lowPriorityItem.setSelected(true);
				ancestor.setPriority(2);
				
				ClientMain.cDataToFile.priorityLevel = ClientMain.cDataToFile.PRIORITYLAW;
				ClientMain.cDataToFile.preserveAll();
			}
		}
	}

	//�^�����v�f���^�����z��̗v�f�ł��邩��Ԃ��B�܂܂�Ă����ꍇ�͂���index�A�܂܂�Ă��Ȃ��ꍇ��-1��Ԃ�
	public int isElement(Object[] array, Object obj) {
		for (int i = 0; i < array.length; i++) {
			if (array[i] != null) {
				if (array[i].equals(obj) == true) {
					return i;
				}
			}
		}
		return -1;
	}
	
	
	private void changeMessage(){
		int okOrNo = JOptionPane.showConfirmDialog(ancestor.cGUI,
				"���b�Z�[�W�̕ύX���s���܂����H�@������ڑ����ɑ��M����܂�", "�m�F",
				JOptionPane.YES_NO_OPTION);
		if (okOrNo == JOptionPane.OK_OPTION) {
			FillMessageDialog fillDialog = new FillMessageDialog(ancestor,ancestor.cGUI,"���b�Z�[�W�ύX",true);
		}
	}
}