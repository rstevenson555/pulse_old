package com.bos.servlets;

import com.bos.arch.HibernateUtil;
import com.bos.helper.ClickStreamServletHelper;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
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
public class ClickStreamServlet {

    String selectedDate = null;
    Date selectedStartTime,selectedEndTime;
    String sessionTXT = null,advancedSearchString = null;
    String sessionOffset = null;
    String fromTime = null;
    String toTime = null;
    String userID = null;
    String sessionStart = null;
    int sessionID = -1;
    private int sessionCount = 0;
    private List clickStream = null;
    private List userSessions = null;
    private Map<ClickStreamServletHelper.HtmlLookupMap, String> queryParamsMap = null;
    private static final int SEARCH_SESSIONS_BY_USER = 1;
    private static final int SEARCH_SESSIONS_BY_ALL = 2;
    private static final int SEARCH_SESSIONS_BY_ADVANCED = 3;

    private String PAGES_BY_HTML_QUERY = "select  ar.recordpk,ar.requestToken,h1.HtmlPageResponse_ID, h1.requestTokenCount,pp.pageName,ar.time,h1.experience "+
        " from queryparamrecords qpr,pages pp,queryparameters q ,accessrecords ar 	    	 "+
        " left outer join htmlpageresponse h1 on(ar.page_id = h1.page_id and h1.sessiontxt = ? and ar.requestToken = h1.requesttoken) "+
        " where ar.session_id = ? and "+
        " qpr.recordpk = ar.recordpk and "+
        " ar.page_id = pp.page_id and "+
        " q.queryparameter_id = qpr.queryparameter_id  "+
        " order by ar.recordpk ";

    private String PAGES_BY_ACCESS_RECORDS_QUERY = "select ar.recordpk, ar.page_id, p.pageName, ar.time "
            + " from accessrecords ar, pages p"
            + " where ar.session_id = ?"
            + " and ar.page_id = p.page_id "
            + " and ar.time >= ? and ar.time <= ? "
            + " order by ar.recordpk ";

    private String SESSIONS_BY_USER_QUERY =  "select sessiontxt, session_id, sessionstarttime, sessionendtime, sessionhits, sessionduration, browsertype, experience "+
            " from sessions where user_id=(select user_id from users where username=?) and sessionstarttime >= ? and sessionstarttime <= ? order by sessionstarttime desc ";
    private String SESSIONS_COUNT_BY_USER_QUERY = "select count(*) "+
            " from sessions where user_id=(select user_id from users where username=?) and sessionstarttime >= ? and sessionstarttime <= ? group by sessionstarttime order by sessionstarttime desc ";

    private String SESSIONS_BY_ALL_QUERY = "select u.username, sessiontxt, session_id, sessionstarttime, sessionendtime, sessionhits, sessionduration, browsertype,experience from sessions, "+
                                        " users u where true and sessions.user_id = u.user_id and sessionstarttime >= ? and sessionstarttime <= ? order by sessionstarttime desc ";
    private String SESSIONS_COUNT_BY_ALL_QUERY = " select count(uu.username) from (select u.username, sessiontxt, sessionstarttime, sessionendtime, sessionhits, sessionduration, browsertype,experience from sessions, "+
      " users u where true and sessions.user_id = u.user_id and sessionstarttime >= ? and "+
        " sessionstarttime <= ?  order by sessionstarttime desc ) as uu ";

    private String SESSIONS_BY_ADVANCED_QUERY = "select u.username, s.sessiontxt, s.session_id,s.sessionstarttime, s.sessionendtime, s.sessionhits, s.sessionduration, s.browsertype,s.experience from sessions s, " +
                " accessrecords ar,users u " +
                " where ar.session_id = s.session_id " +
                " and s.user_id = u.user_id " +
                " and s.sessionstarttime >= ? and sessionstarttime <= ? " +
                " and ar.recordpk in (#RECORDPARAMS#)  order by s.sessionstarttime desc ";

    private String SESSIONS_COUNT_BY_ADVANCED_QUERY = "select count(s.*) from sessions s, " +
                " accessrecords ar " +
                " where ar.session_id = s.session_id " +
                " and s.sessionstarttime >= ? and sessionstarttime <= ? " +
                " and ar.recordpk in (#RECORDPARAMS#)  "+
                " group by s.sessiontxt, s.sessionstarttime, s.sessionendtime, s.sessionhits,s.sessionduration, s.browsertype,s.experience";

