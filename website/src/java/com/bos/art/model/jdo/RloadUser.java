package com.bos.art.model.jdo;

import java.io.Serializable;
import java.util.Date;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

/** @author Hibernate CodeGenerator */
public class RloadUser implements Serializable {

    /** identifier field */
    private Integer userId;

    /** nullable persistent field */
    private String userName;

    /** nullable persistent field */
    private String fullName;

    /** nullable persistent field */
    private String companyName;

    /** nullable persistent field */
    private Date lastModTime;

    /** full constructor */
    public RloadUser(Integer userId, String userName, String fullName, String companyName, Date lastModTime) {
        this.userId = userId;
        this.userName = userName;
        this.fullName = fullName;
        this.companyName = companyName;
        this.lastModTime = lastModTime;
    }

    /** default constructor */
    public RloadUser() {
    }

    /** minimal constructor */
    public RloadUser(Integer userId) {
        this.userId = userId;
    }

    public Integer getUserId() {
        return this.userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return this.userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getFullName() {
        return this.fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getCompanyName() {
        return this.companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public Date getLastModTime() {
        return this.lastModTime;
    }

    public void setLastModTime(Date lastModTime) {
        this.lastModTime = lastModTime;
    }

    public String toString() {
        return new ToStringBuilder(this)
            .append("userId", getUserId())
            .toString();
    }

    public boolean equals(Object other) {
        if ( !(other instanceof RloadUser) ) return false;
        RloadUser castOther = (RloadUser) other;
        return new EqualsBuilder()
            .append(this.getUserId(), castOther.getUserId())
            .isEquals();
    }

    public int hashCode() {
        return new HashCodeBuilder()
            .append(getUserId())
            .toHashCode();
    }

}
