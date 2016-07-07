/* ************************************************************** *
 *                                                                *
 * Copyright (c) 2005, Ryo Kanbayashi, All rights reserved.       *
 *                                                                *
 *                                                                *
 * This software is distributed under the modified BSD License.   *
 * ************************************************************** */

import java.awt.*;
import javax.swing.*;
import javax.swing.*;
import javax.swing.table.*;    //テーブルのためのインポート

public class ServerGui extends JFrame {

    /**
     * 
     * @uml.property name="ancestor"
     * @uml.associationEnd inverse="sGUI:ServerMain" multiplicity="(1 1)"
     */
    public ServerMain ancestor = null; //メインクラスの参照

    /**
     * 
     * @uml.property name="btn"
     * @uml.associationEnd multiplicity="(0 1)"
     */
    public JButton btn;

    /**
     * 
     * @uml.property name="ta"
     * @uml.associationEnd multiplicity="(1 1)"
     */
    public JTextArea ta;

    /**
     * 
     * @uml.property name="mb"
     * @uml.associationEnd multiplicity="(1 1)"
     */
    public JMenuBar mb;

    /**
     * 
     * @uml.property name="configMenu"
     * @uml.associationEnd multiplicity="(1 1)"
     */
    public JMenu configMenu;

    /**
     * 
     * @uml.property name="initItem"
     * @uml.associationEnd multiplicity="(1 1)"
     */
    public JMenuItem initItem;

    /**
     * 
     * @uml.property name="redundancyItem"
     * @uml.associationEnd multiplicity="(1 1)"
     */
    public JMenuItem redundancyItem;

    /**
     * 
     * @uml.property name="processingMenu"
     * @uml.associationEnd multiplicity="(1 1)"
     */
    public JMenu processingMenu; //start

    /**
     * 
     * @uml.property name="startOrStopServerItem"
     * @uml.associationEnd multiplicity="(1 1)"
     */
    public JMenuItem startOrStopServerItem; //処理開始

    /**
     * 
     * @uml.property name="variousMenu"
     * @uml.associationEnd multiplicity="(1 1)"
     */
    public JMenu variousMenu; //その他

    /**
     * 
     * @uml.property name="messageItem"
     * @uml.associationEnd multiplicity="(1 1)"
     */
    public JMenuItem messageItem; //メッセージ変更

    /**
     * 
     * @uml.property name="scrPane"
     * @uml.associationEnd multiplicity="(0 1)"
     */
    public JScrollPane scrPane;

    /**
     * 
     * @uml.property name="clientTb"
     * @uml.associationEnd multiplicity="(1 1)"
     */
    public ClientDBTable clientTb; //クライアント情報を表示するためのテーブル

    /**
     * 
     * @uml.property name="jobTb"
     * @uml.associationEnd multiplicity="(1 1)"
     */
    public JobResultTable jobTb; //ジョブ情報を表示するためのテーブル

	public ServerGui(ServerMain parent) {
		
		super("Tsukuba Open Framework for Using Grid technology(TOFU-G) SERVER");
		ancestor = parent;
		this.setSize(new Dimension(460,480));
		this.setResizable(false);
		this.getContentPane().setLayout(new FlowLayout());
		
		SWindowEvents eventsWorker = new SWindowEvents(ancestor);
		
		mb = new JMenuBar();
		setJMenuBar(mb);
		configMenu = new JMenu("設定");
		mb.add(configMenu);
		initItem = new JMenuItem("初期設定");
		configMenu.add(initItem);
		initItem.addActionListener(eventsWorker);
		redundancyItem = new JMenuItem("job配布数設定");
		configMenu.add(redundancyItem);
		redundancyItem.addActionListener(eventsWorker);
		
		processingMenu = new JMenu("処理");
		startOrStopServerItem = new JMenuItem("サーバ起動");
		startOrStopServerItem.addActionListener(eventsWorker);
		processingMenu.add(startOrStopServerItem);
		mb.add(processingMenu);
		
		variousMenu = new JMenu("その他");
		messageItem = new JMenuItem("メッセージ変更");
		variousMenu.add(messageItem);
		messageItem.addActionListener(eventsWorker);
		mb.add(variousMenu);
		
		clientTb = new ClientDBTable(0,11,new String[]{"signiture","参加者名","App ver.","メールアドレス","App using Start","処理済job数","順位","総貢献時間","Tofu-G 総起動時間","参加開始日時","MessageFrom"});
		clientTb.setRowSelectionAllowed(true); //列・行両方の選択をtrueにすると両方できなくなるようだ
		clientTb.setColumnSelectionAllowed(true);
		clientTb.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		JTableHeader clientHeader = clientTb.getTableHeader(); 
		clientHeader.setReorderingAllowed(false);// テーブルの列移動を不許可にする。

		JScrollPane clientScrPane = new JScrollPane(clientTb,JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		clientScrPane.setPreferredSize(new Dimension(400,150));
		this.getContentPane().add(clientScrPane);
		
		jobTb = new JobResultTable(0,7,new String[]{"jobシグニチャ","job名","result","貢献者","処理時間","配布日時","完了日時"});
		jobTb.setRowSelectionAllowed(true); //列・行両方の選択をtrueにすると両方できなくなるようだ
		jobTb.setColumnSelectionAllowed(true);
		jobTb.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		JTableHeader jobHeader = clientTb.getTableHeader(); 
		clientHeader.setReorderingAllowed(false);// テーブルの列移動を不許可にする。
		JScrollPane jobScrPane = new JScrollPane(jobTb,JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		jobScrPane.setPreferredSize(new Dimension(400,150));
		this.getContentPane().add(jobScrPane);
		
		ta = new JTextArea(20,20);
		ta.setLineWrap(true);
		ta.setEditable(false);
		JScrollPane sp = new JScrollPane(ta,JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		sp.setPreferredSize(new Dimension(400,100));
		this.getContentPane().add(sp);
		
		this.setVisible(true);
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		this.addWindowListener(eventsWorker);
		
		try {         //実行環境と一致したルックアンドフィールに変更
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());  
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		}
		SwingUtilities.updateComponentTreeUI(this);
	}
	

}
