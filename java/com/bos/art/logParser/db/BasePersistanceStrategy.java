/*
 * Created on Jul 7, 2004
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package com.bos.art.logParser.db;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.log4j.Logger;


/**
 * @author I0360D3
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public abstract class BasePersistanceStrategy {
    public static HashMap sequenceNameHashMap = null;
    public static final String FK_BRANCH_INSERT = "insert into Branches (branchName) values (?)";
    public static final String FK_BRANCH_SELECT = "select Branch_Tag_ID from Branches where branchName = ?";
    public static final String FK_APP_INSERT = "insert into Apps (appName) values (?)";
    public static final String FK_APP_SELECT = "select App_ID from Apps where appName = ?";
    public static final String FK_PAGES_INSERT = "insert into Pages (pageName) values (?)";
    public static final String FK_PAGES_SELECT = "select Page_ID from Pages where pageName = ?";
    public static final String FK_MACHINES_INSERT = "insert into Machines (MachineName) values (?)";
    public static final String FK_MACHINES_SELECT = "select Machine_ID from Machines where MachineName = ?";
    public static final String FK_INSTANCES_SELECT = "select Instance_ID from Instances where InstanceName = ?";
    public static final String FK_INSTANCES_INSERT = "insert into Instances (InstanceName) values (?)";
    public static final String FK_SESSIONS_INSERT = "insert into Sessions (IPAddress, sessionTXT, browserType, User_ID ) values (?,?,?,?)";
    public static final String FK_QUERY_PARAMETERS_INSERT = "insert into QueryParameters (queryParams) values (?)";
    public static final String FK_QUERY_PARAMETERS_SELECT = "select QueryParameter_ID from QueryParameters where queryParams=?";
    public static final String FK_USERS_INSERT = "insert into Users (userName) values (?)";
    public static final String FK_USERS_SELECT = "select User_ID from Users where userName = ?";
    public static final String FK_CONTEXTS_INSERT = "insert into Contexts (ContextName) values (?)";
    public static final String FK_CONTEXTS_SELECT = "select Context_ID from Contexts where contextName = ?";
    public static final String FK_STACKTRACEROWS_INSERT = "insert into StackTraceRows (row_message) values(?)";
    public static final String FK_STACKTRACEROWS_SELECT = "select row_id from StackTraceRows where row_message=?";
    private static final String BROWSER = "#BROWSER#";
    private static final String IPADDRESS = "#IPADDRESS#";
    private static final String USERID = "#USERID#";

    protected int contextRead;
    protected int contextWrite;
    public static HashMap databaseMisHashtable = new HashMap();
    public static HashMap databaseMisXRef = new HashMap();
    
    protected static final Logger logger = (Logger) Logger.getLogger(BasePersistanceStrategy.class.getName());
    public static final int DATABASE_MISS_THRESHOLD = 1000000;
    
    static {
        sequenceNameHashMap = new HashMap();
        sequenceNameHashMap.put(FK_BRANCH_INSERT, "branches_branch_tag_id_seq");
        sequenceNameHashMap.put(FK_APP_INSERT, "apps_app_id_seq");
        sequenceNameHashMap.put(FK_PAGES_INSERT, "pages_page_id_seq");
        sequenceNameHashMap.put(FK_MACHINES_INSERT, "machines_machine_id_seq");
        sequenceNameHashMap.put(FK_INSTANCES_INSERT, "instances_instance_id_seq");
        sequenceNameHashMap.put(FK_SESSIONS_INSERT, "sessions_session_id_seq");
        sequenceNameHashMap.put(FK_QUERY_PARAMETERS_INSERT, "queryparameters_queryparamet");
        sequenceNameHashMap.put(FK_USERS_INSERT, "users_user_id_seq");
        sequenceNameHashMap.put(FK_CONTEXTS_INSERT, "contexts_context_id_seq");
        sequenceNameHashMap.put(FK_STACKTRACEROWS_INSERT, "stacktracerows_row_id_seq");

        databaseMisXRef.put(ForeignKeyStore.FK_BRANCH_TAG_ID, FK_BRANCH_SELECT);
        databaseMisXRef.put(ForeignKeyStore.FK_CONTEXTS_CONTEXT_ID, FK_CONTEXTS_SELECT);
        databaseMisXRef.put(ForeignKeyStore.FK_DEPLOYEDAPPS_APP_ID, FK_APP_SELECT);
        databaseMisXRef.put(ForeignKeyStore.FK_MACHINES_MACHINE_ID, FK_MACHINES_SELECT);
        databaseMisXRef.put(ForeignKeyStore.FK_INSTANCES_INSTANCE_ID, FK_INSTANCES_SELECT);
        databaseMisXRef.put(ForeignKeyStore.FK_PAGES_PAGE_ID, FK_PAGES_SELECT);
        databaseMisXRef.put(ForeignKeyStore.FK_QUERY_PARAMETER_ID, FK_QUERY_PARAMETERS_SELECT);
        databaseMisXRef.put(ForeignKeyStore.FK_USERS_USER_ID, FK_USERS_SELECT);
        databaseMisXRef.put(ForeignKeyStore.FK_STACKTRACEROW_ID, FK_STACKTRACEROWS_SELECT);
    }
    
    protected int insertForeignKey(String sqlInsert, List insertValues, Connection con) throws SQLException {
        PreparedStatement pstmt = con.prepareStatement(sqlInsert);
        int resultValue = 0;

        try {
            for (int i = 0,tot = insertValues.size(); i < tot; ++i) {
                Object o = insertValues.get(i);

                if (o instanceof String) {
                    pstmt.setString(i + 1, (String) o);
                } else if (o instanceof Integer) {
                    pstmt.setInt(i + 1, ((Integer) o).intValue());
                } else if (o instanceof java.util.Date){
                    java.util.Date d = (java.util.Date) o;
                    pstmt.setTimestamp(i+1, new java.sql.Timestamp( d.getTime() ));
                } else {
                    logger.error("insertForeignKey unknown type: " + o.getClass().getName() + " for param " + (i+1),new Exception() );
                }
            }
            if (pstmt.executeUpdate() > 0) {
                ++contextWrite;
                String seqName = (String) sequenceNameHashMap.get(sqlInsert);

                if (seqName != null) {
                    resultValue = selectLastInsert(con, seqName);
                } 
            }
        }
        finally {
            pstmt.close();
        }
        return resultValue;
    }
    
    protected int insertForeignKey(String sqlSelect, List selectValues, String sqlInsert, List insertValues) {
        Connection con = null;
        int resultValue = 0;
        Integer integerSelectMis = (Integer) databaseMisHashtable.get(sqlSelect);

        if (integerSelectMis == null) {
            integerSelectMis = new Integer(0);
            databaseMisHashtable.put(sqlSelect, integerSelectMis);
        }
        int selectMis = integerSelectMis.intValue();
        int retries = 0;
        boolean escapeString = false;

        while(retries++<5) {
            try {
                con = ConnectionPoolT.getConnection();
                if (selectMis < DATABASE_MISS_THRESHOLD) {
                    PreparedStatement pstmt = con.prepareStatement(sqlSelect);

                    for (int i = 0,tot = selectValues.size(); i < tot; ++i) {
                        Object o = selectValues.get(i);                        

                        if (o instanceof String) {
                            // I don't want to always escape by default, because the overhead,
                            // so we are only doing it if we get a error in the catch block below,
                            // then set the escapeString value to true
                            if ( escapeString ) {
                                //String input = (String)o;
                                String input = String.valueOf(o);
                                input = StringEscapeUtils.escapeJava(input);
                                selectValues.set(i, input);
                                insertValues.set(i, input);
                                pstmt.setString(i + 1, input);
                            } else
                                pstmt.setString(i + 1, String.valueOf(o));
                        } else if (o instanceof Integer) {
                            pstmt.setInt(i + 1, ((Integer) o).intValue());
                        } else {
                            logger.error("insertForeignKey  unknown type: " + o.getClass().getName(),new Exception() );
                        }
                    }
                    ResultSet rs = pstmt.executeQuery();

                    if (rs.next()) {
                        ++contextRead;
                        resultValue = rs.getInt(1);
                        rs.close();
                        pstmt.close();
                        selectMis = 0;
                    } else {
                        ++selectMis;
                        rs.close();
                        pstmt.close();
                        resultValue = insertForeignKey(sqlInsert, insertValues, con);
                    }
                    databaseMisHashtable.put(sqlSelect, new Integer(selectMis));
                } else {
                    resultValue = insertForeignKey(sqlInsert, insertValues, con);
                }
            } catch (SQLException se) {                
                logger.error("SQLException ", se);
                if ( se.getMessage().contains("duplicate key")) {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException ex) {
                        java.util.logging.Logger.getLogger(BasePersistanceStrategy.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    logger.error("insertForeignKey: Duplicate key violation will retry");
                    try {
                        if ( con!=null) con.close();
                        con = null;
                    } catch (Throwable t) {
                        logger.error("Throwable ", t);
                    }
                    continue;
                } else if(se.getMessage().contains("invalid byte")) {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException ex) {
                        java.util.logging.Logger.getLogger(BasePersistanceStrategy.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    logger.error("insertForeignKey: invalid byte sequence, for select: [" + sqlSelect + "], will retry");
                    try {
                        if ( con!=null) con.close();
                        con = null;
                    } catch (Throwable t) {
                        logger.error("Throwable ", t);
                    }
                    escapeString = true;
                    continue;
                } else {
                    logger.error("****Breaking out of insertForeignKey for select: [" + sqlSelect + "] ****");
                    break;
                }
            }
            finally {
                try {
                    if ( con!=null) con.close();
                    con = null;
                } catch (Throwable t) {
                    logger.error("Throwable ", t);
                }
            }
        }
        return resultValue;
    }

    protected int insertContext(String contextValue) {
        String sqlSelect = FK_CONTEXTS_SELECT;
        String sqlInsert = FK_CONTEXTS_INSERT;
        List bindParams = new ArrayList();

        bindParams.add(contextValue);
        return insertForeignKey(sqlSelect, bindParams, sqlInsert, bindParams);
    }

    protected int selectLastInsert(Connection con, String seqName) throws SQLException {
        PreparedStatement pstmt2 = con.prepareStatement("select currval(?)");

        pstmt2.setString(1, seqName);
        ResultSet rs2 = pstmt2.executeQuery();
        int resultVal = 0;

        if (rs2.next()) {
            resultVal = rs2.getInt(1);
        }
        rs2.close();
        pstmt2.close();
        return resultVal;
    }

    protected int insertUser(String userKey) {
        String sqlSelect = FK_USERS_SELECT;
        String sqlInsert = FK_USERS_INSERT;
        List bindParams = new ArrayList();

        bindParams.add(userKey);
        return insertForeignKey(sqlSelect, bindParams, sqlInsert, bindParams);
    }
    
    public static void main(String []args) {
        String queryParameter = "ProfileFormHandler.email.lastName=smith";
                        int start = 0,equalSign;
        if ( (start = queryParameter.indexOf("ProfileFormHandler"))!=-1) {
            // now find the equal sign
            equalSign = queryParameter.indexOf('=', start);
            queryParameter = queryParameter.substring(0,equalSign);
            queryParameter += "=wiped";
        }
    System.out.println("queryParameter: " + queryParameter);
    }
    
    protected int insertQueryParameter(String queryParameter) {
        String sqlSelect = FK_QUERY_PARAMETERS_SELECT;
        String sqlInsert = FK_QUERY_PARAMETERS_INSERT;
        List bindParams = new ArrayList();

        /*
         *       "password": null, \n\
      "/atg/userprofiling/ProfileFormHandler.value.password": null, \n\
      "/atg/userprofiling/ProfileFormHandler.oldPassword": null, \n\
      "/atg/userprofiling/ProfileFormHandler.value.confirmPassword": null, \n\

         */
        
        int start = 0,equalSign;
        

        if ( (start = queryParameter.indexOf("Password"))!=-1) {
            // now find the equal sign
            equalSign = queryParameter.indexOf('=', start);
            if (equalSign !=-1) {
                queryParameter = queryParameter.substring(0,equalSign);
                queryParameter += "=wiped";
            }
        }       
        if ( (start = queryParameter.indexOf("password"))!=-1) {
            // now find the equal sign
            equalSign = queryParameter.indexOf('=', start);
            if (equalSign !=-1) {
                queryParameter = queryParameter.substring(0,equalSign);
                queryParameter += "=wiped";
            }
        }       

        bindParams.add(queryParameter);
        return insertForeignKey(sqlSelect, bindParams, sqlInsert, bindParams);
    }

    protected int insertStackTraceRow(String queryParameter) {
        String sqlInsert = FK_STACKTRACEROWS_INSERT;// = "insert into StackTraceRows (row_message) values(?)";
        String sqlSelect  = FK_STACKTRACEROWS_SELECT;// = "select row_id from StackTraceRows where row_message=?";
        List bindParams = new ArrayList();

        bindParams.add(queryParameter);
        return insertForeignKey(sqlSelect, bindParams, sqlInsert, bindParams);
    }


    protected int insertSession(String sessiontxt, String ip, String browser, int userID) {
        String sqlInsert = FK_SESSIONS_INSERT;
        List bindParams = new ArrayList();

        bindParams.add(ip);
        bindParams.add(sessiontxt);
        bindParams.add(browser);
        bindParams.add(new Integer(userID));
        Connection con = null;
        int resultValue = 0;

        try {
            con = ConnectionPoolT.getConnection();
            resultValue = insertForeignKey(sqlInsert, bindParams, con);
        } catch (SQLException se) {
            logger.error("SQLException", se);
        }
        finally {
            try {
                if (con != null) {
                    con.close();
                    con = null;
                }
            } catch (Throwable t) {
                logger.error("Throwable", t);
            }
        }
        return resultValue;
    }

    protected int insertInstance(String instanceValue) {
        String sqlSelect = FK_INSTANCES_SELECT;
        String sqlInsert = FK_INSTANCES_INSERT;
        List bindParams = new ArrayList();

        bindParams.add(instanceValue);
        return insertForeignKey(sqlSelect, bindParams, sqlInsert, bindParams);
    }
    
    protected int insertMachine(String machineValue) {
        String sqlSelect = FK_MACHINES_SELECT;
        String sqlInsert = FK_MACHINES_INSERT;
        List bindParams = new ArrayList();

        bindParams.add(machineValue);
        return insertForeignKey(sqlSelect, bindParams, sqlInsert, bindParams);
    }

    protected int insertPage(String pageName) {
        String sqlSelect = FK_PAGES_SELECT;
        String sqlInsert = FK_PAGES_INSERT;
        List bindParams = new ArrayList();

        bindParams.add(pageName);
        return insertForeignKey(sqlSelect, bindParams, sqlInsert, bindParams);
    }

    protected int insertApp(String appName) {
        String sqlSelect = FK_APP_SELECT;
        String sqlInsert = FK_APP_INSERT;
        List bindParams = new ArrayList();

        bindParams.add(appName);
        return insertForeignKey(sqlSelect, bindParams, sqlInsert, bindParams);
    }

    protected int insertBranch(String appName) {
        String sqlSelect = FK_BRANCH_SELECT;
        String sqlInsert = FK_BRANCH_INSERT;
        List bindParams = new ArrayList();

        bindParams.add(appName);
        return insertForeignKey(sqlSelect, bindParams, sqlInsert, bindParams);
    }

    public int writeForeignKey(String foreignKeyName, String foreignKeyValue) {
        if (foreignKeyName.equals(ForeignKeyStore.FK_CONTEXTS_CONTEXT_ID)) {
            return insertContext(foreignKeyValue);
        } else if (foreignKeyName.equals(ForeignKeyStore.FK_USERS_USER_ID)) {
            return insertUser(foreignKeyValue);
        } else if (foreignKeyName.equals(ForeignKeyStore.FK_SESSIONS_SESSION_ID)) {
            int startIPAddress = foreignKeyValue.indexOf(IPADDRESS);
            int startBrowserType = foreignKeyValue.indexOf(BROWSER);
            int startUserID = foreignKeyValue.indexOf(USERID);
            String ip = foreignKeyValue.substring(startIPAddress + IPADDRESS.length(), startBrowserType);
            String browser = foreignKeyValue.substring(startBrowserType + BROWSER.length(), startUserID);

            if (startIPAddress > 255) {
                logger.warn("The Session Text is Longer that 255 chars sessionTXT=> " + foreignKeyValue.substring(0, startIPAddress));
                startIPAddress = 254;
            }
            String sessiontxt = foreignKeyValue.substring(0, startIPAddress);
            int userID = Integer.parseInt(foreignKeyValue.substring(startUserID + 8));

            if (browser != null && browser.length() > 254) {
                logger.warn("The Browser Text is Longer that 255 chars BrowerTXT=> " + browser);
                browser = browser.substring(0, 254);
            }
            return insertSession(sessiontxt, ip, browser, userID);
        } else if (foreignKeyName.equals(ForeignKeyStore.FK_MACHINES_MACHINE_ID)) {
            return insertMachine(foreignKeyValue);
        } else if (foreignKeyName.equals(ForeignKeyStore.FK_INSTANCES_INSTANCE_ID)) {
            return insertInstance(foreignKeyValue);
        } else if (foreignKeyName.equals(ForeignKeyStore.FK_PAGES_PAGE_ID)) {
            return insertPage(foreignKeyValue);
        } else if (foreignKeyName.equals(ForeignKeyStore.FK_DEPLOYEDAPPS_APP_ID)) {
            return insertApp(foreignKeyValue);
        } else if (foreignKeyName.equals(ForeignKeyStore.FK_BRANCH_TAG_ID)) {
            return insertBranch(foreignKeyValue);
        } else if (foreignKeyName.equals(ForeignKeyStore.FK_QUERY_PARAMETER_ID)) {
            return insertQueryParameter(foreignKeyValue);
        } else if (foreignKeyName.equals(ForeignKeyStore.FK_STACKTRACEROW_ID)){
            return insertStackTraceRow(foreignKeyValue);
        } else {
            // TODO: Log Event Types.
            return 0;
        }
    }
    
}