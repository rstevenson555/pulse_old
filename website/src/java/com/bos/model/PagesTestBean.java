package com.bos.model;

import java.io.Serializable;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

/** @author Hibernate CodeGenerator */
public class PagesTestBean implements Serializable {

    /** identifier field */
    private Integer pageTestId;

    /** nullable persistent field */
    private String pageName;

    /** nullable persistent field */
    private String isErrorPage;

    /** full constructor */
    public PagesTestBean(Integer pageTestId, String pageName, String isErrorPage) {
        this.pageTestId = pageTestId;
        this.pageName = pageName;
        this.isErrorPage = isErrorPage;
    }

    /** default constructor */
    public PagesTestBean() {
    }

    /** minimal constructor */
    public PagesTestBean(Integer pageTestId) {
        this.pageTestId = pageTestId;
    }

    public Integer getPageTestId() {
        return this.pageTestId;
    }

    public void setPageTestId(Integer pageTestId) {
        this.pageTestId = pageTestId;
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

    public String toString() {
        return new ToStringBuilder(this)
            .append("pageTestId", getPageTestId())
            .toString();
    }

    public boolean equals(Object other) {
        if ( !(other instanceof PagesTestBean) ) return false;
        PagesTestBean castOther = (PagesTestBean) other;
        return new EqualsBuilder()
            .append(this.getPageTestId(), castOther.getPageTestId())
            .isEquals();
    }

    public int hashCode() {
        return new HashCodeBuilder()
            .append(getPageTestId())
            .toHashCode();
    }

}
