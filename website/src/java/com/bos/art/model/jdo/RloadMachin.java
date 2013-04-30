package com.bos.art.model.jdo;

import java.io.Serializable;
import java.util.Date;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

/** @author Hibernate CodeGenerator */
public class RloadMachin implements Serializable {

    /** identifier field */
    private Integer machineId;

    /** nullable persistent field */
    private String machineName;

    /** nullable persistent field */
    private String shortName;

    /** nullable persistent field */
    private String machineType;

    /** nullable persistent field */
    private Date lastModTime;

    /** full constructor */
    public RloadMachin(Integer machineId, String machineName, String shortName, String machineType, Date lastModTime) {
        this.machineId = machineId;
        this.machineName = machineName;
        this.shortName = shortName;
        this.machineType = machineType;
        this.lastModTime = lastModTime;
    }

    /** default constructor */
    public RloadMachin() {
    }

    /** minimal constructor */
    public RloadMachin(Integer machineId) {
        this.machineId = machineId;
    }

    public Integer getMachineId() {
        return this.machineId;
    }

    public void setMachineId(Integer machineId) {
        this.machineId = machineId;
    }

    public String getMachineName() {
        return this.machineName;
    }

    public void setMachineName(String machineName) {
        this.machineName = machineName;
    }

    public String getShortName() {
        return this.shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public String getMachineType() {
        return this.machineType;
    }

    public void setMachineType(String machineType) {
        this.machineType = machineType;
    }

    public Date getLastModTime() {
        return this.lastModTime;
    }

    public void setLastModTime(Date lastModTime) {
        this.lastModTime = lastModTime;
    }

    public String toString() {
        return new ToStringBuilder(this)
            .append("machineId", getMachineId())
            .toString();
    }

    public boolean equals(Object other) {
        if ( !(other instanceof RloadMachin) ) return false;
        RloadMachin castOther = (RloadMachin) other;
        return new EqualsBuilder()
            .append(this.getMachineId(), castOther.getMachineId())
            .isEquals();
    }

    public int hashCode() {
        return new HashCodeBuilder()
            .append(getMachineId())
            .toHashCode();
    }

}
