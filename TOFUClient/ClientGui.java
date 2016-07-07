/* ************************************************************** *
 *                                                                *
 * Copyright (c) 2005, Ryo Kanbayashi, All rights reserved.       *
 *                                                                *
 *                                                                *
 * This software is distributed under the modified BSD License.   *
 * ************************************************************** */

import java.awt.*;
import javax.swing.*;
import javax.swing.table.*; //�e�[�u���̂��߂̃C���|�[�g

public class ClientGui extends JFrame {
	ClientMain ancestor = null;
	public JTabbedPane tab;
	public PicturePanel useForAttach[] = new PicturePanel[10];    //�^�u�y�C���ɓ\��t���邽�߂ɗ��p
	public JTextArea ta[] = new JTextArea[10];
	public JScrollPane sp[] = new JScrollPane[10];

	public JScrollPane[] proInfoPane = new JScrollPane[10];
	public JScrollPane[] jobLogPane = new JScrollPane[10];
	public ClientStateTable proInfoTable[] = new ClientStateTable[10];
	public JobStateTable jobLogTable[] = new JobStateTable[10];
	
	public JMenuBar mb;
	public JMenu configMenu; //�ݒ�
	public JMenuItem initItem; //�����ݒ�
	
	public JMenu secPolicyMenu; //policy
	public JRadioButtonMenuItem autoConnect; //�����ڑ�
	public JMenuItem autoCulculate; //�����v�Z
	public JRadioButtonMenuItem highPriorityItem;        //�D��x�F��
	public JRadioButtonMenuItem middlePriorityItem;      //�D��x�F��
	public JRadioButtonMenuItem lowPriorityItem;         //�D��x�F��
	
	public JMenu projectMenu; //Project
	public JMenuItem newProItem; //new Project
	public JMenuItem quitProItem[] = new JMenuItem[10];  //���ꂼ��̃v���W�F�N�g��quit�p
	
	public JMenu visualMenu;        //�\��
	public JRadioButtonMenuItem stateShowItem;     //state�\��
	
	public JMenu variousMenu;       //���̑�
	public JMenuItem messageItem;   //���b�Z�[�W�ύX
	
	public JMenu processingMenu; //����
	
	public JMenuItem startAllItem; //All Project GO!!
	public JMenuItem stopAllItem;  //All Project STOP!!
	public JMenuItem[] startStopEachItem = new JMenuItem[10];     //�e�v���W�F�N�g�̊J�n�E��~�p
	
	public CWindowEvents eventsWorker;        //�S�ẴR���|�[�l���g�̃C�x���g�������ǂ�N���X
	
