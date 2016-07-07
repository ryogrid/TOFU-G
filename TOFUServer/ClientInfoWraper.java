/* ************************************************************** *
 *                                                                *
 * Copyright (c) 2005, Ryo Kanbayashi, All rights reserved.       *
 *                                                                *
 *                                                                *
 * This software is distributed under the modified BSD License.   *
 * ************************************************************** */

//�N���C�A���g�̏������b�s���O����N���X�A�c�a�Ɋi�[���邽�߂ɗ��p����
public class ClientInfoWraper {

    /**
     * 
     * @uml.property name="wrapped"
     * @uml.associationEnd multiplicity="(1 1)"
     */
    private ClientInfo wrapped = null; //���b�s���O�����ߋ���job

    /**
     * 
     * @uml.property name="wrapped"
     */
    //�ߋ����O�ɕێ��������f�[�^������ꍇ�͓K�X�t�B�[���h�ɒǉ����邱��
    public ClientInfo getWrapped() {
        return wrapped;
    }

	
	//CulculateData��^���ĐV����LogWraper�𐶐�
	public ClientInfoWraper(ClientInfo wrapped) {
		this.wrapped = wrapped;
	}

    /**
     * 
     * @uml.property name="wrapped"
     */
    public void setWrapped(ClientInfo wrapped) {
        this.wrapped = wrapped;
    }

}
