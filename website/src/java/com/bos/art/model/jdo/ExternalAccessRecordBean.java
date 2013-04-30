package com.bos.art.model.jdo;

import java.io.Serializable;
import java.util.Date;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

/** @author Hibernate CodeGenerator */
public class ExternalAccessRecordBean implements Serializable {

    /** identifier field */
    private Integer recordPk;

    /** nullable persistent field */
    private Date lastModTime;

    /** persistent field */
    private int pageId;

    /** persistent field */
    private int userId;

    /** persistent field */
    private int sessionId;

    /** persistent field */
    private int machineId;

    /** persistent field */
    private int contextId;

    /** persistent field */
    private int appId;

    /** nullable persistent field */
    private int classificationId;

    /** nullable persistent field */
    private String dataSection;

    /** nullable persistent field */
    private Date time;

    /** nullable persistent field */
    private int loadTime;

    /** full constructor */
    public ExternalAccessRecordBean(Integer recordPk, Date lastModTime, int pageId, int userId, int sessionId, int machineId, int contextId, int appId, int classificationId, String dataSection, Date time, int loadTime) {
        this.recordPk = recordPk;
        this.lastModTime = lastModTime;
        this.pageId = pageId;
        this.userId = userId;
        this.sessionId = sessionId;
        this.machineId = machineId;
        this.contextId = contextId;
        this.appId = appId;
        this.classificationId = classificationId;
        this.dataSection = dataSection;
        this.time = time;
        this.loadTime = loadTime;
    }

    /** default constructor */
    public ExternalAccessRecordBean() {
    }

    /** minimal constructor */
    public ExternalAccessRecordBean(Integer recordPk, int pageId, int userId, int sessionId, int machineId, int contextId, int appId) {
        this.recordPk = recordPk;
        this.pageId = pageId;
        this.userId = userId;
        this.sessionId = sessionId;
        this.machineId = machineId;
        this.contextId = contextId;
        this.appId = appId;
    }

    public Integer getRecordPk() {
        return this.recordPk;
    }

    public void setRecordPk(Integer recordPk) {
        this.recordPk = recordPk;
    }

    public Date getLastModTime() {
        return this.lastModTime;
    }

    public void setLastModTime(Date lastModTime) {
        this.lastModTime = lastModTime;
    }

    public int getPageId() {
        return this.pageId;
    }

    public void setPageId(int pageId) {
        this.pageId = pageId;
    }

    public int getUserId() {
        return this.userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getSessionId() {
        return this.sessionId;
    }

    public void setSessionId(int sessionId) {
        this.sessionId = sessionId;
    }

    public int getMachineId() {
        return this.machineId;
    }

    public void setMachineId(int machineId) {
        this.machineId = machineId;
    }

    public int getContextId() {
        return this.contextId;
    }

    public void setContextId(int contextId) {
        this.contextId = contextId;
    }

    public int getAppId() {
        return this.appId;
    }

    public void setAppId(int appId) {
        this.appId = appId;
    }

    public int getClassificationId() {
        return this.classificationId;
    }

    public void setClassificationId(int classificationId) {
        this.classificationId = classificationId;
    }

    public String getDataSection() {
        return this.dataSection;
    }

    public void setDataSection(String dataSection) {
        this.dataSection = dataSection;
    }

    public Date getTime() {
        return this.time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    public int getLoadTime() {
        return this.loadTime;
    }

    public void setLoadTime(int loadTime) {
        this.loadTime = loadTime;
    }

    public String toString() {
        return new ToStringBuilder(this)
            .append("recordPk", getRecordPk())
            .toString();
    }

    public boolean equals(Object other) {
        if ( !(other instanceof ExternalAccessRecordBean) ) return false;
        ExternalAccessRecordBean castOther = (ExternalAccessRecordBean) other;
        return new EqualsBuilder()
            .append(this.getRecordPk(), castOther.getRecordPk())
            .isEquals();
    }

    public int hashCode() {
        return new HashCodeBuilder()
            .append(getRecordPk())
            .toHashCode();
    }

}
