/* ************************************************************** *
 *                                                                *
 * Copyright (c) 2005, Ryo Kanbayashi, All rights reserved.       *
 *                                                                *
 *                                                                *
 * This software is distributed under the modified BSD License.   *
 * ************************************************************** */

//�K��񐔐ڑ������s��������throw������O�N���X
public class FullConnectFailedException extends Exception {
	public FullConnectFailedException(){
		super("�K��񐔃T�[�o�ւ̐ڑ������s���܂���");
	}
}
