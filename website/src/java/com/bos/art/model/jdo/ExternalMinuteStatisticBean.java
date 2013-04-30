package com.bos.art.model.jdo;

import java.io.Serializable;
import java.util.Date;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

/** @author Hibernate CodeGenerator */
public class ExternalMinuteStatisticBean implements Serializable {

    /** identifier field */
    private Date time;

    /** identifier field */
    private int classificationID;

    /** identifier field */
    private int machineID;

    /** nullable persistent field */
    private Date lastModTime;

    /** nullable persistent field */
    private int totalLoads;

    /** nullable persistent field */
    private int averageLoadTime;

    /** nullable persistent field */
    private int ninetiethPercentile;

    /** nullable persistent field */
    private int twentyFifthPercentile;

    /** nullable persistent field */
    private int fiftiethPercentile;

    /** nullable persistent field */
    private int seventyFifthPercentile;

    /** nullable persistent field */
    private int maxLoadTime;

    /** nullable persistent field */
    private int minLoadTime;

    /** nullable persistent field */
    private int distinctUsers;

    /** nullable persistent field */
    private int errorPages;

    /** nullable persistent field */
    private int thirtySecondLoads;

    /** nullable persistent field */
    private int twentySecondLoads;

    /** nullable persistent field */
    private int fifteenSecondLoads;

    /** nullable persistent field */
    private int tenSecondLoads;

    /** nullable persistent field */
    private int fiveSecondLoads;

    /** nullable persistent field */
    private String state;

    /** full constructor */
    public ExternalMinuteStatisticBean(Date time, int classificationID, int machineID, Date lastModTime, int totalLoads, int averageLoadTime, int ninetiethPercentile, int twentyFifthPercentile, int fiftiethPercentile, int seventyFifthPercentile, int maxLoadTime, int minLoadTime, int distinctUsers, int errorPages, int thirtySecondLoads, int twentySecondLoads, int fifteenSecondLoads, int tenSecondLoads, int fiveSecondLoads, String state) {
        this.time = time;
        this.classificationID = classificationID;
        this.machineID = machineID;
        this.lastModTime = lastModTime;
        this.totalLoads = totalLoads;
        this.averageLoadTime = averageLoadTime;
        this.ninetiethPercentile = ninetiethPercentile;
        this.twentyFifthPercentile = twentyFifthPercentile;
        this.fiftiethPercentile = fiftiethPercentile;
        this.seventyFifthPercentile = seventyFifthPercentile;
        this.maxLoadTime = maxLoadTime;
        this.minLoadTime = minLoadTime;
        this.distinctUsers = distinctUsers;
        this.errorPages = errorPages;
        this.thirtySecondLoads = thirtySecondLoads;
        this.twentySecondLoads = twentySecondLoads;
        this.fifteenSecondLoads = fifteenSecondLoads;
        this.tenSecondLoads = tenSecondLoads;
        this.fiveSecondLoads = fiveSecondLoads;
        this.state = state;
    }

    /** default constructor */
    public ExternalMinuteStatisticBean() {
    }

    /** minimal constructor */
    public ExternalMinuteStatisticBean(Date time, int classificationID, int machineID) {
        this.time = time;
        this.classificationID = classificationID;
        this.machineID = machineID;
    }

    public Date getTime() {
        return this.time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    public int getClassificationID() {
        return this.classificationID;
    }

    public void setClassificationID(int classificationID) {
        this.classificationID = classificationID;
    }

    public int getMachineID() {
        return this.machineID;
    }

    public void setMachineID(int machineID) {
        this.machineID = machineID;
    }

    public Date getLastModTime() {
        return this.lastModTime;
    }

    public void setLastModTime(Date lastModTime) {
        this.lastModTime = lastModTime;
    }

    public int getTotalLoads() {
        return this.totalLoads;
    }

    public void setTotalLoads(int totalLoads) {
        this.totalLoads = totalLoads;
    }

    public int getAverageLoadTime() {
        return this.averageLoadTime;
    }

    public void setAverageLoadTime(int averageLoadTime) {
        this.averageLoadTime = averageLoadTime;
    }

    public int getNinetiethPercentile() {
        return this.ninetiethPercentile;
    }

    public void setNinetiethPercentile(int ninetiethPercentile) {
        this.ninetiethPercentile = ninetiethPercentile;
    }

    public int getTwentyFifthPercentile() {
        return this.twentyFifthPercentile;
    }

    public void setTwentyFifthPercentile(int twentyFifthPercentile) {
        this.twentyFifthPercentile = twentyFifthPercentile;
    }

    public int getFiftiethPercentile() {
        return this.fiftiethPercentile;
    }

    public void setFiftiethPercentile(int fiftiethPercentile) {
        this.fiftiethPercentile = fiftiethPercentile;
    }

    public int getSeventyFifthPercentile() {
        return this.seventyFifthPercentile;
    }

    public void setSeventyFifthPercentile(int seventyFifthPercentile) {
        this.seventyFifthPercentile = seventyFifthPercentile;
    }

    public int getMaxLoadTime() {
        return this.maxLoadTime;
    }

    public void setMaxLoadTime(int maxLoadTime) {
        this.maxLoadTime = maxLoadTime;
    }

    public int getMinLoadTime() {
        return this.minLoadTime;
    }

    public void setMinLoadTime(int minLoadTime) {
        this.minLoadTime = minLoadTime;
    }

    public int getDistinctUsers() {
        return this.distinctUsers;
    }

    public void setDistinctUsers(int distinctUsers) {
        this.distinctUsers = distinctUsers;
    }

    public int getErrorPages() {
        return this.errorPages;
    }

    public void setErrorPages(int errorPages) {
        this.errorPages = errorPages;
    }

    public int getThirtySecondLoads() {
        return this.thirtySecondLoads;
    }

    public void setThirtySecondLoads(int thirtySecondLoads) {
        this.thirtySecondLoads = thirtySecondLoads;
    }

    public int getTwentySecondLoads() {
        return this.twentySecondLoads;
    }

    public void setTwentySecondLoads(int twentySecondLoads) {
        this.twentySecondLoads = twentySecondLoads;
    }

    public int getFifteenSecondLoads() {
        return this.fifteenSecondLoads;
    }

    public void setFifteenSecondLoads(int fifteenSecondLoads) {
        this.fifteenSecondLoads = fifteenSecondLoads;
    }

    public int getTenSecondLoads() {
        return this.tenSecondLoads;
    }

    public void setTenSecondLoads(int tenSecondLoads) {
        this.tenSecondLoads = tenSecondLoads;
    }

    public int getFiveSecondLoads() {
        return this.fiveSecondLoads;
    }

    public void setFiveSecondLoads(int fiveSecondLoads) {
        this.fiveSecondLoads = fiveSecondLoads;
    }

    public String getState() {
        return this.state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String toString() {
        return new ToStringBuilder(this)
            .append("time", getTime())
            .append("classificationID", getClassificationID())
            .append("machineID", getMachineID())
            .toString();
    }

    public boolean equals(Object other) {
        if ( !(other instanceof ExternalMinuteStatisticBean) ) return false;
        ExternalMinuteStatisticBean castOther = (ExternalMinuteStatisticBean) other;
        return new EqualsBuilder()
            .append(this.getTime(), castOther.getTime())
            .append(this.getClassificationID(), castOther.getClassificationID())
            .append(this.getMachineID(), castOther.getMachineID())
            .isEquals();
    }

    public int hashCode() {
        return new HashCodeBuilder()
            .append(getTime())
            .append(getClassificationID())
            .append(getMachineID())
            .toHashCode();
    }

}
