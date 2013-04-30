package com.bos.art.model.jdo;

import java.io.Serializable;
import java.util.Date;
import java.util.Set;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

/** @author Hibernate CodeGenerator */
public class ContextBean implements Serializable {

    /** identifier field */
    private Integer contextId;

    /** nullable persistent field */
    private String contextName;

    /** nullable persistent field */
    private Date lastModTime;

    /** persistent field */
    private Set accessRecords;

    /** persistent field */
    private Set fiveSecondLoads;

    /** persistent field */
    private Set dailyPageLoadTims;

    /** persistent field */
    private Set dailyContextStats;

    /** full constructor */
    public ContextBean(Integer contextId, String contextName, Date lastModTime, Set accessRecords, Set fiveSecondLoads, Set dailyPageLoadTims, Set dailyContextStats) {
        this.contextId = contextId;
        this.contextName = contextName;
        this.lastModTime = lastModTime;
        this.accessRecords = accessRecords;
        this.fiveSecondLoads = fiveSecondLoads;
        this.dailyPageLoadTims = dailyPageLoadTims;
        this.dailyContextStats = dailyContextStats;
    }

    /** default constructor */
    public ContextBean() {
    }

    /** minimal constructor */
    public ContextBean(Integer contextId, Set accessRecords, Set fiveSecondLoads, Set dailyPageLoadTims, Set dailyContextStats) {
        this.contextId = contextId;
        this.accessRecords = accessRecords;
        this.fiveSecondLoads = fiveSecondLoads;
        this.dailyPageLoadTims = dailyPageLoadTims;
        this.dailyContextStats = dailyContextStats;
    }

    public Integer getContextId() {
        return this.contextId;
    }

    public void setContextId(Integer contextId) {
        this.contextId = contextId;
    }

    public String getContextName() {
        return this.contextName;
    }

    public void setContextName(String contextName) {
        this.contextName = contextName;
    }

    public Date getLastModTime() {
        return this.lastModTime;
    }

    public void setLastModTime(Date lastModTime) {
        this.lastModTime = lastModTime;
    }

    public Set getAccessRecords() {
        return this.accessRecords;
    }

    public void setAccessRecords(Set accessRecords) {
        this.accessRecords = accessRecords;
    }

    public Set getFiveSecondLoads() {
        return this.fiveSecondLoads;
    }

    public void setFiveSecondLoads(Set fiveSecondLoads) {
        this.fiveSecondLoads = fiveSecondLoads;
    }

    public Set getDailyPageLoadTims() {
        return this.dailyPageLoadTims;
    }

    public void setDailyPageLoadTims(Set dailyPageLoadTims) {
        this.dailyPageLoadTims = dailyPageLoadTims;
    }

    public Set getDailyContextStats() {
        return this.dailyContextStats;
    }

    public void setDailyContextStats(Set dailyContextStats) {
        this.dailyContextStats = dailyContextStats;
    }

    public String toString() {
        return new ToStringBuilder(this)
            .append("contextId", getContextId())
            .toString();
    }

    public boolean equals(Object other) {
        if ( !(other instanceof ContextBean) ) return false;
        ContextBean castOther = (ContextBean) other;
        return new EqualsBuilder()
            .append(this.getContextId(), castOther.getContextId())
            .isEquals();
    }

    public int hashCode() {
        return new HashCodeBuilder()
            .append(getContextId())
            .toHashCode();
    }

}
