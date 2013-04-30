/*
 * Created on Dec 12, 2003
 * 
 * To change the template for this generated file go to Window - Preferences -
 * Java - Code Generation - Code and Comments
 */
package com.bos.helper;

import com.bos.arch.HibernateUtil;
import com.bos.model.CalendarBean;
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
public class ViewExceptionsHelper {
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
    public ViewExceptionsHelper(String selectedDate) {
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
        //Element dailySummary =
        //	appendDailySummaryElement(MainScreenDoc, payload);
        //Element dailyPageSummary =
        // 	appendDailyPageSummaryElement(MainScreenDoc, payload);
        Element exceptionElement = appendExceptionsElement(MainScreenDoc, payload);
        //Element e =	appendDailySessionStatistics(MainScreenDoc,payload);
        pageElement.appendChild(headerElement);
        pageElement.appendChild(leftPanelElement);
        pageElement.appendChild(bodyElement);
        pageElement.appendChild(payload);
        MainScreenDoc.appendChild(pageElement);
		System.out.println(com.bcop.arch.builder.XMLUtils.documentToString(MainScreenDoc));
    }
    private static final String STACK_TRACE_QUERY =
        " select exceptionBean.traceMessage, count(exceptionBean.traceId) from StackTrac as exceptionBean "
            + " where exceptionBean.traceTime> :startTime and exceptionBean.traceTime< :endTime group by exceptionBean.traceMessage";
    private Element appendExceptionsElement(Document doc, Element element) {
        Session session = null;
        Element exceptions = null;
        Element calendarElement = null;
        try {
            calendarElement = doc.createElement("Calendar");
            calendarElement.appendChild(createCalendarElement(doc));
            element.appendChild(calendarElement);
            exceptions = doc.createElement("ExceptionStats");
            session = HibernateUtil.currentSession();
            Query query = session.createQuery(STACK_TRACE_QUERY);
            char[] cDate = selectedDate.toCharArray();
            StringBuffer sDate =
                new StringBuffer()
                    .append(cDate[0])
                    .append(cDate[1])
                    .append(cDate[2])
                    .append(cDate[3])
                    .append(cDate[5])
                    .append(cDate[6])
                    .append(cDate[8])
                    .append(cDate[9]);
            SimpleDateFormat sdf2 = new SimpleDateFormat("yyyyMMddHHmmss");
            Date startd = null,endd = null;
            try {
                startd = sdf2.parse( sDate.toString()+"000000" );
                endd = sdf2.parse( sDate.toString()+"235959" );
                //logger.warn("start date1 parsed : " + sdf2.format( startd ) );
            }
            catch(java.text.ParseException pe)
            {
                startd = new Date();
                endd = new Date();
            }
            //query.setParameter("startTime", sDate.toString() + "000000");
            //query.setParameter("endTime", sDate.toString() + "235959");
            query.setTimestamp("startTime", startd);
            query.setTimestamp("endTime", endd);
            System.out.println("startDateBegin:startDateEnd " + sDate + "000000:" + sDate + "235959");
            List exceptionSummaryList = query.list();
            Databinder db = HibernateUtil.getDataBinder();
            db.setInitializeLazy(true);
            Iterator iter = exceptionSummaryList.iterator();
            while (iter.hasNext()) {
                Element exceptionRecord = doc.createElement("ExceptionRecord");
                Object o[] = (Object[])iter.next();
                String exceptionMessage = (String)o[0];
                Integer iCount = (Integer)o[1];
                if (exceptionMessage == null) {
                    exceptionMessage = "ART Error...";
                }
                if (iCount == null) {
                    iCount = new Integer(0);
                }
                Element exceptionMessageElement = doc.createElement("ExceptionMessage");
                Element countElement = doc.createElement("Count");
                countElement.appendChild(doc.createTextNode(iCount.toString()));
                try {
                	exceptionMessage = checkExceptionMessage(exceptionMessage);
                    exceptionMessageElement.appendChild(doc.createTextNode(exceptionMessage));
                } catch (Exception e) {
                    exceptionMessageElement.appendChild(
                        doc.createTextNode("ERROR HAPPEND IN ART look in /apps/art/tomcat/logs/catalina.log"));
                    System.out.println("topLine " + exceptionMessage);
                    e.printStackTrace();
                }
                exceptionRecord.appendChild(exceptionMessageElement);
                exceptionRecord.appendChild(countElement);
                exceptions.appendChild(exceptionRecord);
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
        element.appendChild(exceptions);
        return exceptions;
    }
    private String checkExceptionMessage(String message){
    	char[] chara = message.toCharArray();
    	boolean haserror = false;
    	char c126= (char)126;
    	char[] charb = new char[chara.length];
    	int errorcount = 0;
    	for(int i = 0;i < chara.length; ++i){
    		int ii = (int)chara[i];
    		if (ii<32 || ii>127){
    			haserror=true;
    			charb[errorcount++]= chara[i];
    			chara[i]=c126;
    			
    		}
    	}
    	if(haserror){
    		for(int i =0;i<errorcount;++i){
    			System.out.print(""+charb[i]+":"+((int)charb[i]));
    			
    		}
    		System.out.println("\n"+message+"\n\nErrors:"+errorcount);
    		return chara.toString();
    	}
    	return message;
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
            SimpleDateFormat sdf2 = new SimpleDateFormat("yyyyMMddHHmmss");

            Date startd = null,endd = null;
            try {
                startd = sdf2.parse( "20040209000000" );
                endd = sdf2.parse( "20040210000000" );
                //logger.warn("start date1 parsed : " + sdf2.format( startd ) );
            }
            catch(java.text.ParseException pe)
            {
                startd = new Date();
                endd = new Date();
            }
        
            //query.setParameter("sessionStart", "20040209000000");
            //query.setParameter("sessionEnd", "20040210000000");
            query.setTimestamp("sessionStart", startd);
            query.setTimestamp("sessionEnd", endd);

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
