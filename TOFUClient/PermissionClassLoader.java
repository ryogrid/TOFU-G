/* ************************************************************** *
 *                                                                *
 * Copyright (c) 2005, Ryo Kanbayashi, All rights reserved.       *
 *                                                                *
 *                                                                *
 * This software is distributed under the modified BSD License.   *
 * ************************************************************** */

import java.io.*;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Hashtable;

//���ʂ�load�ɉ����Aclasspath�͈͓��ɑ��݂��Ȃ��N���X�̏ꍇ�̓p�[�~�b�V������ݒ肵�ăN���X�����[�h����AsetPath();��K���ǂ�ł���loadClass���邱��
public class PermissionClassLoader extends ClassLoader implements Serializable {
	private ProtectDomainMaker domainMaker = new ProtectDomainMaker();
	private String path = "";    //classpath�͈̔͂ɃN���X�����݂��Ȃ��ꍇ�ɒT���ꏊ�AloadClass�𗘗p����O�ɓK�؂ɐݒ�
	private String pathCandidate[];    //classpath�͈̔͂ɃN���X�����݂��Ȃ��ꍇ�ɒT���ꏊ�̌�� path ��
							   // pathCandidate�̂ǂ��炩���g�p
	
    private ArrayList jarFileURLs = new ArrayList();     //���[�h�Ώۂ�jar�t�@�C����URLs
    
	private PermissionClassLoader(){
		
	}
	
	public PermissionClassLoader(ClassLoader loader,String path){
		super(loader);
		this.path = path;
	}
	
	public PermissionClassLoader(ClassLoader loader,String[] path){
		super(loader);
		this.pathCandidate = path;
		this.path = null;          //�O�̂���null�ɂ��Ă���
	}
	
	public PermissionClassLoader(ClassLoader loader){
		super(loader);
	}
	
	public void setPath(String path){
		this.path = path;
	}
	
	public void setPathCandidate(String[] pathCandidate){
		this.pathCandidate=pathCandidate;
	}
	
	protected Class loadClass(String name, boolean resolve)
			throws ClassNotFoundException {
		Hashtable cache = new Hashtable();
		Class cl = null;

		cl = findLoadedClass(name);

		if (cl == null) {
			cl = (Class) cache.get(name);
			if (cl == null) {
				try {
						return findSystemClass(name);
				} catch (Throwable e) {
				}

				try {
					cache.put(name, (cl = regClass(name)));
                }catch(FileNotFoundException e){
                    //���[�h�ł��Ȃ��܂�K�v�Ƃ���N���X��jar�t�@�C�����̂��̂������ꍇ
                    Object urlsInObject[] =  jarFileURLs.toArray();
                    URL urls[] = new URL[urlsInObject.length];
                    for(int i=0;i<urls.length;i++){
                        urls[i] = (URL)urlsInObject[i];
                    }
                    
                    URLClassLoader urlLoader = new URLClassLoader(urls);
                    cache.put(name, cl = urlLoader.loadClass(name));
                } catch (Exception e) {
                    System.out.println(e);
				}
			}
		}
		if (resolve) {
			resolveClass(cl);
		}
		return cl;
	}

	//ProtectionDomain��ݒ肵��defineClass���Ă���郁�\�b�h
	private Class regClass(String name) throws FileNotFoundException{
		ByteArrayOutputStream out = new ByteArrayOutputStream(1024);
		
		File classFile = null;
		if(path != null){    //��̃f�B���N�g�����w�肳��Ă���΂���𗘗p
			classFile = new File(path + name + ".class");
		}else{    //�����̃f�B���N�g�����g�p����Ă���ꍇ�A�ǂ��ɃN���X�t�@�C�������邩�T��
			for(int i = 0;i < pathCandidate.length;i++){
				File tmp = new File(pathCandidate[i] + name);    //�����Ɏw�肵���t�@�C������File������Ă݂�
				if(tmp.exists() == true){     //�p�X�������Ȃ�
					classFile = tmp;
					break;
				}
			}
		}
		
		FileInputStream classStream = null;
		classStream = new FileInputStream(classFile);


		byte buff[] = new byte[1024];
		int n;
		int len = 0;
		try {
			while ((n = classStream.read(buff, 0, 1024)) >= 0) {
				out.write(buff, 0, n);
				len += n;
			}
			classStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		byte data[] = new byte[len];
		data = out.toByteArray();
		try {
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return defineClass(name, data, 0, len, domainMaker
				.getProtectionDomain());
	}
    
    public void addURL(URL newURL){
        jarFileURLs.add(newURL);
    }
    
    //�ݒ肵�Ă�����URL��S�ď����ď�����
    public void initURL(){
        jarFileURLs = new ArrayList();
    }
}