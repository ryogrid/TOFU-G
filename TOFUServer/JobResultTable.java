/* ************************************************************** *
 *                                                                *
 * Copyright (c) 2005, Ryo Kanbayashi, All rights reserved.       *
 *                                                                *
 *                                                                *
 * This software is distributed under the modified BSD License.   *
 * ************************************************************** */

import javax.swing.JTable;
import javax.swing.table.*;

import java.util.ArrayList;
import java.util.Calendar;

//�N���C�A���g����Ԃ��Ă����i�����̌��ʂ�񎦂��邽�߂�Jtable
public class JobResultTable extends JTable {

/**
 * 
 * @uml.property name="tm"
 * @uml.associationEnd multiplicity="(1 1)"
 */
private DefaultTableModel tm = null;

	public JobResultTable(int row,int col,Object[] column){
		
		super(new DefaultTableModel(new Object[row][col],column));
		tm = (DefaultTableModel) this.getModel();
	}
	
	//DataOfEachProject������ɕێ����Ă���ClientInfo��n���Ajob�̌��ʂ�\�ɒǉ��A�X�V����
	public void writeJobResut(ServerLogWraper logWraper){
		CulculateData culData = logWraper.getWrapped();
	    int index = 0;
		index = isExistSigniture(0,ByteArrayToString(culData.getSigniture()));      //���łɒǉ����悤�Ƃ��Ă���N���C�A���g�����邩�m�F
		if(index == -1){
			index = (this.getRowCount() -1) + 1 ;      //�V�K�ɉ�����ꍇ�͐V����index��
			tm.addRow(new Object[this.getColumnCount()]);
		}
			this.setValueAt(ByteArrayToString(culData.getSigniture()),index,0);
			this.setValueAt(culData.getJobName(),index,1);
			this.setValueAt(culData.getResultDescription(),index,2);
			
			ArrayList contributers = logWraper.getContributedMans();
			StringBuffer tmp = new StringBuffer();   //�v���҂�\��������
			
			for(int i =0; i < contributers.size();i++){
			    tmp.append(((ContributerProperty) contributers.get(i)).contributer.participantNum + ":" );
			}
			this.setValueAt(tmp.toString(),index,3);
			
			this.setValueAt(new Double(culData.getNecessaryTime()),index,4);
			this.setValueAt(CalendarToString(culData.getDistributeDate()),index,5);
			this.setValueAt(CalendarToString(culData.getCompleteDate()),index,6);
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
