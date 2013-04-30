package com.bos.art.model.jdo;

import java.io.Serializable;
import java.util.Date;
import java.util.Set;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

/** @author Hibernate CodeGenerator */
public class StackTrac implements Serializable {

    /** identifier field */
    private Integer traceId;

    /** nullable persistent field */
    private String traceKey;

    /** nullable persistent field */
    private String traceMessage;

    /** nullable persistent field */
    private Date traceTime;

    /** nullable persistent field */
    private Integer artUserId;

    /** persistent field */
    private Set stackTraceDetails;

    /** persistent field */
    private Set stackTraceBeanContainers;

    /** full constructor */
    public StackTrac(Integer traceId, String traceKey, String traceMessage, Date traceTime, Integer artUserId, Set stackTraceDetails, Set stackTraceBeanContainers) {
        this.traceId = traceId;
        this.traceKey = traceKey;
        this.traceMessage = traceMessage;
        this.traceTime = traceTime;
        this.artUserId = artUserId;
        this.stackTraceDetails = stackTraceDetails;
        this.stackTraceBeanContainers = stackTraceBeanContainers;
    }

    /** default constructor */
    public StackTrac() {
    }

    /** minimal constructor */
    public StackTrac(Integer traceId, Set stackTraceDetails, Set stackTraceBeanContainers) {
        this.traceId = traceId;
        this.stackTraceDetails = stackTraceDetails;
        this.stackTraceBeanContainers = stackTraceBeanContainers;
    }

    public Integer getTraceId() {
        return this.traceId;
    }

    public void setTraceId(Integer traceId) {
        this.traceId = traceId;
    }

    public String getTraceKey() {
        return this.traceKey;
    }

    public void setTraceKey(String traceKey) {
        this.traceKey = traceKey;
    }

    public String getTraceMessage() {
        return this.traceMessage;
    }

    public void setTraceMessage(String traceMessage) {
        this.traceMessage = traceMessage;
    }

    public Date getTraceTime() {
        return this.traceTime;
    }

    public void setTraceTime(Date traceTime) {
        this.traceTime = traceTime;
    }

    public Integer getArtUserId() {
        return this.artUserId;
    }

    public void setArtUserId(Integer artUserId) {
        this.artUserId = artUserId;
    }

    public Set getStackTraceDetails() {
        return this.stackTraceDetails;
    }

    public void setStackTraceDetails(Set stackTraceDetails) {
        this.stackTraceDetails = stackTraceDetails;
    }

    public Set getStackTraceBeanContainers() {
        return this.stackTraceBeanContainers;
    }

    public void setStackTraceBeanContainers(Set stackTraceBeanContainers) {
        this.stackTraceBeanContainers = stackTraceBeanContainers;
    }

    public String toString() {
        return new ToStringBuilder(this)
            .append("traceId", getTraceId())
            .toString();
    }

    public boolean equals(Object other) {
        if ( !(other instanceof StackTrac) ) return false;
        StackTrac castOther = (StackTrac) other;
        return new EqualsBuilder()
            .append(this.getTraceId(), castOther.getTraceId())
            .isEquals();
    }

    public int hashCode() {
        return new HashCodeBuilder()
            .append(getTraceId())
            .toHashCode();
    }

}
