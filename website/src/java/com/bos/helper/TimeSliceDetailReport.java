package com.bos.helper;

import com.bos.arch.HibernateUtil;
import com.bos.model.CalendarBean;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Session;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * @author I0360D4
 *
 * To change the template for this generated type comment go to Window - Preferences - Java - Code Generation - Code and
 * Comments
 */
public class TimeSliceDetailReport {

    private Document MainScreenDoc = null;
    DocumentBuilderFactory factory = null;
    DocumentBuilder builder = null;
    Document doc = null;
    String parmStartTime = "20040503120000";
    String parmEndTime = "20040503124500";
    String parmPageName = "";
    String parmContextName = "";
    String selectedDate = null;

    /*
     * public ViewHistoricalChartsHelper() { initialize(); }
     */
    public TimeSliceDetailReport(String selectedDate,String startTime, String endTime, String contextQuery, String pageQuery) {
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

        appendTimeSliceStatistics(MainScreenDoc, payload);
        appendHighLineStatistics(MainScreenDoc, payload);

        pageElement.appendChild(headerElement);
        pageElement.appendChild(leftPanelElement);
        pageElement.appendChild(bodyElement);
        pageElement.appendChild(payload);

        MainScreenDoc.appendChild(pageElement);
    }
    
    private static final String TIME_SLICE_DETAIL_QUERY =
            " select EXTRACT(hour from ar.Time) as Hour,cont.contextName,  page.pageName,  avg(ar.loadTime), count(ar.loadTime), "
            + " max(ar.loadTime), count(distinct (ar.user_id)),count (distinct(ar.session_id))  "
            + " from AccessRecords ar, Contexts cont, Pages page where ar.Time> ? and ar.Time< ? "
            + " and cont.Context_ID=ar.Context_ID and page.Page_ID=ar.Page_ID "
            + " and page.pageName like '%' || ? || '%' and cont.contextName like '%' || ? || '%' "
            + " group by Hour, cont.contextName, page.pageName order by Hour desc,count(ar.loadTime) desc";

    private static final String HIGH_LINE_DETAIL_QUERY =
            " select cont.contextName,  page.pageName,  avg(ar.loadTime), count(ar.loadTime), "
            + " max(ar.loadTime), count(distinct (ar.user_id)),count (distinct(ar.session_id))  "
            + " from AccessRecords ar, Contexts cont, Pages page where ar.Time> ? and ar.Time< ? "
            + " and cont.Context_ID=ar.Context_ID and page.Page_ID=ar.Page_ID "
            + " and page.pageName like '%' || ? || '%' and cont.contextName like '%' || ? || '%' "
            + " group by cont.contextName, page.pageName ";
    
    private static final String ORDER_DOLLARS_BY_TIME = "select sum(doublevalue) from accumulatorevent where time>'2012-04-13 00:00:00' and time <'2012-04-13 14:40:00' and " +
            "accumulatorstat_id = 2008";

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

            Connection con = session.connection();
            PreparedStatement pstmt = con.prepareStatement(TIME_SLICE_DETAIL_QUERY);

            Element timeSliceParams = doc.createElement("TimeSliceParams");
            Element startTimeElement = doc.createElement("StartTime");
            Element endTimeElement = doc.createElement("EndTime");
            Element contextSearchElement = doc.createElement("ContextSearch");
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

            Date startd = new Date();
            Date endd = new Date();
            
            if ( parmStartTime!=null) {
                String startdstr = selectedDate + " " + parmStartTime;
                String enddstr = selectedDate + " " + parmEndTime;

                System.out.println("startdatestr: " + startdstr);
                System.out.println("endatestr: " + enddstr);

                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");

                startd = format.parse(startdstr);
                endd = format.parse(enddstr);
            } else {
                String startdstr = selectedDate;
                String enddstr = selectedDate;

                System.out.println("startdatestr: " + startdstr);
                System.out.println("endatestr: " + enddstr);

                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");

                startd = format.parse(startdstr);
                endd = format.parse(enddstr);
            }
            
            System.out.println("Start Date: " + startd);
            System.out.println("End Date: " + endd);
            
            pstmt.setTimestamp(1, new Timestamp(startd.getTime()));
            pstmt.setTimestamp(2, new Timestamp(endd.getTime()));
            
            pstmt.setString(3, parmPageName);
            pstmt.setString(4, parmContextName);
            System.out.println("Created the prepared statement...");
            System.out.println("Query:" + TIME_SLICE_DETAIL_QUERY);
            System.out.println("" + startd + ":" + endd + ":%" + parmPageName + "%:%" + parmContextName + "%");
            ResultSet rs = pstmt.executeQuery();

            System.out.println("After Execute Query");

