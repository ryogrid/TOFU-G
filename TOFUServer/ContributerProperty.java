/* ************************************************************** *
 *                                                                *
 * Copyright (c) 2005, Ryo Kanbayashi, All rights reserved.       *
 *                                                                *
 *                                                                *
 * This software is distributed under the modified BSD License.   *
 * ************************************************************** */

import java.io.Serializable;
import java.util.Calendar;

//�v�Z���Ă��ꂽ�N���C�A���g�̑�����\������N���X�B���̂Ƃ���ServerLogWraper�̓����Ŏg�p
public class ContributerProperty implements Serializable {

    /**
     * 
     * @uml.property name="contributer"
     * @uml.associationEnd multiplicity="(1 1)"
     */
    public ClientInfo contributer = null; //�v�Z�����l

	public Calendar distributeDate = null;	    //�z�z��������
	public Calendar receiveDate = null;       //�v�Z���ʂ��Ԃ��Ă�������

	public ContributerProperty(ClientInfo contributer,Calendar distribute,Calendar receive){
		this.contributer = contributer;
		this.distributeDate = distribute;
		this.receiveDate = receive;
	}

}
