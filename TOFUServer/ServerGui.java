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
import javax.swing.table.*;    //�e�[�u���̂��߂̃C���|�[�g

public class ServerGui extends JFrame {

    /**
     * 
     * @uml.property name="ancestor"
     * @uml.associationEnd inverse="sGUI:ServerMain" multiplicity="(1 1)"
     */
    public ServerMain ancestor = null; //���C���N���X�̎Q��

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
    public JMenuItem startOrStopServerItem; //�����J�n

    /**
     * 
     * @uml.property name="variousMenu"
     * @uml.associationEnd multiplicity="(1 1)"
     */
    public JMenu variousMenu; //���̑�

    /**
     * 
     * @uml.property name="messageItem"
     * @uml.associationEnd multiplicity="(1 1)"
     */
    public JMenuItem messageItem; //���b�Z�[�W�ύX

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
    public ClientDBTable clientTb; //�N���C�A���g����\�����邽�߂̃e�[�u��

    /**
     * 
     * @uml.property name="jobTb"
     * @uml.associationEnd multiplicity="(1 1)"
     */
    public JobResultTable jobTb; //�W���u����\�����邽�߂̃e�[�u��

	public ServerGui(ServerMain parent) {
		
		super("Tsukuba Open Framework for Using Grid technology(TOFU-G) SERVER");
		ancestor = parent;
		this.setSize(new Dimension(460,480));
		this.setResizable(false);
		this.getContentPane().setLayout(new FlowLayout());
		
		SWindowEvents eventsWorker = new SWindowEvents(ancestor);
		
		mb = new JMenuBar();
		setJMenuBar(mb);
		configMenu = new JMenu("�ݒ�");
		mb.add(configMenu);
		initItem = new JMenuItem("�����ݒ�");
		configMenu.add(initItem);
		initItem.addActionListener(eventsWorker);
		redundancyItem = new JMenuItem("job�z�z���ݒ�");
		configMenu.add(redundancyItem);
		redundancyItem.addActionListener(eventsWorker);
		
		processingMenu = new JMenu("����");
		startOrStopServerItem = new JMenuItem("�T�[�o�N��");
		startOrStopServerItem.addActionListener(eventsWorker);
		processingMenu.add(startOrStopServerItem);
		mb.add(processingMenu);
		
		variousMenu = new JMenu("���̑�");
		messageItem = new JMenuItem("���b�Z�[�W�ύX");
		variousMenu.add(messageItem);
		messageItem.addActionListener(eventsWorker);
		mb.add(variousMenu);
		
		clientTb = new ClientDBTable(0,11,new String[]{"signiture","�Q���Җ�","App ver.","���[���A�h���X","App using Start","������job��","����","���v������","Tofu-G ���N������","�Q���J�n����","MessageFrom"});
		clientTb.setRowSelectionAllowed(true); //��E�s�����̑I����true�ɂ���Ɨ����ł��Ȃ��Ȃ�悤��
		clientTb.setColumnSelectionAllowed(true);
		clientTb.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		JTableHeader clientHeader = clientTb.getTableHeader(); 
		clientHeader.setReorderingAllowed(false);// �e�[�u���̗�ړ���s���ɂ���B

		JScrollPane clientScrPane = new JScrollPane(clientTb,JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		clientScrPane.setPreferredSize(new Dimension(400,150));
		this.getContentPane().add(clientScrPane);
		
		jobTb = new JobResultTable(0,7,new String[]{"job�V�O�j�`��","job��","result","�v����","��������","�z�z����","��������"});
		jobTb.setRowSelectionAllowed(true); //��E�s�����̑I����true�ɂ���Ɨ����ł��Ȃ��Ȃ�悤��
		jobTb.setColumnSelectionAllowed(true);
		jobTb.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		JTableHeader jobHeader = clientTb.getTableHeader(); 
		clientHeader.setReorderingAllowed(false);// �e�[�u���̗�ړ���s���ɂ���B
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
		
		try {         //���s���ƈ�v�������b�N�A���h�t�B�[���ɕύX
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
