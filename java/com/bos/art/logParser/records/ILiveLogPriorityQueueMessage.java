/*
 * Created on Nov 10, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package com.bos.art.logParser.records;

import java.io.Serializable;
import java.util.Calendar;

/**
 * @author I0360D3
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public interface ILiveLogPriorityQueueMessage extends Serializable{
	
	/** This is the prioirty of the message 1 placed in front of 2 etc.
	 * The numbers should be less than 20.
	 * <BR>
	 * Current Concepts:
	 * <BR>
	 * UserRequestTimings ..............  15
	 * <BR>
	 * System Shutdown    ..............   5
	 * <BR>
	 * Print Stats        ..............   7
	 * <BR>
	 * External Event Timings ..........  10
	 * <BR>
	 * 
	 * @return int between 1 and 20.
	 */
	public int getPriority();
	
	/** 
	 * returns a calendar object which represents the 
	 * Time that this event occurred at.
	 * 
	 * @return Calendar representing this event.
	 * 
	 */
	public Calendar getEventTime();
}
