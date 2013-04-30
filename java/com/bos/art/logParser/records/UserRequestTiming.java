/*
 * Created on Oct 21, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package com.bos.art.logParser.records;
import com.bos.art.logParser.db.AccessRecordPersistanceStrategy;
import com.bos.art.logParser.db.PersistanceStrategy;
import java.io.Serializable;
import java.util.Calendar;
import org.apache.log4j.Logger;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
/**
 *
 * @author I0360D3 To change the template for this generated type comment go to
 *
 * Window>Preferences>Java>Code Generation>Code and Comments
 *
 */
public class UserRequestTiming extends UserRequestEventDesc implements Serializable, ILiveLogParserRecord {
    private static final String INDEXJSP = "/index.jsp";
    private static final String NAS_APP = "NASApp";
    private static final String NAS_APP_BOISEOP = "NASApp/boiseop";
    private static final String PRODUCT_GROUP_XM_LSHOPPRODUCT_GROUPXSL = "ProductGroupXML:shop/productGroup.xsl";
    private static final String SHOP_ORDER_XM_LSHOPEDIT_ORDERXSL = "shop.OrderXML:shop/editOrder.xsl";
    private static final String SLASH = "/";
    private static final Logger logger = (Logger) Logger.getLogger(UserRequestTiming.class.getName());
    private static DateTimeFormatter sdfDate = DateTimeFormat.forPattern("MM/dd/yyyy");        
    private static DateTimeFormatter sdfTime = DateTimeFormat.forPattern("hh:mm:ss aa");
    private static final String AMPH = "%26";
    private static final String AMP = "&";
    private static final int PAGE_NAME_MAXLENGTH = 255;
    private static final String SEMIl = "%3b";
    private static final String SEMIu = "%3B";
    private static final String QUESTION = "?";
    private static final String JSESSIONID = ";jsessionid=";
    
    private String page;
    private int loadTime;
    private String sessionId;
    private String ipAddress;
    private String userKey;
    private String browser;
    private boolean begin;
    private String queryParams;
    private long requestEndTime;
    private long tokenCreationTime;
    private int requestType = 0;
    private int requestToken;
    // Foreign Keys
    transient private AccessRecordsForeignKeys foreignKeys;
    transient private PersistanceStrategy pStrat;
    
    
    public UserRequestTiming() {
        super();
        setPriority(20);
        browser = "";
    }
    
    public UserRequestTiming(String type, int priority, String eventID, String appName, String serverName, String context,
            String page, java.util.Calendar date, int loadTime, String sessiontxt, String ipaddress,
            String userKey, String browser,String instanceName) {
        super();
        this.setEventId(eventID);
        this.setAppName(appName);
        this.setServerName(serverName);
        this.setInstance(instanceName);
        this.setContext(context);
        this.setPage(page);
        this.setEventTime(date);
        
        this.setDate(sdfDate.print(date.getTime().getTime()));
        this.setTime(sdfTime.print(date.getTime().getTime()));
        this.setLoadTime(loadTime);
        this.setSessionId(sessiontxt);
        this.setIpAddress(ipaddress);
        this.setUserKey(userKey);
        this.setBrowser(browser);
    }
    
    public AccessRecordsForeignKeys obtainForeignKeys() {
        if (foreignKeys == null) {
            foreignKeys = new AccessRecordsForeignKeys(this.obtainEventTime().getTime());
        }
        return foreignKeys;
    }
    
