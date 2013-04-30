package com.bos.art.model.jdo;

import java.io.Serializable;
import java.util.Date;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

/** @author Hibernate CodeGenerator */
public class RloadContext implements Serializable {

    /** identifier field */
    private Integer contextId;

    /** nullable persistent field */
    private String contextName;

    /** nullable persistent field */
    private Date lastModTime;

    /** full constructor */
    public RloadContext(Integer contextId, String contextName, Date lastModTime) {
        this.contextId = contextId;
        this.contextName = contextName;
        this.lastModTime = lastModTime;
    }

    /** default constructor */
    public RloadContext() {
    }

    /** minimal constructor */
    public RloadContext(Integer contextId) {
        this.contextId = contextId;
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

    public String toString() {
        return new ToStringBuilder(this)
            .append("contextId", getContextId())
            .toString();
    }

    public boolean equals(Object other) {
        if ( !(other instanceof RloadContext) ) return false;
        RloadContext castOther = (RloadContext) other;
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
