/* ************************************************************** *
 *                                                                *
 * Copyright (c) 2005, Ryo Kanbayashi, All rights reserved.       *
 *                                                                *
 *                                                                *
 * This software is distributed under the modified BSD License.   *
 * ************************************************************** */

import java.io.Serializable;

public abstract class Culculater implements Serializable{

    protected int version = 0; //計算データのバージョン

    
	//※正常終了の場合は１、途中中断を含め正常終了しなかった場合は０を返す
	abstract public int culculate(CulculateData data, ClientMemo memo);

    /**
     * 
     * @uml.property name="string" multiplicity="(0 1)"
     */
    abstract public String getString();

	abstract public int getVersion();

    /**
     * 
     * @uml.property name="version"
     */
    abstract public void setVersion(int version);

}
