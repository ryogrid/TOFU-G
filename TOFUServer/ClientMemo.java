/* ************************************************************** *
 *                                                                *
 * Copyright (c) 2005, Ryo Kanbayashi, All rights reserved.       *
 *                                                                *
 *                                                                *
 * This software is distributed under the modified BSD License.   *
 * ************************************************************** */

import java.io.Serializable;

//クライアントが永続的に保持しておくデータ。独自形式のデータを保持させておきたい時は実装すること
public abstract class ClientMemo implements Serializable{

    /**
     * 
     * @uml.property name="version" multiplicity="(0 1)"
     */
    protected int version = 0; //計算機構のバージョン

	protected boolean shouldInterrupt;       //計算を中断しなくてはならないかどうか	
	abstract public int getVersion();

    /**
     * 
     * @uml.property name="version"
     */
    abstract public void setVersion(int version);

	abstract public void setInterruption(boolean flag);
	abstract public boolean getInterruption();
}
