/* ************************************************************** *
 *                                                                *
 * Copyright (c) 2005, Ryo Kanbayashi, All rights reserved.       *
 *                                                                *
 *                                                                *
 * This software is distributed under the modified BSD License.   *
 * ************************************************************** */

import java.io.Serializable;
import java.util.ArrayList;

//���O��ێ����鎞�ɁA�v�Z�f�[�^�Ƃ͊ւ��̂Ȃ��A�N���C�A���g�̏���ێ�����ꍇ�����邩������Ȃ�
//���̏ꍇ���̃N���X�ɂ����̃f�[�^��ێ�����B�v��CulculateData�̃��b�p�[
public class ServerLogWraper implements Serializable {
	private int redundancyCount = 2;            //�z�z���ꂽ���_�Ń��[�U�[���v�����Ă����璷���̒l

    /**
     * 
     * @uml.property name="wrapped"
     * @uml.associationEnd multiplicity="(0 1)"
     */
    private CulculateData wrapped = null; //���b�s���O�����ߋ���job

    /**
     * 
     * @uml.property name="reserveData"
     * @uml.associationEnd multiplicity="(0 1)"
     */
    private CulculateData reserveData = null; //�܂����������ʂƔ��f���ꂽ�킯�ł͂Ȃ���Ԃ̃f�[�^�B��r�҂�

    /**
     * 
     * @uml.property name="contributedMans"
     * @uml.associationEnd elementType="ContributerProperty" multiplicity="(0 -1)"
     */
    private ArrayList contributedMans = new ArrayList(); //�z�z���ꂽ�N���C�A���g�B�̏��AContributerProperty�ŕێ��i�ԋp��g�p�j

    /**
     * 
     * @uml.property name="distributeMans"
     * @uml.associationEnd elementType="[B" multiplicity="(0 -1)"
     */
    private ArrayList distributeMans = new ArrayList(); //�z�z���ꂽ�N���C�A���g�B�̏��i�ԋp�O�g�p�j�V�O�j�`��������ێ�

    /**
     * 
     * @uml.property name="wrapped"
     */
    //�ߋ����O�ɕێ��������f�[�^������ꍇ�͓K�X�t�B�[���h�ɒǉ����邱��
    public CulculateData getWrapped() {
        return wrapped;
    }

	
	//CulculateData��^���ĐV����LogWraper�𐶐�
	public ServerLogWraper(CulculateData wrapped,int redundancy) {
		this.wrapped = wrapped;
		redundancyCount = redundancy;
	}
	
	//�N���C�A���g�̃V�O�j�`����ێ�������B�z�z�������Ɏg�p
	public void addDistributer(byte[] signiture){
		distributeMans.add(signiture);
	}
	
//	CulculateData��^���ĐV����LogWraper�𐶐�
	public ServerLogWraper(CulculateData wrapped) {
		this.reserveData = wrapped;
	}

    /**
     * 
     * @uml.property name="wrapped"
     */
    public void setWrapped(CulculateData wrapped) {
        this.wrapped = wrapped;
    }

	
	public void addContributer(ContributerProperty contributer){
		contributedMans.add(contributer);
	}
	
	//�ߋ��ɓ����N���C�A���g�ɔz�z�����̂ł͂Ȃ����`�F�b�N
	public boolean checkFormerDistributer(byte[] signiture){
		
		for(int i=0;i < distributeMans.size();i++){
			if(byteArrayToString((byte[])distributeMans.get(i)).equals(byteArrayToString(signiture))){
				return true;
			}
		}
		return false;
	}
	
	public boolean checkCompleted(CulculateData result,ContributerProperty contributer)throws FindillegalResultException{
		
		if(reserveData!=null){
			//��r�ɂ����Č��ʂ������������ꍇ
			if(result.isResultEqual(reserveData)){
				addContributer(contributer);    //�v���҂�ǉ�
				//���K�萔�̔�r���I���Ă����ꍇ
				if(contributedMans.size() >= redundancyCount){
					wrapped = result;     //�����Ȍv�Z���ʂƂ���
					return true;
				}else{   //�܂���r�𑱂��Ȃ��Ă͂Ȃ�Ȃ��ꍇ
					reserveData = result;
					return false;
				}
			}else{        //�v�Z���ʂɐH���Ⴂ���������ꍇ
				//�H��������Q�l�̃N���C�A���g��z���
				ClientInfo[] tmp = {contributer.contributer,(ClientInfo)contributedMans.get(contributedMans.size()-1)};
				FindillegalResultException e = new FindillegalResultException(result.getSigniture(),tmp);
				throw e;
			}
		
		}else if((redundancyCount != 1) && (reserveData == null)){    //����̔��茋�ʂ̏ꍇ�A���ɐݒ肵�Ă���
			addContributer(contributer);    //�v���҂�ǉ�
			reserveData = result;
			return false;
		}else if((redundancyCount == 1)){   //�璷�x���P�̏ꍇ�A�ⓚ���p�ŐM������
			addContributer(contributer);    //�v���҂�ǉ�
			wrapped = result;     //�����Ȍv�Z���ʂƂ���
			return true;
		}
		return false;
	}
	
//	�o�C�g�z���String�̕�����֕ϊ�
	private String byteArrayToString(byte[] array) {
		StringBuffer tmp = new StringBuffer();
		for (int i = 0; i < array.length; i++) {
			tmp.append(array[i]);
		}
		String result = tmp.toString();
		return result;
	}

    /**
     * 
     * @uml.property name="distributeMans"
     */
    public ArrayList getDistributeMans() {
        return distributeMans;
    }

    /**
     * 
     * @uml.property name="contributedMans"
     */
    public ArrayList getContributedMans() {
        return contributedMans;
    }

	
	//�����̌��ʂ��Ԃ��Ă������Ƃ����A�r���o�߂�Ԃ��B
	public int getProgress(){
		return contributedMans.size();
	}
	
	//�璷���̒l��Ԃ�
	public int getRedundancy(){
		return redundancyCount;
	}
}