	public ClientGui(ClientMain parent) {
		super("Tsukuba Open Framework for Using Grid technology (TOFU-G) CLIENT");
		ancestor = parent;
		this.setSize(new Dimension(650,460));
		this.setResizable(false);

		eventsWorker = new CWindowEvents(ancestor);

		mb = new JMenuBar();
		setJMenuBar(mb);
		configMenu = new JMenu("�ݒ�");
		mb.add(configMenu);
		secPolicyMenu = new JMenu("�|���V�[�ݒ�");
		mb.add(secPolicyMenu);
		autoConnect = new JRadioButtonMenuItem("�����ڑ�");
		autoConnect.addItemListener(eventsWorker);
		secPolicyMenu.add(autoConnect);
		autoCulculate = new JRadioButtonMenuItem("�����v�Z");
		autoCulculate.addItemListener(eventsWorker);
		secPolicyMenu.add(autoCulculate);
		secPolicyMenu.addSeparator();
		highPriorityItem = new JRadioButtonMenuItem("�����D��x�F��");
		highPriorityItem.addItemListener(eventsWorker);
		secPolicyMenu.add(highPriorityItem);
		middlePriorityItem = new JRadioButtonMenuItem("�����D��x�F��");
		middlePriorityItem.addItemListener(eventsWorker);
		secPolicyMenu.add(middlePriorityItem);
		lowPriorityItem = new JRadioButtonMenuItem("�����D��x�F��");
		lowPriorityItem.addItemListener(eventsWorker);
		secPolicyMenu.add(lowPriorityItem);
		
		initItem = new JMenuItem("�����ݒ�");
		configMenu.add(initItem);
		initItem.addActionListener(eventsWorker);
		
		projectMenu = new JMenu("Project");
		mb.add(projectMenu);
		newProItem = new JMenuItem("new Project");
		newProItem.addActionListener(eventsWorker);
		projectMenu.add(newProItem);
		projectMenu.addSeparator();

		visualMenu = new JMenu("�\��");
		stateShowItem = new JRadioButtonMenuItem("state��\��");
		stateShowItem.addItemListener(eventsWorker);
		visualMenu.add(stateShowItem);
		mb.add(visualMenu);
		
		variousMenu = new JMenu("���̑�");
		messageItem = new JMenuItem("���b�Z�[�W�ύX");
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

		try { //���s���ƈ�v�������b�N�A���h�t�B�[���ɕύX
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

	//�t���O���ύX���ꂽ���ɌĂсA�܂܂��t���O�ɂ���čĕ`����s��
	public void informChangeToComp() {

		if (ClientMain.cDataToFile.alreadyInit
			== false) { //�܂������ݒ���s���Ă��Ȃ��ꍇ
			ancestor.cGUI.newProItem.setEnabled(false); //new Project�{�^�����g�p�s��
			ancestor.cGUI.processingMenu.setEnabled(false);
		} else {
			ancestor.cGUI.newProItem.setEnabled(true);
			ancestor.cGUI.startAllItem.setEnabled(true);
		}

		if (ClientMain.cDataToFile.sePolicy.autCulculate == true) {
			//�����v�Z�̏ꍇ�A�����J�n�{�^���͕s�K�v
			ancestor.cGUI.processingMenu.setEnabled(false);
			ancestor.cGUI.autoCulculate.setSelected(true);
		} else {
			ancestor.cGUI.processingMenu.setEnabled(true);
			ancestor.cGUI.autoCulculate.setSelected(false);
		}
		
		//�����ڑ��̃t���O�ɂ���
		if(ClientMain.cDataToFile.sePolicy.autConnect == true){
			ancestor.cGUI.autoConnect.setSelected(true);
		}else{
			ancestor.cGUI.autoConnect.setSelected(false);
		}
		
		//�܂�����v���W�F�N�g�ɎQ�����Ă��Ȃ��ꍇ
		if(ClientMain.cDataToFile.getProjectCount()==0){
			ancestor.cGUI.processingMenu.setEnabled(false);
		}else{
			ancestor.cGUI.processingMenu.setEnabled(true);
		}
		
		//�����D��x�̕ύX���f�t�h�֓`����(���̃{�^���ւ̏�����A���ۂɗD��x��ύX����̂̓E�B���h�E�̃C�x���g�ɂ܂�����)
		if(ClientMain.cDataToFile.priorityLevel == ClientMain.cDataToFile.PRIORITYHIGH){
			ancestor.cGUI.highPriorityItem.setSelected(true);
		}else if(ClientMain.cDataToFile.priorityLevel == ClientMain.cDataToFile.PRIORITYMIDDLE){
			ancestor.cGUI.middlePriorityItem.setSelected(true);
		}else if(ClientMain.cDataToFile.priorityLevel == ClientMain.cDataToFile.PRIORITYLAW){
			ancestor.cGUI.lowPriorityItem.setSelected(true);
		}
	}
	
	//�^�u�y�C���ƃ��j���[�o�[�𑫂��Ă���
	public void addProjectTabMenu(int proNum,Image img,DataOfEachProject assigned){

		
		if(useForAttach[proNum-1] == null){      //�܂��y�C�����ǉ�����Ă��Ȃ��悤�ł����
			useForAttach[proNum -1] = new PicturePanel();
			
			proInfoTable[proNum-1] = new ClientStateTable(0,11,new String[]{"�V�O�j�`��","�v���W�F�N�g��","�^�c��","���[���A�h���X","�����g�o�A�h���X","�S�Q���l��","������job��","���v������","����","�Q���J�n����","���b�Z�[�W"});
			proInfoTable[proNum-1].setRowSelectionAllowed(true); //��E�s�����̑I����true�ɂ���Ɨ����ł��Ȃ��Ȃ�悤��
			proInfoTable[proNum-1].setColumnSelectionAllowed(true);
			proInfoTable[proNum-1].setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
			proInfoTable[proNum-1].writeProInfoToTable(assigned);
			JTableHeader proInfoHeader = proInfoTable[proNum-1].getTableHeader(); 
			proInfoHeader.setReorderingAllowed(false);// �e�[�u���̗�ړ���s���ɂ���B
			
			proInfoPane[proNum-1] = new JScrollPane(proInfoTable[proNum-1],JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
			proInfoTable[proNum-1].setRowHeight(20);
			proInfoPane[proNum-1].setPreferredSize(new Dimension(400,60));
			useForAttach[proNum -1].add(proInfoPane[proNum-1]);
			
			jobLogTable[proNum-1] = new JobStateTable(0,7,new String[]{"job�V�O�j�`��","job��","���","result","���v����","�z�z����","��������"});
			jobLogTable[proNum-1].setRowSelectionAllowed(true); //��E�s�����̑I����true�ɂ���Ɨ����ł��Ȃ��Ȃ�悤��
			jobLogTable[proNum-1].setColumnSelectionAllowed(true);
			jobLogTable[proNum-1].setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
			//			jobLogTable[proNum-1].writeLogToTable(assigned);
			JTableHeader jobLogHeader = jobLogTable[proNum-1].getTableHeader(); 
			jobLogHeader.setReorderingAllowed(false);// �e�[�u���̗�ړ���s���ɂ���B
			
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
			
			//���ꂼ��̃v���W�F�N�g�J�n�E��~�p�̃��j���[��ǉ�����
			startStopEachItem[proNum -1] = new JMenuItem("START " +  assigned.svrInfo.nickname + "!");
			startStopEachItem[proNum -1].addActionListener(eventsWorker);
			processingMenu.add(startStopEachItem[proNum-1]);
			assigned.indexOfMenu = processingMenu.getComponentCount();   //�ǉ������ʒu�̃C���f�b�N�X��ێ����Ă���
			
			//���ꂼ��̃v���W�F�N�g��quit�p�̃��j���[�A�C�e����ǉ�����
			quitProItem[proNum - 1] = new JMenuItem("QUIT " + assigned.svrInfo.nickname);
			quitProItem[proNum - 1].addActionListener(eventsWorker);
			projectMenu.add(quitProItem[proNum - 1]);
			
			tab.addTab(assigned.svrInfo.nickname,useForAttach[proNum -1]);
			
			//�ߋ����O������ꍇ�̓e�[�u���֏o��
			for(int i = 0; i < assigned.pastJobDB.getLogCount();i++){
				ancestor.cGUI.writeLogToTable(proNum,assigned,assigned.pastJobDB.getOneLogWraper(i));
			}
			tab.repaint();
		}
	}
	
	//�v���W�F�N�g�ԍ���^���鎖�ɂ��v���W�F�N�g�̃^�u����菜��(��菜�����R���|�[�l���g��null�Ɂj
	public void removeProjectTab(int proNum){
		tab.remove(useForAttach[proNum-1]);
		useForAttach[proNum-1] = null;
	}
	
	//�v���W�F�N�g�ԍ��ƁA�������ރv���W�F�N�g�f�[�^��n���ď������݂��s��
	public void writeStateToTable(int proNum,DataOfEachProject target){
		proInfoTable[proNum-1].writeProInfoToTable(target);
	}
	
//	�v���W�F�N�g�ԍ��ƁA�������ރv���W�F�N�g�f�[�^��n���ď������݂��s��
	public void writeLogToTable(int proNum,DataOfEachProject target){
		jobLogTable[proNum-1].writeLogToTable(target);
	}
	
//�@�v���W�F�N�g�ԍ��ƁA�������ރv���W�F�N�g�f�[�^�ƁALogWraper��n���ĉߋ����O����������
	public void writeLogToTable(int proNum,DataOfEachProject target,ClientLogWraper log){
		jobLogTable[proNum-1].writeLogToTable(target,log);
	}
}