    public boolean isAccessRecord() {
        return true;
    }
    /*
     * (non-Javadoc)
     *
     * @see com.bos.art.logParser.records.LiveLogParserRecord#obtainPriority()
     *
     */
    public int obtainPriority() {
        return getPriority();
    }
    /*
     * (non-Javadoc)
     *
     * @see com.bos.art.logParser.records.LiveLogParserRecord#obtainEventTime()
     *
     */
    public Calendar obtainEventTime() {
        return getEventTime();
    }
    /*
     * (non-Javadoc)
     *
     * @see com.bos.art.logParser.records.LiveLogParserRecord#obtainStringForComparison()
     *
     */
    public String obtainStringForComparison() {
        return toString();
    }
    public boolean getBegin() {
        return begin;
    }
    public void setBegin(boolean begin) {
        this.begin = begin;
    }
    /**
     * @return
     */
    public String getBrowser() {
        return browser;
    }
    /**
     * @return
     */
    public String getIpAddress() {
        return ipAddress;
    }
    /**
     * @return
     */
    public int getLoadTime() {
        return loadTime;
    }
    /**
     * @return
     */
    public String getPage() {
        return page;
    }
    /**
     * @return
     */
    public String getSessionId() {
        return sessionId;
    }
    /**
     * @return
     */
    public String getUserKey() {
        return userKey;
    }
    /**
     * @param string
     */
    public void setBrowser(String string) {
        browser = string;
    }
    /**
     * @param string
     */
    public void setIpAddress(String string) {
        ipAddress = string;
    }
    /**
     * @param i
     */
    public void setLoadTime(int i) {
        loadTime = i;
        // System.out.println("elapsed time:" +i);
    }
    
    public void setQueryParams(String params) {
        //System.out.println("params: " +params);
        queryParams = params;
    }
    
    public String getQueryParams() {
        return queryParams;
    }
    
    private String massagePageName(String pn) {
        if ( "".equals(pn) ) {
            pn = INDEXJSP;
        }
        // do some massage on pagename;
        int chop = 0;
        if (pn !=null && (chop = pn.indexOf(JSESSIONID))!=-1) {
            pn = pn.substring(0,chop);
        }
        if ( pn!=null && (chop = pn.indexOf(AMPH))!=-1) {
            pn = pn.substring(0,chop);
        }
        if ( pn!=null && (chop = pn.indexOf(AMP))!=-1) {
            pn = pn.substring(0,chop);
        }
        if ( pn!=null && (chop = pn.indexOf(SEMIl))!=-1) {
            pn = pn.substring(0,chop);
        }
        if ( pn!=null && (chop = pn.indexOf(SEMIu))!=-1) {
            pn = pn.substring(0,chop);
        }
        if ( pn!=null && (chop = pn.indexOf(QUESTION))!=-1) {
            pn = pn.substring(0,chop);
        }
        if ( pn.length()>PAGE_NAME_MAXLENGTH) {
            pn = pn.substring(0,PAGE_NAME_MAXLENGTH);
        }
        return pn;
    }

    
    /**
     * @param string
     */
    public void setPage(String string) {
        page = string;
        //String context = page.substring(1,page.indexOf("/",1));
        //System.out.println("context: " + context);
        //NASApp
        if (page == null || page.length() == 0) {
            // ignore this type of record, probably a TimingStopWatch event
            return;
        }
        
        //page = massagePageName(page);
        try {
            int startPos = 0;
            if (page.charAt(0) == '/') {
                startPos = 1;
            }
            int endPos = page.indexOf(SLASH, startPos);
            String mycontext = null;;
            if (page.equals(SHOP_ORDER_XM_LSHOPEDIT_ORDERXSL) || page.equals(PRODUCT_GROUP_XM_LSHOPPRODUCT_GROUPXSL)) {
                // if we see these pages we know the context is NASApp/boiseop
                mycontext = NAS_APP_BOISEOP;
            } else {
                if ( endPos>startPos) {
                    mycontext = page.substring(startPos, endPos);
                    if (mycontext.equals(NAS_APP)) {
                        mycontext += SLASH + page.substring(endPos + 1, page.indexOf(SLASH, endPos + 1));
                        //System.out.println("context is: " + mycontext);
                    }
                }
                // should be left with ,shop or ,NASApp/boiseop, or integration
                //System.out.println("page is : " + page);
            }
            if ( page == null) {
                page = "unknown";                
            } else {
                page = page.substring(page.indexOf(mycontext) + mycontext.length() + 1);
            }
            setContext(mycontext);
        } catch (Exception e) {
            setContext("Exception_Getting Context");
            logger.error("Exception Getting Context ", e);
        }
    }
    /**
     * @param string
     */
    public void setSessionId(String string) {
        sessionId = string;
    }
    
