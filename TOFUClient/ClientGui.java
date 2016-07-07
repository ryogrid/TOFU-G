/* ************************************************************** *
 *                                                                *
 * Copyright (c) 2005, Ryo Kanbayashi, All rights reserved.       *
 *                                                                *
 *                                                                *
 * This software is distributed under the modified BSD License.   *
 * ************************************************************** */

import java.awt.*;
import javax.swing.*;
import javax.swing.table.*; //テーブルのためのインポート

public class ClientGui extends JFrame {
	ClientMain ancestor = null;
	public JTabbedPane tab;
	public PicturePanel useForAttach[] = new PicturePanel[10];    //タブペインに貼り付けるために利用
	public JTextArea ta[] = new JTextArea[10];
	public JScrollPane sp[] = new JScrollPane[10];

	public JScrollPane[] proInfoPane = new JScrollPane[10];
	public JScrollPane[] jobLogPane = new JScrollPane[10];
	public ClientStateTable proInfoTable[] = new ClientStateTable[10];
	public JobStateTable jobLogTable[] = new JobStateTable[10];
	
	public JMenuBar mb;
	public JMenu configMenu; //設定
	public JMenuItem initItem; //初期設定
	
	public JMenu secPolicyMenu; //policy
	public JRadioButtonMenuItem autoConnect; //自動接続
	public JMenuItem autoCulculate; //自動計算
	public JRadioButtonMenuItem highPriorityItem;        //優先度：高
	public JRadioButtonMenuItem middlePriorityItem;      //優先度：中
	public JRadioButtonMenuItem lowPriorityItem;         //優先度：低
	
	public JMenu projectMenu; //Project
	public JMenuItem newProItem; //new Project
	public JMenuItem quitProItem[] = new JMenuItem[10];  //それぞれのプロジェクトのquit用
	
	public JMenu visualMenu;        //表示
	public JRadioButtonMenuItem stateShowItem;     //state表示
	
	public JMenu variousMenu;       //その他
	public JMenuItem messageItem;   //メッセージ変更
	
	public JMenu processingMenu; //処理
	
	public JMenuItem startAllItem; //All Project GO!!
	public JMenuItem stopAllItem;  //All Project STOP!!
	public JMenuItem[] startStopEachItem = new JMenuItem[10];     //各プロジェクトの開始・停止用
	
	public CWindowEvents eventsWorker;        //全てのコンポーネントのイベントをつかさどるクラス
	
	public ClientGui(ClientMain parent) {
		super("Tsukuba Open Framework for Using Grid technology (TOFU-G) CLIENT");
		ancestor = parent;
		this.setSize(new Dimension(650,460));
		this.setResizable(false);

		eventsWorker = new CWindowEvents(ancestor);

		mb = new JMenuBar();
		setJMenuBar(mb);
		configMenu = new JMenu("設定");
		mb.add(configMenu);
		secPolicyMenu = new JMenu("ポリシー設定");
		mb.add(secPolicyMenu);
		autoConnect = new JRadioButtonMenuItem("自動接続");
		autoConnect.addItemListener(eventsWorker);
		secPolicyMenu.add(autoConnect);
		autoCulculate = new JRadioButtonMenuItem("自動計算");
		autoCulculate.addItemListener(eventsWorker);
		secPolicyMenu.add(autoCulculate);
		secPolicyMenu.addSeparator();
		highPriorityItem = new JRadioButtonMenuItem("処理優先度：高");
		highPriorityItem.addItemListener(eventsWorker);
		secPolicyMenu.add(highPriorityItem);
		middlePriorityItem = new JRadioButtonMenuItem("処理優先度：中");
		middlePriorityItem.addItemListener(eventsWorker);
		secPolicyMenu.add(middlePriorityItem);
		lowPriorityItem = new JRadioButtonMenuItem("処理優先度：低");
		lowPriorityItem.addItemListener(eventsWorker);
		secPolicyMenu.add(lowPriorityItem);
		
		initItem = new JMenuItem("初期設定");
		configMenu.add(initItem);
		initItem.addActionListener(eventsWorker);
		
		projectMenu = new JMenu("Project");
		mb.add(projectMenu);
		newProItem = new JMenuItem("new Project");
		newProItem.addActionListener(eventsWorker);
		projectMenu.add(newProItem);
		projectMenu.addSeparator();

		visualMenu = new JMenu("表示");
		stateShowItem = new JRadioButtonMenuItem("state非表示");
		stateShowItem.addItemListener(eventsWorker);
		visualMenu.add(stateShowItem);
		mb.add(visualMenu);
		
		variousMenu = new JMenu("その他");
		messageItem = new JMenuItem("メッセージ変更");
		variousMenu.add(messageItem);
		messageItem.addActionListener(eventsWorker);
		mb.add(variousMenu);

		processingMenu = new JMenu("start");
		stopAllItem = new JMenuItem("STOP All Project!!");
		stopAllItem.addActionListener(eventsWorker);
		startAllItem = new JMenuItem("GO All Project!!");
		startAllItem.addActionListener(eventsWorker);
		processingMenu.add(startAllItem);
		processingMenu.add(stopAllItem);
		processingMenu.addSeparator();
		mb.add(processingMenu);
		
		this.setVisible(true);
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		this.addWindowListener(eventsWorker);
		
		tab = new JTabbedPane();
		
		this.getContentPane().add(tab);

		try { //実行環境と一致したルックアンドフィールに変更
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			SwingUtilities.updateComponentTreeUI(this);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		}

	}

