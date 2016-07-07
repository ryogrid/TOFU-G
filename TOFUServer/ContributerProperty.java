/* ************************************************************** *
 *                                                                *
 * Copyright (c) 2005, Ryo Kanbayashi, All rights reserved.       *
 *                                                                *
 *                                                                *
 * This software is distributed under the modified BSD License.   *
 * ************************************************************** */

import java.io.Serializable;
import java.util.Calendar;

//計算してくれたクライアントの属性を表現するクラス。今のところServerLogWraperの内部で使用
public class ContributerProperty implements Serializable {

    /**
     * 
     * @uml.property name="contributer"
     * @uml.associationEnd multiplicity="(1 1)"
     */
    public ClientInfo contributer = null; //計算した人

	public Calendar distributeDate = null;	    //配布した日時
	public Calendar receiveDate = null;       //計算結果が返ってきた日時

	public ContributerProperty(ClientInfo contributer,Calendar distribute,Calendar receive){
		this.contributer = contributer;
		this.distributeDate = distribute;
		this.receiveDate = receive;
	}

}
