/* ************************************************************** *
 *                                                                *
 * Copyright (c) 2005, Ryo Kanbayashi, All rights reserved.       *
 *                                                                *
 *                                                                *
 * This software is distributed under the modified BSD License.   *
 * ************************************************************** */

// 違法なリザルトが発見された場合にスローされる
public class FindillegalResultException extends Exception {
private byte[] illegalDataSigniture = null;   //発見されたデータのシグニチャ
private ClientInfo[] illegalClients = new ClientInfo[2];    //不正と考えられる２名のクライアントの情報を保持
	
	public FindillegalResultException(byte[] signiture,ClientInfo[] illegals){
		super("ユーザーによって接続処理がキャンセルされました");
		this.illegalDataSigniture = signiture;
		this.illegalClients = illegals;
	}
	
	//発見された不正と思われるデータのシグニチャを設定する
	public void setIllegalDataSigniture(byte[] signiture){
		this.illegalDataSigniture = signiture;
	}
	
	//不正と思われるクライアント２人のデータを保持する
	public void setIllegalClients(ClientInfo[] illegals){
		this.illegalClients = illegals;
	}
	
	public byte[] getIllegalDataSigniture(){
		return this.illegalDataSigniture;
	}
	
	
	public ClientInfo[] getIllegalClients(){
		return this.illegalClients;
	}
	
	
}
