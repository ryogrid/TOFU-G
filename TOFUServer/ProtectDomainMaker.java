/* ************************************************************** *
 *                                                                *
 * Copyright (c) 2005, Ryo Kanbayashi, All rights reserved.       *
 *                                                                *
 *                                                                *
 * This software is distributed under the modified BSD License.   *
 * ************************************************************** */

import java.io.*;
import java.security.*;

//クラスローダーでパーミッション設定をするためのProtectionDomainを生成したりするクラス
public class ProtectDomainMaker {
	  private Permissions permissions = new Permissions();

    /**
     * 
     * @uml.property name="protectionDomain" multiplicity="(0 1)"
     */
    private ProtectionDomain protectionDomain;


	  ProtectDomainMaker(){
	  	setPermissions();
	    CodeSource cs = new CodeSource(null,null);
	    protectionDomain = new ProtectionDomain(cs,permissions);
	    System.setSecurityManager(new SecurityManager());
	  }
	  
	  private void setPermissions(){
	    try{
//		    Permission readPermission = new FilePermission("file:./-","read");
//		    Permission writePermission = new FilePermission("file:./-","write");
		    Permission readPermission = new FilePermission("-","read");
		    Permission writePermission = new FilePermission("-","write");
		    
		    permissions.add(readPermission);
	    	permissions.add(writePermission);
	    	
	    }catch(Exception e){
	      e.printStackTrace();
	    }
	  }

    /**
     * 
     * @uml.property name="protectionDomain"
     */
    public ProtectionDomain getProtectionDomain() {
        return protectionDomain;
    }

}
