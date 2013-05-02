package com.bos.helper;

import com.bos.arch.HibernateUtil;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Session;

/**
 * @author I081299
 */

public class ClickStreamServletHelper {
	private HashMap queryParamsMap = null;
	private String sessiontxt;
	private int sessionID;		
	
    public ClickStreamServletHelper(String sessiontxt) {
    	this.sessiontxt = sessiontxt;
    	sessionID = buildSessionID();
    }
    
    private int buildSessionID() {
    	String SELECT_SESSION_ID = "select session_id from sessions where sessiontxt=?";
    	int sessionID = -1;
    	Session session = null;
    	ResultSet rs = null;
        PreparedStatement pstmt = null;
    	try {
    		session = HibernateUtil.currentSession();
            Connection con = session.connection(); 
            
	    	// Run query to retrieve just the session id
	        pstmt = con.prepareStatement(SELECT_SESSION_ID);
	        pstmt.setString(1, sessiontxt);
	        rs = pstmt.executeQuery();
	        while (rs.next()) {
	        	sessionID = rs.getInt(1);
	        }
    	} catch (Exception ex) {
    		ex.printStackTrace();
    	} finally {
            try {
                pstmt.close();
            } catch (SQLException ex) {
                Logger.getLogger(ClickStreamServletHelper.class.getName()).log(Level.SEVERE, null, ex);
            }
            try {
                rs.close();
            } catch (SQLException ex) {
                Logger.getLogger(ClickStreamServletHelper.class.getName()).log(Level.SEVERE, null, ex);
            }
    		try {
                HibernateUtil.closeSession();
            } catch (HibernateException e) { }
    	}
        
        return sessionID;
    }
    
    
    /**
 	 * Queries the database for each page's query parameters based on the sessionTXT being used. 
 	 * For each unique page ID, all associated query parameters are appended to a string and
 	 * delimited by ampersands. After all query parameters have been parsed for a single page ID, 
 	 * store the delimited query string in a HashMap (using the page ID as the key), and move on
 	 * to the next page ID. Repeat this process until all page ID's have been iterated. 
     * 
     * @return htmlMap - The map of page ID's and their associated query strings
	 */
    public HashMap<String, String> getQueryParameters() {    	            
    	// if session has htmlpageresponse IDs, build from that and return true.
    	queryParamsMap = buildHtmlPageResponseQueryParams();
    	
    	// else, build from accessrecords
    	if (queryParamsMap == null || queryParamsMap.size() == 0)
    		queryParamsMap = buildAccessRecordsQueryParams();
    	
    	
    	return queryParamsMap;
    }
    
    
    private HashMap<String, String> buildHtmlPageResponseQueryParams() {
    	HashMap<String, String> htmlMap = new HashMap<String, String>();
    	StringBuilder queryParamsString = new StringBuilder();
    	int htmlID = 0, previousHtmlID = -1;
    	String SELECT_QUERY_PARAMS= "select distinct h.requestToken, h.htmlpageresponse_id, q.queryparams" + 
    	    	" from accessrecords ar,htmlpageresponse h, pages p,queryparamrecords qpr,queryparameters q where" + 
    	    	" session_id = ?" +
    	    	" and ar.page_id = p.page_id" +
    	    	" and h.sessiontxt = ?" +
    	    	" and p.page_id = h.page_id" + 
    	    	" and qpr.recordpk = ar.recordpk" + 
    	    	" and q.queryparameter_id = qpr.queryparameter_id" + 
    	    	" order by h.requestToken";  
    	ResultSet rs = null;
        PreparedStatement pstmt = null;

    	Session session = null;
    	try {
    		session = HibernateUtil.currentSession();
            Connection con = session.connection(); 
            
	    	// Run query to retrieve just the session id
	        pstmt = con.prepareStatement(SELECT_QUERY_PARAMS);
	        pstmt.setInt(1, sessionID);
	        pstmt.setString(2, sessiontxt);	        
	        rs = pstmt.executeQuery();
	        int paramCounter = 0;
            while (rs.next()) {
            	htmlID = rs.getInt("htmlpageresponse_id");

            	// If this is the first record OR the htmlpageresponse_id for this record is the same as the previous record
        		if ( previousHtmlID == -1 || htmlID == previousHtmlID ) {
        			// Only append ampersand if this is not the first query parameter
        			if (paramCounter > 0)
        				queryParamsString.append("&"); 
        			
        			queryParamsString.append( rs.getString("queryparams") );
        			paramCounter++;
        		}
        		else {
        			// If a new htmlID is found, add query parameters from previous htmlID to htmlMap
        			htmlMap.put(Integer.toString(previousHtmlID), queryParamsString.toString());
        			
        			queryParamsString = new StringBuilder(); // Clear builder for new data
        			paramCounter = 0; // Reset parameter count back to 0
        		}
        		previousHtmlID = htmlID;            	
            }
	        
    	} catch (Exception ex) {
    		ex.printStackTrace();
    	} finally {
            try {
                pstmt.close();
            } catch (SQLException ex) {
                Logger.getLogger(ClickStreamServletHelper.class.getName()).log(Level.SEVERE, null, ex);
            }
            try {
                rs.close();
            } catch (SQLException ex) {
                Logger.getLogger(ClickStreamServletHelper.class.getName()).log(Level.SEVERE, null, ex);
            }
    		try {
                HibernateUtil.closeSession();
            } catch (HibernateException e) { }
    	}
    	return htmlMap;
    }
    