    /**
     * @param string
     */
    public void setUserKey(String string) {
        userKey = string;
    }
    
    public void copyFrom(UserRequestTiming desc) {
        super.copyFrom(desc);
        page = desc.getPage();
        loadTime = desc.getLoadTime();
        sessionId = desc.getSessionId();
        ipAddress = desc.getIpAddress();
        userKey = desc.getUserKey();
        browser = desc.getBrowser();
        begin = desc.getBegin();
        queryParams = desc.getQueryParams();
        requestEndTime = desc.getRequestEndTime();
        tokenCreationTime = desc.getTokenCreationTime();
        requestType = desc.getRequestType();
        requestToken = desc.getRequestToken();
    }
    
    @Override
    public String toString() {
        //.append(date).append(time)
        StringBuilder sb = new StringBuilder();
        sb.append(super.toString());
        sb.append("\nUserRequestTiming: ").append(": ").
                append(page).append(" ").append(begin).append(" ").append(loadTime).append(" ").append(sessionId).append(" ").append(ipAddress).
                append(userKey).append("\n").append(browser);
        sb.append(":").append(queryParams).append(":").append(tokenCreationTime).append(":").append(requestType).append(":").append(requestToken);
        return sb.toString();
    }
    /*
     * (non-Javadoc)
     *
     * @see com.bos.art.logParser.records.LiveLogParserRecord#getRemoteHost()
     *
     */
    public String getRemoteHost() {
        // TODO Auto-generated method stub
        return null;
    }
    /*
     * (non-Javadoc)
     *
     * @see com.bos.art.logParser.records.LiveLogParserRecord#isErrorPage()
     *
     */
    public boolean isErrorPage() {
        // TODO Auto-generated method stub
        return false;
    }
    /*
     * (non-Javadoc)
     *
     * @see com.bos.art.logParser.records.LiveLogParserRecord#isFirstTimeUser()
     *
     */
    public boolean isFirstTimeUser() {
        // TODO Auto-generated method stub
        return false;
    }
    /*
     * (non-Javadoc)
     *
     * @see com.bos.art.logParser.records.ILiveLogParserRecord#writeToDatabase()
     *
     */
    public boolean writeToDatabase() {
        try {
            if (pStrat == null) {
                pStrat = AccessRecordPersistanceStrategy.getInstance();
            }
            return pStrat.writeToDatabase(this);
        } catch (Exception e) {
            logger.warn("Trying to Write... " + this.toString());
            logger.warn("Exception ... " + this.getClass().getName(), e);
        }
        return false;
    }
    /*
     * (non-Javadoc)
     *
     * @see com.bos.art.logParser.records.ILiveLogParserRecord#isExternalAccessEvent()
     *
     */
    public boolean isExternalAccessEvent() {
        return false;
    }
    /*
     * (non-Javadoc)
     *
     * @see com.bos.art.logParser.records.ILiveLogParserRecord#isAccumulatorEvent()
     *
     */
    public boolean isAccumulatorEvent() {
        // TODO Auto-generated method stub
        return false;
    }
    /**
     *
     * @return
     *
     */
    public long getRequestEndTime() {
        return requestEndTime;
    }
    /**
     *
     * @return
     *
     */
    public int getRequestToken() {
        return requestToken;
    }
    /**
     *
     * @return
     *
     */
    public int getRequestType() {
        return requestType;
    }
    /**
     *
     * @return
     *
     */
    public long getTokenCreationTime() {
        return tokenCreationTime;
    }
    /**
     *
     * @param l
     *
     */
    public void setRequestEndTime(long l) {
        requestEndTime = l;
    }
    /**
     *
     * @param string
     *
     */
    public void setRequestToken(int i) {
        requestToken = i;
    }
    /**
     *
     * @param string
     *
     */
    public void setRequestType(int i) {
        requestType = i;
    }
    /**
     *
     * @param l
     *
     */
    public void setTokenCreationTime(long l) {
        tokenCreationTime = l;
    }
}
