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

    /**
     * 
     * @uml.property name="signiture" multiplicity="(0 1)"
     */
    protected byte signiture[] = null; //ジョブを識別するためのシグニチャ

    /**
     * 
     * @uml.property name="jobName" multiplicity="(0 1)"
     */
    protected String jobName = null; //ジョブ名（ユーザーが定義)

    /**
     * 
     * @uml.property name="distributeDate" multiplicity="(0 1)"
     */
    protected Calendar distributeDate = null; //ジョブが配布された日時

    /**
     * 
     * @uml.property name="completeDate" multiplicity="(0 1)"
     */
    protected Calendar completeDate = null; //ジョブの計算が終了した日時

    /**
     * 
     * @uml.property name="necessaryTime" multiplicity="(0 1)"
     */
    protected double necessaryTime = 0; //このジョブの計算に要した時間

    /**
     * 
     * @uml.property name="culculateComplete" multiplicity="(0 1)"
     */
    protected boolean culculateComplete = false; //計算が終了しているか。true=完了 false=計算中

    /**
     * 
     * @uml.property name="resultDescription" multiplicity="(0 1)"
     */
    protected String resultDescription = ""; //計算結果を表現する文字列（ユーザー定義)

    /**
     * 
     * @uml.property name="version" multiplicity="(0 1)"
     */
    protected int version = 0; //計算データのバージョン

	
	abstract public double getNecessaryTime();

    /**
     * 
     * @uml.property name="necessaryTime"
     */
    abstract public void setNecessaryTime(double necessaryTime);

    /**
     * 
     * @uml.property name="completeDate"
     */
    abstract public Calendar getCompleteDate();

    /**
     * 
     * @uml.property name="completeDate"
     */
    abstract public void setCompleteDate(Calendar completeDate);

    /**
     * 
     * @uml.property name="distributeDate"
     */
    abstract public Calendar getDistributeDate();

    /**
     * 
     * @uml.property name="distributeDate"
     */
    abstract public void setDistributeDate(Calendar distributeDate);

	abstract public byte[] getSigniture();

    /**
     * 
     * @uml.property name="signiture"
     */
    abstract public void setSigniture(byte[] signiture);

    /**
     * 
     * @uml.property name="jobName"
     */
    abstract public String getJobName();

    /**
     * 
     * @uml.property name="jobName"
     */
    abstract public void setJobName(String jobName);

    /**
     * 
     * @uml.property name="resultDescription"
     */
    abstract public String getResultDescription();

    /**
     * 
     * @uml.property name="resultDescription"
     */
    abstract public void setResultDescription(String resultDescription);

	abstract public int getVersion();
	abstract public boolean isResultEqual(CulculateData target);
	
	//計算が終了しているかを得る
	abstract public boolean isCulculateComplete();

    /**
     * 
     * @uml.property name="culculateComplete"
     */
    //計算が終了しているかをセット
    abstract public void setCulculateComplete(boolean culculateComplete);

    /**
     * 
     * @uml.property name="version"
     */
    abstract public void setVersion(int version);

	// public void SetData(); 引数を変更したSetData関数を適宜実装せよ（サーバ側用）

	// public void GetResult(); 引数を変更したGetResult関数を適宜実装せよ（サーバ側用）

}