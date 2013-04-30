/*
 * Created on Dec 12, 2003
 * 
 * To change the template for this generated file go to Window - Preferences -
 * Java - Code Generation - Code and Comments
 */
package com.bos.helper;

import EDU.oswego.cs.dl.util.concurrent.BrokenBarrierException;
import EDU.oswego.cs.dl.util.concurrent.CyclicBarrier;
import EDU.oswego.cs.dl.util.concurrent.TimeoutException;
import com.bos.arch.HibernateUtil;
import com.bos.art.model.jdo.DailySummaryBean;
import com.bos.art.model.jdo.OrderStatsSummary;
import com.bos.model.CalendarBean;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import net.sf.hibernate.*;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * @author I0360D4
 * 
 * To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Generation - Code and Comments
 */
public class SessionsSummaryChartsHelper {

    private static final Logger logger = (Logger) Logger.getLogger(SessionsSummaryChartsHelper.class);

    private Document MainScreenDoc = null;

    DocumentBuilderFactory factory = null;

    DocumentBuilder builder = null;

    Document doc = null;

    String selectedDate = null;

    /*
     * public ViewHistoricalChartsHelper() { initialize(); }
     */

    public SessionsSummaryChartsHelper(String selectedDate) {
        setSelectedDate(selectedDate);
        //logger.warn("constructor: " + selectedDate );
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
        //logger.warn("in buildXMLDocument: " + selectedDate );
        Element pageElement = MainScreenDoc.createElement("Page");
        Element headerElement = MainScreenDoc.createElement("DashBoard");
        Element leftPanelElement = MainScreenDoc.createElement("LeftPanel");
        Element bodyElement = MainScreenDoc.createElement("Body");
        Element payload = MainScreenDoc.createElement("Payload");
        long dsTime = System.currentTimeMillis();

        Element dailySummary = appendDailySummaryElement(MainScreenDoc, payload);

        logger.warn("DailySummaryTime : " + (System.currentTimeMillis() - dsTime));

        long ssTime = System.currentTimeMillis();
        Element sessionSummary = appendDailySessionStatistics(MainScreenDoc, payload);

        logger.warn("SessionSummarySummaryTime : " + (System.currentTimeMillis() - ssTime));
        long dalsTime = System.currentTimeMillis();

        Element dollarsAndLinesSummary = appendDollarsAndLinesStatistics(MainScreenDoc, payload);
        logger.warn("DollarsAndLinesSummaryTime : " + (System.currentTimeMillis() - dalsTime));
        long fsTime = System.currentTimeMillis();

        Element financialSummary = appendFinancialSummaryElement(MainScreenDoc, payload);
        logger.warn("FinancialSummaryTime : " + (System.currentTimeMillis() - fsTime));

        pageElement.appendChild(headerElement);
        pageElement.appendChild(leftPanelElement);
        pageElement.appendChild(bodyElement);
        pageElement.appendChild(payload);

        MainScreenDoc.appendChild(pageElement);
    }

