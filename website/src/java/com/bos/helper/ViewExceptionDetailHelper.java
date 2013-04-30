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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import net.sf.hibernate.Databinder;
import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Query;
import net.sf.hibernate.Session;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
/**
 * @author I0360D4
 * 
 * To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Generation - Code and Comments
 */
public class ViewExceptionDetailHelper {
    private Document MainScreenDoc = null;
    DocumentBuilderFactory factory = null;
    DocumentBuilder builder = null;
    Document doc = null;
    String selectedDate = null;
    String exceptionLine = "";
    String firstRowOfTrace = "";
    Date selectedDateAsDate = null;
    SimpleDateFormat sdf = new SimpleDateFormat ("yyyyMMdd");
    SimpleDateFormat sdfPretty = new SimpleDateFormat("MMM dd, yyyy");
    
    /*
        public ViewHistoricalChartsHelper() {
    		initialize();
    	}
    */
    public ViewExceptionDetailHelper(String selectedDate, String el, String eid) {
        setSelectedDate(selectedDate);
        try {
            selectedDateAsDate = sdf.parse(selectedDate);
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            selectedDateAsDate = new Date();
        }
        exceptionLine = el;
        firstRowOfTrace = eid;
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
        Element exceptionElement = appendExceptionsElement(MainScreenDoc, payload);
        pageElement.appendChild(headerElement);
        pageElement.appendChild(leftPanelElement);
        pageElement.appendChild(bodyElement);
        pageElement.appendChild(payload);
        MainScreenDoc.appendChild(pageElement);
    }
    private static final String SELECT_STACK_TRACE_DETAIL =
        " select u.userName, s.sessionTXT, s.sessionStartTime, s.sessionEndTime, c.contextName, "
            + " s.IPAddress, st.Trace_Time, st.Trace_id, u.companyName, u.fullName"
            + " from Users u, Sessions s, StackTraces st, StackTraceDetails std, StackTraceRows str, Contexts c "
            + " where "
            + " u.User_ID=s.User_ID and  "
            + " s.sessionTXT=st.Trace_Key and "
            + " s.Context_ID=c.Context_ID and "
            + " st.Trace_Message = ? and "
            + " std.Trace_id=st.Trace_id and "
            + " std.Stack_Depth = 1 and  "
            + " std.Row_id = str.Row_id and "
            + " str.Row_Message like ? and "
            + " st.Trace_Time>? and "
            + " st.Trace_Time<?  ";
    private Element appendExceptionsElement(Document doc, Element element) {
        Session session = null;
        Element exceptions = null;
        Element calendarElement = null;
        try {
            calendarElement = doc.createElement("Calendar");
            calendarElement.appendChild(createCalendarElement(doc));
            element.appendChild(calendarElement);
            exceptions = doc.createElement("ExceptionStats");
            
            Element traceMessageElement = doc.createElement("TraceMessage");
            traceMessageElement.appendChild(doc.createTextNode(exceptionLine));
            Element traceMessageTopLineElement = doc.createElement("TraceMessageTopLine");
			traceMessageTopLineElement.appendChild(doc.createTextNode(firstRowOfTrace));
            Element dateStringElement          = doc.createElement("DateString");
            String sDateStringPretty = sdfPretty.format(selectedDateAsDate);
            dateStringElement.appendChild(doc.createTextNode(sDateStringPretty));
            exceptions.appendChild(traceMessageElement);
            exceptions.appendChild(traceMessageTopLineElement);
            exceptions.appendChild(dateStringElement);
            
            
            session = HibernateUtil.currentSession();
            Connection con = session.connection();
            PreparedStatement pstmt = con.prepareStatement(SELECT_STACK_TRACE_DETAIL);
            SimpleDateFormat sdf2 = new SimpleDateFormat("yyyyMMddHHmmss");
            Date startd = null,endd = null;
            try {
                startd = sdf2.parse( selectedDate+"000000" );
                endd = sdf2.parse( selectedDate+"235959" );
                //logger.warn("start date1 parsed : " + sdf2.format( startd ) );
            }
            catch(java.text.ParseException pe)
            {
                startd = new Date();
                endd = new Date();
            }
            
            pstmt.setString(1, exceptionLine);
            pstmt.setString(2, "%"+firstRowOfTrace);
            //pstmt.setString(3, selectedDate + "000000");
            //pstmt.setString(4, selectedDate + "235959");
            pstmt.setTimestamp(3,new java.sql.Timestamp(startd.getTime()));
            pstmt.setTimestamp(4,new java.sql.Timestamp(endd.getTime()));
            //
            ResultSet rs = pstmt.executeQuery();
            System.out.println(" Query:" + SELECT_STACK_TRACE_DETAIL);
            System.out.println(
                ":" + selectedDate + "000000:" + selectedDate + "235959:" + "java.lang.NullPointerException:");
            while (rs.next()) {
                //				u.userName,       s.sessionTXT,    s.sessionStartTime, 
                //              s.sessionEndTime, c.contextName,   s.IPAddress,      
                //              st.Trace_Time,   st.Trace_id 
                Element exceptionRecord = doc.createElement("ExceptionRecord");
                String userName = rs.getString(1);
                String sessionTXT = rs.getString(2);
                String sessionStartTime = rs.getString(3);
                String sessionEndTime = rs.getString(4);
                String context = rs.getString(5);
                String ipAddress = rs.getString(6);
                String traceTime = rs.getString(7);
                String traceID = rs.getString(8);
                String company = rs.getString(9);
                String fullName = rs.getString(10);
                
                if (company == null || fullName == null) {
                    String[] sa = new String[2];
                    CompanyHelper.getCompanyAndUser(userName, sa);
                    company = sa[1];
                    fullName = sa[0];
                } else if (company.equalsIgnoreCase("null")) {
                    System.out.println("Company is NULL");
                }
                
                
				Element companyElement = doc.createElement("company");
				Element fullNameElement = doc.createElement("fullName");
				companyElement.appendChild(doc.createTextNode(company));
				fullNameElement.appendChild(doc.createTextNode(fullName));

                
                Element userNameElement = doc.createElement("UserName");
                userNameElement.appendChild(doc.createTextNode(userName));
                Element sessionTXTElement = doc.createElement("SessionTXT");
                sessionTXTElement.appendChild(doc.createTextNode(sessionTXT));
                Element sessionStartTimeElement = doc.createElement("SessionStartTime");
                sessionStartTimeElement.appendChild(doc.createTextNode(sessionStartTime));
                Element sessionEndTimeElement = doc.createElement("SessionEndTime");
                sessionEndTimeElement.appendChild(doc.createTextNode(sessionEndTime));
                Element contextElement = doc.createElement("Context");
                contextElement.appendChild(doc.createTextNode(context));
                Element ipAddressElement = doc.createElement("IPAddress");
                ipAddressElement.appendChild(doc.createTextNode(ipAddress));
                Element traceTimeElement = doc.createElement("TraceTime");
                traceTimeElement.appendChild(doc.createTextNode(traceTime));
                Element traceIDElement = doc.createElement("TraceID");
                
                traceIDElement.appendChild(doc.createTextNode(traceID));
                
				exceptionRecord.appendChild(companyElement);
				exceptionRecord.appendChild(fullNameElement);
                exceptionRecord.appendChild(userNameElement);
                exceptionRecord.appendChild(sessionTXTElement);
                exceptionRecord.appendChild(sessionStartTimeElement);
                exceptionRecord.appendChild(sessionEndTimeElement);
                exceptionRecord.appendChild(contextElement);
                exceptionRecord.appendChild(ipAddressElement);
                exceptionRecord.appendChild(traceTimeElement);
                exceptionRecord.appendChild(traceIDElement);
                exceptions.appendChild(exceptionRecord);
            	
            }
            element.appendChild(exceptions);
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            try {
                HibernateUtil.closeSession();
            } catch (HibernateException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return exceptions;
    }
    public Element createCalendarElement(Document doc) {
        Calendar c = Calendar.getInstance();
        if (this.getSelectedDate() != null) {
            //get calendar for selected DATE
            String selDate = this.getSelectedDate();
            String yearS = selDate.substring(0, 4);
            String monthS = selDate.substring(4, 6);
            String dateS = selDate.substring(6, 8);
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
    private static final String SESSION_STATS_QUERY =
        "  Select avg(sessions.sessionHits), count(sessions.sessionHits), "
            + "    sum(sessions.sessionEndTime - sessions.sessionStartTime)/count(sessions.sessionId), "
            + "    sum(sessions.sessionEndTime - sessions.sessionStartTime)/count(sessions.sessionId)/avg(sessions.sessionHits) "
            + "    from SessionBean as sessions "
            + "    where sessions.sessionStartTime > :sessionStart and sessions.sessionEndTime< :sessionEnd  and sessions.sessionHits > 1 "
            + "        and sessions.sessionId> :minSessionID "
            + "        and sessions.sessionId< :maxSessionID "        //	"        and sessions.contextId = context.contextId " +
    ;
    /*
    private static final String SESSION_STATS_QUERY = 
    " Select context, avg(sessions.sessionHits), count(sessions.sessionHits), "+
    "    sum(sessions.sessionEndTime - sessions.sessionStartTime)/count(sessions.Session_ID), "+
    "    sum(sessions.sessionEndTime - sessions.sessionStartTime)/count(sessions.Session_ID)/avg(sessions.sessionHits) "+
    "    from SessionBean as sessions inner join  sessions.contextId as context "+
    "    where sessions.sessionStartTime > :sessionStart and sessions.sessionEndTime< :sessionEnd  and sessions.sessionHits > 1 "+
    "        and sessions.sessionId> :minSessionID "+
    "        and sessions.sessionId< :maxSessionID " +
    "     group by context "
    ;
    */
    private Element appendDailySessionStatistics(Document doc, Element element) {
        System.out.println("starting appendDailySessionStatistics....");
        Session session = null;
        Element dailySummaryElement = null;
        Element calendarElement = null;
        try {
            dailySummaryElement = doc.createElement("DailySummary");
            calendarElement = doc.createElement("Calendar");
            calendarElement.appendChild(createCalendarElement(doc));
            element.appendChild(calendarElement);
            //Transaction tx= session.beginTransaction();
            session = HibernateUtil.currentSession();
            Query query = session.createQuery(SESSION_STATS_QUERY);
            query.setParameter("sessionStart", "20040209000000");
            query.setParameter("sessionEnd", "20040210000000");
            query.setParameter("minSessionID", new Integer(3346638));
            query.setParameter("maxSessionID", new Integer(3415253));
            System.out.println("starting appendDailySessionStatistics....");
            //				session.find(
            //				"from DailySummaryBean as DailySummary where DailySummary.day = ?", selectedDate, Hibernate.STRING);
            List sessionSummaryList = query.list();
            System.out.println("starting appendDailySessionStatistics....");
            Databinder db = HibernateUtil.getDataBinder();
            db.setInitializeLazy(true);
            Iterator iter = sessionSummaryList.iterator();
            for (; iter.hasNext();) {
                Object o = iter.next();
                System.out.println("Type : " + o.getClass().getName());
                System.out.println("Type : " + o.toString());
                //DailySummaryBean dailySummary = (DailySummaryBean) iter.next();
                //db.bind(dailySummary);
                //Node element1 =
                //	doc.importNode(db.toDOM().getFirstChild(), true);
                ///dailySummaryElement.appendChild(element1);
                //				element.appendChild(dailySummaryElement);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            try {
                HibernateUtil.closeSession();
            } catch (HibernateException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return dailySummaryElement;
    }
}
