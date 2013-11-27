package com.bos.helper;

import com.bos.arch.HibernateUtil;
import java.sql.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Session;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

/**
 * @author I081299
 */

public class ClickStreamServletHelper {
	private Map<HtmlLookupMap,String> queryParamsMap = null;
	private String sessiontxt;
	private int sessionID;
    private String sessionStart = "";

    private static String SELECT_QUERY_PARAMS_QUERY = "select ar.recordpk,ar.requestToken, h1.htmlpageresponse_id, (q.queryparams) AS queryparams,pp.page_id "+
            " from queryparamrecords qpr,pages pp,queryparameters q ,accessrecords ar  	    	"+
            " left outer join htmlpageresponse h1 on(ar.page_id = h1.page_id and h1.sessiontxt = ? and ar.requesttoken = h1.requesttoken) "+
            " where ar.session_id = ? and "+
            " qpr.recordpk = ar.recordpk and "+
            " ar.page_id = pp.page_id and "+
            " q.queryparameter_id = qpr.queryparameter_id  "+
            " order by ar.recordpk";


    public ClickStreamServletHelper(String sessiontxt,String sessionStart) {
    	this.sessiontxt = sessiontxt;
        this.sessionStart = sessionStart;
    	sessionID = buildSessionID();
    }

    private static final DateTimeFormatter fdf  = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");

    private int buildSessionID() {
    	String SELECT_SESSION_ID_WITHSTARTTIME = "select session_id from sessions where sessiontxt=? and sessionStartTime=?";
        String SELECT_SESSION_ID = "select max(session_id) as session_id from sessions where sessiontxt=? ";
    	int sessionID = -1;
    	Session session = null;
    	ResultSet rs = null;
        PreparedStatement pstmt = null;
    	try {
    		session = HibernateUtil.currentSession();
            Connection con = session.connection();
            // 2013-05-29 14:39:58
	    	// Run query to retrieve just the session id
            if (!StringUtils.isEmpty(sessionStart)) {
                DateTime sessionStartDateTime = fdf.parseDateTime(sessionStart);
                pstmt = con.prepareStatement(SELECT_SESSION_ID_WITHSTARTTIME);
                pstmt.setString(1, sessiontxt);
                pstmt.setTimestamp(2, new Timestamp(sessionStartDateTime.toDate().getTime()));
            } else {
                pstmt = con.prepareStatement(SELECT_SESSION_ID);
                pstmt.setString(1, sessiontxt);
            }
	        rs = pstmt.executeQuery();
	        while (rs.next()) {
	        	sessionID = rs.getInt(1);
                System.out.println("buildSessionID: " + sessionID);
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
    public Map<HtmlLookupMap, String> getQueryParameters() {
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

    static public class HtmlLookupMap {

        private int recordpk;

        /**
         * Get the value of recordpk
         *
         * @return the value of recordpk
         */
        public int getRecordpk() {
            return recordpk;
        }

        /**
         * Set the value of recordpk
         *
         * @param recordpk new value of recordpk
         */
        public void setRecordpk(int recordpk) {
            this.recordpk = recordpk;
        }
        private int html_id;

        /**
         * Get the value of html_id
         *
         * @return the value of html_id
         */
        public int getHtml_id() {
            return html_id;
        }

        /**
         * Set the value of html_id
         *
         * @param html_id new value of html_id
         */
        public void setHtml_id(int html_id) {
            this.html_id = html_id;
        }
        private int requestToken;

        /**
         * Get the value of requestToken
         *
         * @return the value of requestToken
         */
        public int getRequestToken() {
            return requestToken;
        }

        /**
         * Set the value of requestToken
         *
         * @param requestToken new value of requestToken
         */
        public void setRequestToken(int requestToken) {
            this.requestToken = requestToken;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final HtmlLookupMap other = (HtmlLookupMap) obj;
            if (this.recordpk != other.recordpk) {
                return false;
            }
            if (this.html_id != other.html_id) {
                return false;
            }
            if (this.requestToken != other.requestToken) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 97 * hash + this.recordpk;
            hash = 97 * hash + this.html_id;
            hash = 97 * hash + this.requestToken;
            return hash;
        }


    }

    private Map<HtmlLookupMap, String> buildHtmlPageResponseQueryParams() {
    	HashMap<HtmlLookupMap, String> htmlMap = new HashMap<HtmlLookupMap, String>();
    	StringBuilder queryParamsString = new StringBuilder();
        //String html_key = null;

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

            while (rs.next()) {
            	int htmlID = rs.getInt("htmlpageresponse_id");
                int recordpk = rs.getInt("recordpk");
                int requestToken = rs.getInt("requestToken");
                //html_key = String.valueOf(htmlID) + "|" + recordpk;
                //System.out.println("html_key: " + html_key);

                HtmlLookupMap map = new HtmlLookupMap();
                map.setRecordpk(recordpk);
                map.setHtml_id(htmlID);
                map.setRequestToken(requestToken);

                String []queryparams = rs.getString("queryparams").split("\\|\\|\\|\\|");

                List<String> lqp = Arrays.asList(queryparams);
                Collections.sort(lqp, new mySortIgnoreCase());
                for(String qs:lqp) {
                    queryParamsString.append( qs ).append("&");
                }

                htmlMap.put(map,queryParamsString.toString());
                queryParamsString = new StringBuilder();
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

    private Map<HtmlLookupMap, String> buildAccessRecordsQueryParams() {
    	String query1 = "select ar.recordpk AS recordpk, p.pagename AS pagename from accessrecords ar, pages p where ar.session_id = ? and ar.page_id = p.page_id order by recordpk";
    	String query2 = "select qpr.recordpk AS recordpk, qpr.queryparameter_id AS queryparameterID from queryparamrecords qpr where qpr.recordpk = ?";
        String query3 = "select (q.queryparams) AS queryparams from queryparameters q where q.queryparameter_id = ?";

    	Map<HtmlLookupMap, String> records = new HashMap<HtmlLookupMap, String>();

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

                HtmlLookupMap map = new HtmlLookupMap();
                map.recordpk = record;
            	records.put(map, queryParamsString.toString());

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
