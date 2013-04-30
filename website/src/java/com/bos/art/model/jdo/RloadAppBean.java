package com.bos.art.model.jdo;

import java.io.Serializable;
import java.util.Date;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

/** @author Hibernate CodeGenerator */
public class RloadAppBean implements Serializable {

    /** identifier field */
    private Integer appId;

    /** nullable persistent field */
    private String appName;

    /** nullable persistent field */
    private Date lastModTime;

    /** full constructor */
    public RloadAppBean(Integer appId, String appName, Date lastModTime) {
        this.appId = appId;
        this.appName = appName;
        this.lastModTime = lastModTime;
    }

    /** default constructor */
    public RloadAppBean() {
    }

    /** minimal constructor */
    public RloadAppBean(Integer appId) {
        this.appId = appId;
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

    public String toString() {
        return new ToStringBuilder(this)
            .append("appId", getAppId())
            .toString();
    }

    public boolean equals(Object other) {
        if ( !(other instanceof RloadAppBean) ) return false;
        RloadAppBean castOther = (RloadAppBean) other;
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
