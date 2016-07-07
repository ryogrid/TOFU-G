/* ************************************************************** *
 *                                                                *
 * Copyright (c) 2005, Ryo Kanbayashi, All rights reserved.       *
 *                                                                *
 *                                                                *
 * This software is distributed under the modified BSD License.   *
 * ************************************************************** */

import java.io.*;
import java.io.IOException;
import java.io.ObjectOutputStream;

public class DebugObjectOutputStream extends ObjectOutputStream {
    
    public DebugObjectOutputStream() throws IOException{
        super();
    }
    
    public DebugObjectOutputStream(OutputStream out) throws IOException{
        super(out);
    }

//    protected void writeObjectOverride(Object obj) throws IOException {
//        System.out.println("now write " + obj.getClass().getName());
//        super.writeObjectOverride(obj);
//    }
    
    protected void writeClassDescriptor(ObjectStreamClass desc)
            throws IOException {
        System.out.println("now write:" + desc.getName());
        super.writeClassDescriptor(desc);
    }
}
