package com.bos.art.model.jdo;

import java.io.Serializable;
import java.util.Date;
import java.util.Set;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

/** @author Hibernate CodeGenerator */
public class AppBean implements Serializable {

    /** identifier field */
    private Integer appId;

    /** nullable persistent field */
    private String appName;

    /** nullable persistent field */
    private Date lastModTime;

    /** persistent field */
    private Set accessRecords;

    /** persistent field */
    private Set fiveSecondLoads;

    /** full constructor */
    public AppBean(Integer appId, String appName, Date lastModTime, Set accessRecords, Set fiveSecondLoads) {
        this.appId = appId;
        this.appName = appName;
        this.lastModTime = lastModTime;
        this.accessRecords = accessRecords;
        this.fiveSecondLoads = fiveSecondLoads;
    }

    /** default constructor */
    public AppBean() {
    }

    /** minimal constructor */
    public AppBean(Integer appId, Set accessRecords, Set fiveSecondLoads) {
        this.appId = appId;
        this.accessRecords = accessRecords;
        this.fiveSecondLoads = fiveSecondLoads;
    }

    public Integer getAppId() {
        return this.appId;
    }

    public void setAppId(Integer appId) {
        this.appId = appId;
    }

    public String getAppName() {
        return this.appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
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

    public String toString() {
        return new ToStringBuilder(this)
            .append("appId", getAppId())
            .toString();
    }

    public boolean equals(Object other) {
        if ( !(other instanceof AppBean) ) return false;
        AppBean castOther = (AppBean) other;
        return new EqualsBuilder()
            .append(this.getAppId(), castOther.getAppId())
            .isEquals();
    }

    public int hashCode() {
        return new HashCodeBuilder()
            .append(getAppId())
            .toHashCode();
    }

}
