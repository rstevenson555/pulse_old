package com.bos.helper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Session;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.bos.arch.HibernateUtil;
import com.bos.model.CalendarBean;

/**
 * @author I0360D4
 * 
 * To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Generation - Code and Comments
 */
public class LoadTestDetailReport {

	private Document MainScreenDoc = null;
	DocumentBuilderFactory factory = null;
	DocumentBuilder builder = null;
	Document doc = null;
	String selectedDate = null;
	String parmStartTime="20040503120000";
	String parmEndTime = "20040503124500";
	String parmPageName="";
	String parmContextName="";

/*
    public ViewHistoricalChartsHelper() {
		initialize();
	}
*/
	
	public LoadTestDetailReport(String startTime, String endTime, String pageQuery, String contextQuery) {
		parmStartTime = startTime;
		parmEndTime = endTime;
		parmContextName = contextQuery;
		parmPageName = pageQuery;
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
		
	    Element temp =appendTimeSliceStatistics(MainScreenDoc, payload);
		
		pageElement.appendChild(headerElement);
		pageElement.appendChild(leftPanelElement);
		pageElement.appendChild(bodyElement);
		pageElement.appendChild(payload);

		MainScreenDoc.appendChild(pageElement);
	}

	


	private static final String TIME_SLICE_DETAIL_QUERY = 
               " select cont.contextName,  page.pageName,  avg(ar.loadTime), count(ar.loadTime), "+
               " max(ar.loadTime)  " +
               " from AccessRecords ar, Contexts cont, Pages page where ar.Time> ? and ar.Time< ? " +
               " and cont.Context_ID=ar.Context_ID and page.Page_ID=ar.Page_ID " +
               " and page.pageName like ? and cont.contextName like ? " +
               " group by cont.contextName, page.pageName ";
               
   private static final String LOAD_TEST_PERFORMANCE_GRAPH =
               "select DATE_FORMAT(Time,\"%H:%i\") , count(*), avg(LoadTime), count(distinct(Session_ID)), " +
               "count(distinct(User_ID)) from AccessRecords a, Contexts c "+
               "where " +
               "Time> ?"+
               "and Time<? "+
               "and a.Context_ID=c.Context_ID and c.contextName=? " +
               "group by DATE_FORMAT(Time,\"%H:%i\") "+
               "order by DATE_FORMAT(Time,\"%H:%i\") ";



	private Element appendTimeSliceStatistics(Document doc, Element element) {
		Session session = null;
		Element sessionSummaryElement = null;
		Element calendarElement = null; 
				
		try {
			sessionSummaryElement = doc.createElement("TimeSlice");
			calendarElement = doc.createElement("Calendar");

			calendarElement.appendChild(createCalendarElement(doc));
			element.appendChild(calendarElement);
			//Transaction tx= session.beginTransaction();
			session = HibernateUtil.currentSession();
            String o1[] = {};
            Class o2[] = {};

			Connection con = session.connection();
			PreparedStatement pstmt = con.prepareStatement(TIME_SLICE_DETAIL_QUERY);
			
			Element timeSliceParams   = doc.createElement("TimeSliceParams");
			Element startTimeElement  = doc.createElement("StartTime");
			Element endTimeElement    = doc.createElement("EndTime");
			Element contextSearchElement	  = doc.createElement("ContextSearch");
			Element pageSearchElement = doc.createElement("PageSearch");
			startTimeElement.appendChild(doc.createTextNode(parmStartTime));
			endTimeElement.appendChild(doc.createTextNode(parmEndTime));
			pageSearchElement.appendChild(doc.createTextNode(parmPageName));
			contextSearchElement.appendChild(doc.createTextNode(parmContextName));
			timeSliceParams.appendChild(startTimeElement);
			timeSliceParams.appendChild(endTimeElement);
			timeSliceParams.appendChild(pageSearchElement);
			timeSliceParams.appendChild(contextSearchElement);
			
			sessionSummaryElement.appendChild(timeSliceParams);
			
			pstmt.setString(1,parmStartTime);
			pstmt.setString(2,parmEndTime);
			pstmt.setString(3,"%"+parmPageName +"%");
			pstmt.setString(4,"%"+parmContextName);
			System.out.println("Created the prepared statement...");
			System.out.println("Query:"+TIME_SLICE_DETAIL_QUERY);
			System.out.println("" + parmStartTime+":"+parmEndTime+":%"+parmPageName+"%:%"+parmContextName+"%");
			ResultSet rs = pstmt.executeQuery();
			
			System.out.println("After Execute Query");

			while (rs.next()) {
			    Element sessionRecordElement = doc.createElement("pageRecord");
                
                String contextName = rs.getString(1);
                String pageName = rs.getString(2);
                double avgLoadTime = rs.getDouble(3);
                int    cntLoads    = rs.getInt(4);
                double maxLoadTime = rs.getDouble(5);

			    Element contextElement = doc.createElement("ContextName");
                contextElement.appendChild(doc.createTextNode(contextName));
                sessionRecordElement.appendChild(contextElement);

			    Element pageElement = doc.createElement("PageName");
                pageElement.appendChild(
                     doc.createTextNode(pageName));
                sessionRecordElement.appendChild(pageElement);


			    Element hitsElement = doc.createElement("Hits");
                hitsElement.appendChild(doc.createTextNode(""+cntLoads));
                sessionRecordElement.appendChild(hitsElement);

			    Element avgElement = doc.createElement("AvgLoadTime");
                avgElement.appendChild(doc.createTextNode(""+(avgLoadTime/1000.0)));
                sessionRecordElement.appendChild(avgElement);

			    Element maxElement = doc.createElement("MaxSession");
                maxElement.appendChild(doc.createTextNode(""+(maxLoadTime/1000.0)));
                sessionRecordElement.appendChild(maxElement);

                sessionSummaryElement.appendChild(sessionRecordElement);
			}
			rs.close();
			pstmt.close();
			con.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}finally{
			try {
				HibernateUtil.closeSession();
			} catch (HibernateException e) {
				e.printStackTrace();
			}
		}
        element.appendChild(sessionSummaryElement);

		return sessionSummaryElement;
    }
	
	public Element createCalendarElement(Document doc) {
		Calendar c = Calendar.getInstance();
		if (this.getSelectedDate() != null) {
			//get calendar for selected DATE
			String selDate = this.getSelectedDate();
			String yearS = selDate.substring(0,4);
			String monthS = selDate.substring(5,7);
			String dateS = selDate.substring(8,10);
		
			int year = new Integer(yearS).intValue();
			int month = new Integer(monthS).intValue();
			int date = new Integer(dateS).intValue();
		
			c.set(year, month - 1, date);
		}
	
		CalendarBean calendarBean = new CalendarBean(c); 

		return (Element) doc.importNode(calendarBean.toDOM().getFirstChild(), true);
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
