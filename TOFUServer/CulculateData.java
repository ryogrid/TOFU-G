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
    protected byte signiture[] = null; //�W���u�����ʂ��邽�߂̃V�O�j�`��

    /**
     * 
     * @uml.property name="jobName" multiplicity="(0 1)"
     */
    protected String jobName = null; //�W���u���i���[�U�[����`)

    /**
     * 
     * @uml.property name="distributeDate" multiplicity="(0 1)"
     */
    protected Calendar distributeDate = null; //�W���u���z�z���ꂽ����

    /**
     * 
     * @uml.property name="completeDate" multiplicity="(0 1)"
     */
    protected Calendar completeDate = null; //�W���u�̌v�Z���I����������

    /**
     * 
     * @uml.property name="necessaryTime" multiplicity="(0 1)"
     */
    protected double necessaryTime = 0; //���̃W���u�̌v�Z�ɗv��������

    /**
     * 
     * @uml.property name="culculateComplete" multiplicity="(0 1)"
     */
    protected boolean culculateComplete = false; //�v�Z���I�����Ă��邩�Btrue=���� false=�v�Z��

    /**
     * 
     * @uml.property name="resultDescription" multiplicity="(0 1)"
     */
    protected String resultDescription = ""; //�v�Z���ʂ�\�����镶����i���[�U�[��`)

    /**
     * 
     * @uml.property name="version" multiplicity="(0 1)"
     */
    protected int version = 0; //�v�Z�f�[�^�̃o�[�W����

	
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
	
	//�v�Z���I�����Ă��邩�𓾂�
	abstract public boolean isCulculateComplete();

    /**
     * 
     * @uml.property name="culculateComplete"
     */
    //�v�Z���I�����Ă��邩���Z�b�g
    abstract public void setCulculateComplete(boolean culculateComplete);

    /**
     * 
     * @uml.property name="version"
     */
    abstract public void setVersion(int version);

	// public void SetData(); ������ύX����SetData�֐���K�X��������i�T�[�o���p�j

	// public void GetResult(); ������ύX����GetResult�֐���K�X��������i�T�[�o���p�j

}