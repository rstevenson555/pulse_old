package com.bos.art.model.jdo;

import java.io.Serializable;
import java.util.Date;
import java.util.Set;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

/** @author Hibernate CodeGenerator */
public class PagBean implements Serializable {

    /** identifier field */
    private Integer pageId;

    /** nullable persistent field */
    private String pageName;

    /** nullable persistent field */
    private String isErrorPage;

    /** nullable persistent field */
    private Date lastModTime;

    /** persistent field */
    private Set accessRecords;

    /** persistent field */
    private Set fiveSecondLoads;

    /** persistent field */
    private Set dailyPageLoadTims;

    /** full constructor */
    public PagBean(Integer pageId, String pageName, String isErrorPage, Date lastModTime, Set accessRecords, Set fiveSecondLoads, Set dailyPageLoadTims) {
        this.pageId = pageId;
        this.pageName = pageName;
        this.isErrorPage = isErrorPage;
        this.lastModTime = lastModTime;
        this.accessRecords = accessRecords;
        this.fiveSecondLoads = fiveSecondLoads;
        this.dailyPageLoadTims = dailyPageLoadTims;
    }

    /** default constructor */
    public PagBean() {
    }

    /** minimal constructor */
    public PagBean(Integer pageId, Set accessRecords, Set fiveSecondLoads, Set dailyPageLoadTims) {
        this.pageId = pageId;
        this.accessRecords = accessRecords;
        this.fiveSecondLoads = fiveSecondLoads;
        this.dailyPageLoadTims = dailyPageLoadTims;
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

    public String toString() {
        return new ToStringBuilder(this)
            .append("pageId", getPageId())
            .toString();
    }

    public boolean equals(Object other) {
        if ( !(other instanceof PagBean) ) return false;
        PagBean castOther = (PagBean) other;
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
