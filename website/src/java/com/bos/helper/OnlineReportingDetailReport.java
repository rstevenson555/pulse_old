/*
 * Created on Dec 12, 2003
 * 
 * To change the template for this generated file go to Window - Preferences -
 * Java - Code Generation - Code and Comments
 */
package com.bos.helper;

import com.bos.arch.HibernateUtil;
import com.bos.model.CalendarBean;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Session;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * @author I0360D4
 * 
 * To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Generation - Code and Comments
 */
public class OnlineReportingDetailReport {

	private Document MainScreenDoc = null;
	DocumentBuilderFactory factory = null;
	DocumentBuilder builder = null;
	Document doc = null;
	String selectedDate = null;
    java.sql.Date sqlStartDate;
    java.sql.Date sqlEndDate;
    int classificationID;

	/*
	    public ViewHistoricalChartsHelper() {
			initialize();
		}
	*/

	public OnlineReportingDetailReport(String selectedDate , String startTime, String  endTime, int classify) {
		setSelectedDate(selectedDate);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMM");
        try{
            sqlStartDate = new java.sql.Date(sdf.parse(startTime).getTime());
            sqlEndDate   = new java.sql.Date(sdf.parse(endTime).getTime());
        }catch(java.text.ParseException e){
            e.printStackTrace();
        }
        int classificationID = classify;
		initialize();
	}

	public void initialize() {
		try {
			factory = DocumentBuilderFactory.newInstance();
			builder = factory.newDocumentBuilder();
			doc = builder.newDocument();
		} catch (Exception ex) {
		}
		buildXMLDocument();
	}

	private void buildXMLDocument() {
		MainScreenDoc = doc;
		Element pageElement = MainScreenDoc.createElement("Page");
		Element headerElement = MainScreenDoc.createElement("DashBoard");
		Element leftPanelElement = MainScreenDoc.createElement("LeftPanel");
		Element bodyElement = MainScreenDoc.createElement("Body");
		Element payload = MainScreenDoc.createElement("Payload");

		Element OnlineReportingElement =
			appendOnlineReportingElement(MainScreenDoc, payload);

		pageElement.appendChild(headerElement);
		pageElement.appendChild(leftPanelElement);
		pageElement.appendChild(bodyElement);
		pageElement.appendChild(payload);
		

		MainScreenDoc.appendChild(pageElement);
	}

            private static final String REPORT_SELECT  = " select to_char(day,'YYYY-MON'), username, sum(reports) "+
                    " from daily_online_report_summary where classification_id=? and day>=? and day<=? "+
                    " group by to_char(day,'YYYY-MON'), username "+
                    " order by username, to_char(day,'YYYY-MON') ";
	private Element appendOnlineReportingElement(
		Document doc,
		Element element) {
		Session session = null;
		Element onlineReportingElement = null;
		Element calendarElement = null;
        Element reportRecords = doc.createElement("ReportRecords");
				// TODO Auto-generated catch block

		try {
			calendarElement = doc.createElement("Calendar");

			calendarElement.appendChild(createCalendarElement(doc));
			element.appendChild(calendarElement);

			onlineReportingElement = doc.createElement("OnlineReportingElement");
			session = HibernateUtil.currentSession();
            Connection con = session.connection();
            PreparedStatement pstmt = con.prepareStatement(REPORT_SELECT);
            pstmt.setInt(1,classificationID);
            pstmt.setDate(2, sqlStartDate);
            pstmt.setDate(3, sqlEndDate);
            ResultSet rs = pstmt.executeQuery();
            Hashtable userHash = new Hashtable(); 
            while(rs.next()){
                String dateString = rs.getString(1);
                String userName   = rs.getString(2);
                int    count      = rs.getInt(3);
                Object o = userHash.get(userName);
                if(o == null){
                    o = new Hashtable();
                    userHash.put(userName,o);
                }
                Hashtable monthHash = (Hashtable)o;
                monthHash.put(dateString,new Integer(count));
            }
            ArrayList dates = getListOfDates(sqlStartDate,sqlEndDate);
            Iterator iter = userHash.keySet().iterator();
            while(iter.hasNext()){
                String userName = (String)iter.next();
                Hashtable ht = (Hashtable)userHash.get(userName);
                Iterator iter2 = dates.iterator();
                Element userRecord = doc.createElement("UserRecord");
                while(iter2.hasNext()){
                    String nextYearMonthKey = (String)iter2.next();
                    Integer i = (Integer)ht.get(nextYearMonthKey);
                    if(i == null){
                        i = new Integer(0);
                    }
                    Element monthRecord = doc.createElement("MonthRecord");
                    Element monthValue  = doc.createElement("MonthValue");
                    monthRecord.appendChild(doc.createTextNode(nextYearMonthKey));
                    monthValue.appendChild(doc.createTextNode(i.toString()));
                    Element userMonthRecord = doc.createElement("UserMonthRecord");
                    userMonthRecord.appendChild(monthRecord);
                    userMonthRecord.appendChild(monthValue);

                    userRecord.appendChild(userMonthRecord);

                }
                reportRecords.appendChild(userRecord);
            }
			element.appendChild(reportRecords);
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			try {
				HibernateUtil.closeSession();
			} catch (HibernateException e) {
				e.printStackTrace();
			}
		}
		return reportRecords;
	}

    private ArrayList getListOfDates(java.sql.Date sqlStartDate,java.sql.Date  sqlEndDate){
        ArrayList arrayList =  new ArrayList();
        SimpleDateFormat yearMonthFormat = new SimpleDateFormat("yyyy-MMM");
        Calendar startCalendar = GregorianCalendar.getInstance();
        Calendar endCalendar = GregorianCalendar.getInstance();
        startCalendar.setTime(sqlStartDate);
        endCalendar.setTime(sqlEndDate);

        while(startCalendar.before(endCalendar)){
            String nextDate = yearMonthFormat.format(startCalendar.getTime());
            arrayList.add(nextDate);
            startCalendar.add(Calendar.MONTH,1);
        }
        if(startCalendar.get(Calendar.MONTH)==endCalendar.get(Calendar.MONTH)){
            String nextDate = yearMonthFormat.format(startCalendar.getTime());
            arrayList.add(nextDate);
        }
        return arrayList;
    }


	public Element createCalendarElement(Document doc) {
		Calendar c = Calendar.getInstance();
		if (this.getSelectedDate() != null) {
			//get calendar for selected DATE
			String selDate = this.getSelectedDate();
			String yearS = selDate.substring(0, 4);
			String monthS = selDate.substring(5, 7);
			String dateS = selDate.substring(8, 10);

			int year = new Integer(yearS).intValue();
			int month = new Integer(monthS).intValue();
			int date = new Integer(dateS).intValue();

			c.set(year, month - 1, date);
		}

		CalendarBean calendarBean = new CalendarBean(c);

		return (Element) doc.importNode(
			calendarBean.toDOM().getFirstChild(),
			true);
	}

	public Document getXMLDocument() {
		return this.doc;
	}
	/**
	 * @return Returns the selectedDate.
	 */
	public String getSelectedDate() {
		return selectedDate;
	}

	/**
	 * @param selectedDate The selectedDate to set.
	 */
	public void setSelectedDate(String selectedDate) {
		this.selectedDate = selectedDate;
	}



}
