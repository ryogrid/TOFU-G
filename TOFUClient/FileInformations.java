/* ************************************************************** *
 *                                                                *
 * Copyright (c) 2005, Ryo Kanbayashi, All rights reserved.       *
 *                                                                *
 *                                                                *
 * This software is distributed under the modified BSD License.   *
 * ************************************************************** */

import java.io.*;

public class FileInformations implements Serializable {
	public long fileLengths[] = new long[10];						//送信するファイルの長さを保持する配列 終端は値を-1とする
	public String fileNames[] = new String[10];                     //送信するファイルの名前を保持する配列
	private static final long serialVersionUID = 1L;
//	要素の末尾を表す値を代入しておく
	  public FileInformations(){
		  fileLengths[0] = -1L;
	  }
	
//	新しいファイルの情報を末尾に加える（ファイル数は１０個まで、エラー処理はしていない）	
	  void addFileInformation(File info){     
		  int end=0;
		  for(int i = 0; i != fileLengths.length; i++){  //要素の終わりを探す
			  if(fileLengths[i] == -1L){
				  end = i;
				  break;
			  }
		  }
		  fileLengths[end] = info.length();    //末尾に新しい情報を加える
		  fileLengths[end +1] = -1L;
		  fileNames[end] = info.getName();
	  }
}
