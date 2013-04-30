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
public class ViewExceptionEventHelper {

	private Document MainScreenDoc = null;
	DocumentBuilderFactory factory = null;
	DocumentBuilder builder = null;
	Document doc = null;
	String exceptionEventID;

	/*
	    public ViewHistoricalChartsHelper() {
			initialize();
		}
	*/

	public ViewExceptionEventHelper(String eventID) {
		exceptionEventID = eventID;
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
		Element exceptionElement =
			appendExceptionsElement(MainScreenDoc, payload);
		Element clickStream = appendClickStream(MainScreenDoc,payload);

		pageElement.appendChild(headerElement);
		pageElement.appendChild(leftPanelElement);
		pageElement.appendChild(bodyElement);
		pageElement.appendChild(payload);
		

		MainScreenDoc.appendChild(pageElement);
		
		System.out.println(MainScreenDoc.toString());
		System.out.println(pageElement.toString());
		System.out.println(com.bcop.arch.builder.XMLUtils.documentToString(MainScreenDoc));

	}


            private static final String SELECT_STACK_TRACE = 
"	select  std.Stack_Depth, str.Row_Message, st.Trace_Message "+
			"			from StackTraceRows str, StackTraceDetails std, StackTraces st "+
			"			where st.Trace_id=std.Trace_id "+
			"			and std.Row_id=str.Row_id "+
			"			and st.Trace_id= ? "+
			"			order by std.Stack_Depth asc";

			private static final String SELECT_USER_CLICK_STREAM = 
       " select u.userName, a.Time, s.browserType, p.pageName, m.shortName, c.contextName, b.branchName, app.appName,  a.requestType " +  
       " from AccessRecords a, Sessions s,Users u, Pages p, Machines m, Contexts c, Branches b, Apps app where  " +  
       " s.Session_ID= ? "  +  
       " and a.Session_ID=s.Session_ID " +  
       " and a.Time>= ? " +  
       " and a.Time<= ? " +  
       " and a.Page_ID=p.Page_ID  " +  
       " and a.Machine_ID=m.Machine_ID " +  
       " and a.Context_ID=c.Context_ID " +  
       " and a.Branch_Tag_ID=b.Branch_Tag_ID " +  
       " and a.App_ID=app.App_ID " +  
       " and a.User_ID=u.User_ID " +  
       " order by a.Time ";


private static final String SELECT_SESSION_TEXT = "select trace_key from stacktraces where trace_id=?";

private static final String SELECT_SESSION_ID = "select session_ID, sessionstarttime, sessionendtime from sessions where sessionTXT=? order by sessionhits asc limit 1"; 

