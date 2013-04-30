package com.bos.servlets;

import EDU.oswego.cs.dl.util.concurrent.BrokenBarrierException;
import EDU.oswego.cs.dl.util.concurrent.CyclicBarrier;
import EDU.oswego.cs.dl.util.concurrent.TimeoutException;
import com.bos.arch.HibernateUtil;
import com.bos.art.model.jdo.DailySummaryBean;
import com.bos.art.model.jdo.OrderStatsSummary;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import net.sf.hibernate.*;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;

/**
 * @author I081299
 * 
 */

public class SessionUsageServlet {
	
	private static final Logger logger = (Logger) Logger.getLogger(SessionUsageServlet.class);
		
	String selectedDate;
	DailySummaryBean dailySummaryBean;
	ArrayList<HashMap<String, String>> dollarsAndLinesMaps, sessionSummaryMaps;
	
	
	public SessionUsageServlet(String selectedDate) {
		this.selectedDate = selectedDate;			
		long dsTime = System.currentTimeMillis();
		dailySummaryBean = buildDailySummaryBean();
		logger.warn("DailySummaryTime : " + (System.currentTimeMillis() - dsTime));
		
		long dalsTime = System.currentTimeMillis();
		dollarsAndLinesMaps = buildDollarsAndLinesStatistics();
		logger.warn("DollarsAndLinesSummaryTime : " + (System.currentTimeMillis() - dalsTime));
		
		long ssTime = System.currentTimeMillis();
		sessionSummaryMaps = buildDailySessionStatistics();
		logger.warn("SessionSummarySummaryTime : " + (System.currentTimeMillis() - ssTime));
	}

	private DailySummaryBean buildDailySummaryBean() {
            Session session = null;
            List dailySummaryList = null;
            DailySummaryBean dsb = null;
		
            try {
                session = HibernateUtil.currentSession();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                Date d = sdf.parse(selectedDate);
                dailySummaryList = session.find("from DailySummaryBean as DailySummary where DailySummary.day = ?", new java.sql.Date(d.getTime()), Hibernate.DATE);
                try {
                	dsb = (DailySummaryBean) dailySummaryList.get(0);     
                } catch (IndexOutOfBoundsException ie) {}
            }
            catch (Exception ex) {
                ex.printStackTrace();
            }
            finally {
                try {
                    HibernateUtil.closeSession();
                } catch (HibernateException e) {
                    e.printStackTrace();
                }
            }
            if (dsb == null) {
            	dsb = new DailySummaryBean();
            	dsb.setDay(new Date());
            }            	
           
           	return dsb;
	}
	
	public static final int UNKNOWN_CONTEXT = 0;
    public static final int SHOP2_CONTEXT = 42;
    public static final int PREVIEW_CONTEXT = 13;
    public static final int SHOP_CONTEXT = 2;
    public static final int INTEGRATION_CONTEXTS = 4;
	
