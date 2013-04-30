package com.bos.servlets;

import com.bos.arch.HibernateUtil;
import com.bos.helper.ClickStreamServletHelper;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Session;

/**
 * @author I081299
 */

public class ClickStreamServlet {
    String selectedDate = null;
    String sessionTXT = null;
    String sessionOffset = null;
    String fromTime = null;
    String toTime = null;
    int userID = -1;    
    int sessionID = -1;
    private int sessionCount = 0;
    private List clickStream = null;
    private List userSessions = null;
    private HashMap<String, String> queryParamsMap = null;    
    private static final int SEARCH_SESSIONS_BY_USER = 1;
    private static final int SEARCH_SESSIONS_BY_ALL = 2;    
    private String SELECT_PAGE_VIEWS = "Select p.pageName, h.requestToken, h.requestTokenCount, h.HtmlPageResponse_ID from HtmlPageResponse h, Pages p " + 
    		" where p.Page_ID=h.Page_ID" + 
    		" and sessionTXT=?";
    private String SELECT_PAGE_VIEWS2 = "select distinct ar.recordpk, ar.page_id, p.pageName, ar.time" + 
        	" from accessrecords ar, pages p" + 
        	" where ar.session_id = ?" + 
        	" and ar.page_id = p.page_id";
    
    public static final int MAX_SESSION_ROWS = 100;
        
    public ClickStreamServlet(String selectedDate, String sessionTXT, String userID, String fromTime, String toTime, String sessionOffset) {
        this.selectedDate = selectedDate;
        this.sessionTXT = sessionTXT;           
    	this.fromTime = fromTime;
    	this.toTime = toTime;
    	this.sessionOffset = sessionOffset;   
    	
    	try {
    		this.userID = Integer.parseInt(userID);    				
    	} catch (NumberFormatException nfe) {
    		this.userID = -1;
    	}
    	
    	initialize();        	
    }
    
