package com.bos.art.model.jdo;

import java.io.Serializable;
import java.util.Date;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

/** @author Hibernate CodeGenerator */
public class FiveSecondLoadBean implements Serializable {

    /** identifier field */
    private Integer recordPk;

    /** nullable persistent field */
    private Date lastModTime;

    /** nullable persistent field */
    private Date time;

    /** nullable persistent field */
    private int loadTime;

    /** persistent field */
    private com.bos.art.model.jdo.SessionBean session;

    /** persistent field */
    private com.bos.art.model.jdo.AppBean app;

    /** persistent field */
    private com.bos.art.model.jdo.ContextBean context;

    /** persistent field */
    private com.bos.art.model.jdo.MachinBean machin;

    /** persistent field */
    private com.bos.art.model.jdo.PagBean pag;

    /** persistent field */
    private com.bos.art.model.jdo.UserBean user;

    /** full constructor */
    public FiveSecondLoadBean(Integer recordPk, Date lastModTime, Date time, int loadTime, com.bos.art.model.jdo.SessionBean session, com.bos.art.model.jdo.AppBean app, com.bos.art.model.jdo.ContextBean context, com.bos.art.model.jdo.MachinBean machin, com.bos.art.model.jdo.PagBean pag, com.bos.art.model.jdo.UserBean user) {
        this.recordPk = recordPk;
        this.lastModTime = lastModTime;
        this.time = time;
        this.loadTime = loadTime;
        this.session = session;
        this.app = app;
        this.context = context;
        this.machin = machin;
        this.pag = pag;
        this.user = user;
    }

    /** default constructor */
    public FiveSecondLoadBean() {
    }

    /** minimal constructor */
    public FiveSecondLoadBean(Integer recordPk, com.bos.art.model.jdo.SessionBean session, com.bos.art.model.jdo.AppBean app, com.bos.art.model.jdo.ContextBean context, com.bos.art.model.jdo.MachinBean machin, com.bos.art.model.jdo.PagBean pag, com.bos.art.model.jdo.UserBean user) {
        this.recordPk = recordPk;
        this.session = session;
        this.app = app;
        this.context = context;
        this.machin = machin;
        this.pag = pag;
        this.user = user;
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

    public com.bos.art.model.jdo.SessionBean getSession() {
        return this.session;
    }

    public void setSession(com.bos.art.model.jdo.SessionBean session) {
        this.session = session;
    }

    public com.bos.art.model.jdo.AppBean getApp() {
        return this.app;
    }

    public void setApp(com.bos.art.model.jdo.AppBean app) {
        this.app = app;
    }

    public com.bos.art.model.jdo.ContextBean getContext() {
        return this.context;
    }

    public void setContext(com.bos.art.model.jdo.ContextBean context) {
        this.context = context;
    }

    public com.bos.art.model.jdo.MachinBean getMachin() {
        return this.machin;
    }

    public void setMachin(com.bos.art.model.jdo.MachinBean machin) {
        this.machin = machin;
    }

    public com.bos.art.model.jdo.PagBean getPag() {
        return this.pag;
    }

    public void setPag(com.bos.art.model.jdo.PagBean pag) {
        this.pag = pag;
    }

    public com.bos.art.model.jdo.UserBean getUser() {
        return this.user;
    }

    public void setUser(com.bos.art.model.jdo.UserBean user) {
        this.user = user;
    }

    public String toString() {
        return new ToStringBuilder(this)
            .append("recordPk", getRecordPk())
            .toString();
    }

    public boolean equals(Object other) {
        if ( !(other instanceof FiveSecondLoadBean) ) return false;
        FiveSecondLoadBean castOther = (FiveSecondLoadBean) other;
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
