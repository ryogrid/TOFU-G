/* ************************************************************** *
 *                                                                *
 * Copyright (c) 2005, Ryo Kanbayashi, All rights reserved.       *
 *                                                                *
 *                                                                *
 * This software is distributed under the modified BSD License.   *
 * ************************************************************** */

import java.io.Serializable;

public abstract class Culculater implements Serializable{
    protected int version = 0;         //�v�Z�f�[�^�̃o�[�W����
    
	//������I���̏ꍇ�͂P�A�r�����f���܂ߐ���I�����Ȃ������ꍇ�͂O��Ԃ�
	abstract public int culculate(CulculateData data, ClientMemo memo);
	abstract public String getString();
	abstract public int getVersion();
	abstract public void setVersion(int version);
}