    private void initialize() {  
    	if (!sessionTXT.isEmpty()) {
	    	SELECT_PAGE_VIEWS = buildSearchQuery(SELECT_PAGE_VIEWS, "requestToken");
	    	SELECT_PAGE_VIEWS2 = buildSearchQuery(SELECT_PAGE_VIEWS2, "ar.recordpk");
	    	
	    	ClickStreamServletHelper cssh = new ClickStreamServletHelper(sessionTXT);
	    	sessionID = cssh.getSessionID();
	    	
	    	// If we have a sessionID, we can check if there are query parameters
	    	if (sessionID != -1)
	    		queryParamsMap = cssh.getQueryParameters();
	    	
	        //queryParamsMap = buildQueryParameters();
	        clickStream = buildClickStream(); 
    	}
    	
	     if (userID != -1 && sessionTXT.isEmpty())
	    	userSessions = buildUserSessions(SEARCH_SESSIONS_BY_USER);  
        
        if (userID == -1 && sessionTXT.isEmpty()  
        	   && !selectedDate.isEmpty() 
        	   && (!fromTime.isEmpty() || !toTime.isEmpty())) { 
        	userSessions = buildUserSessions(SEARCH_SESSIONS_BY_ALL);
        }
    }
    
    
    /**
 	 * Queries the database for a list of page views based on the sessionTXT being used. Each record will return a page ID,
 	 * a record ID, query parameters, and other relevant data. Each column of data from a record is then stored in a 
 	 * HashMap called ClickElement. Then, the ClickElement is stored in a List of HashMaps called ClickElements. After
 	 * all iterations through the returned records are complete, the List of HashMaps is returned to the caller.
     * 
     * @return ClickElements - The List of HashMaps which contains all page views for this session
	 */    
    private List buildClickStream() {
    	// Hash Map to hold a single ClickElement
        HashMap<String, String> ClickElement = new HashMap<String, String>();
    	
        // Array List to hold collection of ClickElements
        ArrayList<HashMap<String, String>> ClickElements = new ArrayList<HashMap<String, String>>();
               
        Session session = null;
        
        try {
            session = HibernateUtil.currentSession();
            Connection con = session.connection();
            
            PreparedStatement pstmt = con.prepareStatement(SELECT_PAGE_VIEWS);
            pstmt.setString(1, sessionTXT);
            ResultSet rs = pstmt.executeQuery();
            boolean hasHtmlID = false; 
            
            while (rs.next()) {
                String HtmlPageResponse_ID = rs.getString("HtmlPageResponse_ID");
                String requestToken = rs.getString("requestToken");
                String pageName   = rs.getString("pageName");
                String requestTokenCount = rs.getString("requestTokenCount");
                
                ClickElement.put("htmlPageID", HtmlPageResponse_ID);
                ClickElement.put("requestToken", requestToken);
                ClickElement.put("pageName", pageName);
                ClickElement.put("requestTokenCount", requestTokenCount);
                ClickElement.put("queryParams", queryParamsMap.get(HtmlPageResponse_ID));
                
                // Add single ClickElement to collection of all ClickElements 
                ClickElements.add(ClickElement);
                
                // Clear ClickElement container for next loop iteration
                ClickElement = new HashMap<String, String>();
                hasHtmlID = true;
            }      
            
            // If previous query did not contain any Html_ID's then we need to run a different query.
            if (!hasHtmlID) {
            	ArrayList<Integer> currQueryParamIDs = new ArrayList<Integer>();
        		ArrayList<Integer> prevQueryParamIDs = new ArrayList<Integer>();
            	pstmt = con.prepareStatement(SELECT_PAGE_VIEWS2);
            	pstmt.setInt(1, sessionID);
            	rs = pstmt.executeQuery();
            	
            	while (rs.next()) {
            		String pageName = rs.getString("pageName");
            		int record = rs.getInt("recordpk");
            		
            		// Retrieve list of all query parameter IDs for each recordpk
            		pstmt = con.prepareStatement("select queryparameter_id from queryparamrecords where recordpk = ?");
            		pstmt.setInt(1, record);
            		ResultSet rs2 = pstmt.executeQuery();
            		while (rs2.next()) {            			
            			currQueryParamIDs.add(rs2.getInt("queryparameter_id"));
            		}
            		
            		/* Check if the current list of query parameter IDs is equal to the previously
            		   saved list. If so, that means the current click element has two recordpk's 
            		   associated with it, and we ignore the duplicate set of query parameters. */           		 
            		if (!currQueryParamIDs.equals(prevQueryParamIDs)) {
	            		ClickElement.put("pageName", pageName);
	            		ClickElement.put("queryParams", (String) queryParamsMap.get( record ));
	            		
	            		// Add single ClickElement to collection of all ClickElements 
	                    ClickElements.add(ClickElement);
	                    
	                    // Clear ClickElement container for next loop iteration
	                    ClickElement = new HashMap<String, String>();
            		}
            		// clear prev 
            		prevQueryParamIDs = new ArrayList<Integer>();
            		
            		// copy curr to prev
            		for (Integer qpID : currQueryParamIDs)
            			prevQueryParamIDs.add(qpID);
            		
            		// clear curr
            		currQueryParamIDs = new ArrayList<Integer>();
            	}
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            try {
                HibernateUtil.closeSession();
            } catch (HibernateException e) { }
        }
        
        return ClickElements;
    }
    
    
    
    /**
 	 * Takes in a partial search query as a parameter and appends more clauses to it if necessary
     * 
     * @return searchQuery - The modified search query 
	 */
    private String buildSearchQuery(String searchPrefix, String orderBy) {
    	StringBuilder searchQuery = new StringBuilder(searchPrefix); 
    	String newFromTime = this.fromTime, 
    		   newToTime = this.toTime;
    	if (newFromTime == null || newFromTime.equals("")) newFromTime = "00:00:00";
    	if (newToTime == null || newToTime.equals("")) newToTime = "23:59:59";
    		
    	if (!selectedDate.isEmpty()) {
    		searchQuery.append(" and time >= to_timestamp('" + selectedDate + " " + newFromTime + "', 'YYYY-MM-DD HH24:MI:SS')");
			searchQuery.append(" and time <= to_timestamp('" + selectedDate + " " + newToTime + "', 'YYYY-MM-DD HH24:MI:SS')");    	
    	} else {
    		searchQuery.append(" and cast(time as TIME) >= '" + newFromTime + "'");
    		searchQuery.append(" and cast(time as TIME) <= '" + newToTime + "'");
    	} 
    	searchQuery.append(" order by " + orderBy); 
    	
    	return searchQuery.toString();
    }    
    
    
    
    private List buildUserSessions(int queryType) {
    	List<HashMap<String, String>> userSessionsList = new ArrayList<HashMap<String, String>>();
    	HashMap<String, String> userSession = new HashMap<String, String>();
    	Session session = null;    	
    	    	
    	try {
    		session = HibernateUtil.currentSession();
            Connection con = session.connection();   
            
            // Retrieve number of sessions for this query 
            PreparedStatement pstmt = con.prepareStatement(buildNumSessionsQuery(queryType));
            if (queryType == SEARCH_SESSIONS_BY_USER)
            	pstmt.setInt(1, userID);
            
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
            	sessionCount = rs.getInt(1);
            }
            
            if (sessionCount > 0) {
            	String user_id="";
	            // Retrieve sessions 
	            pstmt = con.prepareStatement(buildSessionQuery(queryType));
	            if (queryType == SEARCH_SESSIONS_BY_USER)
	            	pstmt.setInt(1, userID);
	            
	            rs = pstmt.executeQuery();	            
	            while (rs.next()) {
	            	if (queryType == SEARCH_SESSIONS_BY_ALL) {	            		
	            		user_id = rs.getString("user_id");
	            		userSession.put("user_id", user_id);
	            	}
	            	
	            	String sessiontxt = rs.getString("sessiontxt");
	            	String startTime = rs.getTimestamp("sessionstarttime").toString();
	            	String endTime = rs.getTimestamp("sessionendtime").toString();
	            	
	            	// Truncate milliseconds
	            	startTime = startTime.split("\\.")[0]; 
	            	endTime = endTime.split("\\.")[0];
	            	
	            	
	            	userSession.put("session", sessiontxt);
	            	userSession.put("starttime", startTime);
	            	userSession.put("endtime", endTime);
	            	
	            	userSessionsList.add(userSession);
	            	userSession = new HashMap<String, String>();
	            }
            }
    	} catch (Exception ex) {
    		ex.printStackTrace();
    	} finally {
    		try {
                HibernateUtil.closeSession();
            } catch (HibernateException e) { }
    	}
    	
    	    	
    	return userSessionsList;
    } 
        
    
    
