package com.bos.art.model.jdo;

import java.io.Serializable;
import java.util.Date;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

/** @author Hibernate CodeGenerator */
public class RloadPag implements Serializable {

    /** identifier field */
    private Integer pageId;

    /** nullable persistent field */
    private String pageName;

    /** nullable persistent field */
    private String isErrorPage;

    /** nullable persistent field */
    private Date lastModTime;

    /** full constructor */
    public RloadPag(Integer pageId, String pageName, String isErrorPage, Date lastModTime) {
        this.pageId = pageId;
        this.pageName = pageName;
        this.isErrorPage = isErrorPage;
        this.lastModTime = lastModTime;
    }

    /** default constructor */
    public RloadPag() {
    }

    /** minimal constructor */
    public RloadPag(Integer pageId) {
        this.pageId = pageId;
    }

    public Integer getPageId() {
        return this.pageId;
    }

    public void setPageId(Integer pageId) {
        this.pageId = pageId;
    }

    public String getPageName() {
        return this.pageName;
    }

    public void setPageName(String pageName) {
        this.pageName = pageName;
    }

    public String getIsErrorPage() {
        return this.isErrorPage;
    }

    public void setIsErrorPage(String isErrorPage) {
        this.isErrorPage = isErrorPage;
    }

    public Date getLastModTime() {
        return this.lastModTime;
    }

    public void setLastModTime(Date lastModTime) {
        this.lastModTime = lastModTime;
    }

    public String toString() {
        return new ToStringBuilder(this)
            .append("pageId", getPageId())
            .toString();
    }

    public boolean equals(Object other) {
        if ( !(other instanceof RloadPag) ) return false;
        RloadPag castOther = (RloadPag) other;
        return new EqualsBuilder()
            .append(this.getPageId(), castOther.getPageId())
            .isEquals();
    }

    public int hashCode() {
        return new HashCodeBuilder()
            .append(getPageId())
            .toHashCode();
    }

}
