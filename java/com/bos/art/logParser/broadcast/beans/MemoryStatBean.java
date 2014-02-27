/*
 * Created on Nov 18, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package com.bos.art.logParser.broadcast.beans;

import com.bos.art.logParser.records.AccumulatorEventTiming;
import java.io.Serializable;

/**
 * @author I0360D3
 *
 * To change the template for this generated type comment go to Window>Preferences>Java>Code Generation>Code and Comments
 */
public class MemoryStatBean extends TransferBean implements Serializable {

    protected String server;
    protected String context;
    protected String branchName;
    protected String appName;
    protected int classification;
    protected long eventTime;
    protected String value;

    public MemoryStatBean() {
    }

    /**
     * @return Returns the appName.
     */
    public String getAppName() {
        return appName;
    }

    /**
     * @param appName The appName to set.
     */
    public void setAppName(String appName) {
        this.appName = appName;
    }

    /**
     * @return Returns the branchName.
     */
    public String getBranchName() {
        return branchName;
    }

    /**
     * @param branchName The branchName to set.
     */
    public void setBranchName(String branchName) {
        this.branchName = branchName;
    }

    /**
     * @return Returns the classification.
     */
    public int getClassification() {
        return classification;
    }

    /**
     * @param classification The classification to set.
     */
    public void setClassification(int classification) {
        this.classification = classification;
    }

    /**
     * @return Returns the context.
     */
    public String getContext() {
        return context;
    }

    /**
     * @param context The context to set.
     */
    public void setContext(String context) {
        this.context = context;
    }

    /**
     * @return Returns the eventTime.
     */
    public long getEventTime() {
        return eventTime;
    }

    /**
     * @param eventTime The eventTime to set.
     */
    public void setEventTime(long eventTime) {
        this.eventTime = eventTime;
    }

    /**
     * @return Returns the server.
     */
    public String getServer() {
        return server;
    }

    /**
     * @param server The server to set.
     */
    public void setServer(String server) {
        this.server = server;
    }

    /**
     * @return Returns the value.
     */
    public String getValue() {
        return value;
    }

    /**
     * @param value The value to set.
     */
    public void setValue(String value) {
        this.value = value;
    }

    public MemoryStatBean(AccumulatorEventTiming event) {

        appName = event.getAppName();
        branchName = event.getBranchName();
        classification = event.getClassification();
        context = event.getContext();
        eventTime = event.getEventTime().getTimeInMillis();
        server = event.getServerName();
        value = event.getValue();
    }

    public void processBean(org.jgroups.Message msg) {
        getClient().process(msg, this);
    }
}
