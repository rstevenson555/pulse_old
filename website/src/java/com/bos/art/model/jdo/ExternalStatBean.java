package com.bos.art.model.jdo;

import java.io.Serializable;
import java.util.Date;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

/** @author Hibernate CodeGenerator */
public class ExternalStatBean implements Serializable {

    /** identifier field */
    private Integer classificationId;

    /** nullable persistent field */
    private String destination;

    /** nullable persistent field */
    private String description;

    /** nullable persistent field */
    private Date lastModTime;

    /** full constructor */
    public ExternalStatBean(Integer classificationId, String destination, String description, Date lastModTime) {
        this.classificationId = classificationId;
        this.destination = destination;
        this.description = description;
        this.lastModTime = lastModTime;
    }

    /** default constructor */
    public ExternalStatBean() {
    }

    /** minimal constructor */
    public ExternalStatBean(Integer classificationId) {
        this.classificationId = classificationId;
    }

    public Integer getClassificationId() {
        return this.classificationId;
    }

    public void setClassificationId(Integer classificationId) {
        this.classificationId = classificationId;
    }

    public String getDestination() {
        return this.destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
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

    public String toString() {
        return new ToStringBuilder(this)
            .append("classificationId", getClassificationId())
            .toString();
    }

    public boolean equals(Object other) {
        if ( !(other instanceof ExternalStatBean) ) return false;
        ExternalStatBean castOther = (ExternalStatBean) other;
        return new EqualsBuilder()
            .append(this.getClassificationId(), castOther.getClassificationId())
            .isEquals();
    }

    public int hashCode() {
        return new HashCodeBuilder()
            .append(getClassificationId())
            .toHashCode();
    }

}
