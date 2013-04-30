package com.bos.art.model.jdo;

import java.io.Serializable;
import java.util.Date;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

/** @author Hibernate CodeGenerator */
public class AccumulatorBean implements Serializable {

    /** identifier field */
    private Integer accumulatorStatId;

    /** nullable persistent field */
    private String accumulatorName;

    /** nullable persistent field */
    private String accumulatorDescription;

    /** nullable persistent field */
    private String accumulatorType;

    /** nullable persistent field */
    private String dataUnits;

    /** nullable persistent field */
    private Date lastModTime;

    /** full constructor */
    public AccumulatorBean(Integer accumulatorStatId, String accumulatorName, String accumulatorDescription, String accumulatorType, String dataUnits, Date lastModTime) {
        this.accumulatorStatId = accumulatorStatId;
        this.accumulatorName = accumulatorName;
        this.accumulatorDescription = accumulatorDescription;
        this.accumulatorType = accumulatorType;
        this.dataUnits = dataUnits;
        this.lastModTime = lastModTime;
    }

    /** default constructor */
    public AccumulatorBean() {
    }

    /** minimal constructor */
    public AccumulatorBean(Integer accumulatorStatId) {
        this.accumulatorStatId = accumulatorStatId;
    }

    public Integer getAccumulatorStatId() {
        return this.accumulatorStatId;
    }

    public void setAccumulatorStatId(Integer accumulatorStatId) {
        this.accumulatorStatId = accumulatorStatId;
    }

    public String getAccumulatorName() {
        return this.accumulatorName;
    }

    public void setAccumulatorName(String accumulatorName) {
        this.accumulatorName = accumulatorName;
    }

    public String getAccumulatorDescription() {
        return this.accumulatorDescription;
    }

    public void setAccumulatorDescription(String accumulatorDescription) {
        this.accumulatorDescription = accumulatorDescription;
    }

    public String getAccumulatorType() {
        return this.accumulatorType;
    }

    public void setAccumulatorType(String accumulatorType) {
        this.accumulatorType = accumulatorType;
    }

    public String getDataUnits() {
        return this.dataUnits;
    }

    public void setDataUnits(String dataUnits) {
        this.dataUnits = dataUnits;
    }

    public Date getLastModTime() {
        return this.lastModTime;
    }

    public void setLastModTime(Date lastModTime) {
        this.lastModTime = lastModTime;
    }

    public String toString() {
        return new ToStringBuilder(this)
            .append("accumulatorStatId", getAccumulatorStatId())
            .toString();
    }

    public boolean equals(Object other) {
        if ( !(other instanceof AccumulatorBean) ) return false;
        AccumulatorBean castOther = (AccumulatorBean) other;
        return new EqualsBuilder()
            .append(this.getAccumulatorStatId(), castOther.getAccumulatorStatId())
            .isEquals();
    }

    public int hashCode() {
        return new HashCodeBuilder()
            .append(getAccumulatorStatId())
            .toHashCode();
    }

}
