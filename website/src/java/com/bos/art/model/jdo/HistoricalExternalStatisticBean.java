package com.bos.art.model.jdo;

import java.io.Serializable;
import java.util.Date;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

/** @author Hibernate CodeGenerator */
public class HistoricalExternalStatisticBean implements Serializable {

    /** identifier field */
    private int statisticsId;

    /** identifier field */
    private Date lastModTime;

    /** identifier field */
    private Date startTime;

    /** identifier field */
    private int summaryPeriodMinutes;

    /** identifier field */
    private int count;

    /** identifier field */
    private int averageLoadTime;

    /** identifier field */
    private int maximumLoadTime;

    /** identifier field */
    private int minimumLoadTime;

    /** identifier field */
    private String state;

    /** full constructor */
    public HistoricalExternalStatisticBean(int statisticsId, Date lastModTime, Date startTime, int summaryPeriodMinutes, int count, int averageLoadTime, int maximumLoadTime, int minimumLoadTime, String state) {
        this.statisticsId = statisticsId;
        this.lastModTime = lastModTime;
        this.startTime = startTime;
        this.summaryPeriodMinutes = summaryPeriodMinutes;
        this.count = count;
        this.averageLoadTime = averageLoadTime;
        this.maximumLoadTime = maximumLoadTime;
        this.minimumLoadTime = minimumLoadTime;
        this.state = state;
    }

    /** default constructor */
    public HistoricalExternalStatisticBean() {
    }

    public int getStatisticsId() {
        return this.statisticsId;
    }

    public void setStatisticsId(int statisticsId) {
        this.statisticsId = statisticsId;
    }

    public Date getLastModTime() {
        return this.lastModTime;
    }

    public void setLastModTime(Date lastModTime) {
        this.lastModTime = lastModTime;
    }

    public Date getStartTime() {
        return this.startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public int getSummaryPeriodMinutes() {
        return this.summaryPeriodMinutes;
    }

    public void setSummaryPeriodMinutes(int summaryPeriodMinutes) {
        this.summaryPeriodMinutes = summaryPeriodMinutes;
    }

    public int getCount() {
        return this.count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getAverageLoadTime() {
        return this.averageLoadTime;
    }

    public void setAverageLoadTime(int averageLoadTime) {
        this.averageLoadTime = averageLoadTime;
    }

    public int getMaximumLoadTime() {
        return this.maximumLoadTime;
    }

    public void setMaximumLoadTime(int maximumLoadTime) {
        this.maximumLoadTime = maximumLoadTime;
    }

    public int getMinimumLoadTime() {
        return this.minimumLoadTime;
    }

    public void setMinimumLoadTime(int minimumLoadTime) {
        this.minimumLoadTime = minimumLoadTime;
    }

    public String getState() {
        return this.state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String toString() {
        return new ToStringBuilder(this)
            .append("statisticsId", getStatisticsId())
            .append("lastModTime", getLastModTime())
            .append("startTime", getStartTime())
            .append("summaryPeriodMinutes", getSummaryPeriodMinutes())
            .append("count", getCount())
            .append("averageLoadTime", getAverageLoadTime())
            .append("maximumLoadTime", getMaximumLoadTime())
            .append("minimumLoadTime", getMinimumLoadTime())
            .append("state", getState())
            .toString();
    }

    public boolean equals(Object other) {
        if ( !(other instanceof HistoricalExternalStatisticBean) ) return false;
        HistoricalExternalStatisticBean castOther = (HistoricalExternalStatisticBean) other;
        return new EqualsBuilder()
            .append(this.getStatisticsId(), castOther.getStatisticsId())
            .append(this.getLastModTime(), castOther.getLastModTime())
            .append(this.getStartTime(), castOther.getStartTime())
            .append(this.getSummaryPeriodMinutes(), castOther.getSummaryPeriodMinutes())
            .append(this.getCount(), castOther.getCount())
            .append(this.getAverageLoadTime(), castOther.getAverageLoadTime())
            .append(this.getMaximumLoadTime(), castOther.getMaximumLoadTime())
            .append(this.getMinimumLoadTime(), castOther.getMinimumLoadTime())
            .append(this.getState(), castOther.getState())
            .isEquals();
    }

    public int hashCode() {
        return new HashCodeBuilder()
            .append(getStatisticsId())
            .append(getLastModTime())
            .append(getStartTime())
            .append(getSummaryPeriodMinutes())
            .append(getCount())
            .append(getAverageLoadTime())
            .append(getMaximumLoadTime())
            .append(getMinimumLoadTime())
            .append(getState())
            .toHashCode();
    }

}
