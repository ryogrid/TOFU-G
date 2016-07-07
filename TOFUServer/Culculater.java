/* ************************************************************** *
 *                                                                *
 * Copyright (c) 2005, Ryo Kanbayashi, All rights reserved.       *
 *                                                                *
 *                                                                *
 * This software is distributed under the modified BSD License.   *
 * ************************************************************** */

import java.io.Serializable;

public abstract class Culculater implements Serializable{

    protected int version = 0; //�v�Z�f�[�^�̃o�[�W����

    
	//������I���̏ꍇ�͂P�A�r�����f���܂ߐ���I�����Ȃ������ꍇ�͂O��Ԃ�
	abstract public int culculate(CulculateData data, ClientMemo memo);

    /**
     * 
     * @uml.property name="string" multiplicity="(0 1)"
     */
    abstract public String getString();

	abstract public int getVersion();

    /**
     * 
     * @uml.property name="version"
     */
    abstract public void setVersion(int version);

}
