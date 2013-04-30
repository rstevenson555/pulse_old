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
public class ViewExceptionTypeHelper {

	private Document MainScreenDoc = null;
	DocumentBuilderFactory factory = null;
	DocumentBuilder builder = null;
	Document doc = null;
	String selectedDate = null;
	String exceptionLine = "";

	/*
	    public ViewHistoricalChartsHelper() {
			initialize();
		}
	*/

	public ViewExceptionTypeHelper(String selectedDate, String el) {
		setSelectedDate(selectedDate);
		exceptionLine =el;
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
		Element exceptionElement =
			appendExceptionsElement(MainScreenDoc, payload);
		//Element e =	appendDailySessionStatistics(MainScreenDoc,payload);
		pageElement.appendChild(headerElement);
		pageElement.appendChild(leftPanelElement);
		pageElement.appendChild(bodyElement);
		pageElement.appendChild(payload);
		

		MainScreenDoc.appendChild(pageElement);
		
		System.out.println(com.bcop.arch.builder.XMLUtils.documentToString(MainScreenDoc));
	}


//    private static final String STACK_TRACE_QUERY = 
//            " select exceptionBean.traceMessage, count(exceptionBean.traceId) from StackTrac as exceptionBean " +
//            " where exceptionBean.traceTime> :startTime and exceptionBean.traceTime< :endTime group by exceptionBean.traceMessage";

    private static final String STACK_TRACE_QUERY = 
            " select row.rowMessage, count(exceptionBean.traceId) from StackTrac as exceptionBean, StackTraceDetailPK as traceDetail " +
            " , StackTraceRow as row, StackTraceDetail as std "+
            " where exceptionBean.traceTime> :startTime and exceptionBean.traceTime< :endTime " +
            " and exceptionBean.traceMessage= :exceptionMessage and traceDetail.stackDepth=1"+
            " group by row.rowMessage";
            
            
   private static final String STACK_TRACE_TYPE = 
   " select count(*) as cnt,  r.Row_Message "+
   " from StackTraces s, StackTraceDetails d, StackTraceRows r "+
   " where "+
   " s.Trace_Time>? and s.Trace_Time<? "+
   " and s.Trace_Message=? "+
   " and s.Trace_id=d.Trace_id "+
   " and d.Stack_Depth=1 "+
   " and d.Row_id=r.Row_id "+
   " group by r.Row_Message "+
   " order by cnt desc ";




	private Element appendExceptionsElement(
		Document doc,
		Element element) {
		Session session = null;
		Element exceptions = null;
		Element calendarElement = null;
		try {
			calendarElement = doc.createElement("Calendar");

			calendarElement.appendChild(createCalendarElement(doc));
			element.appendChild(calendarElement);
			exceptions = doc.createElement("ExceptionStats");
			session = HibernateUtil.currentSession();
			Connection con = session.connection();
			PreparedStatement pstmt = con.prepareStatement(STACK_TRACE_TYPE);

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

            pstmt.setTimestamp(1,new java.sql.Timestamp(startd.getTime()));
            pstmt.setTimestamp(2,new java.sql.Timestamp(endd.getTime()));
			//pstmt.setString(1,selectedDate + "000000");
			//pstmt.setString(2,selectedDate + "235959");
			pstmt.setString(3,exceptionLine);
			ResultSet rs = pstmt.executeQuery();
			
			System.out.println(" Query:" +STACK_TRACE_TYPE);
			System.out.println(":"+selectedDate+"000000:"+selectedDate+"235959:"+ "java.lang.NullPointerException:");
			
            while(rs.next()){
                Element exceptionRecord = doc.createElement("ExceptionRecord");
                String topLine = rs.getString(2);
                int count = rs.getInt(1);
                System.out.println("count: " + count + "  : " + topLine);

                Element exceptionMessageElement = doc.createElement("ExceptionMessage");
				Element exceptionLineTopElement = doc.createElement("ExceptionTopLine");
                Element countElement = doc.createElement("Count");
                countElement.appendChild(doc.createTextNode(""+count));
                exceptionMessageElement.appendChild(doc.createTextNode(exceptionLine));
                try{
					exceptionLineTopElement.appendChild(doc.createTextNode(topLine));
                }catch (Exception e){
					exceptionLineTopElement.appendChild(doc.createTextNode("ERROR HAPPEND IN ART look in /apps/art/tomcat/logs/catalina.log"));
                	System.out.println("topLine " + topLine);
                	e.printStackTrace();
                	
                }
                exceptionRecord.appendChild(exceptionMessageElement);
                exceptionRecord.appendChild(exceptionLineTopElement);
                exceptionRecord.appendChild(countElement);
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



	private static final String SESSION_STATS_QUERY = 
	"  Select avg(sessions.sessionHits), count(sessions.sessionHits), "+
	"    sum(sessions.sessionEndTime - sessions.sessionStartTime)/count(sessions.sessionId), "+
	"    sum(sessions.sessionEndTime - sessions.sessionStartTime)/count(sessions.sessionId)/avg(sessions.sessionHits) "+
	"    from SessionBean as sessions "+
	"    where sessions.sessionStartTime > :sessionStart and sessions.sessionEndTime< :sessionEnd  and sessions.sessionHits > 1 "+
	"        and sessions.sessionId> :minSessionID "+
	"        and sessions.sessionId< :maxSessionID " 
//	"        and sessions.contextId = context.contextId " +
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

			Query query =
				session.createQuery(SESSION_STATS_QUERY);
			query.setParameter("sessionStart","20040209000000");
			query.setParameter("sessionEnd"  ,"20040210000000");
			query.setParameter("minSessionID",new Integer(3346638));
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
		}finally{
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
