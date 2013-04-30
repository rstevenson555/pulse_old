/*
 * Created on Oct 28, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package com.bos.art.logParser.records;

import java.util.Calendar;

/**
 * @author I0360D3
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public interface ILiveLogParserRecord extends Comparable , ILiveLogPriorityQueueMessage {
	public abstract boolean isExternalAccessEvent();
	public abstract boolean isAccessRecord();
	public abstract boolean isAccumulatorEvent();
	public abstract Calendar getEventTime();
	public abstract String getAppName();
	public abstract String getServerName();
	public abstract String getInstance();
	public abstract String getContext();
	public abstract String getRemoteHost();
	public abstract int getLoadTime();
	public abstract boolean isFirstTimeUser();
	public abstract boolean isErrorPage();
	public abstract boolean writeToDatabase();
	public abstract String getBrowser();
}
