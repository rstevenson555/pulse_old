/*
 * Created on Nov 18, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package com.bos.art.logParser.broadcast.beans;

import com.bos.art.logParser.statistics.TimeSpanEventContainer;
import java.io.Serializable;
import org.apache.log4j.Logger;
import org.jgroups.Message;

/**
 * @author I0360D3
 *
 * To change the template for this generated type comment go to Window>Preferences>Java>Code Generation>Code and Comments
 */
public class ExternalAccessRecordsMinuteBean extends TransferBean implements Serializable {

    private static final int MACHINE_START_INDEX = 12;
    private static final String START_CLASSIFICATION_DELIMETER = "#START_CLASSIFICATION#";
    private static final String START_SERVER_DELIMETER = "#START_SERVER#";
    private static final String START_INSTANCE_DELIMETER = "#START_INSTANCE#";
    private static Logger logger = Logger.getLogger(ExternalAccessRecordsMinuteBean.class.getName());
    private String key;
    protected String machine;
    protected String instance;
    protected String timeString;
    protected int totalLoads;
    protected int averageLoadTime;
    protected long totalLoadTime;
    protected int maxLoadTime;
    protected int minLoadTime;
    protected int distinctUsers;
    protected int totalUsers;
    protected int errorPages;
    protected int thirtySecondLoads;
    protected int twentySecondLoads;
    protected int fifteenSecondLoads;
    protected int tenSecondLoads;
    protected int fiveSecondLoads;
    protected int i90Percentile;
    protected int i75Percentile;
    protected int i50Percentile;
    protected int i25Percentile;
    protected int classificationID;

    public ExternalAccessRecordsMinuteBean() {
    }

    public ExternalAccessRecordsMinuteBean(TimeSpanEventContainer tsec, String lkey) {
        key = lkey;
        timeString = lkey.substring(0, MACHINE_START_INDEX);

        System.out.println("lkey: " + lkey);
        
        int startInstance = lkey.indexOf(START_INSTANCE_DELIMETER) + START_INSTANCE_DELIMETER.length();
        int endInstance = lkey.indexOf(START_SERVER_DELIMETER);
        int startMachine = lkey.indexOf(START_SERVER_DELIMETER) + START_SERVER_DELIMETER.length();
        int endMachine = lkey.indexOf(START_CLASSIFICATION_DELIMETER);
        int startClassification = endMachine + START_CLASSIFICATION_DELIMETER.length();
        
        machine = lkey.substring(startMachine, endMachine);
        instance = lkey.substring(startInstance, endInstance);
        classificationID = 0;
        try {
            classificationID = Integer.parseInt(lkey.substring(startClassification));
        } catch (NumberFormatException e) {
            // TODO Auto-generated catch block
            logger.error("ExternalAccessRecordsMinuteBean", e);
        }

        totalLoads = tsec.getTotalLoads();
        averageLoadTime = tsec.getAverageLoadTime();
        totalLoadTime = tsec.getTotalLoadTime();
        maxLoadTime = tsec.getMaxLoadTime();
        minLoadTime = tsec.getMinLoadTime();
        distinctUsers = tsec.getDistinctUsers();
        totalUsers = tsec.getTotalUsers();
        errorPages = tsec.getErrorPages();
        thirtySecondLoads = tsec.getThirtySecondLoads();
        twentySecondLoads = tsec.getTwentySecondLoads();
        fifteenSecondLoads = tsec.getFifteenSecondLoads();
        tenSecondLoads = tsec.getTenSecondLoads();
        fiveSecondLoads = tsec.getFiveSecondLoads();
        i90Percentile = tsec.get90Percentile();
        i75Percentile = tsec.get75Percentile();
        i50Percentile = tsec.get50Percentile();
        i25Percentile = tsec.get25Percentile();
    }

    /**
     * @return
     */
    public int getAverageLoadTime() {
        return averageLoadTime;
    }

    /**
     * @return
     */
    public int getDistinctUsers() {
        return distinctUsers;
    }

    /**
     * @return
     */
    public int getErrorPages() {
        return errorPages;
    }

    /**
     * @return
     */
    public int getFifteenSecondLoads() {
        return fifteenSecondLoads;
    }

