package com.bos.art.model.jdo;

import java.io.Serializable;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

/** @author Hibernate CodeGenerator */
public class OrderStats implements Serializable {

    /** identifier field */
    private String day;

    /** nullable persistent field */
    private String Hour;

    /** nullable persistent field */
    private int orderCount;

    /** nullable persistent field */
    private int lineCount;

    /** nullable persistent field */
    private double dollarTotal;

    /** nullable persistent field */
    private int opOrderCount;

    /** nullable persistent field */
    private int opLineCount;

    /** nullable persistent field */
    private double opDollarTotal;

    /** full constructor */
    public OrderStats(String day, String Hour, int orderCount, int lineCount, double dollarTotal, int opOrderCount, int opLineCount, double opDollarTotal) {
        this.day = day;
        this.Hour = Hour;
        this.orderCount = orderCount;
        this.lineCount = lineCount;
        this.dollarTotal = dollarTotal;
        this.opOrderCount = opOrderCount;
        this.opLineCount = opLineCount;
        this.opDollarTotal = opDollarTotal;
    }

    /** default constructor */
    public OrderStats() {
    }

    /** minimal constructor */
    public OrderStats(String day) {
        this.day = day;
    }

    public String getDay() {
        return this.day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public String getHour() {
        return this.Hour;
    }

    public void setHour(String Hour) {
        this.Hour = Hour;
    }

    public int getOrderCount() {
        return this.orderCount;
    }

    public void setOrderCount(int orderCount) {
        this.orderCount = orderCount;
    }

    public int getLineCount() {
        return this.lineCount;
    }

    public void setLineCount(int lineCount) {
        this.lineCount = lineCount;
    }

    public double getDollarTotal() {
        return this.dollarTotal;
    }

    public void setDollarTotal(double dollarTotal) {
        this.dollarTotal = dollarTotal;
    }

    public int getOpOrderCount() {
        return this.opOrderCount;
    }

    public void setOpOrderCount(int opOrderCount) {
        this.opOrderCount = opOrderCount;
    }

    public int getOpLineCount() {
        return this.opLineCount;
    }

    public void setOpLineCount(int opLineCount) {
        this.opLineCount = opLineCount;
    }

    public double getOpDollarTotal() {
        return this.opDollarTotal;
    }

    public void setOpDollarTotal(double opDollarTotal) {
        this.opDollarTotal = opDollarTotal;
    }

    public String toString() {
        return new ToStringBuilder(this)
            .append("day", getDay())
            .toString();
    }

    public boolean equals(Object other) {
        if ( !(other instanceof OrderStats) ) return false;
        OrderStats castOther = (OrderStats) other;
        return new EqualsBuilder()
            .append(this.getDay(), castOther.getDay())
            .isEquals();
    }

    public int hashCode() {
        return new HashCodeBuilder()
            .append(getDay())
            .toHashCode();
    }

}
