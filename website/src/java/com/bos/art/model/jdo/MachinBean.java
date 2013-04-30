package com.bos.art.model.jdo;

import java.io.Serializable;
import java.util.Date;
import java.util.Set;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

/** @author Hibernate CodeGenerator */
public class MachinBean implements Serializable {

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

    /** persistent field */
    private Set accessRecords;

    /** persistent field */
    private Set hourlyStatistics;

    /** persistent field */
    private Set fiveSecondLoads;

    /** persistent field */
    private Set minuteStatistics;

    /** full constructor */
    public MachinBean(Integer machineId, String machineName, String shortName, String machineType, Date lastModTime, Set accessRecords, Set hourlyStatistics, Set fiveSecondLoads, Set minuteStatistics) {
        this.machineId = machineId;
        this.machineName = machineName;
        this.shortName = shortName;
        this.machineType = machineType;
        this.lastModTime = lastModTime;
        this.accessRecords = accessRecords;
        this.hourlyStatistics = hourlyStatistics;
        this.fiveSecondLoads = fiveSecondLoads;
        this.minuteStatistics = minuteStatistics;
    }

    /** default constructor */
    public MachinBean() {
    }

    /** minimal constructor */
    public MachinBean(Integer machineId, Set accessRecords, Set hourlyStatistics, Set fiveSecondLoads, Set minuteStatistics) {
        this.machineId = machineId;
        this.accessRecords = accessRecords;
        this.hourlyStatistics = hourlyStatistics;
        this.fiveSecondLoads = fiveSecondLoads;
        this.minuteStatistics = minuteStatistics;
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

    public Set getAccessRecords() {
        return this.accessRecords;
    }

    public void setAccessRecords(Set accessRecords) {
        this.accessRecords = accessRecords;
    }

    public Set getHourlyStatistics() {
        return this.hourlyStatistics;
    }

    public void setHourlyStatistics(Set hourlyStatistics) {
        this.hourlyStatistics = hourlyStatistics;
    }

    public Set getFiveSecondLoads() {
        return this.fiveSecondLoads;
    }

    public void setFiveSecondLoads(Set fiveSecondLoads) {
        this.fiveSecondLoads = fiveSecondLoads;
    }

    public Set getMinuteStatistics() {
        return this.minuteStatistics;
    }

    public void setMinuteStatistics(Set minuteStatistics) {
        this.minuteStatistics = minuteStatistics;
    }

    public String toString() {
        return new ToStringBuilder(this)
            .append("machineId", getMachineId())
            .toString();
    }

    public boolean equals(Object other) {
        if ( !(other instanceof MachinBean) ) return false;
        MachinBean castOther = (MachinBean) other;
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
