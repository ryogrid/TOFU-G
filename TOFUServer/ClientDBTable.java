/* ************************************************************** *
 *                                                                *
 * Copyright (c) 2005, Ryo Kanbayashi, All rights reserved.       *
 *                                                                *
 *                                                                *
 * This software is distributed under the modified BSD License.   *
 * ************************************************************** */

import javax.swing.JTable;
import javax.swing.table.*;
import java.util.Calendar;

//�N���C�A���g�̏���񎦂��邽�߂�JTable
public class ClientDBTable extends JTable {

private DefaultTableModel tm = null;

	public ClientDBTable(int row,int col,Object[] column){
		
		super(new DefaultTableModel(new Object[row][col],column));
		tm = (DefaultTableModel) this.getModel();
	}
	
	//DataOfEachProject������ɕێ����Ă���ClientInfo��n���A����Ɋւ��ĕ\�ɒǉ��A�X�V���s��
	public void writeStateToTable(ClientInfo client){
		synchronized(this){
		    int index = 0;
			index = isExistSigniture(0,byteArrayToString(client.signiture));      //���łɒǉ����悤�Ƃ��Ă���N���C�A���g�����邩�m�F
			if(index == -1){
				index = (this.getRowCount() -1) + 1 ;      //�V�K�ɉ�����ꍇ�͐V����index��
				tm.addRow(new Object[this.getColumnCount()]);
			}
				this.setValueAt(byteArrayToString(client.signiture),index,0);
				this.setValueAt(client.participantNum,index,1);
				this.setValueAt(new Float(client.clientVersion),index,2);
				this.setValueAt(client.mailAddress,index,3);
				this.setValueAt(CalendarToString(client.startUseDate),index,4);
				this.setValueAt(new Integer(client.tmpIncluded.workCounts),index,5);
				this.setValueAt(new Integer(client.tmpIncluded.rankOfWorker),index,6);
				this.setValueAt(new Double(client.tmpIncluded.workSeconds),index,7);
				this.setValueAt(new Double(client.AllWorkingTime),index,8);
				this.setValueAt(CalendarToString(client.tmpIncluded.joinDate),index,9);
				this.setValueAt(client.tmpIncluded.messageToServer,index,10);
				this.repaint();
		}
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
	private String byteArrayToString(byte[] array){
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
	
//	DataOfEachProject������ɕێ����Ă���ClientInfo��n���A����Ɋւ��ĕ\�ɒǉ��A�X�V���s��
	public void removeStateFromTable(ClientInfo client){
	    int index = 0;
	    synchronized(this){
		    index = isExistSigniture(0,byteArrayToString(client.signiture));      //���łɒǉ����悤�Ƃ��Ă���N���C�A���g�����邩�m�F
			if(index != -1){    //�����^����ꂽ�N���C�A���g�̏�񂪂������ꍇremove
				tm.removeRow(index);
			}
	    }
	}
}
