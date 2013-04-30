package com.bos.art.model.jdo;

import java.io.Serializable;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

/** @author Hibernate CodeGenerator */
public class StackTraceDetailPK implements Serializable {

    /** identifier field */
    private Integer stackDepth;

    /** identifier field */
    private com.bos.art.model.jdo.StackTrac stackTrac;

    /** full constructor */
    public StackTraceDetailPK(Integer stackDepth, com.bos.art.model.jdo.StackTrac stackTrac) {
        this.stackDepth = stackDepth;
        this.stackTrac = stackTrac;
    }

    /** default constructor */
    public StackTraceDetailPK() {
    }

    public Integer getStackDepth() {
        return this.stackDepth;
    }

    public void setStackDepth(Integer stackDepth) {
        this.stackDepth = stackDepth;
    }

    public com.bos.art.model.jdo.StackTrac getStackTrac() {
        return this.stackTrac;
    }

    public void setStackTrac(com.bos.art.model.jdo.StackTrac stackTrac) {
        this.stackTrac = stackTrac;
    }

    public String toString() {
        return new ToStringBuilder(this)
            .append("stackDepth", getStackDepth())
            .append("stackTrac", getStackTrac())
            .toString();
    }

    public boolean equals(Object other) {
        if ( !(other instanceof StackTraceDetailPK) ) return false;
        StackTraceDetailPK castOther = (StackTraceDetailPK) other;
        return new EqualsBuilder()
            .append(this.getStackDepth(), castOther.getStackDepth())
            .append(this.getStackTrac(), castOther.getStackTrac())
            .isEquals();
    }

    public int hashCode() {
        return new HashCodeBuilder()
            .append(getStackDepth())
            .append(getStackTrac())
            .toHashCode();
    }

}