	//フラグが変更された時に呼び、含まれるフラグによって再描画を行う
	public void informChangeToComp() {

		if (ClientMain.cDataToFile.alreadyInit
			== false) { //まだ初期設定を行っていない場合
			ancestor.cGUI.newProItem.setEnabled(false); //new Projectボタンを使用不可に
			ancestor.cGUI.processingMenu.setEnabled(false);
		} else {
			ancestor.cGUI.newProItem.setEnabled(true);
			ancestor.cGUI.startAllItem.setEnabled(true);
		}

		if (ClientMain.cDataToFile.sePolicy.autCulculate == true) {
			//自動計算の場合、処理開始ボタンは不必要
			ancestor.cGUI.processingMenu.setEnabled(false);
			ancestor.cGUI.autoCulculate.setSelected(true);
		} else {
			ancestor.cGUI.processingMenu.setEnabled(true);
			ancestor.cGUI.autoCulculate.setSelected(false);
		}
		
		//自動接続のフラグについて
		if(ClientMain.cDataToFile.sePolicy.autConnect == true){
			ancestor.cGUI.autoConnect.setSelected(true);
		}else{
			ancestor.cGUI.autoConnect.setSelected(false);
		}
		
		//まだ一つもプロジェクトに参加していない場合
		if(ClientMain.cDataToFile.getProjectCount()==0){
			ancestor.cGUI.processingMenu.setEnabled(false);
		}else{
			ancestor.cGUI.processingMenu.setEnabled(true);
		}
		
		//処理優先度の変更をＧＵＩへ伝える(他のボタンへの処理や、実際に優先度を変更するのはウィンドウのイベントにまかせる)
		if(ClientMain.cDataToFile.priorityLevel == ClientMain.cDataToFile.PRIORITYHIGH){
			ancestor.cGUI.highPriorityItem.setSelected(true);
		}else if(ClientMain.cDataToFile.priorityLevel == ClientMain.cDataToFile.PRIORITYMIDDLE){
			ancestor.cGUI.middlePriorityItem.setSelected(true);
		}else if(ClientMain.cDataToFile.priorityLevel == ClientMain.cDataToFile.PRIORITYLAW){
			ancestor.cGUI.lowPriorityItem.setSelected(true);
		}
	}
	
