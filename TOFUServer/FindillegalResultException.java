/* ************************************************************** *
 *                                                                *
 * Copyright (c) 2005, Ryo Kanbayashi, All rights reserved.       *
 *                                                                *
 *                                                                *
 * This software is distributed under the modified BSD License.   *
 * ************************************************************** */

// 違法なリザルトが発見された場合にスローされる
public class FindillegalResultException extends Exception {

/**
 * 
 * @uml.property name="illegalDataSigniture" multiplicity="(0 1)"
 */
private byte[] illegalDataSigniture = null; //発見されたデータのシグニチャ

/**
 * 
 * @uml.property name="illegalClients"
 * @uml.associationEnd multiplicity="(0 -1)"
 */
private ClientInfo[] illegalClients = new ClientInfo[2]; //不正と考えられる２名のクライアントの情報を保持

	
	public FindillegalResultException(byte[] signiture,ClientInfo[] illegals){
		super("ユーザーによって接続処理がキャンセルされました");
		this.illegalDataSigniture = signiture;
		this.illegalClients = illegals;
	}

    /**
     * 
     * @uml.property name="illegalDataSigniture"
     */
    //発見された不正と思われるデータのシグニチャを設定する
    public void setIllegalDataSigniture(byte[] signiture) {
        this.illegalDataSigniture = signiture;
    }

    /**
     * 
     * @uml.property name="illegalClients"
     */
    //不正と思われるクライアント２人のデータを保持する
    public void setIllegalClients(ClientInfo[] illegals) {
        this.illegalClients = illegals;
    }

    /**
     * 
     * @uml.property name="illegalDataSigniture"
     */
    public byte[] getIllegalDataSigniture() {
        return this.illegalDataSigniture;
    }

    /**
     * 
     * @uml.property name="illegalClients"
     */
    public ClientInfo[] getIllegalClients() {
        return this.illegalClients;
    }

	
	
}
