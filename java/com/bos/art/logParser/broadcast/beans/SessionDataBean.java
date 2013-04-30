/*
 * Created on Dec 23, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package com.bos.art.logParser.broadcast.beans;

import java.io.Serializable;

/**
 * @author I0360D3
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class SessionDataBean extends TransferBean implements Serializable {

	int oneMinSessions;
	int fiveMinSessions;
	int tenMinSessions;
	int thirtyMinSessions;
	String context;
	java.util.Date currentTime;
	public SessionDataBean(){
	}
	
	public SessionDataBean(int one, int five, int ten, int thirty, java.util.Date date){
		oneMinSessions = one;
		fiveMinSessions = five;
		tenMinSessions = ten;
		thirtyMinSessions = thirty;
		
	}
	/**
	 * @return
	 */
	public java.util.Date getCurrentTime() {
		return currentTime;
	}

	/**
	 * @return
	 */
	public int getFiveMinSessions() {
		return fiveMinSessions;
	}

	/**
	 * @return
	 */
	public int getOneMinSessions() {
		return oneMinSessions;
	}

	/**
	 * @return
	 */
	public int getTenMinSessions() {
		return tenMinSessions;
	}

	/**
	 * @return
	 */
	public int getThirtyMinSessions() {
		return thirtyMinSessions;
	}

	/**
	 * @param date
	 */
	public void setCurrentTime(java.util.Date date) {
		currentTime = date;
	}

	/**
	 * @param i
	 */
	public void setFiveMinSessions(int i) {
		fiveMinSessions = i;
	}

	/**
	 * @param i
	 */
	public void setOneMinSessions(int i) {
		oneMinSessions = i;
	}

	/**
	 * @param i
	 */
	public void setTenMinSessions(int i) {
		tenMinSessions = i;
	}

	/**
	 * @param i
	 */
	public void setThirtyMinSessions(int i) {
		thirtyMinSessions = i;
	}

    /**
     * @return
     */
    public String getContext() {
        return context;
    }

    /**
     * @param string
     */
    public void setContext(String string) {
        context = string;
    }

    public void processBean(org.jgroups.Message msg)
    {
        getClient().process(msg,this);
    }

}
