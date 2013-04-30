package com.bos.art.model.jdo;

import java.io.Serializable;
import java.util.Date;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

/** @author Hibernate CodeGenerator */
public class RloadSession implements Serializable {

    /** identifier field */
    private Integer sessionId;

    /** nullable persistent field */
    private String ipaddress;

    /** nullable persistent field */
    private String sessionTxt;

    /** nullable persistent field */
    private String browserType;

    /** nullable persistent field */
    private int userId;

    /** nullable persistent field */
    private Date lastModTime;

    /** full constructor */
    public RloadSession(Integer sessionId, String ipaddress, String sessionTxt, String browserType, int userId, Date lastModTime) {
        this.sessionId = sessionId;
        this.ipaddress = ipaddress;
        this.sessionTxt = sessionTxt;
        this.browserType = browserType;
        this.userId = userId;
        this.lastModTime = lastModTime;
    }

    /** default constructor */
    public RloadSession() {
    }

    /** minimal constructor */
    public RloadSession(Integer sessionId) {
        this.sessionId = sessionId;
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

    public void setSessionTxt(String sessionTxt) {
        this.sessionTxt = sessionTxt;
    }

    public String getBrowserType() {
        return this.browserType;
    }

    public void setBrowserType(String browserType) {
        this.browserType = browserType;
    }

    public int getUserId() {
        return this.userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public Date getLastModTime() {
        return this.lastModTime;
    }

    public void setLastModTime(Date lastModTime) {
        this.lastModTime = lastModTime;
    }

    public String toString() {
        return new ToStringBuilder(this)
            .append("sessionId", getSessionId())
            .toString();
    }

    public boolean equals(Object other) {
        if ( !(other instanceof RloadSession) ) return false;
        RloadSession castOther = (RloadSession) other;
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
