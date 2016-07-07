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

//���[�J�������łɃ��[�h���ꂽ���̂̒��Ɏ�M����I�u�W�F�N�g�̃N���X���Ȃ��ꍇ�A�^�����N���X���[�_�[��p���ĉ�������ObjectInputStreams
public class MyObjectInputStream extends ObjectInputStream {
	ClassLoader loader = new PermissionClassLoader();
	String serchPath = "";   //classpath�͈̔͂ɃN���X�����݂��Ȃ��ꍇ�ɒT���ꏊ�AloadClass�𗘗p����O�ɓK�؂ɐݒ�
	String serchPathCandidate[];    //classPath�͈̔͂ɃN���X�����݂��Ȃ��ꍇ�ɒT���ꏊ�̌��
	
	//����
	public MyObjectInputStream(InputStream in,ClassLoader loader,String path)throws IOException{
		super(in);
		this.loader = loader;
		this.serchPath = path;
	}
	
	//�N���X�t�@�C����serch����path�̌��𕡐��^����
	public MyObjectInputStream(InputStream in,ClassLoader loader,String[] path)throws IOException{
		super(in);
		this.loader = loader;
		serchPathCandidate = path;
		this.serchPath = null;
		
	}
	
	//path�̓f�t�H���g�Ń��[�J���ɂȂ�
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
			
			tmp = loader.loadClass(name);          //��L�̕��@�Ō�����Ȃ��ꍇ���[�U�[���w�肵���N���X���[�_�[�Ń��[�h
		}
		
		if(tmp == null){
			throw new ClassNotFoundException();
		}
		
		return tmp;
	}
}
