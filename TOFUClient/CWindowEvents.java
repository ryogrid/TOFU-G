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
			
			if ((e.getSource()) == ancestor.cGUI.initItem) { //初期設定のボタンを押した場合
			    int okOrNo = JOptionPane.showConfirmDialog(ancestor.cGUI,
						"全データがリセットされますが、進めますか？ ※記入後終了します", "確認",
						JOptionPane.YES_NO_OPTION);
				if (okOrNo == JOptionPane.OK_OPTION) {
				    ancestor.stopAllThread();   //とりあえず、すべての処理スレッドを停止する
				    for (int j = 0; j < 10; j++) {  //ＧＵＩにも変更を伝える
						if (ancestor.cGUI.startStopEachItem[j] != null) {
							int projectNumber = j + 1; //プロジェクト番号を表すために１つ値を増加
							DataOfEachProject tmpPro = ClientMain.cDataToFile
									.getDataOfEachByProNum(projectNumber);
							ancestor.cGUI.startStopEachItem[j].setText("START " + tmpPro.svrInfo.nickname + "!"); //STARTボタンにチェンジ
						}
					}
				    
				    ancestor.dataPreserver.requestIntterupt();   //データ保存スレッドも停止
				    while(ancestor.dataPreserverThread.isAlive() == true){  //データ保存スレッドが終了するまで待機
				        try {
                            Thread.sleep(50);
                        } catch (InterruptedException e2) {
                            e2.printStackTrace();
                        }
				    }
				    
				    boolean isFilled = ancestor.fillClientDat(); //初期設定ダイアログを表示
				    if(isFilled == true){ //記入が成功した場合
				        ancestor.removeAllProjectFiles();
				        System.exit(0);
				    }
				}
			} else if (e.getSource() == ancestor.cGUI.newProItem) { //new project
				// 押し込み時
				ancestor.fillNewPro(); //new project ダイアログを表示
			} else if ((e.getSource()) == ancestor.cGUI.startAllItem) { //GO AllProjectボタンを押した場合
				ancestor.startAllThread();
				for (int j = 0; j < 10; j++) {
					if (ancestor.cGUI.startStopEachItem[j] != null) {
						int projectNumber = j + 1; //プロジェクト番号を表すために１つ値を増加
						DataOfEachProject tmpPro = ClientMain.cDataToFile
								.getDataOfEachByProNum(projectNumber);
						ancestor.cGUI.startStopEachItem[j].setText("STOP " + tmpPro.svrInfo.nickname + "!"); //STOPボタンにチェンジ
					}
				}
			} else if((e.getSource()== ancestor.cGUI.stopAllItem)){   //STOP AllProjectボタンを押した場合
				ancestor.stopAllThread();
				for (int j = 0; j < 10; j++) {
					if (ancestor.cGUI.startStopEachItem[j] != null) {
						int projectNumber = j + 1; //プロジェクト番号を表すために１つ値を増加
						DataOfEachProject tmpPro = ClientMain.cDataToFile
								.getDataOfEachByProNum(projectNumber);
						ancestor.cGUI.startStopEachItem[j].setText("START " + tmpPro.svrInfo.nickname + "!"); //STARTボタンにチェンジ
					}
				}
			} else if (isElement(ancestor.cGUI.quitProItem, e.getSource()) != -1) { //イベントソースがfindQuitProItemの要素である場合
					//イベントソースがquiteProItemの要素のどれかだった場合
					//このブロックでそれぞれのプロジェクトから脱退する処理をする
				int okOrNo = JOptionPane.showConfirmDialog(ancestor.cGUI,
						"プロジェクトから脱退しますか？　※登録が抹消されます", "確認",
						JOptionPane.YES_NO_OPTION);
				
				if (okOrNo == JOptionPane.OK_OPTION) {
					try {
						int index = isElement(ancestor.cGUI.quitProItem, e.getSource());
						DataOfEachProject tmp = ClientMain.cDataToFile
								.getDataOfEachByProNum(index + 1);
						ancestor.stopOneThread(tmp); //すでに処理を開始してしまっている場合は停止
						ancestor.quitOneProject(tmp, ClientMain.cDataToFile.cInfo); //脱退処理をサーバに対して行う
						ancestor.cGUI.removeProjectTab(tmp.projectNumber); //プロジェクトのタブを取り除く
						ancestor.cGUI.projectMenu
								.remove(ancestor.cGUI.quitProItem[tmp.projectNumber - 1]);
						ancestor.cGUI.quitProItem[tmp.projectNumber - 1] = null;
						ancestor.cGUI.processingMenu
								.remove(ancestor.cGUI.startStopEachItem[tmp.projectNumber - 1]);
						ancestor.cGUI.startStopEachItem[tmp.projectNumber - 1] = null;
						ClientMain.cDataToFile.delDataOfEach(tmp.svrInfo.signiture); //プロジェクトデータを取り除く
						ancestor.removeProjectFiles(tmp); //プロジェクトのファイルを含むディレクトリを消去

						ClientMain.cDataToFile.preserveAll();
					} catch (IOException e2) {
						//接続に失敗した場合の処理
						Object[] msg = { "サーバが停止しているか何らかの理由でサーバへ接続できませんでした" };
						JOptionPane.showOptionDialog(ancestor.cGUI, msg, "失敗",
								JOptionPane.PLAIN_MESSAGE, 0, null, null, null);
					}
				}
				
			} else if ((isElement(ancestor.cGUI.startStopEachItem, e.getSource())) != -1) {
				//イベントソースがstartStopEachItemの要素のどれかだった場合
				//このブロックでそれぞれのプロジェクトの処理をスタートorストップする
				
				int index = isElement(ancestor.cGUI.startStopEachItem, e.getSource());
				int projectNumber = index + 1; //プロジェクト番号を表すために１つ値を増加
				DataOfEachProject tmpPro = ClientMain.cDataToFile
						.getDataOfEachByProNum(projectNumber);
				
				//プロジェクトが未開始の場合Startボタンとして押されたと判断
				if(tmpPro.variousState == tmpPro.NOTSTART){
					ancestor.startOneProThread(tmpPro);
					ancestor.cGUI.startStopEachItem[index].setText("STOP " + tmpPro.svrInfo.nickname + "!"); //STOPボタンにチェンジ
				}else{     //プロジェクトが開始している場合stopボタンとして押されたと判断
					ancestor.stopOneThread(tmpPro);
					ClientMain.cDataToFile.removeProThre(tmpPro.projectNumber-1);  //処理スレッドを取り除く
					ancestor.cGUI.startStopEachItem[index].setText("START " + tmpPro.svrInfo.nickname + "!"); //STARTボタンにチェンジ
				}
			}else if((e.getSource()) == ancestor.cGUI.messageItem){    //メッセージ変更が押し込まれた場合
				changeMessage();	//メッセージ変更のためのダイアログを表示する
			}
		} catch (ConnectCanceledException e1) {       //接続が許可されなかった場合
			//何もせずにメソッドを終了する
		}
	}

	public void itemStateChanged(ItemEvent e) {

		//状態表示の表示、非表示を変更する
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
		} else if (e.getSource().equals(ancestor.cGUI.autoCulculate)) { //自動計算のチェックが変更された場合
			if (e.getStateChange() == ItemEvent.SELECTED) {
				ClientMain.cDataToFile.sePolicy.autCulculate = true;
			} else if (e.getStateChange() == ItemEvent.DESELECTED) {
				ClientMain.cDataToFile.sePolicy.autCulculate = false;
			}
		} else if (e.getSource().equals(ancestor.cGUI.autoConnect)) { //自動接続のチェックが変更された場合
			if (e.getStateChange() == ItemEvent.SELECTED) {
				ClientMain.cDataToFile.sePolicy.autConnect = true;
			} else if (e.getStateChange() == ItemEvent.DESELECTED) {
				ClientMain.cDataToFile.sePolicy.autConnect = false;
			}
		}else if(e.getSource().equals(ancestor.cGUI.highPriorityItem)){      //優先度の変更の場合
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

	//与えた要素が与えた配列の要素であるかを返す。含まれていた場合はそのindex、含まれていない場合は-1を返す
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
				"メッセージの変更を行いますか？　※次回接続時に送信されます", "確認",
				JOptionPane.YES_NO_OPTION);
		if (okOrNo == JOptionPane.OK_OPTION) {
			FillMessageDialog fillDialog = new FillMessageDialog(ancestor,ancestor.cGUI,"メッセージ変更",true);
		}
	}
}