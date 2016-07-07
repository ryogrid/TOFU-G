/* ************************************************************** *
 *                                                                *
 * Copyright (c) 2005, Ryo Kanbayashi, All rights reserved.       *
 *                                                                *
 *                                                                *
 * This software is distributed under the modified BSD License.   *
 * ************************************************************** */

import java.io.Serializable;
import java.util.Calendar;
public abstract class CulculateData implements Serializable{
    protected byte signiture[] = null;       //ジョブを識別するためのシグニチャ
	protected String jobName = null;          //ジョブ名（ユーザーが定義)
	protected Calendar distributeDate = null;      //ジョブが配布された日時
	protected Calendar completeDate = null;        //ジョブの計算が終了した日時
	protected double necessaryTime = 0;              //このジョブの計算に要した時間
	protected boolean culculateComplete = false;     //計算が終了しているか。true=完了 false=計算中
	protected String resultDescription = "";        //計算結果を表現する文字列（ユーザー定義)
	protected int version = 0;         //計算データのバージョン
	
	abstract public double getNecessaryTime();
	abstract public void setNecessaryTime(double necessaryTime);
	abstract public Calendar getCompleteDate();
	abstract public void setCompleteDate(Calendar completeDate);
	abstract public Calendar getDistributeDate();
	abstract public void setDistributeDate(Calendar distributeDate);
	abstract public byte[] getSigniture();
	abstract public void setSigniture(byte[] signiture);
	abstract public String getJobName();
	abstract public void setJobName(String jobName);
	abstract public String getResultDescription();
	abstract public void setResultDescription(String resultDescription);
	abstract public int getVersion();
	abstract public boolean isResultEqual(CulculateData target);
	
	//計算が終了しているかを得る
	abstract public boolean isCulculateComplete();
	
	//計算が終了しているかをセット
	abstract public void setCulculateComplete(boolean culculateComplete);
	
	abstract public void setVersion(int version);


	// public void SetData(); 引数を変更したSetData関数を適宜実装せよ（サーバ側用）

	// public void GetResult(); 引数を変更したGetResult関数を適宜実装せよ（サーバ側用）

}