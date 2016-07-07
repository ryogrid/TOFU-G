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

class SWindowEvents extends WindowAdapter implements ActionListener {

    /**
     * 
     * @uml.property name="ancestor"
     * @uml.associationEnd multiplicity="(1 1)"
     */
    public ServerMain ancestor = null; //���C���N���X�̎Q��

	
	public SWindowEvents(ServerMain parent){
		ancestor = parent;
	}
	
	public void windowClosing(WindowEvent e) {
		ancestor.fin();
		System.exit(0);
	}
	
	public void actionPerformed(ActionEvent e) {
		
		if(e.getSource() == ancestor.sGUI.initItem){
		    
		    int okOrNo = JOptionPane.showConfirmDialog(ancestor.sGUI,
					"�S�f�[�^�����Z�b�g����܂����A�i�߂܂����H ���L����I�����܂�", "�m�F",
					JOptionPane.YES_NO_OPTION);
			
			if (okOrNo == JOptionPane.OK_OPTION) {	
			    if(ancestor.requestManager != null){
				    ancestor.requestManager.requestStop();
					while (ancestor.requestManagerThread.isAlive() == true) {
						try {
							Thread.sleep(50);
							//�{���ɏI������܂őҋ@
						} catch (InterruptedException e2) {
							e2.printStackTrace();
						}
					}
					ancestor.requestManager = null;
					ancestor.sGUI.startOrStopServerItem.setText("�T�[�o�J�n");
			    }
				
			    if(ancestor.dataPreserver != null){
				    ancestor.dataPreserver.requestIntterupt();   //�f�[�^�ۑ��X���b�h����~
				    while(ancestor.dataPreserverThread.isAlive() == true){  //�f�[�^�ۑ��X���b�h���I������܂őҋ@
				        try {
	                        Thread.sleep(50);
	                    } catch (InterruptedException e2) {
	                        e2.printStackTrace();
	                    }
				    }
				}
			    
			    if (ancestor.reDisChecker != null) {    //�Ĕz�z�`�F�b�N�X���b�h�̒�~����
					ancestor.reDisChecker.requestIntterupt();
					while (ancestor.reDisCheckerThread.isAlive() == true) {
						try {
							Thread.sleep(50);
							//�{���ɏI������܂őҋ@
						} catch (InterruptedException e3) {
							e3.printStackTrace();
						}
					}
				}
			    
			    boolean isFilled = ancestor.fillServerDat();
			    if(isFilled == true){
			        System.exit(0);
			    }
			}    
			
		}else if((e.getSource() == ancestor.sGUI.startOrStopServerItem)&&(ancestor.requestManager == null)){	//�����J�n�{�^���������ꂽ�ꍇ
		    ancestor.requestManager = new RequestManager(ancestor.svsock,ancestor);
			ancestor.requestManagerThread = new Thread(ancestor.requestManager);
			ancestor.requestManagerThread.start();
			
			ancestor.reDisChecker = new ReDistributeChecker(ServerMain.sDataToFile.reDistributeDays,ServerMain.sDataToFile.reDistributecheckMin);    //���̃u���b�N�ōĔz�z�`�F�b�N�X���b�h���J�n
			ancestor.reDisCheckerThread = new Thread(ancestor.reDisChecker);
			ancestor.reDisCheckerThread.start();
			
			ancestor.dataPreserver = new DataPreserver(1);   //�P���Ԋu�ŕۑ����s�킹��
			ancestor.dataPreserverThread = new Thread(ancestor.dataPreserver);
			ancestor.dataPreserverThread.start();
			
			ancestor.sGUI.startOrStopServerItem.setText("�T�[�o��~");
		}else if((e.getSource() == ancestor.sGUI.startOrStopServerItem)&&(ancestor.requestManager != null)){	//�����I���{�^���������ꂽ�ꍇ
			ancestor.requestManager.requestStop();
			while (ancestor.requestManagerThread.isAlive() == true) {
				try {
					Thread.sleep(50);
					//�{���ɏI������܂őҋ@
				} catch (InterruptedException e2) {
					e2.printStackTrace();
				}
			}
			ServerMain.sDataToFile.preserveAll();
			ancestor.requestManager = null;
			ancestor.sGUI.startOrStopServerItem.setText("�T�[�o�J�n");
		}else if((e.getSource() == ancestor.sGUI.redundancyItem)){     //�璷���̒l�̕ύX�p�A�C�e���̏ꍇ
			int okOrNo = JOptionPane.showConfirmDialog(ancestor.sGUI,
					"�z�zjob���̕ύX���s���܂����H", "�m�F", JOptionPane.YES_NO_OPTION);
			if (okOrNo == JOptionPane.OK_OPTION) {
				String strTmp; //��U�L�����ꂽ���e��ێ��iif���ł̗��p�̂��߁j
				strTmp = (String) JOptionPane.showInputDialog(ancestor.sGUI,
						"�����̌v�Z���ʂ��W�܂邱�Ƃɂ����job�̌��ʂ��m�肵�܂����H", "�z�zjob���ݒ�",
						JOptionPane.PLAIN_MESSAGE,null,null,String.valueOf(ServerMain.sDataToFile.jManager.getRedundancyCount()));
				if (strTmp != null) { //��������������Ă��Ȃ����(�㏑���h�~)
					ServerMain.sDataToFile.jManager.setRedundancyCount(Integer.parseInt(strTmp));
				}
			}
		}else if((e.getSource()) == ancestor.sGUI.messageItem){    //���b�Z�[�W�ύX���������܂ꂽ�ꍇ
			changeMessage();	//���b�Z�[�W�ύX�̂��߂̃_�C�A���O��\������
		}
	}
	
	private void changeMessage(){
		int okOrNo = JOptionPane.showConfirmDialog(ancestor.sGUI,
				"���b�Z�[�W�̕ύX���s���܂����H�@���ύX�����f�����̂͂���ȍ~�ɐڑ����Ă����N���C�A���g�ɑ΂��Ăł�", "�m�F",
				JOptionPane.YES_NO_OPTION);
		if (okOrNo == JOptionPane.OK_OPTION) {
			String strTmp =JOptionPane.showInputDialog(ancestor.sGUI,
					"�Q���҂ւ̃��b�Z�[�W���L�����ĉ�����",
					"���b�Z�[�W�ύX",JOptionPane.PLAIN_MESSAGE);
			if (strTmp != null) { //��������������Ă��Ȃ����(�㏑���h�~)
				ServerMain.sDataToFile.messageToClients = strTmp;
				ServerMain.sDataToFile.preserveAll();
			}
		}
	}
}