	private ArrayList<HashMap<String, String>> buildDollarsAndLinesStatistics() {
		Session session = null;
		CyclicBarrier barrier = new CyclicBarrier(4);

        String shop = "shop";
        String integration = "integration";
        String shop2 = "shop2";
        //String root = "ROOT";
        String preview = "preview";
        
        ArrayList<HashMap<String, String>> maps = new ArrayList<HashMap<String, String>>();
       // new Thread(new DollarsAndLinesHelper(UNKNOWN_CONTEXT     , root       , maps, barrier)).start();
        new Thread(new DollarsAndLinesHelper(SHOP_CONTEXT        , shop       , maps, barrier)).start();
        new Thread(new DollarsAndLinesHelper(PREVIEW_CONTEXT     , preview    , maps, barrier)).start();
        new Thread(new DollarsAndLinesHelper(INTEGRATION_CONTEXTS, integration, maps, barrier)).start();
        
        try {
            barrier.barrier();
        } catch (TimeoutException e) {
            e.printStackTrace();
        } catch (BrokenBarrierException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        		
    	return maps;
	}
	
	class DollarsAndLinesHelper implements Runnable {
        private CyclicBarrier barrier;
        private ArrayList< HashMap<String, String> > maps;
        private int contextID;
        private String contextName;

        public DollarsAndLinesHelper(int i, String s, ArrayList<HashMap<String, String>> maps, CyclicBarrier r) {
            contextID = i;
            contextName = s;
            barrier = r;
            this.maps = maps;
        }

        public void run() {
            HashMap<String, String> contextMap = getDollarsAndLines(contextID, contextName);
            
            synchronized(maps) {
                maps.add(contextMap);
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

    private HashMap<String, String> getDollarsAndLines(int contextID, String contextName) {
        HashMap<String, String> contextMap = new HashMap<String, String>();
        
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
        Session s = null;
        
        Connection connection = null;
         
        try {
            s = HibernateUtil.currentSession();
            connection = s.connection();
 
            logger.warn("SELECT AccumulatorStat_ID, a.Time, c.contextName, a.Value, a.Count from AccumulatorStats a, Contexts c where a.Context_ID=c.Context_ID and c.contextName=? and Time = ?");
            logger.warn(" contextName, startDate " + contextName +", "+startDate);
            
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
            HashMap<Object, Object[]> results = new HashMap<Object, Object[]>();
            while (rs.next()) {
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
                
                results.put(oa[0], oa);
                logger.warn("AccumulatorID oa[0] : " + oa[0].toString() +","+oa[1].toString()+","+oa[2].toString()+","+oa[3].toString());
            }
            
            contextMap.put("contextName", contextName);
            
            contextMap.put("WMDollars", getDoubleValue( (Object[])results.get(new Integer(WM_DIRECT_DOLLARS)) ));
            contextMap.put("WMLines", getValue( (Object[])results.get(new Integer(WM_DIRECT_LINES)), 2) );
            contextMap.put("WMOrders", getValue( (Object[])results.get(new Integer(WM_DIRECT_LINES)), 3) );
            
            contextMap.put("SAPDollars", getDoubleValue( (Object[])results.get(new Integer(SAP_DIRECT_DOLLARS)) ));
            contextMap.put("SAPLines", getValue( (Object[])results.get(new Integer(SAP_DIRECT_LINES)), 2) );
            contextMap.put("SAPOrders", getValue( (Object[])results.get(new Integer(SAP_DIRECT_LINES)), 3) );
            
            contextMap.put("ARIBADollars", getDoubleValue( (Object[])results.get(new Integer(ARIBA_DIRECT_DOLLARS)) ));
            contextMap.put("ARIBALines", getValue( (Object[])results.get(new Integer(ARIBA_DIRECT_LINES)), 2) );
            contextMap.put("ARIBAOrders", getValue( (Object[])results.get(new Integer(ARIBA_DIRECT_LINES)), 3) );
            
            contextMap.put("BOSDollars", getDoubleValue( (Object[])results.get(new Integer(BOS_CHECKOUT_DOLLARS)) ));
            contextMap.put("BOSLines", getValue( (Object[])results.get(new Integer(BOS_CHECKOUT_LINES)), 2) );
            contextMap.put("BOSOrders", getValue( (Object[])results.get(new Integer(BOS_CHECKOUT_LINES)), 3) );
            
            contextMap.put("GUESTDollars", getDoubleValue( (Object[])results.get(new Integer(BOS_GUEST_DOLLARS)) ));
            contextMap.put("GUESTLines", getValue( (Object[])results.get(new Integer(BOS_GUEST_LINES)), 2) );
            contextMap.put("GUESTOrders", getValue( (Object[])results.get(new Integer(BOS_GUEST_LINES)), 3) );
            
        } catch (HibernateException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (SQLException se){
              se.printStackTrace();
        } finally {
            try {
                HibernateUtil.closeSession();
            } catch (HibernateException e1) {
                e1.printStackTrace();
            }
        }
         
        return contextMap;
    }
    
    /**
 	 * Takes an object array as an argument and returns a String representation of 
 	 * the value specified by the index. If the object array is null then return the 
 	 * default value of zero.
     * 
     * @return objValue - A String representation of the floating point value
	 */
    private String getValue(Object[] objArr, int index) {   
    	NumberFormat nf = NumberFormat.getIntegerInstance();
    	String objValue = "0";
    	if (objArr != null)
    		objValue = objArr[index].toString();
    	
    	try {
    		return nf.format(Integer.parseInt(objValue)); 
    	} catch (NumberFormatException nfe) {
    		return "0";
    	}
    }
    
    /**
 	 * Takes an object array as an argument and returns a String representation of 
 	 * the floating point value specified by the index. If the object array is null
 	 * then return the default value of zero.
     * 
     * @return objValue - A String representation of the floating point value
	 */
    private String getDoubleValue(Object[] objArr) {
    	NumberFormat nfc = NumberFormat.getCurrencyInstance();
    	Double objValue = new Double(0.0);    	
    	if (objArr != null)
    		objValue = new Double((((Integer) objArr[2]).doubleValue()/ 100));    	   	
    	return nfc.format(objValue);
    }   
    
    
    
    
    private static final String SESSION_STATS_QUERY = "select sessions.contextId, avg(sessions.sessionHits), count(sessions.sessionHits), "
            + " avg(sessions.sessionDuration), count(distinct sessions.userId )  "
            + "from SessionBean as sessions where sessions.sessionStartTime> :startDateBegin and sessions.sessionStartTime< :startDateEnd "
            + " and sessions.contextId is not null and sessions.sessionHits > 1 " + " group by sessions.contextId";
    
    private ArrayList< HashMap<String, String> > buildDailySessionStatistics() {
    	ArrayList<HashMap<String, String>> sessionSummaryMaps = new ArrayList<HashMap<String,String>>();
    	
    	Session session = null;
        String[] saContexts = { "UNKNOWN", "campaigns", "shop", "NASApp/Reliable", "integration", "onlinereporting","preview" };
        int[] iContexts =     { 0,         1,            2,     3,                 4,             40,               13 };
        
        try {
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
            Date endd  = null;
            try  {
                endd = sdf2.parse( sDate.toString()+"235959" );
            }
            catch(java.text.ParseException pe)
            {
                endd = new Date();
            }
            query.setTimestamp("startDateEnd", new java.sql.Timestamp(endd.getTime()));
            logger.debug("startDateBegin:startDateEnd " + sDate + "000000:" + sDate + "235959");

            List sessionSummaryList = query.list();            
            Iterator iter = sessionSummaryList.iterator();
            logger.debug("Location 15 " );

            while (iter.hasNext()) {
            	HashMap<String, String> sessionSummaryMap = new HashMap<String, String>();
            	
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
                
                int cid = iContextID.intValue();                
                logger.info("cid : " + cid);
                String sConVal = "N.I.A.";
                
                for(int i =0 ;i<iContexts.length;i++) {
                    if (cid == iContexts[i]) {
                        sConVal = saContexts[i];
                        break;
                    }
                }
                
              DecimalFormat df = new DecimalFormat("##,##0");
              DecimalFormat df2 = new DecimalFormat("##,##0.0");
                
              sessionSummaryMap.put("contextName", sConVal);
              sessionSummaryMap.put("AvgSessionHits", df.format(fAvgSessionHits));
              sessionSummaryMap.put("CountSessions", df.format(iCountSessions));
              sessionSummaryMap.put("AvgSessionDuration", df2.format(fAvgSessionDuration/60000));
              sessionSummaryMap.put("AvgSecondsBetweenClick", df.format(fAvgSecondsBetweenClick/250));
              sessionSummaryMap.put("distinctUsers", df.format(iCountDistinctUsers));
                
              sessionSummaryMaps.add(sessionSummaryMap);
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
        
        return sessionSummaryMaps;
    }
    
    /**
 	 * Getter method for retrieving the Daily Summary Bean
     * 
     * @return dailySummaryBean - The field variable that contains the Daily Summary Bean
	 */
        
    public DailySummaryBean getDailySummaryBean() {
        return this.dailySummaryBean;
    }
    
    public ArrayList<HashMap<String, String>> getSessionSummaryMaps() {
    	return this.sessionSummaryMaps;
    }
    
    /**
     * Getter method for retrieving the Dollars and Lines maps
     * 
     * @return dollarsAndLinesMaps - The field variable that contains the Dollars and Lines maps
     */
    
    public ArrayList<HashMap<String, String>> getDollarsAndLinesMaps() {
    	return this.dollarsAndLinesMaps;
    }
}
