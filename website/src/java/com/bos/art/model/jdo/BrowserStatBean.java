package com.bos.art.model.jdo;

import java.io.Serializable;
import java.util.Date;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

/** @author Hibernate CodeGenerator */
public class BrowserStatBean implements Serializable {

    /** identifier field */
    private Date day;

    /** identifier field */
    private int count;

    /** identifier field */
    private String state;

    /** identifier field */
    private Date lastModTime;

    /** persistent field */
    private com.bos.art.model.jdo.BrowserBean browser;

    /** full constructor */
    public BrowserStatBean(Date day, int count, String state, Date lastModTime, com.bos.art.model.jdo.BrowserBean browser) {
        this.day = day;
        this.count = count;
        this.state = state;
        this.lastModTime = lastModTime;
        this.browser = browser;
    }

    /** default constructor */
    public BrowserStatBean() {
    }

    public Date getDay() {
        return this.day;
    }

    public void setDay(Date day) {
        this.day = day;
    }

    public int getCount() {
        return this.count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public String getState() {
        return this.state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public Date getLastModTime() {
        return this.lastModTime;
    }

    public void setLastModTime(Date lastModTime) {
        this.lastModTime = lastModTime;
    }

    public com.bos.art.model.jdo.BrowserBean getBrowser() {
        return this.browser;
    }

    public void setBrowser(com.bos.art.model.jdo.BrowserBean browser) {
        this.browser = browser;
    }

    public String toString() {
        return new ToStringBuilder(this)
            .append("day", getDay())
            .append("count", getCount())
            .append("state", getState())
            .append("lastModTime", getLastModTime())
            .toString();
    }

    public boolean equals(Object other) {
        if ( !(other instanceof BrowserStatBean) ) return false;
        BrowserStatBean castOther = (BrowserStatBean) other;
        return new EqualsBuilder()
            .append(this.getDay(), castOther.getDay())
            .append(this.getCount(), castOther.getCount())
            .append(this.getState(), castOther.getState())
            .append(this.getLastModTime(), castOther.getLastModTime())
            .isEquals();
    }

    public int hashCode() {
        return new HashCodeBuilder()
            .append(getDay())
            .append(getCount())
            .append(getState())
            .append(getLastModTime())
            .toHashCode();
    }

}
