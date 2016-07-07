/* ************************************************************** *
 *                                                                *
 * Copyright (c) 2005, Ryo Kanbayashi, All rights reserved.       *
 *                                                                *
 *                                                                *
 * This software is distributed under the modified BSD License.   *
 * ************************************************************** */

import java.io.Serializable;

//�N���C�A���g���i���I�ɕێ����Ă����f�[�^�B�Ǝ��`���̃f�[�^��ێ������Ă����������͎������邱��
public abstract class ClientMemo implements Serializable{

    /**
     * 
     * @uml.property name="version" multiplicity="(0 1)"
     */
    protected int version = 0; //�v�Z�@�\�̃o�[�W����

	protected boolean shouldInterrupt;       //�v�Z�𒆒f���Ȃ��Ă͂Ȃ�Ȃ����ǂ���	
	abstract public int getVersion();

    /**
     * 
     * @uml.property name="version"
     */
    abstract public void setVersion(int version);

	abstract public void setInterruption(boolean flag);
	abstract public boolean getInterruption();
}
