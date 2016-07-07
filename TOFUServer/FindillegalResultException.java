/* ************************************************************** *
 *                                                                *
 * Copyright (c) 2005, Ryo Kanbayashi, All rights reserved.       *
 *                                                                *
 *                                                                *
 * This software is distributed under the modified BSD License.   *
 * ************************************************************** */

// ��@�ȃ��U���g���������ꂽ�ꍇ�ɃX���[�����
public class FindillegalResultException extends Exception {

/**
 * 
 * @uml.property name="illegalDataSigniture" multiplicity="(0 1)"
 */
private byte[] illegalDataSigniture = null; //�������ꂽ�f�[�^�̃V�O�j�`��

/**
 * 
 * @uml.property name="illegalClients"
 * @uml.associationEnd multiplicity="(0 -1)"
 */
private ClientInfo[] illegalClients = new ClientInfo[2]; //�s���ƍl������Q���̃N���C�A���g�̏���ێ�

	
	public FindillegalResultException(byte[] signiture,ClientInfo[] illegals){
		super("���[�U�[�ɂ���Đڑ��������L�����Z������܂���");
		this.illegalDataSigniture = signiture;
		this.illegalClients = illegals;
	}

    /**
     * 
     * @uml.property name="illegalDataSigniture"
     */
    //�������ꂽ�s���Ǝv����f�[�^�̃V�O�j�`����ݒ肷��
    public void setIllegalDataSigniture(byte[] signiture) {
        this.illegalDataSigniture = signiture;
    }

    /**
     * 
     * @uml.property name="illegalClients"
     */
    //�s���Ǝv����N���C�A���g�Q�l�̃f�[�^��ێ�����
    public void setIllegalClients(ClientInfo[] illegals) {
        this.illegalClients = illegals;
    }

    /**
     * 
     * @uml.property name="illegalDataSigniture"
     */
    public byte[] getIllegalDataSigniture() {
        return this.illegalDataSigniture;
    }

    /**
     * 
     * @uml.property name="illegalClients"
     */
    public ClientInfo[] getIllegalClients() {
        return this.illegalClients;
    }

	
	
}
