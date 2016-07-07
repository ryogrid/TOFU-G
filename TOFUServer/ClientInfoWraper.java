/* ************************************************************** *
 *                                                                *
 * Copyright (c) 2005, Ryo Kanbayashi, All rights reserved.       *
 *                                                                *
 *                                                                *
 * This software is distributed under the modified BSD License.   *
 * ************************************************************** */

//クライアントの情報をラッピングするクラス、ＤＢに格納するために利用する
public class ClientInfoWraper {

    /**
     * 
     * @uml.property name="wrapped"
     * @uml.associationEnd multiplicity="(1 1)"
     */
    private ClientInfo wrapped = null; //ラッピングされる過去のjob

    /**
     * 
     * @uml.property name="wrapped"
     */
    //過去ログに保持したいデータがある場合は適宜フィールドに追加すること
    public ClientInfo getWrapped() {
        return wrapped;
    }

	
	//CulculateDataを与えて新しいLogWraperを生成
	public ClientInfoWraper(ClientInfo wrapped) {
		this.wrapped = wrapped;
	}

    /**
     * 
     * @uml.property name="wrapped"
     */
    public void setWrapped(ClientInfo wrapped) {
        this.wrapped = wrapped;
    }

}
