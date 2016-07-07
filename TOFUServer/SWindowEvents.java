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
    public ServerMain ancestor = null; //メインクラスの参照

	
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
					"全データがリセットされますが、進めますか？ ※記入後終了します", "確認",
					JOptionPane.YES_NO_OPTION);
			
			if (okOrNo == JOptionPane.OK_OPTION) {	
			    if(ancestor.requestManager != null){
				    ancestor.requestManager.requestStop();
					while (ancestor.requestManagerThread.isAlive() == true) {
						try {
							Thread.sleep(50);
							//本当に終了するまで待機
						} catch (InterruptedException e2) {
							e2.printStackTrace();
						}
					}
					ancestor.requestManager = null;
					ancestor.sGUI.startOrStopServerItem.setText("サーバ開始");
			    }
				
			    if(ancestor.dataPreserver != null){
				    ancestor.dataPreserver.requestIntterupt();   //データ保存スレッドも停止
				    while(ancestor.dataPreserverThread.isAlive() == true){  //データ保存スレッドが終了するまで待機
				        try {
	                        Thread.sleep(50);
	                    } catch (InterruptedException e2) {
	                        e2.printStackTrace();
	                    }
				    }
				}
			    
			    if (ancestor.reDisChecker != null) {    //再配布チェックスレッドの停止処理
					ancestor.reDisChecker.requestIntterupt();
					while (ancestor.reDisCheckerThread.isAlive() == true) {
						try {
							Thread.sleep(50);
							//本当に終了するまで待機
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
			
		}else if((e.getSource() == ancestor.sGUI.startOrStopServerItem)&&(ancestor.requestManager == null)){	//処理開始ボタンが押された場合
		    ancestor.requestManager = new RequestManager(ancestor.svsock,ancestor);
			ancestor.requestManagerThread = new Thread(ancestor.requestManager);
			ancestor.requestManagerThread.start();
			
			ancestor.reDisChecker = new ReDistributeChecker(ServerMain.sDataToFile.reDistributeDays,ServerMain.sDataToFile.reDistributecheckMin);    //このブロックで再配布チェックスレッドを開始
			ancestor.reDisCheckerThread = new Thread(ancestor.reDisChecker);
			ancestor.reDisCheckerThread.start();
			
			ancestor.dataPreserver = new DataPreserver(1);   //１分間隔で保存を行わせる
			ancestor.dataPreserverThread = new Thread(ancestor.dataPreserver);
			ancestor.dataPreserverThread.start();
			
			ancestor.sGUI.startOrStopServerItem.setText("サーバ停止");
		}else if((e.getSource() == ancestor.sGUI.startOrStopServerItem)&&(ancestor.requestManager != null)){	//処理終了ボタンが押された場合
			ancestor.requestManager.requestStop();
			while (ancestor.requestManagerThread.isAlive() == true) {
				try {
					Thread.sleep(50);
					//本当に終了するまで待機
				} catch (InterruptedException e2) {
					e2.printStackTrace();
				}
			}
			ServerMain.sDataToFile.preserveAll();
			ancestor.requestManager = null;
			ancestor.sGUI.startOrStopServerItem.setText("サーバ開始");
		}else if((e.getSource() == ancestor.sGUI.redundancyItem)){     //冗長性の値の変更用アイテムの場合
			int okOrNo = JOptionPane.showConfirmDialog(ancestor.sGUI,
					"配布job数の変更を行いますか？", "確認", JOptionPane.YES_NO_OPTION);
			if (okOrNo == JOptionPane.OK_OPTION) {
				String strTmp; //一旦記入された内容を保持（if文での利用のため）
				strTmp = (String) JOptionPane.showInputDialog(ancestor.sGUI,
						"いくつの計算結果が集まることによってjobの結果を確定しますか？", "配布job数設定",
						JOptionPane.PLAIN_MESSAGE,null,null,String.valueOf(ServerMain.sDataToFile.jManager.getRedundancyCount()));
				if (strTmp != null) { //取り消しを押されていなければ(上書き防止)
					ServerMain.sDataToFile.jManager.setRedundancyCount(Integer.parseInt(strTmp));
				}
			}
		}else if((e.getSource()) == ancestor.sGUI.messageItem){    //メッセージ変更が押し込まれた場合
			changeMessage();	//メッセージ変更のためのダイアログを表示する
		}
	}
	
	private void changeMessage(){
		int okOrNo = JOptionPane.showConfirmDialog(ancestor.sGUI,
				"メッセージの変更を行いますか？　※変更が反映されるのはこれ以降に接続してきたクライアントに対してです", "確認",
				JOptionPane.YES_NO_OPTION);
		if (okOrNo == JOptionPane.OK_OPTION) {
			String strTmp =JOptionPane.showInputDialog(ancestor.sGUI,
					"参加者へのメッセージを記入して下さい",
					"メッセージ変更",JOptionPane.PLAIN_MESSAGE);
			if (strTmp != null) { //取り消しを押されていなければ(上書き防止)
				ServerMain.sDataToFile.messageToClients = strTmp;
				ServerMain.sDataToFile.preserveAll();
			}
		}
	}
}