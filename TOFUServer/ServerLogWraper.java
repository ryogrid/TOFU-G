/* ************************************************************** *
 *                                                                *
 * Copyright (c) 2005, Ryo Kanbayashi, All rights reserved.       *
 *                                                                *
 *                                                                *
 * This software is distributed under the modified BSD License.   *
 * ************************************************************** */

import java.io.Serializable;
import java.util.ArrayList;

//ログを保持する時に、計算データとは関わりのない、クライアントの情報を保持する場合があるかもしれない
//その場合このクラスにそれらのデータを保持する。要はCulculateDataのラッパー
public class ServerLogWraper implements Serializable {
	private int redundancyCount = 2;            //配布された時点でユーザーが要求していた冗長性の値

    /**
     * 
     * @uml.property name="wrapped"
     * @uml.associationEnd multiplicity="(0 1)"
     */
    private CulculateData wrapped = null; //ラッピングされる過去のjob

    /**
     * 
     * @uml.property name="reserveData"
     * @uml.associationEnd multiplicity="(0 1)"
     */
    private CulculateData reserveData = null; //まだ正しい結果と判断されたわけではない状態のデータ。比較待ち

    /**
     * 
     * @uml.property name="contributedMans"
     * @uml.associationEnd elementType="ContributerProperty" multiplicity="(0 -1)"
     */
    private ArrayList contributedMans = new ArrayList(); //配布されたクライアント達の情報、ContributerPropertyで保持（返却後使用）

    /**
     * 
     * @uml.property name="distributeMans"
     * @uml.associationEnd elementType="[B" multiplicity="(0 -1)"
     */
    private ArrayList distributeMans = new ArrayList(); //配布されたクライアント達の情報（返却前使用）シグニチャだけを保持

    /**
     * 
     * @uml.property name="wrapped"
     */
    //過去ログに保持したいデータがある場合は適宜フィールドに追加すること
    public CulculateData getWrapped() {
        return wrapped;
    }

	
	//CulculateDataを与えて新しいLogWraperを生成
	public ServerLogWraper(CulculateData wrapped,int redundancy) {
		this.wrapped = wrapped;
		redundancyCount = redundancy;
	}
	
	//クライアントのシグニチャを保持させる。配布した時に使用
	public void addDistributer(byte[] signiture){
		distributeMans.add(signiture);
	}
	
//	CulculateDataを与えて新しいLogWraperを生成
	public ServerLogWraper(CulculateData wrapped) {
		this.reserveData = wrapped;
	}

    /**
     * 
     * @uml.property name="wrapped"
     */
    public void setWrapped(CulculateData wrapped) {
        this.wrapped = wrapped;
    }

	
	public void addContributer(ContributerProperty contributer){
		contributedMans.add(contributer);
	}
	
	//過去に同じクライアントに配布したのではないかチェック
	public boolean checkFormerDistributer(byte[] signiture){
		
		for(int i=0;i < distributeMans.size();i++){
			if(byteArrayToString((byte[])distributeMans.get(i)).equals(byteArrayToString(signiture))){
				return true;
			}
		}
		return false;
	}
	
	public boolean checkCompleted(CulculateData result,ContributerProperty contributer)throws FindillegalResultException{
		
		if(reserveData!=null){
			//比較において結果が正しかった場合
			if(result.isResultEqual(reserveData)){
				addContributer(contributer);    //貢献者を追加
				//かつ規定数の比較を終えていた場合
				if(contributedMans.size() >= redundancyCount){
					wrapped = result;     //正式な計算結果とする
					return true;
				}else{   //まだ比較を続けなくてはならない場合
					reserveData = result;
					return false;
				}
			}else{        //計算結果に食い違いがあった場合
				//食い違った２人のクライアントを配列へ
				ClientInfo[] tmp = {contributer.contributer,(ClientInfo)contributedMans.get(contributedMans.size()-1)};
				FindillegalResultException e = new FindillegalResultException(result.getSigniture(),tmp);
				throw e;
			}
		
		}else if((redundancyCount != 1) && (reserveData == null)){    //初回の判定結果の場合、候補に設定しておく
			addContributer(contributer);    //貢献者を追加
			reserveData = result;
			return false;
		}else if((redundancyCount == 1)){   //冗長度が１の場合、問答無用で信頼する
			addContributer(contributer);    //貢献者を追加
			wrapped = result;     //正式な計算結果とする
			return true;
		}
		return false;
	}
	
//	バイト配列をStringの文字列へ変換
	private String byteArrayToString(byte[] array) {
		StringBuffer tmp = new StringBuffer();
		for (int i = 0; i < array.length; i++) {
			tmp.append(array[i]);
		}
		String result = tmp.toString();
		return result;
	}

    /**
     * 
     * @uml.property name="distributeMans"
     */
    public ArrayList getDistributeMans() {
        return distributeMans;
    }

    /**
     * 
     * @uml.property name="contributedMans"
     */
    public ArrayList getContributedMans() {
        return contributedMans;
    }

	
	//いくつの結果が返ってきたかという、途中経過を返す。
	public int getProgress(){
		return contributedMans.size();
	}
	
	//冗長性の値を返す
	public int getRedundancy(){
		return redundancyCount;
	}
}
