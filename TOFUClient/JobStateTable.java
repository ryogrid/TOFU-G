/* ************************************************************** *
 *                                                                *
 * Copyright (c) 2005, Ryo Kanbayashi, All rights reserved.       *
 *                                                                *
 *                                                                *
 * This software is distributed under the modified BSD License.   *
 * ************************************************************** */

import java.awt.*;
import java.util.Calendar;

import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

//処理した、または処理しているjobの情報を提示するためのJTable
public class JobStateTable extends JTable {
DefaultTableModel tm;
	public JobStateTable(int row,int col,Object[] column){
		
		super(new DefaultTableModel(new Object[row][col],column));
		tm = (DefaultTableModel) this.getModel();
	}
	
	//DataOfEachProjectを渡し、それに応じてログを出力する
	public void writeLogToTable(DataOfEachProject assigned){
		int index = 0;
		index = isExistSigniture(0,ByteArrayToString(assigned.culData.getSigniture()));      //すでに追加しようとしているクライアントがいるか確認
		if(index == -1){
			index = (this.getRowCount() -1) + 1 ;      //新規に加える場合は新しいindexで
			tm.addRow(new Object[this.getColumnCount()]);
		}
		this.setValueAt(ByteArrayToString(assigned.culData.getSigniture()),index,0);
		this.setValueAt(assigned.culData.getJobName(),index,1);
		this.setValueAt(assigned.culData.isCulculateComplete()? "計算完了":"計算中",index,2);
		this.setValueAt(assigned.culData.getResultDescription(),index,3);
		this.setValueAt(new Double(assigned.culData.getNecessaryTime()),index,4);
		this.setValueAt(CalendarToString(assigned.culData.getDistributeDate()),index,5);
		this.setValueAt(CalendarToString(assigned.culData.getCompleteDate()),index,6);
			
		this.repaint();
	}
	
	//LogWraperを渡して、その中の過去ログをテーブルに出力
	public void writeLogToTable(DataOfEachProject assigned,ClientLogWraper log){
		
		CulculateData cData = log.getWrapped();        //LogWraperからCulculateDataを取り出す
		int	index = (this.getRowCount() -1) + 1 ;      //新規に加える場合は新しいindexで
		tm.addRow(new Object[this.getColumnCount()]);

		this.setValueAt(ByteArrayToString(cData.getSigniture()),index,0);
		this.setValueAt(cData.getJobName(),index,1);
		this.setValueAt(cData.isCulculateComplete()? "計算完了":"計算中",index,2);
		this.setValueAt(cData.getResultDescription(),index,3);
		this.setValueAt(new Double(cData.getNecessaryTime()),index,4);
		this.setValueAt(CalendarToString(cData.getDistributeDate()),index,5);
		this.setValueAt(CalendarToString(cData.getCompleteDate()),index,6);
			
		this.repaint();
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
	private String ByteArrayToString(byte[] array){
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
}
