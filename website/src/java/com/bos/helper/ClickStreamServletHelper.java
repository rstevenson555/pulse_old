package com.bos.helper;

import com.bos.arch.HibernateUtil;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Session;
import org.apache.commons.lang3.StringEscapeUtils;

/**
 * @author I081299
 */

public class ClickStreamServletHelper {
	private HashMap queryParamsMap = null;
	private String sessiontxt;
	private int sessionID;		
    //private static String SELECT_QUERY_PARAMS_QUERY = "select distinct ar.recordpk,h1.requestToken, h1.htmlpageresponse_id, (q.queryparams_key || '=' || q.queryparams) AS queryparams,pp.page_id "+
    private static String SELECT_QUERY_PARAMS_QUERY = "select distinct ar.recordpk,ar.requestToken, h1.htmlpageresponse_id, (q.queryparams) AS queryparams,pp.page_id "+
            " from queryparamrecords qpr,pages pp,queryparameters q ,accessrecords ar  	    	"+
            " left outer join htmlpageresponse h1 on(ar.page_id = h1.page_id and h1.sessiontxt = ?) "+
            " where session_id = ? and "+
            " qpr.recordpk = ar.recordpk and "+
            " ar.page_id = pp.page_id and "+
            " q.queryparameter_id = qpr.queryparameter_id and "+
            " ar.requesttoken = h1.requesttoken "+
            //" group by ar.recordpk,h1.requestToken, h1.htmlpageresponse_id, (q.queryparams_key || '=' || q.queryparams),pp.page_id "+
            //" group by ar.recordpk,h1.requestToken, h1.htmlpageresponse_id, (q.queryparams),pp.page_id "+
            " order by ar.requestToken";
            //" order by ar.recordpk,h1.requestToken, h1.htmlpageresponse_id";

	
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
        System.out.println("buildHtmlPageResponseQueryParams");
    	queryParamsMap = buildHtmlPageResponseQueryParams();
    	
    	// else, build from accessrecords
    	if (queryParamsMap == null || queryParamsMap.size() == 0) {
            //System.out.println("buildAccessRecordsQueryParams");
            queryParamsMap = buildAccessRecordsQueryParams();
        }
    	    	
    	return queryParamsMap;
    }
    
    static public class mySortIgnoreCase implements Comparator<Object> {
        public int compare(Object o1, Object o2) {
            String s1 = (String) o1;
            String s2 = (String) o2;
            return s1.toLowerCase().compareTo(s2.toLowerCase());
        }
    }

    private HashMap<String, String> buildHtmlPageResponseQueryParams() {
    	HashMap<String, String> htmlMap = new HashMap<String, String>();
    	StringBuilder queryParamsString = new StringBuilder();
        String html_key = null,previous_html_key = null;
        
    	ResultSet rs = null;
        PreparedStatement pstmt = null;

    	Session session = null;
    	try {
    		session = HibernateUtil.currentSession();
            Connection con = session.connection(); 
            
	    	// Run query to retrieve just the session id
	        pstmt = con.prepareStatement(SELECT_QUERY_PARAMS_QUERY);
	        pstmt.setInt(2, sessionID);
	        pstmt.setString(1, sessiontxt);	        
	        rs = pstmt.executeQuery();
	        int paramCounter = 0;
            while (rs.next()) {
            	int htmlID = rs.getInt("htmlpageresponse_id");
                String recordpk = rs.getString("recordpk");
                html_key = String.valueOf(htmlID) + "|" + recordpk;
                //System.out.println("htmlID: " + html_key);
                System.out.println("queryparams: " + rs.getString("queryparams"));
                String []queryparams = rs.getString("queryparams").split("\\|\\|\\|\\|");                    

                List<String> lqp = Arrays.asList(queryparams);
                //List<String> ary = new ArrayList<String>(queryparams);

                //System.out.println(set);
                Collections.sort(lqp, new mySortIgnoreCase());       
                /*Set<String> set = new HashSet<String>();
                for(String qp:queryparams) {
                    //queryParamsString.append( qp ).append("&");
                    set.add( qp );
                }
                Set<String> sset = new TreeSet<String>(set);
                for(String qs:sset) {
                    queryParamsString.append( qs ).append("&");
                }*/
                for(String qs:lqp) {
                    queryParamsString.append( qs ).append("&");
                }

                htmlMap.put(html_key,queryParamsString.toString());
                queryParamsString = new StringBuilder();
            	// If this is the first record OR the htmlpageresponse_id for this record is the same as the previous record
//        		if ( previous_html_key == null || html_key.equals(previous_html_key) ) {
//        			// Only append ampersand if this is not the first query parameter
//        			//if (paramCounter > 0)
//        			//	queryParamsString.append("&"); 
//        			
//        			//queryParamsString.append( rs.getString("queryparams") );
//                    for(String qp:queryparams) {
//                        queryParamsString.append( qp ).append("&");
//                    }
//        			paramCounter++;
//        		}
//        		else {
//        			// If a new htmlID is found, add query parameters from previous htmlID to htmlMap
//        			htmlMap.put(previous_html_key, queryParamsString.toString());
//                   // System.out.println(Integer.toString(previousHtmlID) + ":" +queryParamsString.toString());
//        			
//        			queryParamsString = new StringBuilder(); // Clear builder for new data
//        			paramCounter = 0; // Reset parameter count back to 0
//        		}
//        		previous_html_key = html_key;            	
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
        System.out.println("return map:");
    	return htmlMap;
    }
    
    private HashMap<Integer, String> buildAccessRecordsQueryParams() {
    	String query1 = "select ar.recordpk AS recordpk, p.pagename AS pagename from accessrecords ar, pages p where ar.session_id = ? and ar.page_id = p.page_id order by recordpk";
    	String query2 = "select qpr.recordpk AS recordpk, qpr.queryparameter_id AS queryparameterID from queryparamrecords qpr where qpr.recordpk = ?";
    	//String query3 = "select (q.queryparams_key || '=' || q.queryparams) AS queryparams from queryparameters q where q.queryparameter_id = ?";    	
        String query3 = "select (q.queryparams) AS queryparams from queryparameters q where q.queryparameter_id = ?";    	
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
        			
                    //System.out.println(StringEscapeUtils.escapeHtml4(queryParam));
        			queryParamsString.append(StringEscapeUtils.escapeHtml4(queryParam));
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
