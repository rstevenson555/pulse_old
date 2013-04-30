package com.bos.art.logParser.broadcast.beans;

import java.io.*;

public class BrowserBean extends TransferBean implements Serializable {
    String browserString;
    int count;
    int totalCount;
    boolean isOs;
    String desc;

    public BrowserBean()
    {
        browserString ="";

    }


    public void setCount(int c){count = c;}
    public void setBrowserString(String  s){browserString = s;}
    public void setTotalCount(int c){totalCount = c;}
    public void setIsOs(boolean b){ isOs = b; }
    public void setDesc(String s){desc =s ;}

    
    public int  getCount(){return count;}
    public String getBrowserString(){return browserString;}
    public String getDesc(){return desc;}
    public boolean isOs(){return isOs;}
/** @depracated */
    public int  getTotalCount(int c){return getTotalCount();}

    public int  getTotalCount(){return totalCount;}


   


public void processBean(org.jgroups.Message msg)
    {
        getClient().process(msg,this);
    }



}
