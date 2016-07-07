/* ************************************************************** *
 *                                                                *
 * Copyright (c) 2005, Ryo Kanbayashi, All rights reserved.       *
 *                                                                *
 *                                                                *
 * This software is distributed under the modified BSD License.   *
 * ************************************************************** */

import java.io.*;
import java.io.IOException;
import java.io.ObjectInputStream;

//ローカルかすでにロードされたものの中に受信するオブジェクトのクラスがない場合、与えたクラスローダーを用いて解決するObjectInputStreams
public class MyObjectInputStream extends ObjectInputStream {
	ClassLoader loader = new PermissionClassLoader();
	String serchPath = "";   //classpathの範囲にクラスが存在しない場合に探す場所、loadClassを利用する前に適切に設定
	String serchPathCandidate[];    //classPathの範囲にクラスが存在しない場合に探す場所の候補
	
	//推奨
	public MyObjectInputStream(InputStream in,ClassLoader loader,String path)throws IOException{
		super(in);
		this.loader = loader;
		this.serchPath = path;
	}
	
	//クラスファイルをserchするpathの候補を複数与える
	public MyObjectInputStream(InputStream in,ClassLoader loader,String[] path)throws IOException{
		super(in);
		this.loader = loader;
		serchPathCandidate = path;
		this.serchPath = null;
		
	}
	
	//pathはデフォルトでローカルになる
	public MyObjectInputStream  (InputStream in,ClassLoader loader)throws IOException{
		super(in);
		this.loader = loader;
	}
	
	public MyObjectInputStream(InputStream in) throws IOException {
		super(in);
	}
	
	protected MyObjectInputStream() throws IOException, SecurityException {
		super();
	}
	
	protected Class resolveClass(ObjectStreamClass desc) throws IOException,
			ClassNotFoundException {

		Class tmp;
		String name = desc.getName();
		try{
			tmp = Class.forName(name, false, ClassLoader.getSystemClassLoader());
		}catch(ClassNotFoundException ex){
			
			if(serchPath != null){
				((PermissionClassLoader) loader).setPath(serchPath);
			}else{
				((PermissionClassLoader) loader).setPathCandidate(serchPathCandidate);
			}
			
			tmp = loader.loadClass(name);          //上記の方法で見つからない場合ユーザーが指定したクラスローダーでロード
		}
		
		if(tmp == null){
			throw new ClassNotFoundException();
		}
		
		return tmp;
	}
}
