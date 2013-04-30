package com.bos.art.logParser.broadcast.beans;

import java.io.*;

public class HistoryBean extends TransferBean implements java.io.Serializable
{
    private String chartName;
    private java.util.Date date;
    private String direction;
    private int dataPoints;
    private String dataPrecision;
    private int classificationId;

    public HistoryBean() 
    {
    }

    public void setClassificationId(int val){
        classificationId = val;
    }

    public void setChartName( String name )
    {
        chartName = name;
    }


    public void setDate (java.util.Date d){
        date = d;
    }


    public void setDirection(String dir){
        direction = dir;
    }


    public void setDataPoints(int points){
        dataPoints = points;
    }


    public void setDataPrecision(String width){
        dataPrecision = width;
    }

    public int getClassificationId(){
        return classificationId;
    }

    public String getChartName(){
        return chartName;
    }

    public java.util.Date getDate(){
        return date;
    }

    public String getDirection(){
        return direction;
    }

    public int getDataPoints(){
        return dataPoints;
    }

    public String getDataPrecision(){
        return dataPrecision;
    }


    public void processBean(org.jgroups.Message msg)
    {
        getClient().process(msg,this);
    }
}
