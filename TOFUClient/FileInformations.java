/* ************************************************************** *
 *                                                                *
 * Copyright (c) 2005, Ryo Kanbayashi, All rights reserved.       *
 *                                                                *
 *                                                                *
 * This software is distributed under the modified BSD License.   *
 * ************************************************************** */

import java.io.*;

public class FileInformations implements Serializable {
	public long fileLengths[] = new long[10];						//���M����t�@�C���̒�����ێ�����z�� �I�[�͒l��-1�Ƃ���
	public String fileNames[] = new String[10];                     //���M����t�@�C���̖��O��ێ�����z��
	private static final long serialVersionUID = 1L;
//	�v�f�̖�����\���l�������Ă���
	  public FileInformations(){
		  fileLengths[0] = -1L;
	  }
	
//	�V�����t�@�C���̏��𖖔��ɉ�����i�t�@�C�����͂P�O�܂ŁA�G���[�����͂��Ă��Ȃ��j	
	  void addFileInformation(File info){     
		  int end=0;
		  for(int i = 0; i != fileLengths.length; i++){  //�v�f�̏I����T��
			  if(fileLengths[i] == -1L){
				  end = i;
				  break;
			  }
		  }
		  fileLengths[end] = info.length();    //�����ɐV��������������
		  fileLengths[end +1] = -1L;
		  fileNames[end] = info.getName();
	  }
}
