package com.bos.art.model.jdo;

import java.io.Serializable;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

/** @author Hibernate CodeGenerator */
public class StackTraceDetail implements Serializable {

    /** identifier field */
    private com.bos.art.model.jdo.StackTraceDetailPK comp_id;

    /** persistent field */
    private com.bos.art.model.jdo.StackTraceRow stackTraceRow;

    /** full constructor */
    public StackTraceDetail(com.bos.art.model.jdo.StackTraceDetailPK comp_id, com.bos.art.model.jdo.StackTraceRow stackTraceRow) {
        this.comp_id = comp_id;
        this.stackTraceRow = stackTraceRow;
    }

    /** default constructor */
    public StackTraceDetail() {
    }

    public com.bos.art.model.jdo.StackTraceDetailPK getComp_id() {
        return this.comp_id;
    }

    public void setComp_id(com.bos.art.model.jdo.StackTraceDetailPK comp_id) {
        this.comp_id = comp_id;
    }

    public com.bos.art.model.jdo.StackTraceRow getStackTraceRow() {
        return this.stackTraceRow;
    }

    public void setStackTraceRow(com.bos.art.model.jdo.StackTraceRow stackTraceRow) {
        this.stackTraceRow = stackTraceRow;
    }

    public String toString() {
        return new ToStringBuilder(this)
            .append("comp_id", getComp_id())
            .toString();
    }

    public boolean equals(Object other) {
        if ( !(other instanceof StackTraceDetail) ) return false;
        StackTraceDetail castOther = (StackTraceDetail) other;
        return new EqualsBuilder()
            .append(this.getComp_id(), castOther.getComp_id())
            .isEquals();
    }

    public int hashCode() {
        return new HashCodeBuilder()
            .append(getComp_id())
            .toHashCode();
    }

}
