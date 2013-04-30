package com.bos.art.model.jdo;

import java.io.Serializable;
import java.util.Set;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

/** @author Hibernate CodeGenerator */
public class StackTraceRow implements Serializable {

    /** identifier field */
    private Integer rowId;

    /** nullable persistent field */
    private String rowMessage;

    /** persistent field */
    private Set stackTraceDetails;

    /** full constructor */
    public StackTraceRow(Integer rowId, String rowMessage, Set stackTraceDetails) {
        this.rowId = rowId;
        this.rowMessage = rowMessage;
        this.stackTraceDetails = stackTraceDetails;
    }

    /** default constructor */
    public StackTraceRow() {
    }

    /** minimal constructor */
    public StackTraceRow(Integer rowId, Set stackTraceDetails) {
        this.rowId = rowId;
        this.stackTraceDetails = stackTraceDetails;
    }

    public Integer getRowId() {
        return this.rowId;
    }

    public void setRowId(Integer rowId) {
        this.rowId = rowId;
    }

    public String getRowMessage() {
        return this.rowMessage;
    }

    public void setRowMessage(String rowMessage) {
        this.rowMessage = rowMessage;
    }

    public Set getStackTraceDetails() {
        return this.stackTraceDetails;
    }

    public void setStackTraceDetails(Set stackTraceDetails) {
        this.stackTraceDetails = stackTraceDetails;
    }

    public String toString() {
        return new ToStringBuilder(this)
            .append("rowId", getRowId())
            .toString();
    }

    public boolean equals(Object other) {
        if ( !(other instanceof StackTraceRow) ) return false;
        StackTraceRow castOther = (StackTraceRow) other;
        return new EqualsBuilder()
            .append(this.getRowId(), castOther.getRowId())
            .isEquals();
    }

    public int hashCode() {
        return new HashCodeBuilder()
            .append(getRowId())
            .toHashCode();
    }

}
