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
    protected byte signiture[] = null;       //�W���u�����ʂ��邽�߂̃V�O�j�`��
	protected String jobName = null;          //�W���u���i���[�U�[����`)
	protected Calendar distributeDate = null;      //�W���u���z�z���ꂽ����
	protected Calendar completeDate = null;        //�W���u�̌v�Z���I����������
	protected double necessaryTime = 0;              //���̃W���u�̌v�Z�ɗv��������
	protected boolean culculateComplete = false;     //�v�Z���I�����Ă��邩�Btrue=���� false=�v�Z��
	protected String resultDescription = "";        //�v�Z���ʂ�\�����镶����i���[�U�[��`)
	protected int version = 0;         //�v�Z�f�[�^�̃o�[�W����
	
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
	
	//�v�Z���I�����Ă��邩�𓾂�
	abstract public boolean isCulculateComplete();
	
	//�v�Z���I�����Ă��邩���Z�b�g
	abstract public void setCulculateComplete(boolean culculateComplete);
	
	abstract public void setVersion(int version);


	// public void SetData(); ������ύX����SetData�֐���K�X��������i�T�[�o���p�j

	// public void GetResult(); ������ύX����GetResult�֐���K�X��������i�T�[�o���p�j

}