/*
 * Created on Nov 7, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package com.bos.art.logParser.tools;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;

/**
 * @author I0360D3
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class MemoryTool {
	public static int showSize(Object o,String name) {
		int size = 0;
		try {
            synchronized(o) {
			    ByteArrayOutputStream ostream = new ByteArrayOutputStream();
			    ObjectOutputStream out = new ObjectOutputStream(ostream);
			    out.writeObject(o);
			    size = ostream.toByteArray().length;
			    //System.out.println("object : " + name + " size is: " + size);
			    out.close();
			    out= null;
			    ostream= null;
            }
		} catch(java.io.IOException io) {
			io.printStackTrace();
		}
		return size;
	}
}