    private static String QUERY_RECORDPKS_FOR_QUERYPARAM = "select recordpk from queryparamrecords where queryparameter_id in(select q.queryparameter_id "+
            " from queryparameters q where q.queryparameter_id in (select qpr2.queryparameter_id from queryparamrecords qpr2,accessrecords ar2 where "+
            " ar2.recordpk = qpr2.recordpk and ar2.time >=? and ar2.time <=?) and q.queryparams like ?)";

    public static final int MAX_SESSION_ROWS = 100;

    public ClickStreamServlet(String selectedDate, String sessionTXT, String sessionStart,String advancedSearch,String userID, String fromTime, String toTime, String sessionOffset) {
        this.selectedDate = selectedDate;
        this.sessionTXT = StringUtils.trim(sessionTXT);
        this.fromTime = fromTime;
        this.toTime = toTime;
        this.sessionOffset = sessionOffset;
        this.advancedSearchString = StringUtils.trim(advancedSearch);
        this.userID = StringUtils.trim(userID);
        this.sessionStart = sessionStart;
        System.out.println("sessionStart: " + sessionStart);

        initialize();
    }

    /**
     * build the start and end dates
     */
    private void buildStartEndDate() {
        //DateTime selectedDateTime = null;
        String newFromTime = this.fromTime,
                    newToTime = this.toTime;
        DateTimeFormatter dateformat = DateTimeFormat.forPattern("yyyy-MM-dd");
        DateTimeFormatter timeformat = DateTimeFormat.forPattern("HH:mm:ss");

        String selectedBeginDate = "",  selectedEndDate = "";

        // HANDLE SELECTED DATE
        // selected date is NOT empty
        if (!StringUtils.isEmpty(selectedDate)) {
            selectedBeginDate = selectedDate;
            selectedEndDate = selectedDate;
        } else {
            // date is empty default start and end time to be now
            DateTime dt_startDate = new DateTime();
            DateTime dt_endDate = new DateTime();
            selectedBeginDate = dateformat.print(dt_startDate);
            selectedEndDate = dateformat.print(dt_endDate);
        }
        System.out.println("newFromTime: " + newFromTime);

        // HANDLE FROM TIME
        // if from time is empty and session is empty
        if (StringUtils.isEmpty(newFromTime) && StringUtils.isEmpty(sessionTXT)) {
            DateTime dt_fromTime = new DateTime();
            dt_fromTime.minusHours(1);
            //newFromTime = "00:00:00";
            newFromTime = timeformat.print(dt_fromTime);
        } else if (!StringUtils.isEmpty(sessionTXT)) {
            // session is not empty
            DateTime dt_fromTime = new DateTime();
            dt_fromTime = dt_fromTime.toDateMidnight().toDateTime(); // 00
            System.out.println("dt_fromTime: " + dt_fromTime);

            //newFromTime = "00:00:00";
            newFromTime = timeformat.print(dt_fromTime);
        }

        // HANDLE TO TIME
        // if new time is empty and session is empty
        System.out.println("newToTime: " + newToTime);
        if (StringUtils.isEmpty(newToTime) && StringUtils.isEmpty(sessionTXT)) {
            //newToTime = "23:59:59";
            DateTime dt_toTime = new DateTime();
            newToTime = timeformat.print(dt_toTime);
        } else if (!StringUtils.isEmpty(sessionTXT)) {
            // session is not empty
            //newToTime = "23:59:59";
            DateTime dt_toTime = new DateTime();
            dt_toTime = dt_toTime.plusDays(1);
            dt_toTime = dt_toTime.toDateMidnight().toDateTime(); // 00
            dt_toTime = dt_toTime.minusMinutes(1);
            System.out.println("to_time: " + dt_toTime);

            newToTime = timeformat.print(dt_toTime);
        }

        // BUILD START END DATE
        //YYYY-MM-DD HH24:MI:SS
        DateTimeFormatter sdf = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
        DateTimeFormatter sdf2 = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm");
        try {
            selectedStartTime = sdf.parseDateTime(selectedBeginDate + " " + newFromTime).toDate();
            selectedEndTime = sdf.parseDateTime(selectedEndDate + " " + newToTime).toDate();
        }catch(IllegalArgumentException ie) {
            selectedStartTime = sdf2.parseDateTime(selectedBeginDate + " " + newFromTime).toDate();
            selectedEndTime = sdf2.parseDateTime(selectedEndDate + " " + newToTime).toDate();
        }
    }

