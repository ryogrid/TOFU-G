/* ************************************************************** *
 *                                                                *
 * Copyright (c) 2005, Ryo Kanbayashi, All rights reserved.       *
 *                                                                *
 *                                                                *
 * This software is distributed under the modified BSD License.   *
 * ************************************************************** */

import java.awt.*;
import java.awt.Button;
import java.awt.Dialog;
import java.awt.event.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComboBox;

//�T�[�o�ւ̃��b�Z�[�W��ύX���邽�߂�Dialog Box
public class FillMessageDialog extends Dialog implements ActionListener,WindowListener {
	
	private ClientMain ancestor = null;
	private JComboBox proSelectBox = new JComboBox();
	private Button okButton = new Button("OK");
	private Button cancelButton = new Button("Cancel");
	private TextField tf = new TextField(30);
	private DataOfEachProject temporaryHave[] = new DataOfEachProject[10];
	private DataOfEachProject nowSelected = null;
	
	public FillMessageDialog(ClientMain ancestor,Frame f,String title,boolean model){
		super(f,title,model);
		this.ancestor = ancestor;
		setSize(500,60);
		setLayout(new FlowLayout());
		
		add(proSelectBox);
		proSelectBox.addActionListener(this);
		proSelectBox.setPreferredSize(new Dimension(150,20));
		
		ClientMain.cDataToFile.initSInfosIterator();
		//�R���{�{�b�N�X�ɑS�Ẵv���W�F�N�g����ǉ�����
		int i = 0;      //�z��̓Y�����p�̃J�E���^
		while(ClientMain.cDataToFile.sInfosHasNext()){
			 DataOfEachProject tmp =ClientMain.cDataToFile.sInfosNext();
			 proSelectBox.addItem(tmp.svrInfo.projectNum);    //�R���{�{�b�N�X�ɒǉ�
			 temporaryHave[i] = tmp;       //��Ɠ��l�̏����Ńv���W�F�N�g�̃f�[�^��ێ����Ă���
			 i++;
		}
		
		nowSelected = temporaryHave[0];      //�Ƃ肠�����ŏ��̗v�f��ݒ肵�Ă���
		tf.setText(nowSelected.messageToServer);
		
		add(tf);
		add(okButton);
		okButton.addActionListener(this);
		add(cancelButton);
		cancelButton.addActionListener(this);
		this.addWindowListener(this);
		setVisible(true);
	}
	
	public void actionPerformed(ActionEvent e) {
		if(e.getSource()== this.okButton){
			nowSelected.messageToServer = tf.getText();     //TextBox�̓��e��ۑ�
			if (nowSelected.variousState != nowSelected.NOTSTART) {        //�����X���b�h���J�n���Ă���ꍇ
				EachProThread tmp = ClientMain.cDataToFile.getProThreByPro(nowSelected);
				(tmp.getHavinProject()).messageToServer = tf.getText(); 
			}
			
			setVisible(false);
			ClientMain.cDataToFile.preserveAll();
		}else if(e.getSource()== this.cancelButton){
			setVisible(false);        //���������ɏI��
		}else if(e.getSource()== proSelectBox){   //�v���W�F�N�g�����I�����ꂽ�ꍇ
			if(nowSelected!=null){//�C���X�^���X�쐬���ȊO�̏ꍇ
				int selectedIndex = proSelectBox.getSelectedIndex();
				nowSelected =temporaryHave[selectedIndex];
				tf.setText(nowSelected.messageToServer);
			}
		}
	}
	
	public void windowActivated(WindowEvent e) {}
	public void windowClosed(WindowEvent e) {}
	public void windowDeactivated(WindowEvent e) {}
	public void windowDeiconified(WindowEvent e) {}
	public void windowIconified(WindowEvent e) {}
	public void windowOpened(WindowEvent e) {}
	
	public void windowClosing(WindowEvent e) {
		setVisible(false);
	}
}
