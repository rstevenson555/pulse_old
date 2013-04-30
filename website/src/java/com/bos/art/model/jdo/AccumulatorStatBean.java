package com.bos.art.model.jdo;

import java.io.Serializable;
import java.util.Date;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

/** @author Hibernate CodeGenerator */
public class AccumulatorStatBean implements Serializable {

    /** identifier field */
    private int accumulatorStatId;

    /** identifier field */
    private Date lastModTime;

    /** identifier field */
    private Date time;

    /** identifier field */
    private int value;

    /** full constructor */
    public AccumulatorStatBean(int accumulatorStatId, Date lastModTime, Date time, int value) {
        this.accumulatorStatId = accumulatorStatId;
        this.lastModTime = lastModTime;
        this.time = time;
        this.value = value;
    }

    /** default constructor */
    public AccumulatorStatBean() {
    }

    public int getAccumulatorStatId() {
        return this.accumulatorStatId;
    }

    public void setAccumulatorStatId(int accumulatorStatId) {
        this.accumulatorStatId = accumulatorStatId;
    }

    public Date getLastModTime() {
        return this.lastModTime;
    }

    public void setLastModTime(Date lastModTime) {
        this.lastModTime = lastModTime;
    }

    public Date getTime() {
        return this.time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    public int getValue() {
        return this.value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public String toString() {
        return new ToStringBuilder(this)
            .append("accumulatorStatId", getAccumulatorStatId())
            .append("lastModTime", getLastModTime())
            .append("time", getTime())
            .append("value", getValue())
            .toString();
    }

    public boolean equals(Object other) {
        if ( !(other instanceof AccumulatorStatBean) ) return false;
        AccumulatorStatBean castOther = (AccumulatorStatBean) other;
        return new EqualsBuilder()
            .append(this.getAccumulatorStatId(), castOther.getAccumulatorStatId())
            .append(this.getLastModTime(), castOther.getLastModTime())
            .append(this.getTime(), castOther.getTime())
            .append(this.getValue(), castOther.getValue())
            .isEquals();
    }

    public int hashCode() {
        return new HashCodeBuilder()
            .append(getAccumulatorStatId())
            .append(getLastModTime())
            .append(getTime())
            .append(getValue())
            .toHashCode();
    }

}
