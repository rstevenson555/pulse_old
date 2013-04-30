/*
 * Created on Oct 29, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package com.bos.art.logParser.db;

import com.bos.art.logParser.records.ILiveLogParserRecord;

/**
 * @author I0360D3
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public interface PersistanceStrategy {
	public boolean writeToDatabase(ILiveLogParserRecord record);
	public int writeForeignKey(String foreignKeyName, String foreignKeyValue);
	
}
