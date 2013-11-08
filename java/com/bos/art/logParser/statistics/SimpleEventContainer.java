/*
 * Created on Oct 22, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package com.bos.art.logParser.statistics;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Comparator;
import java.util.GregorianCalendar;
import java.util.TreeSet;
import java.text.SimpleDateFormat;


/**
 * @author I0360D3
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class SimpleEventContainer implements Serializable, IEventContainer {

    Calendar  day = null;
    Calendar  dataCloseTime = null;
    Calendar  dataModTime = null;
    int classificationID =0;
    String userName = "";
    int count = 0;
    int timesPersisted = 0;
    boolean isNew = false;
    boolean isDirty;

    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");	
    SimpleDateFormat sdf2 = new SimpleDateFormat("yyyyMMdd hh:mm:ss");	
	public SimpleEventContainer(java.util.Date d, int classify,String uName){

        userName = uName;
        classificationID = classify;

        day = java.util.GregorianCalendar.getInstance();
        try{
            day.setTime(sdf2.parse(sdf.format(d)+" 00:00:00"));
        }catch(Exception e){
            e.printStackTrace();
            //logger.error("Error parsing and formatting a date object...",e);
        }

        dataCloseTime = java.util.GregorianCalendar.getInstance();
        dataCloseTime.setTime(d);
        dataCloseTime.add(Calendar.HOUR,25);

        dataModTime = java.util.GregorianCalendar.getInstance();
        dataModTime.setTime(d);
        dataModTime.add(Calendar.HOUR,25);
	}


	public SimpleEventContainer(java.util.Date d, int classify,String uName, int ct){
        this(d,classify,uName);
        count = ct;
	}


    public int getClassificationID(){
        return classificationID;
    }
    public String getUserName(){
        return userName;
    }
    public boolean isNew(){
        return isNew;
    }
    public void setIsNew(boolean b ){
        isNew = b;
    }
	
	/*synchronized */public void tally(int ct){
        count+=ct;
        isDirty = true;
	}

    public String getShortDesc(){
        StringBuffer sb = new StringBuffer();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd"); 
        sb.append("userName: ").append(userName)
          .append("  day: ").append(sdf.format(day.getTime()))
          .append("  classification: ").append(classificationID);
        return sb.toString();
    }

    /**
	 * @return
	 */
	public Calendar getTime(){
        return day;
    }


	/**
	 * @return
	 */
	public boolean isDatabaseDirty(){
        return isDirty;
    }

	/**
	 * @param b
	 */
	public void setDatabaseDirty(boolean b){
        isDirty = b;
    }
	/**
	 * @return
	 */
	public Calendar getCloseTimeForData(){
        return dataCloseTime;
    }
	/**
	 * @return
	 */
	public Calendar getCloseTimeForMod(){
	    return dataModTime; 
    }

	public int getTimesPersisted(){
        return timesPersisted;
    }

    public void setTimesPersisted(int i ){
        timesPersisted = i;
    }

    
	/**
	 * @return
	 */
	public int getTotalLoads() {
		return count;
	}

	public String toString(){
		StringBuffer sb = new StringBuffer();
        
		return sb.toString();

	}

}
