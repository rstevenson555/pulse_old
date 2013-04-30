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
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Hashtable;
import java.util.Iterator;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Session;
import org.apache.commons.codec.binary.Base64;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * @author I0360D4
 * 
 * To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Generation - Code and Comments
 */
public class PlayBackErrorPageHelper {

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

	String sStartDate;
	String sEndDate;
	public PlayBackErrorPageHelper(String selectedDate, String parmsStartDate, String parmsEndDate) {
		setSelectedDate(selectedDate);
		sStartDate = parmsStartDate; 
		sEndDate = parmsEndDate;
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

    private static final String SELECT_FROM_HTML_PAGES = "select * from htmlpageresponse where time > ? and time <?";
    
	private Element append30SecondLoadSummary(
		Document doc,
		Element element) {
		Hashtable htErrors = getErrors(sStartDate, sEndDate);
		Iterator iter = htErrors.keySet().iterator();
		
		Element errorReportElement = doc.createElement("ErrorReports");
		Element calendarElement = null;
		calendarElement = doc.createElement("Calendar");

		calendarElement.appendChild(createCalendarElement(doc));
		element.appendChild(calendarElement);
		
		
		while(iter.hasNext()){
			String nextErrorString = (String)iter.next();
			ErrorData ed = (ErrorData)htErrors.get(nextErrorString);
			Element errorReportItem = doc.createElement("ErrorReportItem");

			Element errorItemName = doc.createElement("ErrorDesc");
			Element errorItemCount = doc.createElement("count");
			
			errorItemName.appendChild(doc.createTextNode(nextErrorString));
			errorItemCount.appendChild(doc.createTextNode(""+ed.count));
			
			errorReportItem.appendChild(errorItemName);
			errorReportItem.appendChild(errorItemCount);

			
			errorReportElement.appendChild(errorReportItem);
		}
		
        element.appendChild(errorReportElement);
		return errorReportElement;
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
	private Hashtable getErrors(String startTime, String endTime){
		Hashtable ht = new Hashtable();
		
		try {
			SimpleDateFormat sdf2 = new SimpleDateFormat("yyyyMMddHHmmss");
			
			java.util.Date startDate = sdf2.parse(startTime);
			java.util.Date endDate   = sdf2.parse(endTime);

			
			
			System.out.println("startTime:"+startTime);
			System.out.println("endTime:"+endTime);
			
			
			
			
			Session session = HibernateUtil.currentSession();
			Connection con = session.connection();

			PreparedStatement pstmt = con.prepareStatement(SELECT_FROM_HTML_PAGES);
			java.sql.Timestamp startTimeSQL = new java.sql.Timestamp(startDate.getTime());
			java.sql.Timestamp endTimeSQL = new java.sql.Timestamp(endDate.getTime());

			pstmt.setTimestamp(1,startTimeSQL);
			pstmt.setTimestamp(2,endTimeSQL);

			ResultSet rs = pstmt.executeQuery();
			rs.setFetchSize(10);
			while(rs.next()){
				//System.out.println("in rs.next()");
				String encodedPage = rs.getString("encodedpage");
				String sessiontxt = rs.getString("sessiontxt");
				String branch_id = rs.getString("branch_id");
				String decodedPage = new String (Base64.decodeBase64(encodedPage.getBytes()));
				catalog(ht, decodedPage, sessiontxt, branch_id);
				
			}
			HibernateUtil.closeSession();
			
		} catch (HibernateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Location v");
		return ht;
	}


	
	private ArrayList getErrorStrings(){
		
		ArrayList al = new ArrayList();
		al.add("An unrecoverable error has been encountered");
		al.add("We're Sorry");
		
		return al;
		
	}
	public static final String TOTAL_PAGES = "AllPagesLoaded: ";
	private void catalog(Hashtable ht, String page, String session, String branch){
		ArrayList errorStrings = getErrorStrings();
		Iterator iter = errorStrings.iterator();
		ErrorData totalPages = (ErrorData)ht.get(TOTAL_PAGES);
		if(totalPages == null){
			totalPages = new ErrorData();
			totalPages.errorName = TOTAL_PAGES;
			ht.put(TOTAL_PAGES,totalPages);
		}
		totalPages.count += 1;
		ht.put(TOTAL_PAGES,totalPages);
		
		while(iter.hasNext()){
			String nextString = (String)iter.next();
			ErrorData currentError = (ErrorData)ht.get(nextString);
			if(currentError == null){
				currentError = new ErrorData();
				currentError.errorName = nextString;
				ht.put(nextString, currentError);
			}
			if(page.indexOf(nextString)>-1){
				currentError.count += 1;
				ht.put(nextString,currentError);
			}
		}
	}
	private class ErrorData {
		String errorName;
		int count;
	}

}