    /**
     * @return
     */
    public int getFiveSecondLoads() {
        return fiveSecondLoads;
    }

    /**
     * @return
     */
    public int getMaxLoadTime() {
        return maxLoadTime;
    }

    /**
     * @return
     */
    public int getMinLoadTime() {
        return minLoadTime;
    }

    /**
     * @return
     */
    public int getI25Percentile() {
        return i25Percentile;
    }

    /**
     * @return
     */
    public int getI50Percentile() {
        return i50Percentile;
    }

    /**
     * @return
     */
    public int getI75Percentile() {
        return i75Percentile;
    }

    /**
     * @return
     */
    public int getI90Percentile() {
        return i90Percentile;
    }

    /**
     * @return
     */
    public int getTenSecondLoads() {
        return tenSecondLoads;
    }

    /**
     * @return
     */
    public int getThirtySecondLoads() {
        return thirtySecondLoads;
    }

    /**
     * @return
     */
    public int getTotalLoads() {
        return totalLoads;
    }

    /**
     * @return
     */
    public long getTotalLoadTime() {
        return totalLoadTime;
    }

    /**
     * @return
     */
    public int getTotalUsers() {
        return totalUsers;
    }

    /**
     * @return
     */
    public int getTwentySecondLoads() {
        return twentySecondLoads;
    }

    /**
     * @param i
     */
    public void setAverageLoadTime(int i) {
        averageLoadTime = i;
    }

    /**
     * @param i
     */
    public void setDistinctUsers(int i) {
        distinctUsers = i;
    }

    /**
     * @param i
     */
    public void setErrorPages(int i) {
        errorPages = i;
    }

    /**
     * @param i
     */
    public void setFifteenSecondLoads(int i) {
        fifteenSecondLoads = i;
    }

    /**
     * @param i
     */
    public void setFiveSecondLoads(int i) {
        fiveSecondLoads = i;
    }

    /**
     * @param i
     */
    public void setMaxLoadTime(int i) {
        maxLoadTime = i;
    }

    /**
     * @param i
     */
    public void setMinLoadTime(int i) {
        minLoadTime = i;
    }

    /**
     * @param i
     */
    public void setI25Percentile(int i) {
        i25Percentile = i;
    }

    /**
     * @param i
     */
    public void setI50Percentile(int i) {
        i50Percentile = i;
    }

    /**
     * @param i
     */
    public void setI75Percentile(int i) {
        i75Percentile = i;
    }

    /**
     * @param i
     */
    public void setI90Percentile(int i) {
        i90Percentile = i;
    }

    /**
     * @param i
     */
    public void setTenSecondLoads(int i) {
        tenSecondLoads = i;
    }

    /**
     * @param i
     */
    public void setThirtySecondLoads(int i) {
        thirtySecondLoads = i;
    }

    /**
     * @param i
     */
    public void setTotalLoads(int i) {
        totalLoads = i;
    }

    /**
     * @param l
     */
    public void setTotalLoadTime(long l) {
        totalLoadTime = l;
    }

    /**
     * @param i
     */
    public void setTotalUsers(int i) {
        totalUsers = i;
    }

    /**
     * @param i
     */
    public void setTwentySecondLoads(int i) {
        twentySecondLoads = i;
    }

    /**
     * @return
     */
    public String getKey() {
        return key;
    }

    /**
     * @param string
     */
    public void setKey(String string) {
        key = string;
    }

    /**
     * @return
     */
    public String getMachine() {
        return machine;
    }
    
    /**
     * @return
     */
    public String getInstance() {
        return instance;
    }

    /**
     * @return
     */
    public String getTimeString() {
        return timeString;
    }

    /**
     * @param string
     */
    public void setMachine(String string) {
        machine = string;
    }
    
    /**
     * @param string
     */
    public void setInstance(String instance) {
        this.instance = instance;
    }

    /**
     * @param string
     */
    public void setTimeString(String string) {
        timeString = string;
    }

    /**
     * @return
     */
    public int getClassificationID() {
        return classificationID;
    }

    /**
     * @param i
     */
    public void setClassificationID(int i) {
        classificationID = i;
    }

    public void processBean(Message msg) {
        getClient().process(msg, this);
    }
}

