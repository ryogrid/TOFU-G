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

//クライアントの情報を提示するためのJTable
public class ClientDBTable extends JTable {

private DefaultTableModel tm = null;

	public ClientDBTable(int row,int col,Object[] column){
		
		super(new DefaultTableModel(new Object[row][col],column));
		tm = (DefaultTableModel) this.getModel();
	}
	
	//DataOfEachProjectを内部に保持しているClientInfoを渡し、それに関して表に追加、更新を行う
	public void writeStateToTable(ClientInfo client){
		synchronized(this){
		    int index = 0;
			index = isExistSigniture(0,byteArrayToString(client.signiture));      //すでに追加しようとしているクライアントがいるか確認
			if(index == -1){
				index = (this.getRowCount() -1) + 1 ;      //新規に加える場合は新しいindexで
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
	
	//指定した列に同じオブジェクトがあった場合そこのIndex、ない場合-1を返す
	private int isExistSigniture(int col,Object target){

		for(int i = 0;i<this.getRowCount();i++){
			if(target.equals(this.getValueAt(i,0))){
					return i;
			}
		}
		return -1;
	}
	
	//バイト配列をStringの文字列へ変換
	private String byteArrayToString(byte[] array){
		StringBuffer tmp = new StringBuffer();
		for(int i = 0; i < array.length;i++){
			tmp.append(array[i]);
		}
		String result = tmp.toString();
		return result;
	}
	
	//Calendarクラスの保持する日付をString文字列として返す
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
	
//	DataOfEachProjectを内部に保持しているClientInfoを渡し、それに関して表に追加、更新を行う
	public void removeStateFromTable(ClientInfo client){
	    int index = 0;
	    synchronized(this){
		    index = isExistSigniture(0,byteArrayToString(client.signiture));      //すでに追加しようとしているクライアントがいるか確認
			if(index != -1){    //もし与えられたクライアントの情報があった場合remove
				tm.removeRow(index);
			}
	    }
	}
}
