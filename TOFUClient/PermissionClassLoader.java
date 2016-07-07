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

//普通のloadに加え、classpath範囲内に存在しないクラスの場合はパーミッションを設定してクラスをロードする、setPath();を必ず読んでからloadClassすること
public class PermissionClassLoader extends ClassLoader implements Serializable {
	private ProtectDomainMaker domainMaker = new ProtectDomainMaker();
	private String path = "";    //classpathの範囲にクラスが存在しない場合に探す場所、loadClassを利用する前に適切に設定
	private String pathCandidate[];    //classpathの範囲にクラスが存在しない場合に探す場所の候補 path か
							   // pathCandidateのどちらかを使用
	
    private ArrayList jarFileURLs = new ArrayList();     //ロード対象のjarファイルのURLs
    
	private PermissionClassLoader(){
		
	}
	
	public PermissionClassLoader(ClassLoader loader,String path){
		super(loader);
		this.path = path;
	}
	
	public PermissionClassLoader(ClassLoader loader,String[] path){
		super(loader);
		this.pathCandidate = path;
		this.path = null;          //念のためnullにしておく
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
                    //ロードできないつまり必要とするクラスがjarファイル内のものだった場合
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

	//ProtectionDomainを設定してdefineClassしてくれるメソッド
	private Class regClass(String name) throws FileNotFoundException{
		ByteArrayOutputStream out = new ByteArrayOutputStream(1024);
		
		File classFile = null;
		if(path != null){    //一つのディレクトリが指定されていればそれを利用
			classFile = new File(path + name + ".class");
		}else{    //複数のディレクトリが使用されている場合、どこにクラスファイルがあるか探す
			for(int i = 0;i < pathCandidate.length;i++){
				File tmp = new File(pathCandidate[i] + name);    //試しに指定したファイル名のFileを作ってみる
				if(tmp.exists() == true){     //パスが正解なら
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
    
    //設定してあったURLを全て消して初期化
    public void initURL(){
        jarFileURLs = new ArrayList();
    }
}