    private static final DateTimeFormatter fdf  = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * return the session id from the sessiontxt
     * @param con
     * @return
     * @throws SQLException
     */
    private int getSessionId(Connection con) throws SQLException {
        // get the session id
        PreparedStatement pstmt2 = null;
        if (!StringUtils.isEmpty(sessionStart)) {
            DateTime sessionStartDateTime = fdf.parseDateTime(sessionStart);

            pstmt2 = con.prepareStatement("select session_id from sessions where sessiontxt = ? and sessionStartTime=?");
            pstmt2.setString(1,sessionTXT);
            pstmt2.setTimestamp(2, new Timestamp(sessionStartDateTime.getMillis()));
        } else {

            pstmt2 = con.prepareStatement("select max(session_id) as session_id from sessions where sessiontxt = ? ");
            pstmt2.setString(1,sessionTXT);
        }
        ResultSet rs4 = pstmt2.executeQuery();
        int sessionid = 0;

        if ( rs4.next()) {
            sessionid = rs4.getInt("session_id");
            System.out.println("getSessionId: " + sessionid);
        }
        try {
            rs4.close();
            pstmt2.close();
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return sessionid;
    }

    private void getSessionRecordCount(int queryType, Connection con) throws SQLException {
        System.out.println("queryis: " + buildNumSessionsQuery(queryType));
        // Retrieve number of sessions for this query
        PreparedStatement pstmtcount = null;

        Set<String> recordpks = null;
        String user_id = "";
        // Retrieve sessions
        if ( queryType == SEARCH_SESSIONS_BY_ADVANCED) {
            recordpks = getRecordPksForQueryParams();
            String newQuery = buildNumSessionsQuery(queryType);
            String question = "?,";
            String allQuestions = "";
            for(String param:recordpks) {
                allQuestions += question;
            }
            // trim off the last ,
            if (recordpks.size()>0) {
                allQuestions = allQuestions.substring(0,allQuestions.length()-1);
            } else {
                return;
            }
            newQuery = newQuery.replace("#RECORDPARAMS#",allQuestions);
            pstmtcount = con.prepareStatement( newQuery );

            System.out.println("new count query: " + newQuery);
        } else {
            pstmtcount = con.prepareStatement(buildNumSessionsQuery(queryType));
        }

        //pstmtcount = con.prepareStatement(buildNumSessionsQuery(queryType));

        // build the start and end date
        buildStartEndDate();

        if (queryType == SEARCH_SESSIONS_BY_USER) {
            System.out.println("userID: " + userID);
            pstmtcount.setString(1, userID);
            pstmtcount.setTimestamp(2, new java.sql.Timestamp(selectedStartTime.getTime()));
            pstmtcount.setTimestamp(3, new java.sql.Timestamp(selectedEndTime.getTime()));
        } else if ( queryType == SEARCH_SESSIONS_BY_ALL) {
            pstmtcount.setTimestamp(1, new java.sql.Timestamp(selectedStartTime.getTime()));
            pstmtcount.setTimestamp(2, new java.sql.Timestamp(selectedEndTime.getTime()));
        } else if ( queryType == SEARCH_SESSIONS_BY_ADVANCED) {
            pstmtcount.setTimestamp(1, new java.sql.Timestamp(selectedStartTime.getTime()));
            pstmtcount.setTimestamp(2, new java.sql.Timestamp(selectedEndTime.getTime()));
            // recordpk in clause
            int cc = 3;
            for(String param:recordpks) {
                pstmtcount.setInt(cc++,Integer.parseInt(param));
            }
        }

        ResultSet rscount = pstmtcount.executeQuery();
        if (rscount.next()) {
            sessionCount = rscount.getInt(1);
        }
        System.out.println("session count: " + sessionCount);
    }

    private void initialize() {
        //buildStartEndDate();

        // get click stream if sessiontxt is ! empty
        if (!StringUtils.isEmpty(sessionTXT)) {
            ClickStreamServletHelper cssh = new ClickStreamServletHelper(sessionTXT,sessionStart);
            sessionID = cssh.getSessionID();

            System.out.println("sessionID: " +sessionID);

            // If we have a sessionID, we can check if there are query parameters
            if (sessionID != -1) {
                System.out.println("getqueryparameters: ");
                queryParamsMap = cssh.getQueryParameters();
                //System.out.println("queryParamsMap: " + queryParamsMap);
            }

            //queryParamsMap = buildQueryParameters();
            clickStream = buildClickStream();
        }

        // if user is filled in
        System.out.println("use "+ (!StringUtils.isEmpty(userID) && sessionTXT.isEmpty()) );
        if (!StringUtils.isEmpty(userID) && sessionTXT.isEmpty()) {
            System.out.println("usersessions by user");
            userSessions = buildUserSessions(SEARCH_SESSIONS_BY_USER);
        }

        // if date is filled in
        System.out.println("date: " +(StringUtils.isEmpty(userID) && sessionTXT.isEmpty()
                && !selectedDate.isEmpty()));
        if (StringUtils.isEmpty(userID) && sessionTXT.isEmpty()
                && !selectedDate.isEmpty())
        {
                //&& (!fromTime.isEmpty() || !toTime.isEmpty())) {
            System.out.println("usersessions by all");
            userSessions = buildUserSessions(SEARCH_SESSIONS_BY_ALL);
            //System.out.println("usersessions: " + userSessions);
        }

        // if advanced field is filled in
        System.out.println("advanced: " + (StringUtils.isEmpty(userID) && StringUtils.isEmpty(sessionTXT) && !StringUtils.isEmpty(advancedSearchString)));
        if (StringUtils.isEmpty(userID) && StringUtils.isEmpty(sessionTXT) && !StringUtils.isEmpty(advancedSearchString)) {
            System.out.println("usersessions by advanced");
            userSessions = buildUserSessions(SEARCH_SESSIONS_BY_ADVANCED);
        }
        //System.out.println("initialize: userSessions " + userSessions);
    }

    public static String padRight(String s, int n) {
        return String.format("%1$-" + n + "s", s);
    }
    /**
     * Queries the database for a list of page views based on the sessionTXT being used. Each record will return a page ID, a
     * record ID, query parameters, and other relevant data. Each column of data from a record is then stored in a HashMap
     * called ClickElement. Then, the ClickElement is stored in a List of HashMaps called ClickElements. After all iterations
     * through the returned records are complete, the List of HashMaps is returned to the caller.
     *
     * @return ClickElements - The List of HashMaps which contains all page views for this session
     */
    private List buildClickStream() {
        // Hash Map to hold a single ClickElement
        HashMap<String, String> ClickElement = new HashMap<String, String>();

        // Array List to hold collection of ClickElements
        ArrayList<HashMap<String, String>> ClickElements = new ArrayList<HashMap<String, String>>();

        System.out.println("buildClickStream.buildStartEndDate");
        buildStartEndDate();

        PreparedStatement pstmt = null;
        ResultSet rs = null;
        Session session = null;
        Connection con = null;
        DateTimeFormatter timeformat = DateTimeFormat.forPattern("HH:mm:ss");

        try {
            session = HibernateUtil.currentSession();
            con = session.connection();

            pstmt = con.prepareStatement(PAGES_BY_HTML_QUERY);
            pstmt.setString(1, sessionTXT);
            System.out.println("pages query: " + PAGES_BY_HTML_QUERY);

            int sessionid = getSessionId(con);

            System.out.println("buildClickStream: " + sessionid);
            System.out.println(selectedStartTime);
            System.out.println(selectedEndTime);

            pstmt.setInt(2, sessionid);
            rs = pstmt.executeQuery();

            boolean hasHtmlID = false;

            while (rs.next()) {
                int HtmlPageResponse_ID = rs.getInt("HtmlPageResponse_ID");
                int requestToken = rs.getInt("requestToken");
                String pageName = rs.getString("pageName");
                Integer requestTokenCount = rs.getInt("requestTokenCount");
                int recordpk = rs.getInt("recordpk");
                Timestamp accessTime = rs.getTimestamp("time");
                String experience = rs.getString("experience");
                experience = ( experience!=null) ? experience.trim() : "";

                ClickElement.put("htmlPageID", String.valueOf(HtmlPageResponse_ID));
                ClickElement.put("requestToken", String.valueOf(requestToken));
                ClickElement.put("pageName", pageName);
                System.out.println("htmlPageID " + HtmlPageResponse_ID);
                System.out.println("requestTokenCount: " + requestTokenCount);
                ClickElement.put("requestTokenCount", requestTokenCount.toString());

                // setup lookup object
                ClickStreamServletHelper.HtmlLookupMap map = new ClickStreamServletHelper.HtmlLookupMap();

                map.setHtml_id(HtmlPageResponse_ID);
                map.setRecordpk(recordpk);
                map.setRequestToken(requestToken);

                String queryParamsAsString = queryParamsMap.get( map );

                ClickElement.put("queryParams", queryParamsAsString);

                DateTime accessDTTime = new DateTime(accessTime.getTime());
                ClickElement.put("time",timeformat.print(accessDTTime));
                ClickElement.put("experience",experience);

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

                pstmt = con.prepareStatement(PAGES_BY_ACCESS_RECORDS_QUERY);
                pstmt.setInt(1, sessionID);
                pstmt.setTimestamp(2, new java.sql.Timestamp(this.selectedStartTime.getTime()));
                pstmt.setTimestamp(3, new java.sql.Timestamp(this.selectedEndTime.getTime()));

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

                    /*
                     * Check if the current list of query parameter IDs is equal to the previously saved list. If so, that
                     * means the current click element has two recordpk's associated with it, and we ignore the duplicate set
                     * of query parameters.
                     */
                    if (!currQueryParamIDs.equals(prevQueryParamIDs)) {
                        ClickElement.put("pageName", pageName);
                        ClickElement.put("queryParams", (String) queryParamsMap.get(record));

                        // Add single ClickElement to collection of all ClickElements
                        ClickElements.add(ClickElement);

                        // Clear ClickElement container for next loop iteration
                        ClickElement = new HashMap<String, String>();
                    }
                    // clear prev
                    prevQueryParamIDs = new ArrayList<Integer>();

                    // copy curr to prev
                    for (Integer qpID : currQueryParamIDs) {
                        prevQueryParamIDs.add(qpID);
                    }

                    // clear curr
                    currQueryParamIDs = new ArrayList<Integer>();
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            try {
                pstmt.close();
            } catch (SQLException ex) {
                Logger.getLogger(ClickStreamServlet.class.getName()).log(Level.SEVERE, null, ex);
            }
            try {
                rs.close();
            } catch (SQLException ex) {
                Logger.getLogger(ClickStreamServlet.class.getName()).log(Level.SEVERE, null, ex);
            }
            try {
                HibernateUtil.closeSession();
            } catch (HibernateException e) {
            }
            try {
                con.close();
            } catch (SQLException ex) {
                Logger.getLogger(ClickStreamServlet.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        return ClickElements;
    }

    /**
     * get the record pks for a query param
     * @return
     */
    private Set<String> getRecordPksForQueryParams() {
        Session session = null;
        ResultSet rs = null;
        PreparedStatement pstmt = null;
        Connection con = null;
        HashSet<String> results = new HashSet<String>();

        try {
            System.out.println("getRecordPksForQueryParams");
            session = HibernateUtil.currentSession();
            con = session.connection();

            pstmt = con.prepareStatement(QUERY_RECORDPKS_FOR_QUERYPARAM);
            pstmt.setTimestamp(1, new java.sql.Timestamp(this.selectedStartTime.getTime()));
            pstmt.setTimestamp(2, new java.sql.Timestamp(this.selectedEndTime.getTime()));
            pstmt.setString(3,"%"+StringEscapeUtils.unescapeHtml4(advancedSearchString)+"%");
            System.out.println("Unescape: " + StringEscapeUtils.unescapeHtml4(advancedSearchString));
            rs = pstmt.executeQuery();

            while(rs.next()) {
                results.add(rs.getString("recordpk"));
            }
        } catch (SQLException ex) {
            Logger.getLogger(ClickStreamServlet.class.getName()).log(Level.SEVERE, null, ex);
        } catch (HibernateException ex) {
            Logger.getLogger(ClickStreamServlet.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                rs.close();
                pstmt.close();
                //con.close();
            } catch (SQLException ex) {
                Logger.getLogger(ClickStreamServlet.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return results;
    }

    private List buildUserSessions(int queryType) {
        List<HashMap<String, String>> userSessionsList = new ArrayList<HashMap<String, String>>();
        HashMap<String, String> userSession = new HashMap<String, String>();
        Session session = null;
        ResultSet rs = null;
        PreparedStatement pstmt = null;
        Connection con = null;
        String allQuestions = "";

        try {
            session = HibernateUtil.currentSession();
            con = session.connection();

            getSessionRecordCount(queryType, con);

            System.out.println(sessionCount);
            if (sessionCount > 0) {
                String user_id = "";
                Set<String> recordpks = null;
                // Retrieve sessions
                System.out.println("queryTYpe: " + queryType);
                if ( queryType == SEARCH_SESSIONS_BY_ADVANCED) {
                    recordpks = getRecordPksForQueryParams();
                    String newQuery = buildSessionQuery(queryType);
                    String question = "?,";
                    for(String param:recordpks) {
                        allQuestions += question;
                    }
                    // trim off the last ,
                    if ( allQuestions.length()>0) {
                        allQuestions = allQuestions.substring(0,allQuestions.length()-1);
                        newQuery = newQuery.replace("#RECORDPARAMS#",allQuestions);
                        pstmt = con.prepareStatement( newQuery );
                        System.out.println("new query: " + newQuery);
                    }

                } else {
                    pstmt = con.prepareStatement(buildSessionQuery(queryType));
                }

                if (queryType == SEARCH_SESSIONS_BY_USER) {
                    pstmt.setString(1, String.valueOf(userID));
                    pstmt.setTimestamp(2, new java.sql.Timestamp(selectedStartTime.getTime()));
                    pstmt.setTimestamp(3, new java.sql.Timestamp(selectedEndTime.getTime()));
                } else if ( queryType == SEARCH_SESSIONS_BY_ALL) {
                    System.out.println("search sessions by all "+selectedStartTime + " " + selectedEndTime);
                    pstmt.setTimestamp(1, new java.sql.Timestamp(selectedStartTime.getTime()));
                    pstmt.setTimestamp(2, new java.sql.Timestamp(selectedEndTime.getTime()));
                } else if (queryType == SEARCH_SESSIONS_BY_ADVANCED) {
                    if ( allQuestions.length()>0) {
                        //pstmt.setTimestamp(1, new java.sql.Timestamp(selectedStartTime.getTime()));
                        //pstmt.setTimestamp(2, new java.sql.Timestamp(selectedEndTime.getTime()));
                        //pstmt.setString(3, "%"+StringEscapeUtils.unescapeHtml4(advancedSearchString)+"%"); // query param search
                        pstmt.setTimestamp(1, new java.sql.Timestamp(selectedStartTime.getTime()));
                        pstmt.setTimestamp(2, new java.sql.Timestamp(selectedEndTime.getTime()));
                        // recordpk in clause
                        int cc = 3;
                        for(String param:recordpks) {
                            pstmt.setInt(cc++,Integer.parseInt(param));
                        }
                    }
                }

                if ( pstmt!=null) {
                    rs = pstmt.executeQuery();
                    //System.out.println("Execute query");
                    while (rs.next()) {
                        if ( queryType == SEARCH_SESSIONS_BY_ADVANCED) {
                            user_id = rs.getString("username");
                            userSession.put("user_id", user_id);
                        }
                        if (queryType == SEARCH_SESSIONS_BY_ALL) {
                            //System.out.println("get username");
                            user_id = rs.getString("username");
                            userSession.put("user_id", user_id);
                            //System.out.println("got username");
                        }

                        String sessiontxt = rs.getString("sessiontxt");
                        String startTime = rs.getTimestamp("sessionstarttime").toString();
                        String endTime = rs.getTimestamp("sessionendtime").toString();
                        String hits = String.valueOf(rs.getInt("sessionhits"));
                        String duration = String.valueOf(rs.getInt("sessionduration"));
                        String browsertype = rs.getString("browsertype");
                        int experience = rs.getInt("experience");

                        // Truncate milliseconds
                        startTime = startTime.split("\\.")[0];
                        endTime = endTime.split("\\.")[0];

                        userSession.put("session", sessiontxt);
                        userSession.put("starttime", startTime);
                        userSession.put("endtime", endTime);
                        userSession.put("sessionhits", hits);
                        userSession.put("sessionduration", duration);
                        userSession.put("browsertype", browsertype);
                        userSession.put("experience", String.valueOf(experience));

                        userSessionsList.add(userSession);
                        //System.out.println("build userSession " + userSession);
                        userSession = new HashMap<String, String>();
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if ( rs == null) {
                return userSessionsList;
            } else {
                try {
                    rs.close();
                } catch (SQLException ex) {
                    Logger.getLogger(ClickStreamServlet.class.getName()).log(Level.SEVERE, null, ex);
                }
                try {
                    pstmt.close();
                } catch (SQLException ex) {
                    Logger.getLogger(ClickStreamServlet.class.getName()).log(Level.SEVERE, null, ex);
                }
                try {
                    HibernateUtil.closeSession();

                } catch (HibernateException e) {
                }
                try {
                    con.close();
                }catch(Exception ex) {
                    Logger.getLogger(ClickStreamServlet.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }


        return userSessionsList;
    }

    /**
     * Builds a query string to be used to retrieve the number of sessions with the date and time appended to it.
     *
     * @return numSessionsQuery - The query string to retrieve the number of sessions
     */
    private String buildNumSessionsQuery(int queryType) {
        String numSessionsQuery = "";
        if (queryType == SEARCH_SESSIONS_BY_USER) {
            numSessionsQuery = SESSIONS_COUNT_BY_USER_QUERY;
        }
        if (queryType == SEARCH_SESSIONS_BY_ALL) {
            numSessionsQuery = SESSIONS_COUNT_BY_ALL_QUERY;
        }
        if (queryType == SEARCH_SESSIONS_BY_ADVANCED) {
            numSessionsQuery = SESSIONS_COUNT_BY_ADVANCED_QUERY;
        }

            	// If a date is selected, add it to query.
//    	if (!selectedDate.isEmpty()) {
//    		String newFromTime = this.fromTime,
//	    		   newToTime = this.toTime;
//
//    		// If a time is not specified, set a default time value.
//    		if (newFromTime.isEmpty()) newFromTime = "00:00:00";
//    		if (newToTime.isEmpty()) newToTime = "23:59:59";
//
////	    	query += " and sessionstarttime >= to_timestamp('" + selectedDate + " " + newFromTime + "', 'YYYY-MM-DD HH24:MI:SS')";
////	    	query += " and sessionstarttime <= to_timestamp('" + selectedDate + " " + newToTime + "', 'YYYY-MM-DD HH24:MI:SS')";
//    	}



        return numSessionsQuery;
    }

    /**
     * Builds a query string to be used to retrieve the sessions for a specified user ID.
     *
     * @return userIDQuery - The query string to retrieve the sessions for the specified user ID
     */
    private String buildSessionQuery(int searchType) {
        String query = "";
        if (searchType == SEARCH_SESSIONS_BY_USER) {
            query = SESSIONS_BY_USER_QUERY;
        }
        if (searchType == SEARCH_SESSIONS_BY_ALL) {
            query = SESSIONS_BY_ALL_QUERY;
        }
        if ( searchType == SEARCH_SESSIONS_BY_ADVANCED) {
            query = SESSIONS_BY_ADVANCED_QUERY;
        }

            	// If a date is selected, add it to query.
//    	if (!selectedDate.isEmpty()) {
//    		String newFromTime = this.fromTime,
//	    		   newToTime = this.toTime;
//
//    		// If a time is not specified, set a default time value.
//    		if (newFromTime.isEmpty()) newFromTime = "00:00:00";
//    		if (newToTime.isEmpty()) newToTime = "23:59:59";
//
////	    	query += " and sessionstarttime >= to_timestamp('" + selectedDate + " " + newFromTime + "', 'YYYY-MM-DD HH24:MI:SS')";
////	    	query += " and sessionstarttime <= to_timestamp('" + selectedDate + " " + newToTime + "', 'YYYY-MM-DD HH24:MI:SS')";
//    	}


        if (sessionOffset.equals("")) {
            query += " LIMIT " + MAX_SESSION_ROWS;
        } else {
            query += " OFFSET " + sessionOffset + " LIMIT " + MAX_SESSION_ROWS;
        }

        return query;
    }

    /**
     * @return The row offset that was used when retrieving sessions for a single user ID
     */
    public int getSessionOffset() {
        int offset = 0;
        try {
            offset = Integer.parseInt(sessionOffset);
        } catch (NumberFormatException nfe) {
        }

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
        if (clickStream != null) {
            return clickStream;
        } else {
            return new ArrayList<HashMap<String, String>>();
        }
    }
    private static String AMP_SEP = "&";
    private static String AMP_ESCAPED = "&amp;";

    public HashSet<String> processQueryParameters(String queryParameters) {
        HashSet<String> set = new HashSet();
        if (queryParameters != null) {
            int seplength = AMP_SEP.length();
            int sep = 0, asep = 0;
            int start = 0;
            while (queryParameters != null) {
                if ((sep = queryParameters.indexOf(AMP_SEP, start)) > -1) {
                    asep = queryParameters.indexOf(AMP_ESCAPED, start);
                    if (asep == sep) {
                        // this was a encoded amp and not just a &
                        start = asep + AMP_ESCAPED.length();
                        continue; // skip this & because it's an encoded amp;
                    }
                    seplength = AMP_SEP.length();
                    String currentParameter = queryParameters.substring(0, sep);
                    queryParameters = queryParameters.substring(sep + seplength);
                    //if (!"".equals(currentParameter)) {
                    if (!StringUtils.isEmpty(currentParameter)) {
                        set.add(currentParameter);
                    }
                    start = 0;
                } else {
                    String currentParameter = queryParameters;
                    queryParameters = null;
                    set.add(currentParameter);
                }
            }
        }
        return set;
    }

    public Map<String,String> getCustomerInfoByQueryParameters(String queryparams) {
        System.out.println("queryparams are: " + queryparams);
        HashSet<String> pset = processQueryParameters(queryparams);

        String sessionid = "";

        for(String parameter:pset) {
            if ( parameter.indexOf("header.cookie.JSESSIONID")!=-1) {
                //System.out.println("queryparams jsessionid is: " + parameter);
                sessionid = parameter.substring(parameter.indexOf("=")+1);
                break;
            }
        }
        return getCustomerInfoBySessionTxt(sessionid);
    }

    public Map<String,String> getCustomerInfoBySessionTxt(String sessionid) {
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        Session session = null;
        Connection con = null;
        HashMap<String,String> customerData = new HashMap<String,String>();
        DateTimeFormatter datetime_format = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss a");

        try {
            session = HibernateUtil.currentSession();
            con = session.connection();

            //pstmt = con.prepareStatement("select *,u.username from sessions s,users u where s.sessiontxt = ? and s.user_id = u.user_id");
            pstmt = con.prepareStatement("select *,u.username from sessions s,users u where s.user_id = u.user_id and s.session_id = (select max(session_id) from sessions where sessiontxt = ? )");
            pstmt.setString(1,sessionid);
            //System.out.println("sessionid: " + sessionid);

            rs = pstmt.executeQuery();

            if (rs.next()) {
                //System.out.println("in resultset: " + rs);
                customerData.put("IP Address",rs.getString("ipaddress"));
                customerData.put("Browser Type",rs.getString("browsertype"));
                customerData.put("Profile",rs.getString("username"));
                int hits = Integer.parseInt(rs.getString("sessionhits"));
                customerData.put("Number of Clicks",String.valueOf(hits));
                int duration = Integer.parseInt(rs.getString("sessionduration"));
                customerData.put("Session Duration Secs",String.valueOf(duration/1000));
                Timestamp starttime = rs.getTimestamp("sessionstarttime");
                DateTime dt_starttime = new DateTime(starttime.getTime());
                String str_starttime = datetime_format.print(dt_starttime);
                customerData.put("Session Start Time",str_starttime);
                customerData.put("Session Id",sessionid);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            try {
                rs.close();
            } catch (SQLException ex) {
                Logger.getLogger(ClickStreamServlet.class.getName()).log(Level.SEVERE, null, ex);
            }
            try {
                pstmt.close();
            } catch (SQLException ex) {
                Logger.getLogger(ClickStreamServlet.class.getName()).log(Level.SEVERE, null, ex);
            }
            try {
                HibernateUtil.closeSession();
            } catch (HibernateException e) {
            }
            try {
                con.close();
            } catch (SQLException ex) {
                Logger.getLogger(ClickStreamServlet.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        return customerData;
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
