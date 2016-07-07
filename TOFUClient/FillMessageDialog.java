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

//サーバへのメッセージを変更するためのDialog Box
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
		//コンボボックスに全てのプロジェクト名を追加する
		int i = 0;      //配列の添え字用のカウンタ
		while(ClientMain.cDataToFile.sInfosHasNext()){
			 DataOfEachProject tmp =ClientMain.cDataToFile.sInfosNext();
			 proSelectBox.addItem(tmp.svrInfo.projectNum);    //コンボボックスに追加
			 temporaryHave[i] = tmp;       //上と同様の順序でプロジェクトのデータを保持しておく
			 i++;
		}
		
		nowSelected = temporaryHave[0];      //とりあえず最初の要素を設定しておく
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
			nowSelected.messageToServer = tf.getText();     //TextBoxの内容を保存
			if (nowSelected.variousState != nowSelected.NOTSTART) {        //処理スレッドが開始している場合
				EachProThread tmp = ClientMain.cDataToFile.getProThreByPro(nowSelected);
				(tmp.getHavinProject()).messageToServer = tf.getText(); 
			}
			
			setVisible(false);
			ClientMain.cDataToFile.preserveAll();
		}else if(e.getSource()== this.cancelButton){
			setVisible(false);        //何もせずに終了
		}else if(e.getSource()== proSelectBox){   //プロジェクト名が選択された場合
			if(nowSelected!=null){//インスタンス作成時以外の場合
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