	private Element appendExceptionsElement(
		Document doc,
		Element element) {
		Session session = null;
		Element exceptionRecord = null;
		Element calendarElement = null;
		try {
			calendarElement = doc.createElement("Calendar");

			calendarElement.appendChild(createCalendarElement(doc));
			element.appendChild(calendarElement);
			exceptionRecord = doc.createElement("ExceptionRecord");
			session = HibernateUtil.currentSession();
			Connection con = session.connection();
			PreparedStatement pstmt = con.prepareStatement(SELECT_STACK_TRACE );
			pstmt.setString(1,exceptionEventID );
			ResultSet rs = pstmt.executeQuery();
			
			Element stackTrace = doc.createElement("StackTrace");
			
            while(rs.next()){
//				"	select  std.Stack_Depth, str.Row_Message, st.Trace_Message "+
				
				String sTraceDepth   = rs.getString(1);
				String sRowMessage   = rs.getString(2);
				String sTraceMessage = rs.getString(3);


				Element traceLine = doc.createElement("TraceLine");
				
				Element traceDepth = doc.createElement("Depth");
				traceDepth.appendChild(doc.createTextNode(sTraceDepth));
				
				
				Element rowMessage = doc.createElement("RowMessage");
				rowMessage.appendChild(doc.createTextNode(sRowMessage));
				Element traceMessage = doc.createElement("TraceMessage");
				traceMessage.appendChild(doc.createTextNode(sTraceMessage));
				
				traceLine.appendChild(traceDepth);
				traceLine.appendChild(rowMessage);
				traceLine.appendChild(traceMessage);
				
				stackTrace.appendChild(traceLine);

            }
            exceptionRecord.appendChild(stackTrace);
            element.appendChild(exceptionRecord);

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
		return exceptionRecord;
    }
    
    private Element appendClickStream(
	Document doc,
	Element element) {
	Session session = null;
	Element clickStream = doc.createElement("ClickStream");
	try {

		session = HibernateUtil.currentSession();
		Connection con = session.connection();

        PreparedStatement pstmta = con.prepareStatement(SELECT_SESSION_TEXT);
        int eeid = Integer.parseInt(exceptionEventID);
        pstmta.setInt(1,eeid);
        ResultSet rsa = pstmta.executeQuery();
        String sessionTXT = null;
        if(rsa.next()){
            sessionTXT = rsa.getString("trace_key"); 
        }
        rsa.close();
        pstmta.close();
        pstmta = con.prepareStatement(SELECT_SESSION_ID);
        pstmta.setString(1,sessionTXT);
        rsa = pstmta.executeQuery();
        java.sql.Timestamp startTime = null;
        java.sql.Timestamp endTime   = null;
        int sessionId = 0;
        if(rsa.next()){
            sessionId = rsa.getInt("session_id");
            startTime = rsa.getTimestamp("sessionstarttime");
            endTime   = rsa.getTimestamp("sessionendtime");
        }
        rsa.close();
        pstmta.close();
        
        
		PreparedStatement pstmt = con.prepareStatement(SELECT_USER_CLICK_STREAM );
		pstmt.setInt(1,sessionId);
        pstmt.setTimestamp(2,startTime);
        pstmt.setTimestamp(3,endTime);

        ResultSet rs = pstmt.executeQuery();
			
			
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		SimpleDateFormat sdfDate = new SimpleDateFormat("HH:mm:ss");
		SimpleDateFormat sdfHour = new SimpleDateFormat("yyyy-MM-dd");
		while(rs.next()){
//			"select u.userName, a.Time, s.browserType, p.pageName, m.shortName, c.contextName, b.branchName, app.appName , a.queryParams "+
				
			String sUserName    = rs.getString(1);
			String sTime   = rs.getString(2);
			String sBrowserType = rs.getString(3);
			String sPageName = rs.getString(4);
			String sMachine = rs.getString(5);
			String sContext = rs.getString(6);
			String sBranchName = rs.getString(7);
			String sAppName    = rs.getString(8);
			String sQueryParmsRequestParms = "Not Available";//:rs.getString(9);
			String sRequestType = rs.getString(9);
			String sQueryParms = sQueryParmsRequestParms;
			String sRequestParms = "null";
			if(sQueryParmsRequestParms != null && sQueryParmsRequestParms.indexOf("#P#") > 0){
				sQueryParms = sQueryParmsRequestParms.substring(0,sQueryParmsRequestParms.indexOf("#P#"));
				sRequestParms = sQueryParmsRequestParms.substring((sQueryParmsRequestParms.indexOf("#P#") +3));
			}


			Element request = doc.createElement("Request");
			
			Element userName = doc.createElement("UserName");
			Element time     = doc.createElement("Time");
			Element timeDate = doc.createElement("Date");
			Element timeHour = doc.createElement("Hour");
			Element browserType = doc.createElement("BrowserType");
			Element pageName    = doc.createElement("PageName");
			Element machineName = doc.createElement("MachineName");
			Element contextName = doc.createElement("ContextName");
			Element branchName = doc.createElement("BranchName");
			Element appName    = doc.createElement("AppName");
			Element queryParams = doc.createElement("QueryParams");
			Element requestParams = doc.createElement("RequestParams"); 
			Element requestType = doc.createElement("RequestType"); 
				
			userName.appendChild(doc.createTextNode(sUserName));	
			timeDate.appendChild(doc.createTextNode(sTime.substring(0,10)));	
			timeHour.appendChild(doc.createTextNode(sTime.substring(10)));	
			time.appendChild(timeDate);
			time.appendChild(timeHour);
			browserType.appendChild(doc.createTextNode(sBrowserType));
			pageName.appendChild(doc.createTextNode(sPageName));
			machineName.appendChild(doc.createTextNode(sMachine));
			contextName.appendChild(doc.createTextNode(sContext));
			branchName.appendChild(doc.createTextNode(sBranchName));
			appName.appendChild(doc.createTextNode(sAppName));
			queryParams.appendChild(doc.createTextNode(sQueryParms));
			requestParams.appendChild(doc.createTextNode(sRequestParms));
			requestType.appendChild(doc.createTextNode(sRequestType));
			
			request.appendChild(userName);
			request.appendChild(time);
			request.appendChild(browserType);
			request.appendChild(pageName);
			request.appendChild(machineName);
			request.appendChild(contextName);
			request.appendChild(branchName);
			request.appendChild(appName);
			request.appendChild(queryParams);
			request.appendChild(requestParams);
			request.appendChild(requestType);
			
			clickStream.appendChild(request);
		}
		element.appendChild(clickStream);

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
	return clickStream;

    }

	public Element createCalendarElement(Document doc) {
		Calendar c = Calendar.getInstance();
		

		CalendarBean calendarBean = new CalendarBean(c);

		return (Element) doc.importNode(
			calendarBean.toDOM().getFirstChild(),
			true);
	}

	public Document getXMLDocument() {
		return this.doc;
	}


}
