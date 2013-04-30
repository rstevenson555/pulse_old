/*
 * Created on Oct 22, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package com.bos.art.logParser.statistics;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Properties;
import java.util.TreeMap;
import java.util.Vector;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

//import org.apache.axis.utils.Base64;
import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;
import com.bos.art.logParser.db.AccessRecordPersistanceStrategy;
import com.bos.art.logParser.db.ConnectionPoolT;
import com.bos.art.logParser.db.ForeignKeyStore;
import com.bos.art.logParser.db.PersistanceStrategy;
import com.bos.art.logParser.records.AccessRecordsForeignKeys;
import com.bos.art.logParser.records.ILiveLogParserRecord;
import com.bos.art.logParser.records.UserRequestTiming;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;


/**
 * @author I0360D3
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class LoadTestStats extends StatisticsUnit {
	
	/**
	 * Valid states
	 * IDLE           -> 1      (Waiting to Recieve Test Start Signal)
	 * RUNNING_INIT   -> 2      (Accepting data, Not Allowed to persist)
	 * RUNNING        -> 3      (Accepting data, Allowed to persist)
	 * CLOSE          -> 4      (Transition State going into Time_wait)
	 * TIME_WAIT      -> 5      (Prepare to flush and reset, or go back to running or running_init)
	 * FLUSH          -> 6      (Transition State to IDLE or RUNNING_INIT)
	 */
	private int STATE=1;
	private static final int IDLE          =1;
	private static final int RUNNING_INIT  =2;
	private static final int RUNNING       =3;
	private static final int CLOSE         =4;
	private static final int TIME_WAIT     =5;
	private static final int FLUSH         =6;
	
	
    private static final Logger logger = (Logger)Logger.getLogger(LoadTestStats.class.getName());
    private static LoadTestStats instance;
    private static final DateTimeFormatter fdf  = DateTimeFormat.forPattern("yyyy-MM/dd HH:mm:ss");
    private static final DateTimeFormatter fdfKey  = DateTimeFormat.forPattern("yyyyMMddHHmm");
   //private Hashtable minutes;
    private TreeMap minutesTimeSpanEventContainer;
    private TreeMap testTimeSpanEventContainer;
    private TreeMap testSessionTimeSpanEventContainer;
    private int calls;
    private int eventsProcessed;
    private int timeSlices;
    private java.util.Date lastDataWriteTime;
    private static final int HOUR_DELAY = 1;
    private static final int MINUTE_DELAY = 1;
    private static final int SECONDS_DELAY = 5;
    transient private PersistanceStrategy pStrat;
    private static final SimpleDateFormat sdfDate = new SimpleDateFormat("yyyyMMddHHmmss");
    private java.util.Date lastPersistDate;
    private boolean threadsRunning = false;

    public LoadTestStats() {
        //minutes = new Hashtable();
        minutesTimeSpanEventContainer = new TreeMap();
        testTimeSpanEventContainer = new TreeMap();
        testSessionTimeSpanEventContainer = new TreeMap();
		tmRequestTokenData = new TreeMap();
		tmSessionTokenData = new TreeMap();
        lastDataWriteTime = new java.util.Date();
        pStrat = LoadTestPersistanceStrategy.getInstance();
    }
    

    public static LoadTestStats getInstance() {
        if (instance == null) {
            instance = new LoadTestStats();
            if (!instance.threadsRunning) {
                new Thread(instance.new RequestTokenData(), "LoadTest-RequestThread").start();
                new Thread(instance.new SessionData(), "LoadTest-SessionData_Thread").start();
                new Thread(instance.new StateWatcher(),"LoadTest-State Watcher").start();
                instance.threadsRunning = true;
            }
        }
        return instance;
    }
    public void setInstance(StatisticsUnit su) {
        if (su instanceof LoadTestStats) {
            if (instance!=null) {
                instance.runnable =false;
            }
            instance = (LoadTestStats)su;
            if (!instance.threadsRunning) {
                new Thread(instance.new RequestTokenData(), "LoadTest-RequestThread").start();
                new Thread(instance.new SessionData(), "LoadTest-SessionData_Thread").start();
				new Thread(instance.new StateWatcher(),"LoadTest-State Watcher").start();
                instance.threadsRunning = true;
            }
        }
    }
    
    
    /* (non-Javadoc)
     * @see com.bos.art.logParser.statistics.StatisticsUnit#processRecord(com.bos.art.logParser.records.LiveLogParserRecord)
     */
    public void processRecord(ILiveLogParserRecord record) {
        if (record.isAccessRecord()) {
            UserRequestTiming ar = (UserRequestTiming)record;
            if(ar.getSessionId() == null || ar.getSessionId().equals("nullSession")){
            	//Don't process if we don't have a sessionID.
            	return;
            }

            RequestTokenData rtd = new RequestTokenData();
            rtd.forwards = 0;
            rtd.lastModDate = record.getEventTime().getTime();
            String qp = ar.getQueryParams();
            //Base64 base64 = new Base64();
            //logger.warn("Raw QP:"+qp);

            if(qp!=null && qp.indexOf("#P#")<0){
				byte[] qpChar = Base64.decodeBase64(qp.getBytes()); 
				qp = new String(qpChar);
                //logger.warn("Decoded QP:"+qp);
            }
            if (qp != null && qp.indexOf("loadtesttransaction") > -1) {
                int l1 = qp.indexOf("loadtesttransaction=") + 20;
                int l2 = l1 + 6;
                if (qp.length() >= l2) {
                    rtd.loadTestTransaction = qp.substring(l1, l2);
                    if(rtd.loadTestTransaction.equals("FLUSHV")){
                    	flushData();
                    	return;
                    }else if(rtd.loadTestTransaction.equals("STARTV")){
                    	createTest(qp,ar);
                    	return;
                    }else if(rtd.loadTestTransaction.equals("PRINTV")){
                    	printData();
                    	return;
                    }
                } else if (qp.length() < l1) {
                    rtd.loadTestTransaction = null;
                    logger.warn("ql.length<l1" + qp.length()+":"+l1);
                    return;
                } else {
					logger.warn("ql.length<l2" + qp.length()+":"+l2+":"+qp);

                    rtd.loadTestTransaction = qp.substring(l1);
                }
            }else{
            	//  Return because we are not part of the load test.
            	return;
            }
            if(!persistableTest){
            	//  We have not triggered the start of a test, so don't bother to record the record.
            	return;
            }
            rtd.maxValue = record.getLoadTime();
            rtd.requestToken = ar.getRequestToken();
			
			AccessRecordsForeignKeys fk =
						((UserRequestTiming) record).obtainForeignKeys();
            
			String userKey = ((UserRequestTiming) record).getUserKey();
					fk.fkUserID =
						ForeignKeyStore.getInstance().getForeignKey(
							fk,
							userKey,
							ForeignKeyStore.FK_USERS_USER_ID,
							pStrat);
							
			StringBuffer sessionValue =
						new StringBuffer()
							.append(ar.getSessionId())
							.append("#IPADDRESS#")
							.append(ar.getIpAddress())
							.append("#BROWSER#")
							.append(ar.getBrowser())
							.append("#USERID#")
							.append(fk.fkUserID);

					fk.fkSessionID =
						ForeignKeyStore.getInstance().getForeignKey(
							fk,
							sessionValue.toString(),
							ForeignKeyStore.FK_SESSIONS_SESSION_ID,
							pStrat);
            
            
            
            rtd.sessionId = fk.fkSessionID;
            tallyRequestTokenData(rtd);
            ++eventsProcessed;
        }
        if (calls % 50000 == 0) {
            logger.debug(this.toString());
        }
        return;
    }
    
    private boolean persistableTest = false;
    private java.util.Date testEndTime ;
    private java.util.Date testStartTime ;
    private String testName ;
    private String testBranch ;
    private String testContext ;
    private String testMachine ;

	private void flushData(){
		if(STATE == TIME_WAIT){
			if(lastDataDate==null){
				return;
			}
			if((System.currentTimeMillis() - lastDataDate.getTime()) > (1000*60*10)){
				forceFlush();
			}
			return;
		}else if(STATE == RUNNING_INIT){
			deleteData();

			STATE=IDLE;
			return;
		}else if(STATE == RUNNING){
			STATE=TIME_WAIT;
			return;
		}
	}
	
	
	private class StateWatcher extends Thread{
		public void run(){
			while(true){
                try {
                    Thread.sleep(1000*30);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
				checkState();
			}
		}
		private void checkState(){
			if(STATE == RUNNING){
				if(lastDataDate==null){
					return;
				}
				if((System.currentTimeMillis() - lastDataDate.getTime()) > (1000*60*1)){
					STATE=TIME_WAIT;//forceFlush();
				}
			}else if(STATE == TIME_WAIT || STATE==RUNNING_INIT){
				if(lastDataDate==null){
					return;
				}
				if((System.currentTimeMillis() - lastDataDate.getTime()) > (1000*60*4)){
					forceFlush();
				}
			}
		}
	}

    private void forceFlush() {
       	testEndTime = lastDataDate;//new java.util.Date();
       	if(lastDataDate.getTime() -testStartTime.getTime()> 10*1000*60){
       		forceHardFlushOfSessionData();
       		printData();
           	int testID = writeTestToDB();
           	writeLoadTestTransactionMinuteData(testID);
           	writeLoadTestTransactionSummary(testID);
           	writeLoadTestSummary(testID);
           	try {
				emailResults(testID);
			} catch (Throwable e) {
				e.printStackTrace();
			}
       	}
        deleteData();
        STATE=IDLE;
    }
    
    private static final String MAIL_HOST="mailhost";
    private void emailResults(int testID) throws Throwable{
    	Properties props = new Properties();
    	props.put("mail.smtp.host",MAIL_HOST);
    	Session s = Session.getDefaultInstance(props,null);
    	MimeMessage message = new MimeMessage(s);
    	message.setFrom(new InternetAddress("loadtestresults@boiseoffice.com"));
    	
    	message.addRecipient(Message.RecipientType.TO, new InternetAddress("brycealcock@boiseoffice.com"));
    	message.addRecipient(Message.RecipientType.TO, new InternetAddress("curtistaylor@boiseoffice.com"));
    	message.setSubject(buildMailSubject());
    	message.setContent(buildMailMessage(testID),"text/html");
    	Transport.send(message);
    }
    
    private void forceHardFlushOfSessionData() {
		synchronized (tmSessionTokenData) {
			Iterator iter = tmSessionTokenData.keySet().iterator();
			while (iter.hasNext()) {
				Object o = iter.next();
				SessionData sessionData = (SessionData) tmSessionTokenData.get(o);
				TimeSpanEventContainer tsec1 = getBySessionTimeSpanEventContainer(sessionData);
				tsec1.tally(sessionData.maxValue, false, false);
				iter.remove();
			}
		}
	}
	
	private static final String INSERT_LOAD_TEST = "INSERT INTO LoadTests (testName, Context_ID, Branch_ID, startTime, endTime) values (?,?,?,?,?)";

	private int writeTestToDB(){
		int branchID = ForeignKeyStore.getInstance().getForeignKey(null,testBranch,ForeignKeyStore.FK_BRANCH_TAG_ID,pStrat);
		int contextID = ForeignKeyStore.getInstance().getForeignKey(null,testContext,ForeignKeyStore.FK_CONTEXTS_CONTEXT_ID,pStrat);
		Vector values = new Vector();
		values.add(testName);
		values.add(new Integer(contextID));
		values.add(new Integer(branchID));
		values.add(testStartTime);
		values.add(testEndTime);
		return ((LoadTestPersistanceStrategy)pStrat).insertTestDefinition(INSERT_LOAD_TEST,values);
	}
	
	
  /*

	LoadTestTransactionMinuteRecords(
	   LoadTest_ID INT UNSIGNED NOT NULL,
	   insertTime  Timestamp,
	   Time        Timestamp,
	   Transaction_ID INT UNSIGNED,
	   count       INT,
	   avg         INT,
	   max         INT,
	   min         INT,
	   NinetiethPercentile INT,
	   FiftiethPercentile INT,
	   );
  */
	   
	private static final String LOAD_TEST_MINUTE_TRANSACTION = "insert into LoadTestTransactionMinuteRecords "+
	            " (LoadTest_ID, Time, Transaction_ID, count, avg, max, min, NinetiethPercentile, FiftiethPercentile) values (?,?,?,?,?,?,?,?,?) ";
    private static SimpleDateFormat sdf2 = new SimpleDateFormat("yyyyMMddHHmmss");   

	private void writeLoadTestTransactionMinuteData(int testID){
		synchronized(minutesTimeSpanEventContainer){
		Iterator iter = minutesTimeSpanEventContainer.keySet().iterator();
		//200408121724PC_007  :2      :201    :218    :0      :218    :218    :
		//012345678901234567
		while(iter.hasNext()){
			String nextKey = (String) iter.next();

            Date d = null;
            try {
                d = sdf2.parse(nextKey.substring(0, 12) + "00");
            }
            catch(java.text.ParseException pe)
            {
                d = new Date();
            }
			//pstmt.setTimestamp(2, new java.sql.Timestamp( d.getTime() ));

			//String sDate = nextKey.substring(0,12);
			//sDate +="00";
			String transactionName = nextKey.substring(12);
			TimeSpanEventContainer tsec = (TimeSpanEventContainer)minutesTimeSpanEventContainer.get(nextKey);
			Vector values = new Vector();
			values.add(new Integer(testID));
			values.add(d);
			values.add(new Integer(ForeignKeyStore.getInstance().getForeignKey(null,transactionName,ForeignKeyStore.FK_LOAD_TEST_TRANSACTION_ID,pStrat)));
			values.add(new Integer(tsec.getTotalLoads()));
			values.add(new Integer(tsec.getAverageLoadTime()));
			values.add(new Integer(tsec.getMaxLoadTime()));
			values.add(new Integer(tsec.getMinLoadTime()));
			values.add(new Integer(tsec.get90Percentile()));
			values.add(new Integer(tsec.get50Percentile()));
			((LoadTestPersistanceStrategy)pStrat).insertTestDefinition(LOAD_TEST_MINUTE_TRANSACTION,values);
		}
		}
	}
	
	private static final String LOAD_TEST_TRANSACTION = "insert into LoadTestTransactionSummary "+
				" (LoadTest_ID, Transaction_ID, count, avg, max, min, NinetiethPercentile, FiftiethPercentile) values (?,?,?,?,?,?,?,?) ";

	private void writeLoadTestTransactionSummary(int testID){
		synchronized(testTimeSpanEventContainer){
		Iterator iter = testTimeSpanEventContainer.keySet().iterator();
		//200408121724PC_007  :2      :201    :218    :0      :218    :218    :
		//012345678901234567
		while(iter.hasNext()){
			String nextKey = (String) iter.next();
			String transactionName = nextKey;
			TimeSpanEventContainer tsec = (TimeSpanEventContainer)testTimeSpanEventContainer.get(nextKey);
			Vector values = new Vector();
			values.add(new Integer(testID));
			values.add(new Integer(ForeignKeyStore.getInstance().getForeignKey(null,transactionName,ForeignKeyStore.FK_LOAD_TEST_TRANSACTION_ID,pStrat)));
			values.add(new Integer(tsec.getTotalLoads()));
			values.add(new Integer(tsec.getAverageLoadTime()));
			values.add(new Integer(tsec.getMaxLoadTime()));
			values.add(new Integer(tsec.getMinLoadTime()));
			values.add(new Integer(tsec.get90Percentile()));
			values.add(new Integer(tsec.get50Percentile()));
			((LoadTestPersistanceStrategy)pStrat).insertTestDefinition(LOAD_TEST_TRANSACTION,values);
		}
		}
	}
	private static final String LOAD_TEST_SUMMARY = "insert into LoadTestSummary "+
				" (LoadTest_ID, Script_ID, count, avg, max, min, NinetiethPercentile, FiftiethPercentile) values (?,?,?,?,?,?,?,?) ";
	
	private void writeLoadTestSummary(int testID){
		synchronized(testSessionTimeSpanEventContainer){
		Iterator iter = testSessionTimeSpanEventContainer.keySet().iterator();
		//200408121724PC_007  :2      :201    :218    :0      :218    :218    :
		//012345678901234567
		while(iter.hasNext()){
			String nextKey = (String) iter.next();
			String scriptName = nextKey;
			TimeSpanEventContainer tsec = (TimeSpanEventContainer)testSessionTimeSpanEventContainer.get(nextKey);
			Vector values = new Vector();
			values.add(new Integer(testID));
			values.add(new Integer(ForeignKeyStore.getInstance().getForeignKey(null,scriptName,ForeignKeyStore.FK_LOAD_TEST_SCRIPT_ID,pStrat)));
			values.add(new Integer(tsec.getTotalLoads()));
			values.add(new Integer(tsec.getAverageLoadTime()));
			values.add(new Integer(tsec.getMaxLoadTime()));
			values.add(new Integer(tsec.getMinLoadTime()));
			values.add(new Integer(tsec.get90Percentile()));
			values.add(new Integer(tsec.get50Percentile()));
			((LoadTestPersistanceStrategy)pStrat).insertTestDefinition(LOAD_TEST_SUMMARY,values);
		}
		}
	}


	private void deleteData(){
		logger.warn("deleteData() called....Deleting All the Load Test Data............");
		minutesTimeSpanEventContainer = new TreeMap();
		testTimeSpanEventContainer = new TreeMap();
		testSessionTimeSpanEventContainer = new TreeMap();
		tmRequestTokenData = new TreeMap();
		tmSessionTokenData = new TreeMap();
		testEndTime=null;
		testStartTime=null;
		testName="";
		testBranch="";
		testContext="";
		testMachine="";
	}


	private void createTest(String qp,UserRequestTiming urt){
		if(STATE==IDLE){
			STATE = RUNNING_INIT;
		}else if(STATE==TIME_WAIT){
			forceFlush();
			STATE = RUNNING_INIT;
		}else{
			logger.warn("State Transition Error, Can't create a Test from State: " + STATE);
			return;
		}
		String oldTestName = testName;
		String newTestName ="No Name";
		persistableTest = true;
		int testNameIndex = qp.indexOf("loadtestname=");
		if(testNameIndex>-1){
			testNameIndex +=13;
			int testNameIndexEnd = qp.indexOf('&',testNameIndex);
			if(testNameIndexEnd >-1){
				newTestName = qp.substring(testNameIndex,testNameIndexEnd);
			}else {
				newTestName  = qp.substring(testNameIndex);
			}
			if(oldTestName != null && oldTestName.equals(newTestName)){
				logger.warn("createTest Called, returning and not doing anything...");
				return;
			}
			logger.warn("createTest Called, deleting the system qp=" + qp);
			logger.warn("createTest Called, deleting the system UserRequestTiming" + urt.toString());
			deleteData();
		}
		testName = newTestName;
		testStartTime = new java.util.Date();
		testBranch = urt.getBranchName();
		testContext = urt.getContext() ;
		testMachine = urt.getServerName();
	}

	private void printData(){
		new printerClass().printToScreen();
	}

    private TimeSpanEventContainer getByTestTimeSpanEventContainer(RequestTokenData rtd) {
        String script = "TT-002";
        if (rtd.loadTestTransaction == null || rtd.loadTestTransaction.length() < 6) {
            logger.warn(
                "Error getByTestTimeSpanEventContainer..... rtd.loadTestTransaction.length<2 using TT"
                    + rtd.loadTestTransaction);
        } else {
            script = rtd.loadTestTransaction.substring(0, 6);
        }
        synchronized (testTimeSpanEventContainer) {
            TimeSpanEventContainer container = (TimeSpanEventContainer)testTimeSpanEventContainer.get(script);
            if (container == null) {
                ++timeSlices;
                Calendar c = GregorianCalendar.getInstance();
                c.setTime(rtd.lastModDate);
                container = new TimeSpanEventContainer(script, "", "", "", c);
                testTimeSpanEventContainer.put(script, container);
            }
            return container;
        }
    }
    
    
    private TimeSpanEventContainer getByMinuteTimeSpanEventContainer(RequestTokenData rtd) {
        String key = fdfKey.print(rtd.lastModDate.getTime());
        key = key + rtd.loadTestTransaction;
        //+ record.getServerName();
        // + record.getClassification();
        synchronized (minutesTimeSpanEventContainer) {
            TimeSpanEventContainer container = (TimeSpanEventContainer)minutesTimeSpanEventContainer.get(key);
            if (container == null) {
                ++timeSlices;
                Calendar c = GregorianCalendar.getInstance();
                c.setTime(rtd.lastModDate);
                container = new TimeSpanEventContainer(rtd.loadTestTransaction, "", "", "", c);
                minutesTimeSpanEventContainer.put(key, container);
            }
            return container;
        }
    }
    private TimeSpanEventContainer getBySessionTimeSpanEventContainer(SessionData rtd) {
        String script = "ZZ";
        if (rtd.loadTestTransaction == null || rtd.loadTestTransaction.length() < 2) {
            logger.warn(
                "Error getBySessionTimeSpanEventContainer..... rtd.loadTestTransaction.length<2 using ZZ"
                    + rtd.loadTestTransaction);
        } else {
            script = rtd.loadTestTransaction.substring(0, 2);
        }
        synchronized (testSessionTimeSpanEventContainer) {
            TimeSpanEventContainer container = (TimeSpanEventContainer)testSessionTimeSpanEventContainer.get(script);
            if (container == null) {
                ++timeSlices;
                Calendar c = GregorianCalendar.getInstance();
                c.setTime(rtd.lastModDate);
                container = new TimeSpanEventContainer(script, "", "", "", c);
                testSessionTimeSpanEventContainer.put(script, container);
            }
            return container;
        }
    }
    
    
    public Hashtable getData() {
        //return minutes;
        return null;
    }
    
    
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("\n\n\nAccessRecordsMinuteStats");
        sb.append(calls).append(":").append(eventsProcessed).append(":").append(timeSlices).append("\n");
       // sb.append(minutes.toString());
        return sb.toString();
    }
    public void persistData() {
    }
    private String getString(TimeSpanEventContainer tsec, String nextKey) {
        StringBuffer sb = new StringBuffer();
        int machineID =
            ForeignKeyStore.getInstance().getForeignKey(
                tsec.getAccessRecordsForeignKeys(),
                nextKey.substring(12),
                ForeignKeyStore.FK_MACHINES_MACHINE_ID,
                pStrat);
        sb.append("\nKey: " + nextKey);
        sb.append(",\n" + machineID);
        sb.append(",\n" + nextKey.substring(0, 12) + "00");
        sb.append(",\n" + tsec.getTotalLoads());
        sb.append(",\n" + tsec.getAverageLoadTime());
        sb.append(",\n" + tsec.get90Percentile());
        sb.append(",\n" + tsec.get25Percentile());
        sb.append(",\n" + tsec.get50Percentile());
        sb.append(",\n" + tsec.get75Percentile());
        sb.append(",\n" + tsec.getMaxLoadTime());
        sb.append(",\n" + tsec.getMinLoadTime());
        sb.append(",\n" + tsec.getDistinctUsers());
        sb.append(",\n" + tsec.getErrorPages());
        sb.append(",\n" + tsec.getThirtySecondLoads());
        sb.append(",\n" + tsec.getTwentySecondLoads());
        sb.append(",\n" + tsec.getFifteenSecondLoads());
        sb.append(",\n" + tsec.getTenSecondLoads());
        sb.append(",\n" + tsec.getFiveSecondLoads());
        return sb.toString();
    }
    private SimpleDateFormat sdfForClose = new SimpleDateFormat("yyyyMMddHHmmss");
    private Calendar gcForClose = GregorianCalendar.getInstance();
    public void flush() {
    }
    private class RequestTokenData implements Comparable, Runnable {
        int requestToken;
        int sessionId;
        String loadTestTransaction;
        int maxValue;
        java.util.Date lastModDate;
        int forwards = 0;
        /* (non-Javadoc)
         * @see java.lang.Comparable#compareTo(java.lang.Object)
         */
        public int compareTo(Object o) {
            if (o instanceof RequestTokenData) {
                RequestTokenData rtd = (RequestTokenData)o;
                if(rtd.sessionId == this.sessionId){
					return rtd.requestToken - this.requestToken;
                }else{
                	return rtd.sessionId - this.sessionId;
                }
            }
            logger.warn("Error trying to compare RequestTokenData to:" + o.getClass().getName());
            return 0;
        }
        /* (non-Javadoc)
         * @see java.lang.Runnable#run()
         */
        public void run() {
            long twoMinutesInMilliseconds = (long)1000 * 60 * 2;
            while (true) {
                try {
                    Thread.sleep(twoMinutesInMilliseconds);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                long currentTime = System.currentTimeMillis();
                synchronized (tmRequestTokenData) {
                    Iterator iter = tmRequestTokenData.keySet().iterator();
                    while (iter.hasNext()) {
                        Object o = iter.next();
                        RequestTokenData currentRTD = (RequestTokenData)tmRequestTokenData.get(o);
                        if(currentRTD.lastModDate==null){
                        	currentRTD.lastModDate = new java.util.Date();
                        }
                        if (currentTime - currentRTD.lastModDate.getTime() > twoMinutesInMilliseconds) {
                            TimeSpanEventContainer tsec1 = getByMinuteTimeSpanEventContainer(currentRTD);
                            tsec1.tally(currentRTD.maxValue, false, false);
                            TimeSpanEventContainer tsec2 = getByTestTimeSpanEventContainer(currentRTD);
                            tsec2.tally(currentRTD.maxValue, false, false);
                            synchronized (tmSessionTokenData) {
                                Integer sessionId = new Integer(currentRTD.sessionId);
                                SessionData sessionData = (SessionData)tmSessionTokenData.get(sessionId);
                                if (sessionData == null) {
                                    sessionData = new SessionData();
                                    sessionData.lastModDate = new java.util.Date();
                                    sessionData.loadTestTransaction = currentRTD.loadTestTransaction;
                                    sessionData.maxValue = currentRTD.maxValue;
                                    sessionData.sessionId = sessionId.intValue();
                                    tmSessionTokenData.put(sessionId, sessionData);
                                } else {
                                    sessionData.lastModDate = new java.util.Date();
                                    if (sessionData.loadTestTransaction == null) {
                                        if (currentRTD.loadTestTransaction != null
                                            && currentRTD.loadTestTransaction.length() >= 2) {
                                            sessionData.loadTestTransaction = currentRTD.loadTestTransaction.substring(0, 2);
                                        }
                                    } else if (
                                        sessionData.loadTestTransaction.length() < 2
                                            || ((currentRTD.loadTestTransaction != null)
                                                && !currentRTD.loadTestTransaction.substring(0, 2).equals(
                                                    sessionData.loadTestTransaction.substring(0, 2)))) {
                                        logger.warn(
                                            "SessionTokenData Area ... loadtest stat unit loadTestTransaction MissMatch in tallyRequestTokenData "
                                                + currentRTD.loadTestTransaction
                                                + "!="
                                                + sessionData.loadTestTransaction +"::sessionID:"+sessionData.sessionId );
                                    }
                                    sessionData.maxValue += currentRTD.maxValue;
                                }
                            }
                            iter.remove();
                        }
                    }
                }
            }
        }
    }
    
    
    private class printerClass implements Runnable {
        /* (non-Javadoc)
        * @see java.lang.Runnable#run()
        */
        public void run() {
        	int counter=0;
            while (true) {
                try {
                    Thread.sleep(1000 * 3 * 60);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                printToScreen();
            }
        }
        public void printToScreen() {

			StringBuffer stateSB = new StringBuffer();
			stateSB.append("Load Test State: " + STATE);
			logger.warn(stateSB.toString());

			if(!persistableTest){
				StringBuffer sb = new StringBuffer();
				sb.append("\n-----------------------------------------------------------------").
				append("\n-----------------------------------------------------------------").
				append("\n...........-----NO TEST RUNNING-----.....................................................................")
				.append("\ntestName:"+testName+"     .........................................")
				.append("\ntestBranch:"+testBranch+"     .........................................")
				.append("\ntestContext:"+testContext+"     .........................................")
				.append("\ntestMachine:"+testMachine+"     .........................................");
				if(testStartTime != null){
					sb.append("\ntestStartTime:"+fdf.print(testStartTime.getTime())+"     .........................................");
				}
				if(testEndTime != null){
					sb.append("\ntestStartTime:"+fdf.print(testEndTime.getTime())+"     .........................................");
				}
				logger.warn(sb.toString());
			}


            int requestTokenSize = tmRequestTokenData.size();
            int sessionDataSize = tmSessionTokenData.size();
            StringBuffer sb = new StringBuffer();
            sb.append("\n................................................................................")
			.append("\ntestName:"+testName+"     .........................................")
			.append("\ntestBranch:"+testBranch+"     .........................................")
			.append("\ntestContext:"+testContext+"     .........................................")
			.append("\ntestMachine:"+testMachine+"     .........................................");
			if(testStartTime != null){
	    		sb.append("\ntestStartTime:"+fdf.print(testStartTime.getTime())+"     .........................................");
			}
			if(testEndTime != null){
    			sb.append("\ntestStartTime:"+fdf.print(testEndTime.getTime())+"     .........................................");
			}

            sb
                .append("\nLoadTestStats....................................................................\n")
                .append(".....................................................................................\n")
                .append("\nMinute TimeSpanEventContainer")
                .append("\n key\t:count\t:avg\t:max\t:min\t:90%\t:50%\t\n");
            synchronized (minutesTimeSpanEventContainer) {
                Iterator iter = minutesTimeSpanEventContainer.keySet().iterator();
                while (iter.hasNext()) {
                    String key = (String)iter.next();
                    TimeSpanEventContainer tsec = (TimeSpanEventContainer)minutesTimeSpanEventContainer.get(key);
                    sb
                        .append(key)
                        .append("\t:")
                        .append(tsec.getTotalLoads())
                        .append("\t:")
                        .append(tsec.getAverageLoadTime())
                        .append("\t:")
                        .append(tsec.getMaxLoadTime())
                        .append("\t:")
                        .append(tsec.getMinLoadTime())
                        .append("\t:")
                        .append(tsec.get90Percentile())
                        .append("\t:")
                        .append(tsec.get50Percentile())
                        .append("\t:\n");
                }
            }
            sb.append("\n..........................................................................");
            sb.append("\n..........................................................................");
            sb.append("\nTest Totals TimeSpanEventContainer").append("\n key\t:count\t:avg\t:max\t:min\t:90%\t:50%\t\n");
            synchronized (testTimeSpanEventContainer) {
                Iterator iter = testTimeSpanEventContainer.keySet().iterator();
                while (iter.hasNext()) {
                    String key = (String)iter.next();
                    TimeSpanEventContainer tsec = (TimeSpanEventContainer)testTimeSpanEventContainer.get(key);
                    if (tsec != null) {
                        sb
                            .append(key)
                            .append("\t:")
                            .append(tsec.getTotalLoads())
                            .append("\t:")
                            .append(tsec.getAverageLoadTime())
                            .append("\t:")
                            .append(tsec.getMaxLoadTime())
                            .append("\t:")
                            .append(tsec.getMinLoadTime())
                            .append("\t:")
                            .append(tsec.get90Percentile())
                            .append("\t:")
                            .append(tsec.get50Percentile())
                            .append("\t:\n");
                    } else {
                        sb.append("tsec is null!!!!!!!");
                    }
                }
            }
            sb.append("\n..........................................................................");
            sb.append("\n..........................................................................");
            sb.append("\nTest Script Total TimeSpanEventContainer").append(
                "\n key\t:count\t:avg\t:max\t:min\t:90%\t:50%\t\n");
            synchronized (testSessionTimeSpanEventContainer) {
                Iterator iter = testSessionTimeSpanEventContainer.keySet().iterator();
                while (iter.hasNext()) {
                    String key = (String)iter.next();
                    TimeSpanEventContainer tsec = (TimeSpanEventContainer)testSessionTimeSpanEventContainer.get(key);
                    sb
                        .append(key)
                        .append("\t:")
                        .append(tsec.getTotalLoads())
                        .append("\t:")
                        .append(tsec.getAverageLoadTime())
                        .append("\t:")
                        .append(tsec.getMaxLoadTime())
                        .append("\t:")
                        .append(tsec.getMinLoadTime())
                        .append("\t:")
                        .append(tsec.get90Percentile())
                        .append("\t:")
                        .append(tsec.get50Percentile())
                        .append("\t:\n");
                }
            }
            sb.append("\n..........................................................................");
            sb.append("\n..........................................................................");
            sb.append("\nrequestTokens:").append(requestTokenSize);
            sb.append("\nsessionTokens:").append(sessionDataSize);
            logger.warn(sb.toString());
            
        }
    }
    private class SessionData implements Comparable, Runnable {
        int sessionId;
        String loadTestTransaction;
        int maxValue;
        java.util.Date lastModDate;
        /* (non-Javadoc)
         * @see java.lang.Comparable#compareTo(java.lang.Object)
         */
        public int compareTo(Object o) {
            if (o instanceof SessionData) {
                SessionData rtd = (SessionData)o;
                return rtd.sessionId - this.sessionId;
            }
            logger.warn("Error trying to compare SessionData to:" + o.getClass().getName());
            return 0;
        }
        public void run() {
        	long sleepTime = (long)1000*30;
            long twoMinutesInMilliseconds = (long)1000 * 60 * 2;
            while (true) {
                try {
                    Thread.sleep(sleepTime);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                long currentTime = System.currentTimeMillis();
                synchronized (tmSessionTokenData) {
                    Iterator iter = tmSessionTokenData.keySet().iterator();
                    while (iter.hasNext()) {
                        Object o = iter.next();
                        SessionData sessionData = (SessionData)tmSessionTokenData.get(o);
                        if (currentTime - sessionData.lastModDate.getTime() > twoMinutesInMilliseconds) {
                            TimeSpanEventContainer tsec1 = getBySessionTimeSpanEventContainer(sessionData);
                            tsec1.tally(sessionData.maxValue, false, false);
                            iter.remove();
                        }
                    }
                }
            }
        }
    }
    private TreeMap tmRequestTokenData = new TreeMap();
    private TreeMap tmSessionTokenData = new TreeMap();
    private Date lastDataDate = null;
    private void tallyRequestTokenData(RequestTokenData rtd) {
		lastDataDate = new java.util.Date();
    	if(STATE==TIME_WAIT || STATE==RUNNING_INIT){
    		if((lastDataDate.getTime() - testStartTime.getTime()) > (1000*60*10)){
    			STATE= RUNNING;
    		}else{
    			STATE = RUNNING_INIT;
    		}
    		
    	}else if(STATE != RUNNING){
    		return;
    	}
        //Integer rtdKey = new Integer(""+rtd.requestToken+""+rtd.sessionId);
        synchronized (tmRequestTokenData) {
            RequestTokenData currentRTD = (RequestTokenData)tmRequestTokenData.get(rtd);
            if (currentRTD == null) {
                tmRequestTokenData.put(rtd, rtd);
            } else {
                currentRTD.forwards++;
                currentRTD.lastModDate = new java.util.Date();
                if (currentRTD.loadTestTransaction == null) {
                    currentRTD.loadTestTransaction = rtd.loadTestTransaction;
                } else if (
                    (rtd.loadTestTransaction != null) && !currentRTD.loadTestTransaction.equals(rtd.loadTestTransaction)) {
                    logger.warn(
                        "loadtest stat unit loadTestTransaction MissMatch in tallyRequestTokenData "
                            + rtd.loadTestTransaction
                            + "!="
                            + currentRTD.loadTestTransaction);
                }
                if (currentRTD.maxValue < rtd.maxValue) {
                    currentRTD.maxValue = rtd.maxValue;
                }
                if (currentRTD.sessionId == 0) {
                    currentRTD.sessionId = rtd.sessionId;
                } else if (rtd.sessionId != currentRTD.sessionId) {
                    logger.warn(
                        "loadtest stat unit loadTestSessionID MissMatch in tallyRequestTokenData "
                            + rtd.sessionId
                            + "!="
                            + currentRTD.sessionId);
                }
            }
        }
    }
    private String buildMailSubject(){
    	StringBuffer sb = new StringBuffer();
    	sb.append("LoadTest Results  for testName->").append(testName).append(":  ");
    	return sb.toString();
    }

    

	   private static final String LOAD_TEST_COMPARISON_QUERY = 
	       "select    s.scriptName,  "+
			"  base.avg - test.avg as AVGDiff,  "+
			"  base.NinetiethPercentile - test.NinetiethPercentile as NPDiff, "+
			"  base.FiftiethPercentile  - test.FiftiethPercentile as FPDiff, "+
			"  base.avg as baseAvg, "+
			"  base.NinetiethPercentile as baseNP, "+
			"  base.FiftiethPercentile as baseFP, "+
			"  test.avg as testAvg, "+
			"  test.NinetiethPercentile as testNP, "+
			"  test.FiftiethPercentile as testFP, "+
			"  test.count as testCount "+

			" From  "+
			" LoadTestScript as s,  "+
			" LoadTestSummary as base  "+
			"   Left Outer Join LoadTestSummary as test "+
			"   on base.Script_ID=test.Script_ID "+
			"   and test.LoadTest_ID=? "+
			" where "+
			" s.Script_ID=base.Script_ID "+
			" and base.LoadTest_ID=? ";

	   private static final int baselineID=246;
	private String buildMailMessage(int testID){
		NumberFormat nf = new DecimalFormat("#,#00");
		NumberFormat nf2 = new DecimalFormat("#,##0.000");
		


		StringBuffer sb = new StringBuffer();
		sb.append("<html>");
		sb.append("<head>");
		sb.append("  <meta name=\"generator\" content=");
		sb.append("  \"HTML Tidy for Windows (vers 1st June 2004), see www.w3.org\">");
		sb.append("");
		sb.append("  <title></title>");
		sb.append("  <style>");
		sb.append("/* defaultStyle.css controls all basic styles for the website */");
		sb.append("/* menu styles are in defaultMenus.css */");
		sb.append("/* Taylor styles are in taylor.css */");
		sb.append("");
		sb.append("/* main body style - applies to all pages */");
		sb.append("   body {");
		sb.append("        margin: 0px;");
		sb.append("        padding: 0px;");
		sb.append("        scrollbar-face-color: #EFEFEF; ");
		sb.append("        scrollbar-highlight-color: #FFFFFF; ");
		sb.append("        scrollbar-shadow-color: #C0C0C0; ");
		sb.append("        scrollbar-3dlight-color: #C0C0C0; ");
		sb.append("        scrollbar-arrow-color: #000000; ");
		sb.append("        scrollbar-darkshadow-color: #EEEEEE;");
		sb.append("        background-color: #ffffff; /*#80c080;*/");
		sb.append("   }");
		sb.append("");
		sb.append("/* the default font style is set from table and td selectors */");
		sb.append("table {");
		sb.append("        color: #000000;");
		sb.append("        font-size: 13px;");
		sb.append("        font-family: Arial, Helvetica, sans-serif;");
		sb.append("}");
		sb.append("");
		sb.append("td {");
		sb.append("        color: #000000;");
		sb.append("        font-size: 13px;");
		sb.append("        font-family: Arial, Helvetica, sans-serif;");
		sb.append("        vertical-align: top;");
		sb.append("}");
		sb.append("");
		sb.append("/* shopping cart/shopping list summary table */");
		sb.append(".cartSummary {");
		sb.append("}");
		sb.append("");
		sb.append(".cartSummaryGutter {");
		sb.append("  		height: 2px;");
		sb.append("}");
		sb.append("");
		sb.append(".cartSummaryBorderTopBottom   { background-color: #000000; height: 1px; }");
		sb.append(".cartSummaryBorderSide        { background-color: #000000; width: 1px; }");
		sb.append("");
		sb.append(".cartSummaryHeadingRowShoppingCart td {");
		sb.append("        color: #FFFFFF;");
		sb.append("        background-color: #FF6600;");
		sb.append("        vertical-align: middle;");
		sb.append("}");
		sb.append("");
		sb.append(".cartSummaryHeadingRowShoppingList td { /* use this style to change the background color of the header bar when this is a shopping list summary */");
		sb.append("        color: #FFFFFF;");
		sb.append("        background-color: #006633;");
		sb.append("        vertical-align: middle;");
		sb.append("}");
		sb.append("");
		sb.append(".cartSummaryDetailRow td {");
		sb.append("        color: #000000;");
		sb.append("        background-color: #E6E6E6;");
		sb.append("        vertical-align: middle;");
		sb.append("}");
		sb.append("");
		sb.append(".cartSummaryDetailHeading {");
		sb.append("}");
		sb.append("");
		sb.append(".cartSummaryDetailDesc {");
		sb.append("        text-align: right;");
		sb.append("}");
		sb.append(".cartSummaryResultsHeading{");
		sb.append("}");
		sb.append(".cartSummaryResultsData{");
		sb.append("  text-align: right;");
		sb.append("}");
		sb.append("</style>");
		sb.append("</head>");
		sb.append("<body>");
		sb.append("              <table class=\"cartSummary\" width=\"180\" border=\"0\"");
		sb.append("              cellspacing=\"0\" cellpadding=\"0\">");
		sb.append("                <tr>");
		sb.append("                  <td class=\"cartSummaryGutter\" colspan=\"3\">");
		sb.append("                  <spacer type=\"block\" height=\"2\"></td>");
		sb.append("                </tr>");
		sb.append("                <tr>");
		sb.append("                  <td class=\"cartSummaryBorderTopBottom\" colspan=");
		sb.append("                  \"3\"><spacer type=\"block\" height=\"1\"></td>");
		sb.append("                </tr>");
		sb.append("                <tr>");
		sb.append("                  <td width=\"178\">");
		sb.append("                    <table width=\"178\" border=\"0\" cellspacing=\"0\"");
		sb.append("                    cellpadding=\"0\">");
		sb.append("                      <tr class=");
		sb.append("                      \"cartSummaryHeadingRowShoppingCart\">");
		sb.append("                      <td colspan=\"2\">&nbsp;<strong>Load Test Results Anouncement!");
		sb.append("                        </strong></td>");
		sb.append("                    </tr>");
		sb.append("                      <tr class=\"cartSummaryDetailRow\">");
		sb.append("                        <td class=\"cartSummaryDetailHeading\">&nbsp;Scenario:</td><td class=\"cartSummaryDetailDesc\">&nbsp;&nbsp;&nbsp;").append(testName).append("&nbsp;</td>");
		sb.append("                    </tr>");
		sb.append("                      <tr class=\"cartSummaryDetailRow\">");
		sb.append("                        <td class=\"cartSummaryDetailHeading\">&nbsp;Machine:</td><td class=\"cartSummaryDetailDesc\">&nbsp;&nbsp;&nbsp; ").append(testMachine).append("&nbsp;</td>");
		sb.append("                      </tr>");
		sb.append("                      <tr class=\"cartSummaryDetailRow\">");
		sb.append("                        <td class=\"cartSummaryDetailHeading\">&nbsp;Release:</td><td class=\"cartSummaryDetailDesc\">&nbsp;&nbsp;&nbsp; ").append(testBranch).append("&nbsp;</td>");
		sb.append("                      </tr>");
		sb.append("                      <tr class=\"cartSummaryDetailRow\">");
		sb.append("                        <td class=\"cartSummaryDetailHeading\">&nbsp;Context:</td><td class=\"cartSummaryDetailDesc\">&nbsp;&nbsp;&nbsp; ").append(testContext).append("&nbsp;</td>");
		sb.append("                      </tr>");
		sb.append("                      <tr class=\"cartSummaryDetailRow\">");
		sb.append("                        <td class=\"cartSummaryDetailHeading\">&nbsp;StartTime:</td><td class=\"cartSummaryDetailDesc\">&nbsp;&nbsp;&nbsp; ").append(testStartTime).append("&nbsp;</td>");
		sb.append("                    </tr>");
		sb.append("                      <tr class=\"cartSummaryDetailRow\">");
		sb.append("                        <td class=\"cartSummaryDetailHeading\">&nbsp;EndTime:</td><td class=\"cartSummaryDetailDesc\">&nbsp;&nbsp;&nbsp; ").append(testEndTime).append("&nbsp;</td>");
		sb.append("                    </tr>");
		sb.append("                    <tr>");
		sb.append("                        <td>_________________________</td><td>________________________________________________________</td>");
		sb.append("                    </tr>");
		sb.append("                    <tr>");
		sb.append("                        <td>&nbsp;</td><td>&nbsp;</td>");
		sb.append("                    </tr>");
		sb.append("                    <tr>");
		sb.append("                        <td>Contact No. </td><td>x2920</td>");
		sb.append("                    </tr>");
		sb.append("                    <tr>");
		sb.append("                        <td>Email</td><td>curtistaylor@boiseoffice.com</td>");
		sb.append("                    </tr>");
		sb.append("                   </table>");
		sb.append("                  </td>");
		sb.append("                </tr>");
		sb.append("                <tr>");
		sb.append("                  <td class=\"cartSummaryBorderSide\"><spacer type=");
		sb.append("                  \"block\" height=\"1\" width=\"1\"></td>");
		sb.append("                </tr>");
		sb.append("                <tr>");
		sb.append("                  <td>");
		sb.append("                  <table width=\"178\" border=\"0\" cellspacing=\"0\"");
		sb.append("                      <tr class=\"cartSummaryHeadingRowShoppingCart\">");
		sb.append("                      <td colspan=\"6\">&nbsp;<strong>Summary Results</strong></td>");
		sb.append("                    </tr>");
		sb.append("                      <tr class=\"cartSummaryDetailRow\">");
		sb.append("                        <td class=\"cartSummaryResultsHeading\">&nbsp;Script</td>");
		sb.append("                        <td class=\"cartSummaryResultsData\">&nbsp;Transactions</td>");
		sb.append("                        <td class=\"cartSummaryResultsData\">&nbsp;Avg. Load Time</td>");
		sb.append("                        <td class=\"cartSummaryResultsData\">&nbsp;BaseLine Avg</td>");
		sb.append("                        <td class=\"cartSummaryResultsData\">&nbsp;Diff from BaseLine</td>");
		sb.append("                        <td class=\"cartSummaryResultsData\">&nbsp;% Diff from BaseLine:</td>");
		sb.append("                      </tr>");
		sb.append("                      <tr class=\"cartSummaryDetailRow\">");
		sb.append("                        <td class=\"cartSummaryResultsHeading\">_______________</td>");
		sb.append("                        <td class=\"cartSummaryResultsData\">_______________</td>");
		sb.append("                        <td class=\"cartSummaryResultsData\">_______________</td>");
		sb.append("                        <td class=\"cartSummaryResultsData\">_______________</td>");
		sb.append("                        <td class=\"cartSummaryResultsData\">_______________</td>");
		sb.append("                        <td class=\"cartSummaryResultsData\">_______________</td>");
		sb.append("                      </tr>");
		sb.append("                      ");
		
		
		
		Connection con = null;
        try {
			con = ConnectionPoolT.getConnection();
			PreparedStatement pstmt = con.prepareStatement(LOAD_TEST_COMPARISON_QUERY);
			pstmt.setInt(1,testID);
			pstmt.setInt(2,baselineID);
			ResultSet rs = pstmt.executeQuery();
			while(rs.next()){
				sb.append("                     <tr class=\"cartSummaryDetailRow\">");
				sb.append("                        <td class=\"cartSummaryResultsHeading\">").append(rs.getString("scriptName")).append("</td>");
				sb.append("                        <td class=\"cartSummaryResultsData\">").append(rs.getString("testCount")).append(" </td>");
				sb.append("                        <td class=\"cartSummaryResultsData\">").append(nf2.format((double)rs.getInt("testAvg")/1000.0)).append("(sec)</td>");
				sb.append("                        <td class=\"cartSummaryResultsData\">").append(nf2.format((double)rs.getInt("baseAvg")/1000.0)).append("(sec)</td>");
				sb.append("                        <td class=\"cartSummaryResultsData\">").append(nf2.format((double)rs.getInt("AVGDiff")/1000.0)).append("(sec)</td>");
				sb.append("                        <td class=\"cartSummaryResultsData\">").append(nf.format(((double)rs.getInt("AVGDiff")/(double)rs.getInt("baseAvg"))*100.0)).append("%</td>");
				sb.append("                      </tr>");
			}
        } catch (SQLException e) {
            e.printStackTrace();
        }finally{
        	if(con != null){
        		try{
        			con.close();
        		}catch(SQLException se){
        			se.printStackTrace();
        		}
        	}
        }
		
				sb.append("                          ");
		sb.append("                  </table>");
		sb.append("                  </td>");
		sb.append("                </tr>");

		sb.append("                <tr>");
		sb.append("                  <td class=\"cartSummaryBorderSide\"><spacer type=");
		sb.append("                  \"block\" height=\"1\" width=\"1\"></td>");
		sb.append("                </tr>");
		
		sb.append("                <tr>");
		sb.append("                  <td class=\"cartSummaryResultsHeading\">");
		sb.append("                  <BR/>Please Visit the Links below for Details.<BR/></td>");
		sb.append("                </tr>");
		
		sb.append("                <tr>");
		sb.append("                  <td class=\"cartSummaryResultsHeading\">");
		sb.append("                  <a href=\"http://test-art-app1.int.bcop.com/logparser-website/ViewLoadTestDetail.web?loadTestID="
				                     +testID +"\"> Complete Test Details </a></td>");
		sb.append("                </tr>");
		sb.append("                <tr>");
		sb.append("                  <td class=\"cartSummaryResultsHeading\">");
		sb.append("                  <a href=\"http://test-art/logparser-website/ViewLoadTestDiff.web?baseReport="+baselineID+"&"+testID+"=on&CompareName=\"> Baseline Diff Details </a></td>");
		sb.append("                </tr>");
		
		
		sb.append("                <tr>");
		sb.append("                  <td class=\"cartSummaryBorderTopBottom\" colspan=");
		sb.append("                  \"3\"><spacer type=\"block\" height=\"1\"></td>");
		sb.append("                </tr>");
		sb.append("                <tr>");
		sb.append("                  <td class=\"cartSummaryGutter\" colspan=\"3\">");
		sb.append("                  <spacer type=\"block\" height=\"2\"></td>");
		sb.append("                </tr>");
		sb.append("              </table>");
		sb.append("</body>");
		sb.append("</html>");
		
		
		return sb.toString();
	}
    
    
    
    private static class LoadTestPersistanceStrategy extends AccessRecordPersistanceStrategy {
    	
		private static LoadTestPersistanceStrategy instance;
		public static AccessRecordPersistanceStrategy getInstance() {
			if (instance == null) {
				instance = new LoadTestPersistanceStrategy();
			}
			return instance;
		}
		
		
		int insertTestDefinition(String sqlInsert, Vector insertValues){
			int returnValue = 0;
			Connection con = null;
            try {
				con = ConnectionPoolT.getConnection();
                returnValue = insertForeignKey(sqlInsert,insertValues, con);
            } catch (SQLException e) {
                e.printStackTrace();
            }finally{
            	if(con != null){
            		try{
            			con.close();
            		}catch(SQLException se){
            			se.printStackTrace();
            		}
            	}
            }
            return returnValue;
		}
		
		
        @Override
		public int writeForeignKey(String foreignKeyName, String foreignKeyValue) {
			
				if (foreignKeyName.equals(ForeignKeyStore.FK_LOAD_TEST_SCRIPT_ID)) {
					return insertLoadTestScript(foreignKeyValue);
				} else if (foreignKeyName.equals(ForeignKeyStore.FK_LOAD_TEST_TRANSACTION_ID)) {
					return insertLoadTestTransaction(foreignKeyValue);
				}  else {
					
					return super.writeForeignKey(foreignKeyName,foreignKeyValue);
				}
			}
			
		private static final String FK_LOAD_TEST_SCRIPT_INSERT = "insert into LoadTestScript (scriptName) values (?)";
		private static final String FK_LOAD_TEST_SCRIPT_SELECT = "select Script_ID from LoadTestScript where scriptName=?";
		private static final String FK_LOAD_TEST_TRANSACTION_INSERT = "insert into LoadTestTransactions (transactionName, Script_ID) values (?,?) ";
		private static final String FK_LOAD_TEST_TRANSACTION_SELECT = "select Transaction_ID from LoadTestTransactions where transactionName=? and Script_ID=?";
		    protected int insertLoadTestScript(String scriptName) {
				String sqlSelect = FK_LOAD_TEST_SCRIPT_SELECT;
				String sqlInsert = FK_LOAD_TEST_SCRIPT_INSERT;
				Vector bindParams = new Vector();
				bindParams.add(scriptName);
				return insertForeignKey(sqlSelect, bindParams, sqlInsert, bindParams);
			}
			
		protected int insertLoadTestTransaction(String transaction) {
			String sqlSelect = FK_LOAD_TEST_TRANSACTION_SELECT;
			String sqlInsert = FK_LOAD_TEST_TRANSACTION_INSERT;
			Vector bindParams = new Vector();
			bindParams.add(transaction);
			int Script_ID = ForeignKeyStore.getInstance().getForeignKey(null,transaction.substring(0,2),ForeignKeyStore.FK_LOAD_TEST_SCRIPT_ID,this);
			bindParams.add(new Integer(Script_ID));
			return insertForeignKey(sqlSelect, bindParams, sqlInsert, bindParams);
		}
    }
}
