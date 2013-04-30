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
public class View30SecondHelper {

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

	public View30SecondHelper(String selectedDate) {
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
		Element exceptionElement =
		       append30SecondLoadSummary(MainScreenDoc, payload);
		//Element e =	appendDailySessionStatistics(MainScreenDoc,payload);
		pageElement.appendChild(headerElement);
		pageElement.appendChild(leftPanelElement);
		pageElement.appendChild(bodyElement);
		pageElement.appendChild(payload);
		

		MainScreenDoc.appendChild(pageElement);
		System.out.println(com.bcop.arch.builder.XMLUtils.documentToString(MainScreenDoc));
	}


    private static final String SELECT_30_SECOND_LOADS = "select count(*) as cnt, p.pagename, c.contextname, b.branchname from accessrecords a, pages p, contexts c, branches b where a.time > ? and a.time < ? and a.LoadTime >30000 and a.page_id = p.page_id and a.context_id = c.context_id and a.branch_tag_ID = b.branch_tag_id group by p.pageName,b.branchname, c.contextname order by c.contextname, cnt desc";

	private Element append30SecondLoadSummary(
		Document doc,
		Element element) {
		Session session = null;
		Element ThirtySecondLoads = null;
		Element calendarElement = null;
		try {
			calendarElement = doc.createElement("Calendar");

			calendarElement.appendChild(createCalendarElement(doc));
			element.appendChild(calendarElement);
			ThirtySecondLoads = doc.createElement("ThirtySecondLoads");
			session = HibernateUtil.currentSession();
			Connection con = session.connection();
            
            char[] cDate = selectedDate.toCharArray();
            StringBuffer sDate = new StringBuffer().append(cDate[0]).append(cDate[1])
                .append(cDate[2]).append(cDate[3])
                .append(cDate[5]).append(cDate[6])
                .append(cDate[8]).append(cDate[9]);
		
			PreparedStatement pstmt = con.prepareStatement(SELECT_30_SECOND_LOADS);
		
	        SimpleDateFormat sdf2 = new SimpleDateFormat("yyyyMMddHHmmss");
            Date d  = null;
            try  {
                d = sdf2.parse( sDate.toString()+"000000" );
            }
            catch(java.text.ParseException pe)
            {
                d = new Date();
            }
            
			//pstmt.setString(1,sDate.toString()+"000000");
            pstmt.setTimestamp(1,new java.sql.Timestamp( d.getTime() ));

            // ***************************************************************
            Date endd  = null;
            try  {
                endd = sdf2.parse( sDate.toString()+"235959" );
            }
            catch(java.text.ParseException pe)
            {
                endd = new Date();
            }

            pstmt.setTimestamp(2,new java.sql.Timestamp( endd.getTime() ));
			
			ResultSet rs = pstmt.executeQuery();
			
            while(rs.next()){
//"	select count(*) as cnt, p.pageName, c.contextName, b.branchName "+

				String count = rs.getString(1);
				String page = rs.getString(2);
				String context = rs.getString(3);
				String branch = rs.getString(4);

				Element ThirtySecondLoadElement = doc.createElement("ThirtySecondLoadElement");

				Element count30SecondLoads = doc.createElement("Count");
				Element pageNameElement = doc.createElement("PageName");
				Element contextNameElement = doc.createElement("ContextName");
				Element branchNameElement = doc.createElement("BranchName");
				
				count30SecondLoads.appendChild(doc.createTextNode(count));
				pageNameElement.appendChild(doc.createTextNode(page));
				contextNameElement.appendChild(doc.createTextNode(context));
				branchNameElement.appendChild( doc.createTextNode(branch));
				
				ThirtySecondLoadElement.appendChild(count30SecondLoads);
				ThirtySecondLoadElement.appendChild(pageNameElement);
				ThirtySecondLoadElement.appendChild(contextNameElement);
				ThirtySecondLoadElement.appendChild(branchNameElement);

				ThirtySecondLoads.appendChild(ThirtySecondLoadElement);
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
        element.appendChild(ThirtySecondLoads);
		return ThirtySecondLoads;
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
