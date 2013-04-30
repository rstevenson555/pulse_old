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
import java.util.Calendar;
import java.util.Hashtable;
import java.util.Iterator;
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
public class ViewOnlineReportingHelper {
    private Document MainScreenDoc = null;
    DocumentBuilderFactory factory = null;
    DocumentBuilder builder = null;
    Document doc = null;
    String selectedDate = null;
    /*
        public ViewHistoricalChartsHelper() {
    		initialize();
    	}
    */
    public ViewOnlineReportingHelper(String selectedDate) {
        setSelectedDate(selectedDate);
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
        Element exceptionElement = appendMonthlyOnlineReport(MainScreenDoc, payload);
        pageElement.appendChild(headerElement);
        pageElement.appendChild(leftPanelElement);
        pageElement.appendChild(bodyElement);
        pageElement.appendChild(payload);
        MainScreenDoc.appendChild(pageElement);
		System.out.println(com.bcop.arch.builder.XMLUtils.documentToString(MainScreenDoc));
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
        return (Element)doc.importNode(calendarBean.toDOM().getFirstChild(), true);
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

    private static final String ONLINE_REPORT_SUMMARY = 
        " select sum(count) as executions, to_char(time,'yyyy-MM') as month, accumulatorstat_id  from accumulatorstats "+
        " where accumulatorstat_id in (110,111,112)  and time >= ? and time <= ?  "+
        " group by month, accumulatorstat_id order by month, accumulatorstat_id ";

        java.text.SimpleDateFormat sdf1 = new java.text.SimpleDateFormat("yyyy-MM-dd");
        java.text.SimpleDateFormat sdf2 = new java.text.SimpleDateFormat("yyyy");
        java.text.SimpleDateFormat sdf3 = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        java.text.SimpleDateFormat sdf4 = new java.text.SimpleDateFormat("MM");
        java.text.SimpleDateFormat sdf5 = new java.text.SimpleDateFormat("yyyy-MM");
        java.text.SimpleDateFormat sdf6 = new java.text.SimpleDateFormat("yyyy-MMM");

    protected Element appendMonthlyOnlineReport(Document doc, Element element){
	    Element calendarElement = null;
        System.out.println("selectedDAte ... : "+ selectedDate);
        Element monthlyOnlineReportElement = null;
        monthlyOnlineReportElement = doc.createElement("MonthlyOnlineReports");
        java.sql.Timestamp startTimestamp = null;
        java.sql.Timestamp endTimestamp = null;
        if(selectedDate == null){
            selectedDate = sdf1.format(new java.util.Date());
        }
        String twoDigitYear = null;

        try{
            calendarElement = doc.createElement("Calendar");
			calendarElement.appendChild(createCalendarElement(doc));
			element.appendChild(calendarElement);
            java.util.Date date = sdf1.parse(selectedDate);
            String year = sdf2.format(date);
            twoDigitYear = year.substring(2);
            String yearStart = year+"-01-01 00:00:00";
            String yearEnd = year+"-12-31 23:59:59";
            java.util.Date dateStart = sdf3.parse(yearStart);
            java.util.Date dateEnd   = sdf3.parse(yearEnd);
            startTimestamp = new java.sql.Timestamp(dateStart.getTime());
            endTimestamp   = new java.sql.Timestamp(dateEnd.getTime());
            System.out.println("yearStart : "+ yearStart);
            System.out.println("yearEnd : " + yearEnd);
        }catch(Exception e){
            e.printStackTrace();
        }

        try{
        	Session session = HibernateUtil.currentSession();
			Connection con = session.connection();
			PreparedStatement pstmt = con.prepareStatement(ONLINE_REPORT_SUMMARY);
            pstmt.setTimestamp(1,startTimestamp);
            pstmt.setTimestamp(2,endTimestamp);
            ResultSet rs = pstmt.executeQuery();
            java.util.TreeMap months = new java.util.TreeMap();
            while(rs.next()){
                
                int executions   = rs.getInt("executions");
                String yearMonth = rs.getString("month");
                System.out.println("retrieving: " + yearMonth);
                
                int id           = rs.getInt("accumulatorstat_id");
                Object o = months.get(yearMonth);
                if(o == null){
                    o = new Hashtable();
                }
                months.put(yearMonth,o);
                Hashtable ht = (Hashtable)o;
                Integer idInteger = new Integer(id);
                ht.put(idInteger,new Integer(executions));
                    
            }
            Iterator iter = months.keySet().iterator();
            Element yearMonthsElement = doc.createElement("yearMonths");
            while(iter.hasNext()){
                String yearMonth = (String)iter.next();
                Element yearMonthElement = doc.createElement("yearMonth");
                yearMonthElement.appendChild(doc.createTextNode(yearMonth));
                yearMonthsElement.appendChild(yearMonthElement);
            }
            monthlyOnlineReportElement.appendChild(yearMonthsElement);
            Element reportTypesElement = doc.createElement("ReportTypes");


            {
                Element reportTypeElement = doc.createElement("ReportType");
                Element descriptionElement = doc.createElement("Description");
                descriptionElement.appendChild(doc.createTextNode("Customer Report"));
                Element recordsElement = doc.createElement("Records");
                iter = months.keySet().iterator();

                while(iter.hasNext()){
                    
                    String yearMonth = (String)iter.next();
                    Hashtable ht = (Hashtable)months.get(yearMonth);
                    Integer countInteger = (Integer)ht.get(new Integer(110));
                    if(countInteger == null){
                        countInteger = new Integer(0);
                    }
                    Element countElement =doc.createElement("Count");
                    countElement.setAttribute("yearMonth",yearMonth);
                    countElement.appendChild(doc.createTextNode(countInteger.toString()));


                    recordsElement.appendChild(countElement);
                }


                reportTypeElement.appendChild(descriptionElement);
                reportTypeElement.appendChild(recordsElement);
                reportTypesElement.appendChild(reportTypeElement);
            }
            
            {
                Element reportTypeElement = doc.createElement("ReportType");
                Element descriptionElement = doc.createElement("Description");
                descriptionElement.appendChild(doc.createTextNode("ECS Report"));
                Element recordsElement = doc.createElement("Records");

                iter = months.keySet().iterator();
                while(iter.hasNext()){
                    String yearMonth = (String)iter.next();
                    Hashtable ht = (Hashtable)months.get(yearMonth);
                    Integer countInteger = (Integer)ht.get(new Integer(111));
                    if(countInteger == null){
                        countInteger = new Integer(0);
                    }
                    Element countElement =doc.createElement("Count");
                    countElement.setAttribute("yearMonth",yearMonth);
                    countElement.appendChild(doc.createTextNode(countInteger.toString()));


                    recordsElement.appendChild(countElement);
                }


                reportTypeElement.appendChild(descriptionElement);
                reportTypeElement.appendChild(recordsElement);
                reportTypesElement.appendChild(reportTypeElement);
            }

            {
                Element reportTypeElement = doc.createElement("ReportType");
                Element descriptionElement = doc.createElement("Description");
                descriptionElement.appendChild(doc.createTextNode("Sales Report"));
                Element recordsElement = doc.createElement("Records");

                iter = months.keySet().iterator();
                while(iter.hasNext()){
                    String yearMonth = (String)iter.next();
                    Hashtable ht = (Hashtable)months.get(yearMonth);
                    Integer countInteger = (Integer)ht.get(new Integer(112));
                    if(countInteger == null){
                        countInteger = new Integer(0);
                    }
                    Element countElement =doc.createElement("Count");
                    countElement.setAttribute("yearMonth",yearMonth);
                    countElement.appendChild(doc.createTextNode(countInteger.toString()));


                    recordsElement.appendChild(countElement);
                }


                reportTypeElement.appendChild(descriptionElement);
                reportTypeElement.appendChild(recordsElement);
                reportTypesElement.appendChild(reportTypeElement);
            }
            

            monthlyOnlineReportElement.appendChild(reportTypesElement);
        /*
            <yearMonths>
               <yearMonth> </yearMonth>
               ...
            </yearMonths>
            <ReportTypes>
               <ReportType>
                 <Description></Description>
                 <Records>
                    <Count yearMonth=""></Count>
                 </Records>
               </ReportType>
               ...
            </ReportTypes>
            */
             
        } catch (Exception ex) {
			ex.printStackTrace();
		}finally{
			try {
				HibernateUtil.closeSession();
			} catch (HibernateException e) {
				e.printStackTrace();
			}
		}
        element.appendChild(monthlyOnlineReportElement);

		return monthlyOnlineReportElement;
        
    }
}
