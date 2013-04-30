package com.bos.art.model.jdo;

import java.io.Serializable;
import java.util.Date;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

/** @author Hibernate CodeGenerator */
public class HourlyStatisticBean implements Serializable {

    /** identifier field */
    private Date lastModTime;

    /** identifier field */
    private Date time;

    /** identifier field */
    private int totalLoads;

    /** identifier field */
    private int averageLoadTime;

    /** identifier field */
    private int ninetiethPercentile;

    /** identifier field */
    private int twentyFifthPercentile;

    /** identifier field */
    private int fiftiethPercentile;

    /** identifier field */
    private int seventyFifthPercentile;

    /** identifier field */
    private int maxLoadTime;

    /** identifier field */
    private int minLoadTime;

    /** identifier field */
    private int distinctUsers;

    /** identifier field */
    private int errorPages;

    /** identifier field */
    private int thirtySecondLoads;

    /** identifier field */
    private int twentySecondLoads;

    /** identifier field */
    private int fifteenSecondLoads;

    /** identifier field */
    private int tenSecondLoads;

    /** identifier field */
    private int fiveSecondLoads;

    /** identifier field */
    private String state;

    /** persistent field */
    private com.bos.art.model.jdo.MachinBean machin;

    /** full constructor */
    public HourlyStatisticBean(Date lastModTime, Date time, int totalLoads, int averageLoadTime, int ninetiethPercentile, int twentyFifthPercentile, int fiftiethPercentile, int seventyFifthPercentile, int maxLoadTime, int minLoadTime, int distinctUsers, int errorPages, int thirtySecondLoads, int twentySecondLoads, int fifteenSecondLoads, int tenSecondLoads, int fiveSecondLoads, String state, com.bos.art.model.jdo.MachinBean machin) {
        this.lastModTime = lastModTime;
        this.time = time;
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
        this.machin = machin;
    }

    /** default constructor */
    public HourlyStatisticBean() {
    }

    public Date getLastModTime() {
        return this.lastModTime;
    }

    public void setLastModTime(Date lastModTime) {
        this.lastModTime = lastModTime;
    }

    public Date getTime() {
        return this.time;
    }

    public void setTime(Date time) {
        this.time = time;
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

    public com.bos.art.model.jdo.MachinBean getMachin() {
        return this.machin;
    }

    public void setMachin(com.bos.art.model.jdo.MachinBean machin) {
        this.machin = machin;
    }

    public String toString() {
        return new ToStringBuilder(this)
            .append("lastModTime", getLastModTime())
            .append("time", getTime())
            .append("totalLoads", getTotalLoads())
            .append("averageLoadTime", getAverageLoadTime())
            .append("ninetiethPercentile", getNinetiethPercentile())
            .append("twentyFifthPercentile", getTwentyFifthPercentile())
            .append("fiftiethPercentile", getFiftiethPercentile())
            .append("seventyFifthPercentile", getSeventyFifthPercentile())
            .append("maxLoadTime", getMaxLoadTime())
            .append("minLoadTime", getMinLoadTime())
            .append("distinctUsers", getDistinctUsers())
            .append("errorPages", getErrorPages())
            .append("thirtySecondLoads", getThirtySecondLoads())
            .append("twentySecondLoads", getTwentySecondLoads())
            .append("fifteenSecondLoads", getFifteenSecondLoads())
            .append("tenSecondLoads", getTenSecondLoads())
            .append("fiveSecondLoads", getFiveSecondLoads())
            .append("state", getState())
            .toString();
    }

    public boolean equals(Object other) {
        if ( !(other instanceof HourlyStatisticBean) ) return false;
        HourlyStatisticBean castOther = (HourlyStatisticBean) other;
        return new EqualsBuilder()
            .append(this.getLastModTime(), castOther.getLastModTime())
            .append(this.getTime(), castOther.getTime())
            .append(this.getTotalLoads(), castOther.getTotalLoads())
            .append(this.getAverageLoadTime(), castOther.getAverageLoadTime())
            .append(this.getNinetiethPercentile(), castOther.getNinetiethPercentile())
            .append(this.getTwentyFifthPercentile(), castOther.getTwentyFifthPercentile())
            .append(this.getFiftiethPercentile(), castOther.getFiftiethPercentile())
            .append(this.getSeventyFifthPercentile(), castOther.getSeventyFifthPercentile())
            .append(this.getMaxLoadTime(), castOther.getMaxLoadTime())
            .append(this.getMinLoadTime(), castOther.getMinLoadTime())
            .append(this.getDistinctUsers(), castOther.getDistinctUsers())
            .append(this.getErrorPages(), castOther.getErrorPages())
            .append(this.getThirtySecondLoads(), castOther.getThirtySecondLoads())
            .append(this.getTwentySecondLoads(), castOther.getTwentySecondLoads())
            .append(this.getFifteenSecondLoads(), castOther.getFifteenSecondLoads())
            .append(this.getTenSecondLoads(), castOther.getTenSecondLoads())
            .append(this.getFiveSecondLoads(), castOther.getFiveSecondLoads())
            .append(this.getState(), castOther.getState())
            .isEquals();
    }

    public int hashCode() {
        return new HashCodeBuilder()
            .append(getLastModTime())
            .append(getTime())
            .append(getTotalLoads())
            .append(getAverageLoadTime())
            .append(getNinetiethPercentile())
            .append(getTwentyFifthPercentile())
            .append(getFiftiethPercentile())
            .append(getSeventyFifthPercentile())
            .append(getMaxLoadTime())
            .append(getMinLoadTime())
            .append(getDistinctUsers())
            .append(getErrorPages())
            .append(getThirtySecondLoads())
            .append(getTwentySecondLoads())
            .append(getFifteenSecondLoads())
            .append(getTenSecondLoads())
            .append(getFiveSecondLoads())
            .append(getState())
            .toHashCode();
    }

}
