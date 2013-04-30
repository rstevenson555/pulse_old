/*
 * Created on Dec 12, 2003
 * 
 * To change the template for this generated file go to Window - Preferences -
 * Java - Code Generation - Code and Comments
 */
package com.bos.helper;

import com.bos.arch.HibernateUtil;
import com.bos.art.model.jdo.DailyPageLoadTimBean;
import com.bos.model.CalendarBean;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import net.sf.hibernate.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * @author I0360D4
 * 
 * To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Generation - Code and Comments
 */
public class ViewDailyPageLoadTimesHelper {

	private Document MainScreenDoc = null;
	DocumentBuilderFactory factory = null;
	DocumentBuilder builder = null;
	Document doc = null;
	Date selectedDate = null;

	/*
	    public ViewHistoricalChartsHelper() {
			initialize();
		}
	*/

	public ViewDailyPageLoadTimesHelper(Date selectedDate) {
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
		Element dailyPageSummary =
			appendDailyPageSummaryElement(MainScreenDoc, payload);
		//Element e =	appendDailySessionStatistics(MainScreenDoc,payload);
		pageElement.appendChild(headerElement);
		pageElement.appendChild(leftPanelElement);
		pageElement.appendChild(bodyElement);
		pageElement.appendChild(payload);
		

		MainScreenDoc.appendChild(pageElement);
	}

	private Element appendDailyPageSummaryElement(
		Document doc,
		Element element) {
		Session session = null;
		Element dailyPageSummaryElement = null;
		Element calendarElement = null;
		try {
			calendarElement = doc.createElement("Calendar");

			calendarElement.appendChild(createCalendarElement(doc));
			element.appendChild(calendarElement);
			dailyPageSummaryElement = doc.createElement("DailyPageSummary");
			session = HibernateUtil.currentSession();
			//			List dailyPageSummaryList = session.find("from DailyPageLoadTimBean as DailyPageLoadTimes where DailyPageLoadTimes.day='2004-01-23'");
            //
            System.out.println("ViewDailyPageLoadTimesHelper query date: " + selectedDate);
			List dailyPageSummaryList2 =
				session.find(
					"from DailyPageLoadTimBean as dpltb "
						+ " where dpltb.day=?",
					selectedDate,
					Hibernate.DATE);
			//System.out.println("Location 1 --------------------------------------------");
			Databinder db = HibernateUtil.getDataBinder();
			//System.out.println("Location 2--------------------------------------------");
			db.setInitializeLazy(true);
			//System.out.println("Location 3 --------------------------------------------");
			Iterator iter = dailyPageSummaryList2.iterator();
			//System.out.println("Location 4 --------------------------------------------");
			while (iter.hasNext()) {
				//	System.out.println("Location 5 --------------------------------------------");
				Object oa = (Object) iter.next();
				//	System.out.println("Location 6 --------------------------------------------");
				if (oa instanceof DailyPageLoadTimBean) {
					//		System.out.println("Location 7 --------------------------------------------");
					DailyPageLoadTimBean dpltb = (DailyPageLoadTimBean) oa;
					//		System.out.println("Location 8 --------------------------------------------");
					db.bind(dpltb);
					//		System.out.println("Location 9 --------------------------------------------");
					Node element1 =
						doc.importNode(db.toDOM().getFirstChild(), true);
					//		System.out.println("Location 10 --------------------------------------------");
					dailyPageSummaryElement.appendChild(element1);
					//		System.out.println("Location 11 --------------------------------------------");
				}
			}
			//System.out.println("Location 12 --------------------------------------------");
			element.appendChild(dailyPageSummaryElement);
			//System.out.println("Location 13 --------------------------------------------");
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
		return dailyPageSummaryElement;
	}

	public Element createCalendarElement(Document doc) {
		Calendar c = Calendar.getInstance();
		if (this.getSelectedDate() != null) {
			//get calendar for selected DATE
			/*String selDate = this.getSelectedDate();
			String yearS = selDate.substring(0, 4);
			String monthS = selDate.substring(5, 7);
			String dateS = selDate.substring(8, 10);

			int year = new Integer(yearS).intValue();
			int month = new Integer(monthS).intValue();
			int date = new Integer(dateS).intValue(); */
            Date selDate = getSelectedDate();
            Calendar cal = Calendar.getInstance();
            cal.setTime(selDate);

            int year = cal.get(Calendar.YEAR);
            int month = cal.get(Calendar.MONTH);
            int date = cal.get(Calendar.DAY_OF_MONTH);

			c.set(year, month , date);
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
	public Date getSelectedDate() {
		return selectedDate;
	}

	/**
	 * @param selectedDate The selectedDate to set.
	 */
	public void setSelectedDate(Date selectedDate) {
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
