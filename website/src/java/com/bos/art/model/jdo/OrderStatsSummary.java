package com.bos.art.model.jdo;

import java.io.Serializable;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

/** @author Hibernate CodeGenerator */
public class OrderStatsSummary implements Serializable {

    /** identifier field */
    private String day;

    /** nullable persistent field */
    private Integer orderCount;

    /** nullable persistent field */
    private Integer opOrderCount;

    /** nullable persistent field */
    private Integer bxOrders;

    /** nullable persistent field */
    private Integer currentOrders;

    /** nullable persistent field */
    private Integer deletedOrders;

    /** nullable persistent field */
    private Integer lineCount;

    /** nullable persistent field */
    private Double dollarTotal;

    /** nullable persistent field */
    private Integer opLineCount;

    /** nullable persistent field */
    private Double opDollarTotal;

    /** nullable persistent field */
    private Integer bxLines;

    /** nullable persistent field */
    private Integer bxDollars;

    /** nullable persistent field */
    private Integer currentOrdersTlines;

    /** nullable persistent field */
    private Double currentOrdersTdollars;

    /** nullable persistent field */
    private Integer deletedOrdersTlines;

    /** nullable persistent field */
    private Double deletedOrdersTdollars;

    /** nullable persistent field */
    private Integer cscImprintOrderCount;

    /** nullable persistent field */
    private Integer cscImprintLineCount;

    /** nullable persistent field */
    private Double cscImprintDollarTotal;

    /** full constructor */
    public OrderStatsSummary(String day, Integer orderCount, Integer opOrderCount, Integer bxOrders, Integer currentOrders, Integer deletedOrders, Integer lineCount, Double dollarTotal, Integer opLineCount, Double opDollarTotal, Integer bxLines, Integer bxDollars, Integer currentOrdersTlines, Double currentOrdersTdollars, Integer deletedOrdersTlines, Double deletedOrdersTdollars, Integer cscImprintOrderCount, Integer cscImprintLineCount, Double cscImprintDollarTotal) {
        this.day = day;
        this.orderCount = orderCount;
        this.opOrderCount = opOrderCount;
        this.bxOrders = bxOrders;
        this.currentOrders = currentOrders;
        this.deletedOrders = deletedOrders;
        this.lineCount = lineCount;
        this.dollarTotal = dollarTotal;
        this.opLineCount = opLineCount;
        this.opDollarTotal = opDollarTotal;
        this.bxLines = bxLines;
        this.bxDollars = bxDollars;
        this.currentOrdersTlines = currentOrdersTlines;
        this.currentOrdersTdollars = currentOrdersTdollars;
        this.deletedOrdersTlines = deletedOrdersTlines;
        this.deletedOrdersTdollars = deletedOrdersTdollars;
        this.cscImprintOrderCount = cscImprintOrderCount;
        this.cscImprintLineCount = cscImprintLineCount;
        this.cscImprintDollarTotal = cscImprintDollarTotal;
    }

    /** default constructor */
    public OrderStatsSummary() {
    }

    /** minimal constructor */
    public OrderStatsSummary(String day) {
        this.day = day;
    }

    public String getDay() {
        return this.day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public Integer getOrderCount() {
        return this.orderCount;
    }

    public void setOrderCount(Integer orderCount) {
        this.orderCount = orderCount;
    }

    public Integer getOpOrderCount() {
        return this.opOrderCount;
    }

    public void setOpOrderCount(Integer opOrderCount) {
        this.opOrderCount = opOrderCount;
    }

    public Integer getBxOrders() {
        return this.bxOrders;
    }

    public void setBxOrders(Integer bxOrders) {
        this.bxOrders = bxOrders;
    }

    public Integer getCurrentOrders() {
        return this.currentOrders;
    }

    public void setCurrentOrders(Integer currentOrders) {
        this.currentOrders = currentOrders;
    }

    public Integer getDeletedOrders() {
        return this.deletedOrders;
    }

    public void setDeletedOrders(Integer deletedOrders) {
        this.deletedOrders = deletedOrders;
    }

    public Integer getLineCount() {
        return this.lineCount;
    }

    public void setLineCount(Integer lineCount) {
        this.lineCount = lineCount;
    }

    public Double getDollarTotal() {
        return this.dollarTotal;
    }

    public void setDollarTotal(Double dollarTotal) {
        this.dollarTotal = dollarTotal;
    }

    public Integer getOpLineCount() {
        return this.opLineCount;
    }

    public void setOpLineCount(Integer opLineCount) {
        this.opLineCount = opLineCount;
    }

    public Double getOpDollarTotal() {
        return this.opDollarTotal;
    }

    public void setOpDollarTotal(Double opDollarTotal) {
        this.opDollarTotal = opDollarTotal;
    }

    public Integer getBxLines() {
        return this.bxLines;
    }

    public void setBxLines(Integer bxLines) {
        this.bxLines = bxLines;
    }

    public Integer getBxDollars() {
        return this.bxDollars;
    }

    public void setBxDollars(Integer bxDollars) {
        this.bxDollars = bxDollars;
    }

    public Integer getCurrentOrdersTlines() {
        return this.currentOrdersTlines;
    }

    public void setCurrentOrdersTlines(Integer currentOrdersTlines) {
        this.currentOrdersTlines = currentOrdersTlines;
    }

    public Double getCurrentOrdersTdollars() {
        return this.currentOrdersTdollars;
    }

    public void setCurrentOrdersTdollars(Double currentOrdersTdollars) {
        this.currentOrdersTdollars = currentOrdersTdollars;
    }

    public Integer getDeletedOrdersTlines() {
        return this.deletedOrdersTlines;
    }

    public void setDeletedOrdersTlines(Integer deletedOrdersTlines) {
        this.deletedOrdersTlines = deletedOrdersTlines;
    }

    public Double getDeletedOrdersTdollars() {
        return this.deletedOrdersTdollars;
    }

    public void setDeletedOrdersTdollars(Double deletedOrdersTdollars) {
        this.deletedOrdersTdollars = deletedOrdersTdollars;
    }

    public Integer getCscImprintOrderCount() {
        return this.cscImprintOrderCount;
    }

    public void setCscImprintOrderCount(Integer cscImprintOrderCount) {
        this.cscImprintOrderCount = cscImprintOrderCount;
    }

    public Integer getCscImprintLineCount() {
        return this.cscImprintLineCount;
    }

    public void setCscImprintLineCount(Integer cscImprintLineCount) {
        this.cscImprintLineCount = cscImprintLineCount;
    }

    public Double getCscImprintDollarTotal() {
        return this.cscImprintDollarTotal;
    }

    public void setCscImprintDollarTotal(Double cscImprintDollarTotal) {
        this.cscImprintDollarTotal = cscImprintDollarTotal;
    }

    public String toString() {
        return new ToStringBuilder(this)
            .append("day", getDay())
            .toString();
    }

    public boolean equals(Object other) {
        if ( !(other instanceof OrderStatsSummary) ) return false;
        OrderStatsSummary castOther = (OrderStatsSummary) other;
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