	//タブペインとメニューバーを足していく
	public void addProjectTabMenu(int proNum,Image img,DataOfEachProject assigned){

		
		if(useForAttach[proNum-1] == null){      //まだペインが追加されていないようであれば
			useForAttach[proNum -1] = new PicturePanel();
			
			proInfoTable[proNum-1] = new ClientStateTable(0,11,new String[]{"シグニチャ","プロジェクト名","運営者","メールアドレス","公式ＨＰアドレス","全参加人数","総処理job数","総貢献時間","順位","参加開始日時","メッセージ"});
			proInfoTable[proNum-1].setRowSelectionAllowed(true); //列・行両方の選択をtrueにすると両方できなくなるようだ
			proInfoTable[proNum-1].setColumnSelectionAllowed(true);
			proInfoTable[proNum-1].setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
			proInfoTable[proNum-1].writeProInfoToTable(assigned);
			JTableHeader proInfoHeader = proInfoTable[proNum-1].getTableHeader(); 
			proInfoHeader.setReorderingAllowed(false);// テーブルの列移動を不許可にする。
			
			proInfoPane[proNum-1] = new JScrollPane(proInfoTable[proNum-1],JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
			proInfoTable[proNum-1].setRowHeight(20);
			proInfoPane[proNum-1].setPreferredSize(new Dimension(400,60));
			useForAttach[proNum -1].add(proInfoPane[proNum-1]);
			
			jobLogTable[proNum-1] = new JobStateTable(0,7,new String[]{"jobシグニチャ","job名","状態","result","所要時間","配布日時","完了日時"});
			jobLogTable[proNum-1].setRowSelectionAllowed(true); //列・行両方の選択をtrueにすると両方できなくなるようだ
			jobLogTable[proNum-1].setColumnSelectionAllowed(true);
			jobLogTable[proNum-1].setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
			//			jobLogTable[proNum-1].writeLogToTable(assigned);
			JTableHeader jobLogHeader = jobLogTable[proNum-1].getTableHeader(); 
			jobLogHeader.setReorderingAllowed(false);// テーブルの列移動を不許可にする。
			
			jobLogPane[proNum-1] = new JScrollPane(jobLogTable[proNum-1],JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
			jobLogTable[proNum-1].setRowHeight(20);
			jobLogPane[proNum-1].setPreferredSize(new Dimension(400,300));
			useForAttach[proNum -1].add(jobLogPane[proNum-1]);
			
			ta[proNum -1] = new JTextArea(20, 20);
			ta[proNum -1].setLineWrap(true);
			ta[proNum -1].setEditable(false);

			sp[proNum-1] = new JScrollPane(ta[proNum-1],JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
			sp[proNum-1].setPreferredSize(new Dimension(200,300));
			
			useForAttach[proNum -1].add(sp[proNum-1]);
			useForAttach[proNum -1].setImg(img);
			useForAttach[proNum -1].repaint();
			
			//それぞれのプロジェクト開始・停止用のメニューを追加する
			startStopEachItem[proNum -1] = new JMenuItem("START " +  assigned.svrInfo.nickname + "!");
			startStopEachItem[proNum -1].addActionListener(eventsWorker);
			processingMenu.add(startStopEachItem[proNum-1]);
			assigned.indexOfMenu = processingMenu.getComponentCount();   //追加した位置のインデックスを保持しておく
			
			//それぞれのプロジェクトのquit用のメニューアイテムを追加する
			quitProItem[proNum - 1] = new JMenuItem("QUIT " + assigned.svrInfo.nickname);
			quitProItem[proNum - 1].addActionListener(eventsWorker);
			projectMenu.add(quitProItem[proNum - 1]);
			
			tab.addTab(assigned.svrInfo.nickname,useForAttach[proNum -1]);
			
			//過去ログがある場合はテーブルへ出力
			for(int i = 0; i < assigned.pastJobDB.getLogCount();i++){
				ancestor.cGUI.writeLogToTable(proNum,assigned,assigned.pastJobDB.getOneLogWraper(i));
			}
			tab.repaint();
		}
	}
	
	//プロジェクト番号を与える事によりプロジェクトのタブを取り除く(取り除いたコンポーネントはnullに）
	public void removeProjectTab(int proNum){
		tab.remove(useForAttach[proNum-1]);
		useForAttach[proNum-1] = null;
	}
	
	//プロジェクト番号と、書き込むプロジェクトデータを渡して書き込みを行う
	public void writeStateToTable(int proNum,DataOfEachProject target){
		proInfoTable[proNum-1].writeProInfoToTable(target);
	}
	
//	プロジェクト番号と、書き込むプロジェクトデータを渡して書き込みを行う
	public void writeLogToTable(int proNum,DataOfEachProject target){
		jobLogTable[proNum-1].writeLogToTable(target);
	}
	
//　プロジェクト番号と、書き込むプロジェクトデータと、LogWraperを渡して過去ログを書き込む
	public void writeLogToTable(int proNum,DataOfEachProject target,ClientLogWraper log){
		jobLogTable[proNum-1].writeLogToTable(target,log);
	}
}