    private Element appendDailySummaryElement(Document doc, Element element) {
        Session session = null;
        Element dailySummaryElement = null;
        Element calendarElement = null;

        try {
            dailySummaryElement = doc.createElement("DailySummary");
            calendarElement = doc.createElement("Calendar");

            calendarElement.appendChild(createCalendarElement(doc));
            element.appendChild(calendarElement);
            //Transaction tx= session.beginTransaction();
            session = HibernateUtil.currentSession();
            SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd");
            //logger.warn("selectedDate 333: " + selectedDate );
            Date d = null;
            try {
                d = sdf2.parse( selectedDate );
                //logger.warn("parsed selectedDate 333: " + sdf2.format(d));
            }
            catch(java.text.ParseException pe)
            {
                d = new Date();
            }

            List dailySummaryList = session.find("from DailySummaryBean as DailySummary where DailySummary.day = ?", new java.sql.Date(d.getTime()),
                    Hibernate.DATE);

            Databinder db = HibernateUtil.getDataBinder();
            db.setInitializeLazy(true);

            Iterator iter = dailySummaryList.iterator();
            for (; iter.hasNext();) {
                DailySummaryBean dailySummary = (DailySummaryBean) iter.next();

                db.bind(dailySummary);
                Node element1 = doc.importNode(db.toDOM().getFirstChild(), true);

                dailySummaryElement.appendChild(element1);

                element.appendChild(dailySummaryElement);

            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            try {
                HibernateUtil.closeSession();
            } catch (HibernateException e) {
                e.printStackTrace();
            }
        }
        return dailySummaryElement;
    }

    private static final String SESSION_STATS_QUERY =
    //	" Select context, avg(sessions.sessionHits), count(sessions.sessionHits),
    // "+
    //	" sum(sessions.sessionEndTime -
    // sessions.sessionStartTime)/count(sessions.Session_ID), "+
    //	" sum(sessions.sessionEndTime -
    // sessions.sessionStartTime)/count(sessions.Session_ID)/avg(sessions.sessionHits)
    // "+
    //	" from SessionBean as sessions inner join sessions.context as context "+
    //	" where sessions.sessionStartTime > :sessionStart and
    // sessions.sessionEndTime< :sessionEnd and sessions.sessionHits > 1 "+
    //	" and Sessions.Session_ID> :minSessionID "+
    //	" and Sessions.Session_ID< :maxSessionID " +
    //	" group by context "
    //	;

    "select sessions.contextId, avg(sessions.sessionHits), count(sessions.sessionHits), "
            + " avg(sessions.sessionDuration), count(distinct sessions.userId )  " +
            //	" sum(sessions.sessionEndTime -
            // sessions.sessionStartTime)/count(sessions.Session_ID), "+
            //	" sum(sessions.sessionEndTime -
            // sessions.sessionStartTime)/count(sessions.Session_ID)/avg(sessions.sessionHits)
            // "+
            "from SessionBean as sessions where sessions.sessionStartTime> :startDateBegin and sessions.sessionStartTime< :startDateEnd "
            + " and sessions.contextId is not null and sessions.sessionHits > 1 " + " group by sessions.contextId";

    private static final String SQL_SESSION_STATS_QUERY = " Select c.contextName, AVG(s.sessionHits) as hits, count(s.Session_ID) as cnt ,  "
            +
            //        " sum(s.sessionEndTime - s.sessionStartTime)/count(s.Session_ID)
            // as seconds, "+
            //        " sum(s.sessionEndTime -
            // s.sessionStartTime)/count(s.Session_ID)/AVG(s.sessionHits) as
            // seconds_per_hit "+
            " from Sessions s, Contexts c "
            + " where s.sessionStartTime > '20040209000000' and s.sessionEndTime<'20040210000000' and s.sessionHits > 1 "
            + " and Session_ID> 3346638 " + " and Session_ID< 3415253 " + " and c.Context_ID=s.Context_ID " + " group by c.Context_ID ";

    private static final String CONTEXT_QUERY = "from ContextBean as context where context.contextId=:contextid";

    private Element appendDailySessionStatistics(Document doc, Element element) {
        Session session = null;
        Element sessionSummaryElement = null;
        Element calendarElement = null;
        String[] saContexts = { "UNKNOWN", "campaigns", "shop", "NASApp/Reliable", "integration", "onlinereporting","preview" };
        int[] iContexts =     { 0,         1,            2,     3,                 4,             40,              13 };

        try {
            sessionSummaryElement = doc.createElement("SessionElement");
            session = HibernateUtil.currentSession();
            String o1[] = {};
            Class o2[] = {};

            Query query = session.createQuery(SESSION_STATS_QUERY);
            char[] cDate = selectedDate.toCharArray();
            StringBuffer sDate = new StringBuffer().append(cDate[0]).append(cDate[1]).append(cDate[2]).append(cDate[3]).append(cDate[5])
                    .append(cDate[6]).append(cDate[8]).append(cDate[9]);
            SimpleDateFormat sdf2 = new SimpleDateFormat("yyyyMMddHHmmss");
            Date d  = null;
            try  {
                d = sdf2.parse( sDate.toString()+"000000" );
            }
            catch(java.text.ParseException pe)
            {
                d = new Date();
            }
            
            query.setTimestamp("startDateBegin", new java.sql.Timestamp(d.getTime()));
            //query.setParameter("startDateBegin", sDate.toString() + "000000");
            //

            Date endd  = null;
            try  {
                endd = sdf2.parse( sDate.toString()+"235959" );
            }
            catch(java.text.ParseException pe)
            {
                endd = new Date();
            }
            query.setTimestamp("startDateEnd", new java.sql.Timestamp(endd.getTime()));
            //query.setParameter("startDateEnd", sDate.toString() + "235959");
            logger.debug("startDateBegin:startDateEnd " + sDate + "000000:" + sDate + "235959");

            List sessionSummaryList = query.list();
            Databinder db = HibernateUtil.getDataBinder();
            db.setInitializeLazy(true);

            Iterator iter = sessionSummaryList.iterator();
            logger.debug("Location 15 " );

            for (; iter.hasNext();) {
                Element sessionRecordElement = doc.createElement("SessionRecord");
                logger.info("Location 16 ");
                Object o[] = (Object[]) iter.next();
                logger.info("Location 16.5 "+o);
                logger.info("Location 16.6 "+o[0]);
                logger.info("Type : " + o.getClass().getName());
                logger.info("Type : " + o.toString());

                Integer iContextID = (Integer) o[0];
                Float fAvgSessionHits = (Float) o[1];
                Integer iCountSessions = (Integer) o[2];
                Float fAvgSessionDuration = (Float) o[3];
                Integer iCountDistinctUsers = (Integer) o[4];
                if (iContextID == null) {
                    iContextID = new Integer(0);
                }
                if (fAvgSessionHits == null) {
                    fAvgSessionHits = new Float(0);
                }
                if (iCountSessions == null) {
                    iCountSessions = new Integer(0);
                }
                if (fAvgSessionDuration == null) {
                    fAvgSessionDuration = new Float(0);
                }
                Float fAvgSecondsBetweenClick = new Float(0.0);

                if (!(fAvgSessionHits.floatValue() == 0.0)) {
                    fAvgSecondsBetweenClick = new Float(fAvgSessionDuration.floatValue() / fAvgSessionHits.floatValue());
                }
                if (iCountDistinctUsers == null) {
                    iCountDistinctUsers = new Integer(0);
                }

                Element sessionContextElement = doc.createElement("ContextName");
                int cid = iContextID.intValue();
                
                logger.info("cid : " + cid);
                String sConVal = "N.I.A.";
                /*if (cid < saContexts.length) {
                    logger.info("in cid : " + cid);

                    sConVal = saContexts[cid];
                } */
                for(int i =0 ;i<iContexts.length;i++) {
                    if (cid == iContexts[i]) {
                        sConVal = saContexts[i];
                        break;
                    }
                }
                sessionContextElement.appendChild(doc.createTextNode(sConVal));
                sessionRecordElement.appendChild(sessionContextElement);

                Element sessionHitsElement = doc.createElement("AvgSessionHits");
                sessionHitsElement.appendChild(doc.createTextNode(fAvgSessionHits.toString()));
                sessionRecordElement.appendChild(sessionHitsElement);

                Element sessionCountElement = doc.createElement("CountSessions");
                sessionCountElement.appendChild(doc.createTextNode(iCountSessions.toString()));
                sessionRecordElement.appendChild(sessionCountElement);

                Element sessionDurationElement = doc.createElement("AvgSessionDuration");
                sessionDurationElement.appendChild(doc.createTextNode(fAvgSessionDuration.toString()));
                sessionRecordElement.appendChild(sessionDurationElement);

                Element sessionClickAvgElement = doc.createElement("AvgSecondsBetweenClick");
                sessionClickAvgElement.appendChild(doc.createTextNode(fAvgSecondsBetweenClick.toString()));
                sessionRecordElement.appendChild(sessionClickAvgElement);

                Element distinctUsers = doc.createElement("distinctUsers");
                distinctUsers.appendChild(doc.createTextNode(iCountDistinctUsers.toString()));
                sessionRecordElement.appendChild(distinctUsers);

                sessionSummaryElement.appendChild(sessionRecordElement);

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
        element.appendChild(sessionSummaryElement);

        return sessionSummaryElement;
    }

    private Element appendFinancialSummaryElement(Document doc, Element element) {
        Session session = null;
        Element orderStatsSummaryElement = null;
        Element calendarElement = null;

        try {
            orderStatsSummaryElement = doc.createElement("OrderStatsSummaryElement");
            session = HibernateUtil.currentOracleSession();
            //logger.warn("selectedDate" + selectedDate);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            SimpleDateFormat sdf2 = new SimpleDateFormat("yyyyMMdd");
            Date d = sdf.parse(selectedDate);
            String lselectedDate = sdf2.format(d);
            List dailySummaryList = session.find("from OrderStatsSummary as OrderStatsSummary where OrderStatsSummary.day = ?",
                    lselectedDate, Hibernate.STRING);

            Databinder db = HibernateUtil.getOracleDataBinder();
            db.setInitializeLazy(true);

            Iterator iter = dailySummaryList.iterator();
            for (; iter.hasNext();) {
                OrderStatsSummary orderStatsSummary = (OrderStatsSummary) iter.next();

                db.bind(orderStatsSummary);
                Node element1 = doc.importNode(db.toDOM().getFirstChild(), true);

                orderStatsSummaryElement.appendChild(element1);

                element.appendChild(orderStatsSummaryElement);

            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            try {
                HibernateUtil.closeOracleSession();
            } catch (HibernateException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return orderStatsSummaryElement;
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

        return (Element) doc.importNode(calendarBean.toDOM().getFirstChild(), true);
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
     * @param selectedDate
     *            The selectedDate to set.
     */
    public void setSelectedDate(String selectedDate) {
        this.selectedDate = selectedDate;
    }

    public static final int UNKNOWN_CONTEXT = 0;

    public static final int SHOP2_CONTEXT = 42;
    public static final int PREVIEW_CONTEXT = 13;

    public static final int SHOP_CONTEXT = 2;
    public static final int INTEGRATION_CONTEXTS = 4;

    private Element appendDollarsAndLinesStatistics(Document doc, Element element) {
        Session session = null;
        Element dollarsAndLinesElement = null;

        dollarsAndLinesElement = doc.createElement("DollarsLines");

        //			contextElement = doc.createElement("Context");
        //			Element contextName = doc.createElement("ContextName");
        //			contextName.appendChild(doc.createTextNode("shop-integration"));

        CyclicBarrier barrier = new CyclicBarrier(4);

        String shop = "shop";
        String integration = "integration";
        String shop2 = "shop2";
        String preview = "preview";

        HashMap map = new HashMap();

        new Thread(new DollarsAndLinesHelper(doc, SHOP_CONTEXT   , shop, map,barrier)).start();
        new Thread(new DollarsAndLinesHelper(doc, PREVIEW_CONTEXT, preview, map,barrier)).start();
        new Thread(new DollarsAndLinesHelper(doc, INTEGRATION_CONTEXTS, integration, map,barrier)).start();
        try {
            barrier.barrier();
        } catch (TimeoutException e) {
            e.printStackTrace();
        } catch (BrokenBarrierException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        dollarsAndLinesElement.appendChild( (Element)map.get(shop));
        dollarsAndLinesElement.appendChild( (Element)map.get(preview));
        dollarsAndLinesElement.appendChild( (Element)map.get(integration));

        //Element valuesShopContextElement =
        // getDollarsAndLines(doc,SHOP_CONTEXT,shop);
        //Element valuesIntegrationContextElement =
        // getDollarsAndLines(doc,UNKNOWN_CONTEXT,integration);
        //Element valuesShoppingContextElement =
        // getDollarsAndLines(doc,SHOPPING_CONTEXT,shopping);

        //dollarsAndLinesElement.appendChild(valuesShopContextElement);
        //dollarsAndLinesElement.appendChild(valuesIntegrationContextElement);
        //dollarsAndLinesElement.appendChild(valuesShoppingContextElement);

        element.appendChild(dollarsAndLinesElement);
        return dollarsAndLinesElement;
    }

    class DollarsAndLinesHelper implements Runnable {
        private CyclicBarrier barrier;
        private Document doc;
        private HashMap map;
        private int contextID;
        private String contextName;

        public DollarsAndLinesHelper(Document d, int i, String s, HashMap map, CyclicBarrier r) {
            doc = d;
            contextID = i;
            contextName = s;
            barrier = r;
            this.map = map;
        }

        public void run() {
            Element derrivedElement = getDollarsAndLines(doc, contextID, contextName);
            //element.appendChild(derrivedElement);
            synchronized(map) {
                map.put(contextName,derrivedElement);
            }
            try {
                logger.info("Attempting CyclicBarrier(2): " + Thread.currentThread().getName());
                barrier.barrier();
                logger.info("Attempting CyclicBarrier Accomplished : " + Thread.currentThread().getName());
            } catch (TimeoutException e) {
                e.printStackTrace();
            } catch (BrokenBarrierException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    public static final int ARIBA_DIRECT_LINES = 2001;
    public static final int ARIBA_DIRECT_DOLLARS = 2002;
    public static final int SAP_DIRECT_LINES = 2003;
    public static final int SAP_DIRECT_DOLLARS = 2004;
    public static final int WM_DIRECT_LINES = 2005;
    public static final int WM_DIRECT_DOLLARS = 2006;
    public static final int BOS_CHECKOUT_LINES = 2007;
    public static final int BOS_CHECKOUT_DOLLARS = 2008;
    public static final int BOS_GUEST_LINES = 2009;
    public static final int BOS_GUEST_DOLLARS = 2010;

    private Element getDollarsAndLines(Document doc, int contextID, String contextName) {
        Element contextElement = null;
        Element contextNameElement = null;
        Element wmDollars = null;
        Element wmLines = null;
        Element wmOrders = null;
        Element sapDollars = null;
        Element sapLines = null;
        Element sapOrders = null;
        Element aribaDollars = null;
        Element aribaLines = null;
        Element aribaOrders = null;
        Element bosDollars = null;
        Element bosLines = null;
        Element bosOrders = null;
        Element guestDollars = null;
        Element guestLines = null;
        Element guestOrders = null;
        logger.info("getDollarsAndLines Called " + contextName +" : contextID" + contextID);
        
        ////////////////////////////////////////////////////
        // initialize the date from the selectedDateString
        char[] cDate = selectedDate.toCharArray();
        StringBuffer sDate = new StringBuffer().append(cDate[0]).append(cDate[1]).append(cDate[2]).append(cDate[3]).append(cDate[5])
                .append(cDate[6]).append(cDate[8]).append(cDate[9]);
        String startDate = sDate.toString() + "000000";
        String endDate = sDate.toString() + "235959";
        //
        //////////////////////////////////////////////

        contextElement = doc.createElement("Context");
        contextNameElement = doc.createElement("ContextName");
        contextNameElement.appendChild(doc.createTextNode(contextName));

        Session s = null;
        
        Connection connection = null;
         
        try {
            s = HibernateUtil.currentSession();
            connection = s.connection();
 
            logger.warn("SELECT AccumulatorStat_ID, a.Time, c.contextName, a.Value, a.Count from AccumulatorStats a, Contexts c where a.Context_ID=c.Context_ID and c.contextName=? and Time = ?");
            logger.warn(" contextName, startDate " + contextName +","+startDate);
            
            PreparedStatement pstmt = connection.prepareStatement("SELECT AccumulatorStat_ID, a.Time, c.contextName, a.Value, a.Count from AccumulatorStats a, Contexts c where a.Context_ID=c.Context_ID and c.contextName=? and Time = ?");
            pstmt.setString(1,contextName);

            //logger.warn("start date1 : " + startDate );
            SimpleDateFormat sdf2 = new SimpleDateFormat("yyyyMMddHHmmss");
            Date startd = null;
            try {
                startd = sdf2.parse( startDate );
                //logger.warn("start date1 parsed : " + sdf2.format( startd ) );
            }
            catch(java.text.ParseException pe)
            {
                startd = new Date();
            }

            pstmt.setTimestamp(2,new java.sql.Timestamp(startd.getTime()));
            //pstmt.setString(2,startDate);
            ResultSet rs = pstmt.executeQuery();
            HashMap results = new HashMap();
            while(rs.next()){
                Object[] oa = new Object[5];
                int accumulatorID = rs.getInt("AccumulatorStat_ID");
                String time = rs.getString("Time");
                int value = rs.getInt("Value");
                int count = rs.getInt("Count");
                logger.warn("AccumulatorID : " + accumulatorID);
                
                oa[0] = new Integer(accumulatorID);
                oa[1] = time;
                oa[2] = new Integer(value);
                oa[3] = new Integer(count);
                
                results.put(oa[0],oa);
                logger.warn("AccumulatorID oa[0] : " + oa[0].toString() +","+oa[1].toString()+","+oa[2].toString()+","+oa[3].toString());
            }
            
            wmDollars = createAccumulatorDoubleNode(doc, "WMDollars",(Object[])results.get(new Integer(WM_DIRECT_DOLLARS)));
            wmLines   = createElement(doc, "WMLines", (Object[])results.get(new Integer(WM_DIRECT_LINES)),2);
            wmOrders  = createElement(doc, "WMOrders", (Object[])results.get(new Integer(WM_DIRECT_LINES)),3);
            
            sapDollars = createAccumulatorDoubleNode(doc, "SAPDollars",(Object[])results.get(new Integer(SAP_DIRECT_DOLLARS)));
            sapLines   = createElement(doc, "SAPLines", (Object[])results.get(new Integer(SAP_DIRECT_LINES)),2);
            sapOrders  = createElement(doc, "SAPOrders", (Object[])results.get(new Integer(SAP_DIRECT_LINES)),3);
            
            aribaDollars = createAccumulatorDoubleNode(doc, "ARIBADollars",(Object[])results.get(new Integer(ARIBA_DIRECT_DOLLARS)));
            aribaLines   = createElement(doc, "ARIBALines", (Object[])results.get(new Integer(ARIBA_DIRECT_LINES)),2);
            aribaOrders   = createElement(doc, "ARIBAOrders", (Object[])results.get(new Integer(ARIBA_DIRECT_LINES)),3);
            
            bosDollars = createAccumulatorDoubleNode(doc, "BOSDollars",(Object[])results.get(new Integer(BOS_CHECKOUT_DOLLARS)));
            bosLines   = createElement(doc, "BOSLines", (Object[])results.get(new Integer(BOS_CHECKOUT_LINES)),2);
            bosOrders   = createElement(doc, "BOSOrders", (Object[])results.get(new Integer(BOS_CHECKOUT_LINES)),3);
            
            guestDollars = createAccumulatorDoubleNode(doc, "GUESTDollars",(Object[])results.get(new Integer(BOS_GUEST_DOLLARS)));
            guestLines   = createElement(doc, "GUESTLines", (Object[])results.get(new Integer(BOS_GUEST_LINES)),2);
            guestOrders   = createElement(doc, "GUESTOrders", (Object[])results.get(new Integer(BOS_GUEST_LINES)),3);
           
            
            contextElement.appendChild(contextNameElement);
            contextElement.appendChild(wmDollars);
            contextElement.appendChild(wmLines);
            contextElement.appendChild(wmOrders);
            contextElement.appendChild(sapDollars);
            contextElement.appendChild(sapLines);
            contextElement.appendChild(sapOrders);
            contextElement.appendChild(aribaDollars);
            contextElement.appendChild(aribaLines);
            contextElement.appendChild(aribaOrders);
            contextElement.appendChild(bosDollars);
            contextElement.appendChild(bosLines);
            contextElement.appendChild(bosOrders);
            contextElement.appendChild(guestDollars);
            contextElement.appendChild(guestLines);
            contextElement.appendChild(guestOrders);
           
        } catch (HibernateException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }catch (SQLException se){
            se.printStackTrace();
        }finally{
            try {
                HibernateUtil.closeSession();
            } catch (HibernateException e1) {

                e1.printStackTrace();
            }
        }
        
 
        return contextElement;
    }

    private Element createAccumulatorDoubleNode(Document doc, 
                        String elementName, 
                        Object[] oa) {
        Element element = doc.createElement(elementName);
        if(oa == null){
            String sValue = new Double(0.0).toString();
            element.appendChild(doc.createTextNode(sValue));
        }else{
            String sValue = new Double((((Integer)oa[2]).doubleValue()/100)).toString();
            element.appendChild(doc.createTextNode(sValue));
        }
        return element;
    }

    Element createElement(Document doc, String elementName, Object[] oa, int index) {
        Element element = doc.createElement(elementName);
        if(oa== null){
            element.appendChild(doc.createTextNode("0"));
        }else{
            element.appendChild(doc.createTextNode(oa[index].toString()));
        }
        return element;
    }

}