            while (rs.next()) {
                Element sessionRecordElement = doc.createElement("pageRecord");

                String hour = rs.getString(1);
                String contextName = rs.getString(2);
                String pageName = rs.getString(3);
                double avgLoadTime = rs.getDouble(4);
                int cntLoads = rs.getInt(5);
                double maxLoadTime = rs.getDouble(6);
                int userTot = rs.getInt(7);
                int sessTot = rs.getInt(8);

                Element hourElement = doc.createElement("Hour");
                hourElement.appendChild(doc.createTextNode(hour));
                sessionRecordElement.appendChild(hourElement);

                Element contextElement = doc.createElement("ContextName");
                contextElement.appendChild(doc.createTextNode(contextName));
                sessionRecordElement.appendChild(contextElement);

                Element pageElement = doc.createElement("PageName");
                pageElement.appendChild(
                        doc.createTextNode(pageName));
                sessionRecordElement.appendChild(pageElement);

                Element hitsElement = doc.createElement("Hits");
                hitsElement.appendChild(doc.createTextNode("" + cntLoads));
                sessionRecordElement.appendChild(hitsElement);

                Element avgElement = doc.createElement("AvgLoadTime");
                double avg = (avgLoadTime / 1000.0f);
                avgElement.appendChild(doc.createTextNode(String.valueOf(avg)));
                sessionRecordElement.appendChild(avgElement);

                Element maxElement = doc.createElement("MaxLoadTime");
                double max = (maxLoadTime / 1000.0f);
                maxElement.appendChild(doc.createTextNode( String.valueOf(max)));
                sessionRecordElement.appendChild(maxElement);
                /**
                 * new elements
                 */
                Element userElement = doc.createElement("Users");
                userElement.appendChild(doc.createTextNode( String.valueOf(userTot)));
                sessionRecordElement.appendChild(userElement);
                
                Element sessionElement = doc.createElement("Sessions");
                sessionElement.appendChild(doc.createTextNode( String.valueOf(sessTot)));
                sessionRecordElement.appendChild(sessionElement);                                

                sessionSummaryElement.appendChild(sessionRecordElement);
            }
            rs.close();
            pstmt.close();
            con.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            try {
                HibernateUtil.closeSession();
            } catch (HibernateException e) {
                e.printStackTrace();
            }
        }
        element.appendChild(sessionSummaryElement);

        return sessionSummaryElement;
    }

    private Element appendHighLineStatistics(Document doc, Element element) {
        Session session = null;
        Element sessionSummaryElement = null;
        Element calendarElement = null;

        try {
            sessionSummaryElement = doc.createElement("HighLineStats");
            calendarElement = doc.createElement("Calendar");

            calendarElement.appendChild(createCalendarElement(doc));
            element.appendChild(calendarElement);
            //Transaction tx= session.beginTransaction();
            session = HibernateUtil.currentSession();

            Connection con = session.connection();
            PreparedStatement pstmt = con.prepareStatement(TIME_SLICE_DETAIL_QUERY);

            Element timeSliceParams = doc.createElement("TimeSliceParams");
            Element startTimeElement = doc.createElement("StartTime");
            Element endTimeElement = doc.createElement("EndTime");
            Element contextSearchElement = doc.createElement("ContextSearch");
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

            Date startd = new Date();
            Date endd = new Date();
            
            if ( parmStartTime!=null) {
                String startdstr = selectedDate + " " + parmStartTime;
                String enddstr = selectedDate + " " + parmEndTime;

                System.out.println("startdatestr: " + startdstr);
                System.out.println("endatestr: " + enddstr);

                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");

                startd = format.parse(startdstr);
                endd = format.parse(enddstr);
            } else {
                String startdstr = selectedDate;
                String enddstr = selectedDate;

                System.out.println("startdatestr: " + startdstr);
                System.out.println("endatestr: " + enddstr);

                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");

                startd = format.parse(startdstr);
                endd = format.parse(enddstr);
            }
            
            System.out.println("Start Date: " + startd);
            System.out.println("End Date: " + endd);
            
            pstmt.setTimestamp(1, new Timestamp(startd.getTime()));
            pstmt.setTimestamp(2, new Timestamp(endd.getTime()));
            
            pstmt.setString(3, parmPageName);
            pstmt.setString(4, parmContextName);
            System.out.println("Created the prepared statement...");
            System.out.println("Query:" + TIME_SLICE_DETAIL_QUERY);
            System.out.println("" + startd + ":" + endd + ":%" + parmPageName + "%:%" + parmContextName + "%");
            ResultSet rs = pstmt.executeQuery();

            System.out.println("After Execute Query");

            while (rs.next()) {
                Element sessionRecordElement = doc.createElement("pageRecord");

                String contextName = rs.getString(1);
                String pageName = rs.getString(2);
                double avgLoadTime = rs.getDouble(3);
                int cntLoads = rs.getInt(4);
                double maxLoadTime = rs.getDouble(5);
                int userTot = rs.getInt(6);
                int sessTot = rs.getInt(7);

                Element contextElement = doc.createElement("ContextName");
                contextElement.appendChild(doc.createTextNode(contextName));
                sessionRecordElement.appendChild(contextElement);

                Element pageElement = doc.createElement("PageName");
                pageElement.appendChild(
                        doc.createTextNode(pageName));
                sessionRecordElement.appendChild(pageElement);

                Element hitsElement = doc.createElement("Hits");
                hitsElement.appendChild(doc.createTextNode("" + cntLoads));
                sessionRecordElement.appendChild(hitsElement);

                Element avgElement = doc.createElement("AvgLoadTime");
                double avg = (avgLoadTime / 1000.0f);
                avgElement.appendChild(doc.createTextNode(String.valueOf(avg)));
                sessionRecordElement.appendChild(avgElement);

                Element maxElement = doc.createElement("MaxLoadTime");
                double max = (maxLoadTime / 1000.0f);
                maxElement.appendChild(doc.createTextNode( String.valueOf(max)));
                sessionRecordElement.appendChild(maxElement);
                /**
                 * new elements
                 */
                Element userElement = doc.createElement("Users");
                userElement.appendChild(doc.createTextNode( String.valueOf(userTot)));
                sessionRecordElement.appendChild(userElement);
                
                Element sessionElement = doc.createElement("Sessions");
                sessionElement.appendChild(doc.createTextNode( String.valueOf(sessTot)));
                sessionRecordElement.appendChild(sessionElement);                                

                sessionSummaryElement.appendChild(sessionRecordElement);
            }
            rs.close();
            pstmt.close();
            con.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
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
            String yearS = selDate.substring(0, 4);
            String monthS = selDate.substring(5, 7);
            String dateS = selDate.substring(8, 10);

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
