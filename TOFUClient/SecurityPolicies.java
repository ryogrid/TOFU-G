/* ************************************************************** *
 *                                                                *
 * Copyright (c) 2005, Ryo Kanbayashi, All rights reserved.       *
 *                                                                *
 *                                                                *
 * This software is distributed under the modified BSD License.   *
 * ************************************************************** */

import java.io.*;
public class SecurityPolicies implements Serializable{
	private static final long serialVersionUID = 1L;
	boolean autConnect = false;    //�����ڑ����s���Ă��悢��
	boolean autCulculate = false;   //�����Ōv�Z���s���Ă��悢��
	File preservePath = null;        //�t�@�C����ۑ�����p�X��ێ�
}
