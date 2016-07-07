/* ************************************************************** *
 *                                                                *
 * Copyright (c) 2005, Ryo Kanbayashi, All rights reserved.       *
 *                                                                *
 *                                                                *
 * This software is distributed under the modified BSD License.   *
 * ************************************************************** */

import java.awt.*;
import java.nio.ByteBuffer;
import java.util.Calendar;

import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableModel;

//���݂̏���񎦂��邽�߂�JTable
public class ClientStateTable extends JTable {
DefaultTableModel tm;
	public ClientStateTable(int row,int col,Object[] column){
		
		super(new DefaultTableModel(new Object[row][col],column));
		tm = (DefaultTableModel) this.getModel();
	}
	
	//DataOfEachProject��n���A����Ɋւ��ĕ\�ɒǉ��A�X�V���s��
	public void writeProInfoToTable(DataOfEachProject assigned){
		int index = 0;
		index = isExistSigniture(0,ByteArrayToString(assigned.svrInfo.signiture));      //���łɒǉ����悤�Ƃ��Ă���N���C�A���g�����邩�m�F
		if(index == -1){
			index = (this.getRowCount() -1) + 1 ;      //�V�K�ɉ�����ꍇ�͐V����index��
			tm.addRow(new Object[this.getColumnCount()]);
		}
		this.setValueAt(ByteArrayToString(assigned.svrInfo.signiture),index,0);
		this.setValueAt(assigned.svrInfo.projectNum,index,1);
		this.setValueAt(assigned.svrInfo.managerNum,index,2);
		this.setValueAt(assigned.svrInfo.eMailAddress,index,3);
		this.setValueAt(assigned.svrInfo.addresOfHP,index,4);
		this.setValueAt(new Integer(assigned.svrInfo.workerCounts),index,5);
		this.setValueAt(new Integer(assigned.workCounts),index,6);
		this.setValueAt(new Double(assigned.workSeconds),index,7);
		this.setValueAt(new Integer(assigned.rankOfWorker),index,8);
		this.setValueAt(CalendarToString(assigned.joinDate),index,9);
		this.setValueAt(assigned.messageFromServer,index,10);
		
		this.repaint();
	}
	
	//�w�肵����ɓ����I�u�W�F�N�g���������ꍇ������Index�A�Ȃ��ꍇ-1��Ԃ�
	private int isExistSigniture(int col,Object target){

		for(int i = 0;i<this.getRowCount();i++){
			if(target.equals(this.getValueAt(i,0))){
					return i;
			}
		}
		return -1;
	}
	
	//�o�C�g�z���String�̕�����֕ϊ�
	private String ByteArrayToString(byte[] array){
		StringBuffer tmp = new StringBuffer();
		for(int i = 0; i < array.length;i++){
			tmp.append(array[i]);
		}
		String result = tmp.toString();
		return result;
	}

	//Calendar�N���X�̕ێ�������t��String������Ƃ��ĕԂ�
	private String CalendarToString(Calendar d){
		String strResult;
		if(d!=null){
			String year = String.valueOf(d.get(Calendar.YEAR));
			String month = String.valueOf(d.get(Calendar.MONTH));
			String day = String.valueOf(d.get(Calendar.DATE));
			String hour = String.valueOf(d.get(Calendar.HOUR_OF_DAY));
			String minute = String.valueOf(d.get(Calendar.MINUTE));
			
			StringBuffer result = new StringBuffer();
			result.append(year + "/" + month + "/" + day + "/" + hour + ":" + minute);
			strResult = result.toString();
		}else{
			strResult= null;
		}
		
		return strResult;
	}
}
