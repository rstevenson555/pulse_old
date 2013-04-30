package com.bos.art.model.jdo;

import java.io.Serializable;
import java.util.Date;
import java.util.Set;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

/** @author Hibernate CodeGenerator */
public class SessionBean implements Serializable {

    /** identifier field */
    private Integer sessionId;

    /** nullable persistent field */
    private String ipaddress;

    /** nullable persistent field */
    private String sessionTxt;

    /** nullable persistent field */
    private String browserType;

    /** nullable persistent field */
    private Date lastModTime;

    /** nullable persistent field */
    private Date insertTime;

    /** nullable persistent field */
    private int userId;

    /** nullable persistent field */
    private Integer contextId;

    /** nullable persistent field */
    private Date sessionStartTime;

    /** nullable persistent field */
    private Date sessionEndTime;

    /** nullable persistent field */
    private Integer sessionHits;

    /** persistent field */
    private Set accessRecords;

    /** persistent field */
    private Set fiveSecondLoads;

    /** nullable persistent field */
    private Long sessionDuration;

    /** full constructor */
    public SessionBean(Integer sessionId, String ipaddress, String sessionTxt, 
            String browserType, Date lastModTime, Date insertTime, 
            int userId, Integer contextId, 
            Date sessionStartTime, Date sessionEndTime, 
            Integer sessionHits, Set accessRecords, Set fiveSecondLoads) {
        this.sessionId = sessionId;
        this.ipaddress = ipaddress;
        this.sessionTxt = sessionTxt;
        this.browserType = browserType;
        this.lastModTime = lastModTime;
        this.insertTime = insertTime;
        this.userId = userId;
        this.contextId = contextId;
        this.sessionStartTime = sessionStartTime;
        this.sessionEndTime = sessionEndTime;
        this.sessionHits = sessionHits;
        this.accessRecords = accessRecords;
        this.fiveSecondLoads = fiveSecondLoads;
    }

    /** default constructor */
    public SessionBean() {
    }

    /** minimal constructor */
    public SessionBean(Integer sessionId, Set accessRecords, Set fiveSecondLoads) {
        this.sessionId = sessionId;
        this.accessRecords = accessRecords;
        this.fiveSecondLoads = fiveSecondLoads;
    }

    public Integer getSessionId() {
        return this.sessionId;
    }

    public void setSessionId(Integer sessionId) {
        this.sessionId = sessionId;
    }

    public String getIpaddress() {
        return this.ipaddress;
    }

    public void setIpaddress(String ipaddress) {
        this.ipaddress = ipaddress;
    }

    public String getSessionTxt() {
        return this.sessionTxt;
    }

    public void setSessionDuration(Long sessionDuration) {
        this.sessionDuration = sessionDuration;
    }

    public Long getSessionDuration() {
        return this.sessionDuration;
    }

    public void setSessionTxt(String sessionTxt) {
        this.sessionTxt = sessionTxt;
    }

    public String getBrowserType() {
        return this.browserType;
    }

    public void setBrowserType(String browserType) {
        this.browserType = browserType;
    }

    public Date getLastModTime() {
        return this.lastModTime;
    }

    public void setLastModTime(Date lastModTime) {
        this.lastModTime = lastModTime;
    }

    public Date getInsertTime() {
        return this.insertTime;
    }

    public void setInsertTime(Date insertTime) {
        this.insertTime = insertTime;
    }

    public int getUserId() {
        return this.userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public Integer getContextId() {
        return this.contextId;
    }

    public void setContextId(Integer contextId) {
        this.contextId = contextId;
    }

    public Date getSessionStartTime() {
        return this.sessionStartTime;
    }

    public void setSessionStartTime(Date sessionStartTime) {
        this.sessionStartTime = sessionStartTime;
    }

    public Date getSessionEndTime() {
        return this.sessionEndTime;
    }

    public void setSessionEndTime(Date sessionEndTime) {
        this.sessionEndTime = sessionEndTime;
    }

    public Integer getSessionHits() {
        return this.sessionHits;
    }

    public void setSessionHits(Integer sessionHits) {
        this.sessionHits = sessionHits;
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

    public String toString() {
        return new ToStringBuilder(this)
            .append("sessionId", getSessionId())
            .toString();
    }

    public boolean equals(Object other) {
        if ( !(other instanceof SessionBean) ) return false;
        SessionBean castOther = (SessionBean) other;
        return new EqualsBuilder()
            .append(this.getSessionId(), castOther.getSessionId())
            .isEquals();
    }

    public int hashCode() {
        return new HashCodeBuilder()
            .append(getSessionId())
            .toHashCode();
    }

}
