package com.bos.art.model.jdo;

import java.io.Serializable;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

/** @author Hibernate CodeGenerator */
public class StackTraceBeanContainer implements Serializable {

    /** identifier field */
    private String jspBeanContainer;

    /** persistent field */
    private com.bos.art.model.jdo.StackTrac stackTrac;

    /** full constructor */
    public StackTraceBeanContainer(String jspBeanContainer, com.bos.art.model.jdo.StackTrac stackTrac) {
        this.jspBeanContainer = jspBeanContainer;
        this.stackTrac = stackTrac;
    }

    /** default constructor */
    public StackTraceBeanContainer() {
    }

    public String getJspBeanContainer() {
        return this.jspBeanContainer;
    }

    public void setJspBeanContainer(String jspBeanContainer) {
        this.jspBeanContainer = jspBeanContainer;
    }

    public com.bos.art.model.jdo.StackTrac getStackTrac() {
        return this.stackTrac;
    }

    public void setStackTrac(com.bos.art.model.jdo.StackTrac stackTrac) {
        this.stackTrac = stackTrac;
    }

    public String toString() {
        return new ToStringBuilder(this)
            .append("jspBeanContainer", getJspBeanContainer())
            .toString();
    }

    public boolean equals(Object other) {
        if ( !(other instanceof StackTraceBeanContainer) ) return false;
        StackTraceBeanContainer castOther = (StackTraceBeanContainer) other;
        return new EqualsBuilder()
            .append(this.getJspBeanContainer(), castOther.getJspBeanContainer())
            .isEquals();
    }

    public int hashCode() {
        return new HashCodeBuilder()
            .append(getJspBeanContainer())
            .toHashCode();
    }

}
