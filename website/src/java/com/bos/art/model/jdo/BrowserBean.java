package com.bos.art.model.jdo;

import java.io.Serializable;
import java.util.Date;
import java.util.Set;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

/** @author Hibernate CodeGenerator */
public class BrowserBean implements Serializable {

    /** identifier field */
    private Integer browserId;

    /** nullable persistent field */
    private String patternMatchString;

    /** nullable persistent field */
    private String description;

    /** nullable persistent field */
    private Date lastModTime;

    /** persistent field */

    private Set browserStats;

    /** full constructor */
    public BrowserBean(Integer browserId, String patternMatchString, String description, Date lastModTime, Set browserStats) {
        this.browserId = browserId;
        this.patternMatchString = patternMatchString;
        this.description = description;
        this.lastModTime = lastModTime;
        this.browserStats = browserStats;
    }

    /** default constructor */
    public BrowserBean() {
    }

    /** minimal constructor */
    public BrowserBean(Integer browserId, Set browserStats) {
        this.browserId = browserId;
        this.browserStats = browserStats;
    }

    public Integer getBrowserId() {
        return this.browserId;
    }

    public void setBrowserId(Integer browserId) {
        this.browserId = browserId;
    }

    public String getPatternMatchString() {
        return this.patternMatchString;
    }

    public void setPatternMatchString(String patternMatchString) {
        this.patternMatchString = patternMatchString;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getLastModTime() {
        return this.lastModTime;
    }

    public void setLastModTime(Date lastModTime) {
        this.lastModTime = lastModTime;
    }

    public Set getBrowserStats() {
        return this.browserStats;
    }

    public void setBrowserStats(Set browserStats) {
        this.browserStats = browserStats;
    }

    public String toString() {
        return new ToStringBuilder(this)
            .append("browserId", getBrowserId())
            .toString();
    }

    public boolean equals(Object other) {
        if ( !(other instanceof BrowserBean) ) return false;
        BrowserBean castOther = (BrowserBean) other;
        return new EqualsBuilder()
            .append(this.getBrowserId(), castOther.getBrowserId())
            .isEquals();
    }

    public int hashCode() {
        return new HashCodeBuilder()
            .append(getBrowserId())
            .toHashCode();
    }

}