    /**
 	 * Builds a query string to be used to retrieve the number of sessions with 
 	 * the date and time appended to it.
     * 
     * @return numSessionsQuery - The query string to retrieve the number of sessions
	 */
    private String buildNumSessionsQuery(int queryType) {
    	String numSessionsQuery = "";
    	if (queryType == SEARCH_SESSIONS_BY_USER)
    		numSessionsQuery = "SELECT COUNT(*) FROM sessions WHERE user_id = ?";
    	if (queryType == SEARCH_SESSIONS_BY_ALL)
    		numSessionsQuery = "SELECT COUNT(*) FROM sessions where true";
    	
    	if (!selectedDate.isEmpty()) {
    		String newFromTime = this.fromTime, 
	    		   newToTime = this.toTime;
    		if (newFromTime.isEmpty()) newFromTime = "00:00:00";
    		if (newToTime.isEmpty()) newToTime = "23:59:59";
    		
    		numSessionsQuery += " and sessionstarttime >= to_timestamp('" + selectedDate + " " + newFromTime + "', 'YYYY-MM-DD HH24:MI:SS')";
    		numSessionsQuery += " and sessionstarttime <= to_timestamp('" + selectedDate + " " + newToTime + "', 'YYYY-MM-DD HH24:MI:SS')";
    	}
    		
    	return numSessionsQuery;    			
    }
    
    
    
    /**
 	 * Builds a query string to be used to retrieve the sessions for a specified user ID.
     * 
     * @return userIDQuery - The query string to retrieve the sessions for the specified user ID
	 */
    private String buildSessionQuery(int searchType) {
    	String query = "";
    	if (searchType == SEARCH_SESSIONS_BY_USER)
    		query = "select sessiontxt, sessionstarttime, sessionendtime from sessions where user_id=?";
    	if (searchType == SEARCH_SESSIONS_BY_ALL)
    		query = "select user_id, sessiontxt, sessionstarttime, sessionendtime from sessions where true ";
    	
    	// If a date is selected, add it to query.
    	if (!selectedDate.isEmpty()) {
    		String newFromTime = this.fromTime, 
	    		   newToTime = this.toTime;
    		
    		// If a time is not specified, set a default time value.
    		if (newFromTime.isEmpty()) newFromTime = "00:00:00";
    		if (newToTime.isEmpty()) newToTime = "23:59:59";
    		
	    	query += " and sessionstarttime >= to_timestamp('" + selectedDate + " " + newFromTime + "', 'YYYY-MM-DD HH24:MI:SS')";
	    	query += " and sessionstarttime <= to_timestamp('" + selectedDate + " " + newToTime + "', 'YYYY-MM-DD HH24:MI:SS')";    
    	}
    	
        query += " order by sessionstarttime desc";     
        
        if (sessionOffset.equals(""))
        	query += " LIMIT " + MAX_SESSION_ROWS;
        else
        	query += " OFFSET " + sessionOffset + " LIMIT " + MAX_SESSION_ROWS;   
    	
        return query;
    }
    
    
    
    /** 
     * @return The row offset that was used when retrieving sessions for a single user ID
     */
    public int getSessionOffset() {
    	int offset = 0;
    	try {
    		offset = Integer.parseInt(sessionOffset);
    	}
    	catch (NumberFormatException nfe) { }
    	
    	return offset;
    }
    
    /** 
     * @return The number of sessions for a single user ID
     */
    public int getSessionCount() {
    	return sessionCount;
    }
    
    /** 
     * @return A list of sessions for a user
     */
    public List getUserSessions() {
    	return userSessions;
    }
    
    /**
     * @return The click stream for the current session
     */
    public List getClickStream() {
    	if (clickStream != null)
    		return clickStream;
    	else 
			return new ArrayList<HashMap<String, String>>();
    }
    
    /**
     * @param selectedDate The selectedDate to set.
     */
    
    public void setSelectedDate(String selectedDate) {
        this.selectedDate = selectedDate;
    }
    /**
     * @return The selected date.
     */
    public String getSelectedDate() {
        return selectedDate;
    }
    
}
