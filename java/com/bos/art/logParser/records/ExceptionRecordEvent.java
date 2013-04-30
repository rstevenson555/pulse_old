/*
 * Created on Nov 19, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.bos.art.logParser.records;


import com.bos.art.logParser.db.ExceptionRecordEventPersistanceStrategy;
import com.bos.art.logParser.db.PersistanceStrategy;
import org.apache.commons.codec.binary.Base64;
/**
 * @author I0360D3
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class ExceptionRecordEvent extends UserRequestTiming implements ILiveLogParserRecord {

	private String pageName ;
	private String sessionId;
	private int requestToken;
	private int requestTokenCount;
	private String encodedException;
	private String encodedBeanContainer;
    private String message;
	
    public void setMessage(String m){
        message = m;
    }
    public String getMessage(){
        return message;
    }
    @Override
	public String getBrowser() {
		return null;
	}
    @Override
	public int getLoadTime() {
		return 0;
	}
    @Override
	public String getRemoteHost() {
		return null;
	}
    @Override
	public boolean isAccessRecord() {
		return false;
	}
    @Override
	public boolean isAccumulatorEvent() {
		return false;
	}
    @Override
	public boolean isErrorPage() {
		return false;
	}
    @Override
	public boolean isExternalAccessEvent() {
		return false;
	}
    @Override
	public boolean isFirstTimeUser() {
		return false;
	}
    @Override
	public boolean writeToDatabase() {
		return getPersistanceStrategy().writeToDatabase(this);
	}
	public String getEncodedException() {
		return encodedException;
	}
	public void setEncodedException(String ee) {
		this.encodedException = ee;
	}
	public String getEncodedBeanContainer() {
		return encodedBeanContainer;
	}
	public void setEncodedBeanContainer(String ebc) {
		this.encodedBeanContainer = ebc;
	}

    @Override
	public int getRequestToken() {
		return requestToken;
	}
    @Override
	public void setRequestToken(int requestToken) {
		this.requestToken = requestToken;
	}
	public int getRequestTokenCount() {
		return requestTokenCount;
	}
	public void setRequestTokenCount(int requestTokenCount) {
		this.requestTokenCount = requestTokenCount;
	}
    @Override
	public String getSessionId() {
		return sessionId;
	}
	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}
	
	
    transient private AccessRecordsForeignKeys foreignKeys;
    transient private PersistanceStrategy pStrat;


    public AccessRecordsForeignKeys obtainForeignKeys() {
        if (foreignKeys == null) {
            foreignKeys = new AccessRecordsForeignKeys(getEventTime().getTime());
        }
        return foreignKeys;
    }
    
    public PersistanceStrategy getPersistanceStrategy(){
    	if(pStrat == null){
    		pStrat = ExceptionRecordEventPersistanceStrategy.getInstance();
    	}
    	return pStrat;
    }


    public String toString(){
        StringBuffer sb = new StringBuffer();
        sb.append(super.toString());
        sb.append("\n------------------------------------------end super.toString()--------------------------\n");
        sb.append("\n------------------------------------------end super.toString()--------------------------\n");
        sb.append(new String(new Base64().decode(getEncodedException().getBytes())));
        sb.append("\n------------------------------------------end Exception...--------------------------\n");
        sb.append(getEncodedException());
        sb.append("\n------------------------------------------end Exception...--------------------------\n");
        sb.append(new String(new Base64().decode(getEncodedBeanContainer().getBytes())));
        sb.append("\n------------------------------------------end Beancontainer...--------------------------\n");
        sb.append(getEncodedBeanContainer());
        sb.append("\n------------------------------------------end Beancontainer...--------------------------\n");

        return sb.toString();
    }
}