    private HashMap<Integer, String> buildAccessRecordsQueryParams() {
    	String query1 = "select ar.recordpk AS recordpk, p.pagename AS pagename from accessrecords ar, pages p where ar.session_id = ? and ar.page_id = p.page_id order by recordpk";
    	String query2 = "select qpr.recordpk AS recordpk, qpr.queryparameter_id AS queryparameterID from queryparamrecords qpr where qpr.recordpk = ?";
    	String query3 = "select q.queryparams AS queryparams from queryparameters q where q.queryparameter_id = ?";    	
    	HashMap<Integer, String> records = new HashMap<Integer, String>();
    	StringBuilder queryParamsString = new StringBuilder();
    	Session session = null;
    	ResultSet rs1, rs2, rs3 = null;
        PreparedStatement pstmt1,pstmt2,pstmt3 = null;

    	try {
    		session = HibernateUtil.currentSession();
            Connection con = session.connection(); 
        	int record = 0, queryParameterID = 0;
        	String queryParam = "";
            
            pstmt1 = con.prepareStatement(query1);
            pstmt1.setInt(1, sessionID);
            rs1 = pstmt1.executeQuery();
            
            while (rs1.next()) {
            	record = rs1.getInt("recordpk");            	            	
            	pstmt2 = con.prepareStatement(query2);
            	pstmt2.setInt(1, record);
            	rs2 = pstmt2.executeQuery();       
            	
            	while (rs2.next()) {
            		queryParameterID = rs2.getInt("queryparameterID");            		
            		pstmt3 = con.prepareStatement(query3);
            		pstmt3.setInt(1, queryParameterID);
            		rs3 = pstmt3.executeQuery();   
            		
            		while (rs3.next()) {
            			queryParam = rs3.getString("queryparams");
            		}
            		
            		// Only append ampersand if this is not the first and last query parameter
        			if (!rs2.isFirst() && !rs2.isLast())
        				queryParamsString.append("&"); 
        			
        			queryParamsString.append(queryParam);
            	}            
            	
            	records.put(record, queryParamsString.toString());            	
            	queryParamsString = new StringBuilder(); // Clear builder for new data
            }      
    	} catch (Exception ex) {
    		ex.printStackTrace();
    	} finally {
    		try {
                HibernateUtil.closeSession();
            } catch (HibernateException e) { }
    	}
    	
    	return records;
    }
    
    public int getSessionID() {
    	return this.sessionID;
    }
}
