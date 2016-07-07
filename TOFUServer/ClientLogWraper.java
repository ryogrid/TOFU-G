/* ************************************************************** *
 *                                                                *
 * Copyright (c) 2005, Ryo Kanbayashi, All rights reserved.       *
 *                                                                *
 *                                                                *
 * This software is distributed under the modified BSD License.   *
 * ************************************************************** */

import java.io.Serializable;

//���O��ێ����鎞�ɁA�v�Z�f�[�^�Ƃ͊ւ��̂Ȃ��A�N���C�A���g�̏���ێ�����ꍇ�����邩������Ȃ�
//���̏ꍇ���̃N���X�ɂ����̃f�[�^��ێ�����B�v��CulculateData�̃��b�p�[
public class ClientLogWraper implements Serializable {

    /**
     * 
     * @uml.property name="wrapped"
     * @uml.associationEnd multiplicity="(1 1)"
     */
    private CulculateData wrapped = null; //���b�s���O�����ߋ���job

    /**
     * 
     * @uml.property name="wrapped"
     */
    //�ߋ����O�ɕێ��������f�[�^������ꍇ�͓K�X�t�B�[���h�ɒǉ����邱��
    public CulculateData getWrapped() {
        return wrapped;
    }

	
	//CulculateData��^���ĐV����LogWraper�𐶐�
	public ClientLogWraper(CulculateData wrapped) {
		this.wrapped = wrapped;
	}

    /**
     * 
     * @uml.property name="wrapped"
     */
    public void setWrapped(CulculateData wrapped) {
        this.wrapped = wrapped;
    }

}
