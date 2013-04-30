/*
 * Created on Dec 12, 2003
 * 
 * To change the template for this generated file go to Window - Preferences -
 * Java - Code Generation - Code and Comments
 */
package com.bos.helper;

import com.bos.arch.HibernateUtil;
import com.bos.art.model.jdo.DailySummaryBean;
import com.bos.art.model.jdo.OrderStatsSummary;
import com.bos.model.CalendarBean;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import net.sf.hibernate.Databinder;
import net.sf.hibernate.Hibernate;
import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Session;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * @author I0360D4
 * 
 * To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Generation - Code and Comments
 */
public class ViewHistoricalChartsHelper {

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
	
	public ViewHistoricalChartsHelper(String selectedDate) {
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
		Element dailySummary =
			appendDailySummaryElement(MainScreenDoc, payload);
		Element financialSummary = appendFinancialSummaryElement(MainScreenDoc, payload);
		pageElement.appendChild(headerElement);
		pageElement.appendChild(leftPanelElement);
		pageElement.appendChild(bodyElement);
		pageElement.appendChild(payload);

		MainScreenDoc.appendChild(pageElement);
	}

	private Element appendDailySummaryElement(Document doc, Element element) {
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

			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			Date d = sdf.parse(selectedDate);

			List dailySummaryList =
				session.find(
				"from DailySummaryBean as DailySummary where DailySummary.day = ?", d, Hibernate.DATE);
			
			Databinder db = HibernateUtil.getDataBinder();
			db.setInitializeLazy(true);
			
			Iterator iter = dailySummaryList.iterator();
			for (; iter.hasNext();) {
				DailySummaryBean dailySummary = (DailySummaryBean) iter.next();

				db.bind(dailySummary);
				Node element1 =
					doc.importNode(db.toDOM().getFirstChild(), true);
				
				dailySummaryElement.appendChild(element1);
								
				element.appendChild(dailySummaryElement);

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
	private Element appendFinancialSummaryElement(Document doc, Element element) {
		Session session = null;
		Element orderStatsSummaryElement = null;
		Element calendarElement = null; 
				
		try {
			orderStatsSummaryElement = doc.createElement("OrderStatsSummaryElement");
//			calendarElement = doc.createElement("Calendar");

//			calendarElement.appendChild(createCalendarElement(doc));
//			element.appendChild(calendarElement);
			//Transaction tx= session.beginTransaction();
			session = HibernateUtil.currentOracleSession();
			System.out.println("selectedDate" + selectedDate);
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			SimpleDateFormat sdf2 = new SimpleDateFormat("yyyyMMdd");
			Date d = sdf.parse(selectedDate);
			String lselectedDate = sdf2.format(d);
//			String lselectedDate = selectedDate.('-','');
			List dailySummaryList =
				session.find(
				"from OrderStatsSummary as OrderStatsSummary where OrderStatsSummary.day = ?", lselectedDate, Hibernate.STRING);
			
			Databinder db = HibernateUtil.getOracleDataBinder();
			db.setInitializeLazy(true);
			
			Iterator iter = dailySummaryList.iterator();
			for (; iter.hasNext();) {
				OrderStatsSummary orderStatsSummary = (OrderStatsSummary) iter.next();

				db.bind(orderStatsSummary);
				Node element1 =
					doc.importNode(db.toDOM().getFirstChild(), true);
				
				orderStatsSummaryElement.appendChild(element1);
								
				element.appendChild(orderStatsSummaryElement);

			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}finally{
			try {
				HibernateUtil.closeOracleSession();
			} catch (HibernateException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return orderStatsSummaryElement;
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
