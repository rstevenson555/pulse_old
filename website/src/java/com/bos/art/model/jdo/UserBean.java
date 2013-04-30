package com.bos.art.model.jdo;

import java.io.Serializable;
import java.util.Date;
import java.util.Set;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

/** @author Hibernate CodeGenerator */
public class UserBean implements Serializable {

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

    /** persistent field */
    private Set accessRecords;

    /** persistent field */
    private Set fiveSecondLoads;

    /** full constructor */
    public UserBean(Integer userId, String userName, String fullName, String companyName, Date lastModTime, Set accessRecords, Set fiveSecondLoads) {
        this.userId = userId;
        this.userName = userName;
        this.fullName = fullName;
        this.companyName = companyName;
        this.lastModTime = lastModTime;
        this.accessRecords = accessRecords;
        this.fiveSecondLoads = fiveSecondLoads;
    }

    /** default constructor */
    public UserBean() {
    }

    /** minimal constructor */
    public UserBean(Integer userId, Set accessRecords, Set fiveSecondLoads) {
        this.userId = userId;
        this.accessRecords = accessRecords;
        this.fiveSecondLoads = fiveSecondLoads;
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
            .append("userId", getUserId())
            .toString();
    }

    public boolean equals(Object other) {
        if ( !(other instanceof UserBean) ) return false;
        UserBean castOther = (UserBean) other;
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
