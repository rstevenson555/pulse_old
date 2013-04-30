/*
 * Created on Dec 12, 2003
 * 
 * To change the template for this generated file go to Window - Preferences -
 * Java - Code Generation - Code and Comments
 */
package com.bos.helper;

import com.bos.arch.HibernateUtil;
import com.bos.model.CalendarBean;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Session;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
/**
 * @author I0360D4
 * 
 * To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Generation - Code and Comments
 */
public class ViewLoadTestDiffHelper {
    private Document MainScreenDoc = null;
    DocumentBuilderFactory factory = null;
    DocumentBuilder builder = null;
    Document doc = null;
    String selectedDate = null;
    ArrayList testList ;
    int iBaseLoadTest=0;
    String queryParams;
    /*
        public ViewHistoricalChartsHelper() {
    		initialize();
    	}
    */
    public ViewLoadTestDiffHelper(String selectedDate, ArrayList al, int baseID, String qp) {
        queryParams = qp;
        setSelectedDate(selectedDate);
        testList =al;
        iBaseLoadTest =baseID;
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
        Element pageElement = MainScreenDoc.createElement("Page");
        Element headerElement = MainScreenDoc.createElement("DashBoard");
        Element leftPanelElement = MainScreenDoc.createElement("LeftPanel");
        Element bodyElement = MainScreenDoc.createElement("Body");
        Element payload = MainScreenDoc.createElement("Payload");
        //Element dailySummary =
        //	appendDailySummaryElement(MainScreenDoc, payload);
        //Element dailyPageSummary =
        // 	appendDailyPageSummaryElement(MainScreenDoc, payload);
        Element loadTestElement = appendLoadTests(MainScreenDoc, payload);
        //Element e =	appendDailySessionStatistics(MainScreenDoc,payload);
        pageElement.appendChild(headerElement);
        pageElement.appendChild(leftPanelElement);
        pageElement.appendChild(bodyElement);
        pageElement.appendChild(payload);
        MainScreenDoc.appendChild(pageElement);
        System.out.println(com.bcop.arch.builder.XMLUtils.documentToString(MainScreenDoc));
    }
    private static final String LOAD_TEST_QUERY =
        "select LoadTest_ID, testName, c.contextName, b.branchName, startTime,endTime "
            + "from LoadTests lt, Contexts c, Branches b "
            + "where lt.Context_ID=c.Context_ID and b.Branch_Tag_ID=lt.Branch_ID ";

    private Element appendLoadTests(Document doc, Element payload) {
    	Integer baseTest = new Integer(iBaseLoadTest);

        Element loadTests = null;
        //		Session session = null;
        loadTests = doc.createElement("LoadTests");
        Element calendarElement = null;
        calendarElement = doc.createElement("Calendar");
        calendarElement.appendChild(createCalendarElement(doc));
        payload.appendChild(calendarElement);
		for(int i =0; i<testList.size(); ++i){
			Element loadTest = doc.createElement("LoadTest");
			Integer compareTest = (Integer)testList.get(i);
			appendLoadTestCompare(baseTest, compareTest,doc,loadTest);
			appendLoadTestDetailCompare(baseTest,compareTest,doc,loadTest);
			appendLoadTestDetailLRCompare(baseTest,compareTest,doc,loadTest);
			loadTests.appendChild(loadTest);
		}
        payload.appendChild(loadTests);
        return loadTests;
    }

    
    
    private static final String LOAD_TESTS_DESC = " select testName, c.contextName, b.branchName, startTime,endTime "+
            " from LoadTests l, Branches b, Contexts c "+
            " where LoadTest_ID=? and l.Context_ID=c.Context_ID and b.Branch_Tag_ID=l.Branch_ID";
    
    
	private static final String LOAD_TEST_DETAIL_COMPARISON_QUERY = 
			    " select    s.transactionName , s.transactionDesc,  "+
				"  base.avg - test.avg as AVGDiff,  "+
				"  base.NinetiethPercentile - test.NinetiethPercentile as NPDiff, "+
				"  base.FiftiethPercentile  - test.FiftiethPercentile as FPDiff, "+
				"  base.avg as baseAvg, "+
				"  base.NinetiethPercentile as baseNP, "+
				"  base.FiftiethPercentile as baseFP, "+
				"  test.avg as testAvg, "+
				"  test.NinetiethPercentile as testNP, "+
				"  test.FiftiethPercentile as testFP "+
				" From  "+
				" LoadTestTransactions as s,  "+
				" LoadTestTransactionSummary as base  "+
				"  Left Outer Join LoadTestTransactionSummary as test "+
				"  on base.Transaction_ID=test.Transaction_ID "+
				"  and test.LoadTest_ID=? "+
				" where "+
				" s.Transaction_ID=base.Transaction_ID "+
				" and base.LoadTest_ID=? ";

    
    private void appendLoadTestDetailCompare(Integer base, Integer compare, Document doc, Element loadTests){
		Connection con = null;
				try {
					Session session = HibernateUtil.currentSession();
					con = session.connection();
					PreparedStatement pstmt = con.prepareStatement(LOAD_TEST_DETAIL_COMPARISON_QUERY);
					pstmt.setInt(1,compare.intValue());
					pstmt.setInt(2,base.intValue());
					ResultSet rs = pstmt.executeQuery();
					Element testComparison = doc.createElement("ComparisonTestDetails");
					while(rs.next()){
						Element transaction = doc.createElement("Transaction");
						String transactionName   = rs.getString(1);
						String transactionDesc   = rs.getString(2);
						String avgDiff           = rs.getString("AVGDiff");
						String NPDiff            = rs.getString("NPDiff");
						String FPDiff            = rs.getString("FPDiff");
						String baseAvg           = rs.getString("baseAvg");
						String baseNP            = rs.getString("baseNP");
						String baseFP            = rs.getString("baseFP");
						String testAvg           = rs.getString("testAvg");
						String testNP            = rs.getString("testNP");
						String testFP            = rs.getString("testFP");
            	
						Element transactionNameElement = doc.createElement("transactionName");
						transactionNameElement.appendChild(doc.createTextNode(transactionName));
						Element transactionDescElement = doc.createElement("transactionDesc");
						transactionDescElement.appendChild(doc.createTextNode(transactionDesc));
						Element avgDiffElement = doc.createElement("AvgDiff");
						avgDiffElement.appendChild(doc.createTextNode(avgDiff));
						Element NPDiffElement = doc.createElement("NPDiff");
						NPDiffElement.appendChild(doc.createTextNode(NPDiff));
						Element FPDiffElement = doc.createElement("FPDiff");
						FPDiffElement.appendChild(doc.createTextNode(FPDiff));
						Element baseAvgElement = doc.createElement("baseAvg");
						baseAvgElement.appendChild(doc.createTextNode(baseAvg));
						Element baseNPElement = doc.createElement("baseNP");
						baseNPElement.appendChild(doc.createTextNode(baseNP));
						Element baseFPElement = doc.createElement("baseFP");
						baseFPElement.appendChild(doc.createTextNode(baseFP));
						Element testAvgElement = doc.createElement("testAvg");
						testAvgElement.appendChild(doc.createTextNode(testAvg));
						Element testNPElement = doc.createElement("testNP");
						testNPElement.appendChild(doc.createTextNode(testNP));
						Element testFPElement = doc.createElement("testFP");
						testFPElement.appendChild(doc.createTextNode(testFP));
            	
						transaction.appendChild(transactionNameElement);
						transaction.appendChild(transactionDescElement);
						transaction.appendChild(avgDiffElement);
						transaction.appendChild(NPDiffElement);
						transaction.appendChild(FPDiffElement);
						transaction.appendChild(baseAvgElement);
						transaction.appendChild(baseNPElement);
						transaction.appendChild(baseFPElement);
						transaction.appendChild(testAvgElement);
						transaction.appendChild(testNPElement);
						transaction.appendChild(testFPElement);
            
						testComparison.appendChild(transaction);
					}
					loadTests.appendChild(testComparison);
				} catch (HibernateException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (DOMException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}finally{
					try {
						HibernateUtil.closeSession();
					} catch (HibernateException e1) {
						 e1.printStackTrace();
					}
				}
    	
    }
    
    
    private static final String LOAD_TEST_DETAIL_LOAD_RUNNER_COMPARISON_QUERY = 
	" select ag.name, ag.tid, ag.tDesc, test1.cnt - test2.cnt as countDiff, test1.avgLT-test2.avgLT as avgDiff,  "+
	" 	   test1.maxLT-test2.maxLT as maxDiff, test2.cnt,test2.avgLT, test2.maxLT "+
	" From "+
	" (select ear.Classification_ID as classID,  "+
	" 	   count(*) as cnt,  "+
	" 	   avg(LoadTime) as avgLT,  "+
	" 	   max(LoadTime) as maxLT,  "+
	" 	   min(LoadTime) as minLT "+
	" 		 from ExternalAccessRecords ear  "+
	" 			where Time>=? and Time<=? "+
	" 			and Classification_ID>100 "+
	" 			  group by Classification_ID) as test1, "+
	" (select ear.Classification_ID as classID,  "+
	" 	   count(*) as cnt,  "+
	" 	   avg(LoadTime) as avgLT,  "+
	" 	   max(LoadTime) as maxLT,  "+
	" 	   min(LoadTime) as minLT "+
	" 		 from ExternalAccessRecords ear  "+
	" 			where Time>=? and Time<=? "+
	" 			and Classification_ID>100 "+
	" 			  group by Classification_ID) as test2,  "+
	" (select es.Classification_ID as classify,  "+
	" 	   es.Description as name,  "+
	" 	   ls.Transaction_ID as tid,  "+
	" 	   ls.transactionDesc as tDesc "+
	" 		   from LoadTestTransactions ls, ExternalStats es "+
	" 			  where ls.transactionName=es.Description) as ag "+
	" 	   where  "+
	" 	   ag.classify=test1.classID and "+
	" 	   ag.classify=test2.classID ";
       
	private void appendLoadTestDetailLRCompare(Integer base, Integer compare, Document doc, Element loadTests){
		Connection con = null;
				try {
//					" select ag.name, ag.tid, ag.tDesc, test1.cnt - test2.cnt as countDiff, test1.avgLT-test2.avgLT as avgDiff,  "+
//					" 	   test1.maxLT-test2.maxLT as maxDiff, test2.cnt,test2.avgLT, test2.maxLT "+
					Session session = HibernateUtil.currentSession();
					con = session.connection();
					PreparedStatement pstmt = con.prepareStatement(LOAD_TEST_DETAIL_LOAD_RUNNER_COMPARISON_QUERY);
					pstmt.setString(1,getTIDTime(con, base.intValue(),"startTime"));
					pstmt.setString(2,getTIDTime(con, base.intValue(),"endTime"));
					pstmt.setString(3,getTIDTime(con, compare.intValue(),"startTime"));
					pstmt.setString(4,getTIDTime(con, compare.intValue(),"endTime"));
					ResultSet rs = pstmt.executeQuery();
					Element testComparison = doc.createElement("LRComparisonTestDetails");
					while(rs.next()){
						Element transaction = doc.createElement("LRTransaction");
						String transactionName   = rs.getString(1);
						String transactionDesc   = rs.getString(3);
						String countDiff         = rs.getString(4);
						String avgDiff           = rs.getString(5);
						String maxDiff           = rs.getString(6);
						String testCnt           = rs.getString(7);
						String testAvg           = rs.getString(8);
						String testMax            = rs.getString(9);
            	
						Element transactionNameElement = doc.createElement("LRtransactionName");
						transactionNameElement.appendChild(doc.createTextNode(transactionName));
						
						Element transactionDescElement = doc.createElement("LRtransactionDesc");
						transactionDescElement.appendChild(doc.createTextNode(transactionDesc));
						
						Element avgDiffElement = doc.createElement("LRAvgDiff");
						avgDiffElement.appendChild(doc.createTextNode(avgDiff));
						
						Element CountDiffElement = doc.createElement("LRCountDiff");
						CountDiffElement.appendChild(doc.createTextNode(countDiff));
						
						Element MaxDiffElement = doc.createElement("LRMaxDiff");
						MaxDiffElement.appendChild(doc.createTextNode(maxDiff));
						
						Element testCntElement = doc.createElement("LRTestCnt");
						testCntElement.appendChild(doc.createTextNode(testCnt));
						
						Element testAvgElement = doc.createElement("LRTestAvg");
						testAvgElement.appendChild(doc.createTextNode(testAvg));
						
						Element testMaxElement = doc.createElement("LRTestMax");
						testMaxElement.appendChild(doc.createTextNode(testMax));
            	
						transaction.appendChild(transactionNameElement);
						transaction.appendChild(transactionDescElement);
						transaction.appendChild(avgDiffElement);
						transaction.appendChild(CountDiffElement);
						transaction.appendChild(MaxDiffElement);
						transaction.appendChild(testCntElement);
						transaction.appendChild(testAvgElement);
						transaction.appendChild(testMaxElement);
            
						testComparison.appendChild(transaction);
					}
					loadTests.appendChild(testComparison);
				} catch (HibernateException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (DOMException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}finally{
					try {
						HibernateUtil.closeSession();
					} catch (HibernateException e1) {
						 e1.printStackTrace();
					}
				}
    	
	}
	
	private static final String LOAD_TEST = "SELECT startTime, endTime from LoadTests where LoadTest_ID=?";

	public String getTIDTime(Connection con, int tid, String whichTime) throws SQLException{
		PreparedStatement pstmt = con.prepareStatement(LOAD_TEST);
		pstmt.setInt(1,tid);
		ResultSet rs = pstmt.executeQuery();
		String s=null;
		if(rs.next()){
			s = rs.getString(whichTime);
			System.out.println("Time: "+s);
		}
		rs.close();
		pstmt.close();
		return s;
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
			"  test.FiftiethPercentile as testFP "+
			"From  "+
			"LoadTestScript as s,  "+
			"LoadTestSummary as base  "+
			"   Left Outer Join LoadTestSummary as test "+
			"   on base.Script_ID=test.Script_ID "+
			"   and test.LoadTest_ID=? "+
			"where "+
			"s.Script_ID=base.Script_ID "+
			"and base.LoadTest_ID=? ";
			
	private Element getTestDesc(int test, Document doc, Connection con,String testType) throws SQLException{
		PreparedStatement pstmt = con.prepareStatement(LOAD_TESTS_DESC);
		pstmt.setInt(1,test);
		ResultSet rs = pstmt.executeQuery();
		Element testElement = doc.createElement(testType);
		if(rs.next()){
			String testName = rs.getString(1);
			String contextName = rs.getString(2);
			String branchName = rs.getString(3);
			String startTime = rs.getString(4);
			String endTime = rs.getString(5);
			Element testNameElement = doc.createElement("TestName");
			Element contextNameElement = doc.createElement("ContextName");
			Element branchNameElement = doc.createElement("BranchName");
			Element startTimeElement = doc.createElement("startTime");
			Element endTimeElement = doc.createElement("endTime");
			Element linkElement = doc.createElement("Link");
			
            
			testNameElement.appendChild(doc.createTextNode(testName));
			contextNameElement.appendChild(doc.createTextNode(contextName));
			branchNameElement.appendChild(doc.createTextNode(branchName));
			startTimeElement.appendChild(doc.createTextNode(startTime));
			endTimeElement.appendChild(doc.createTextNode(endTime));
            linkElement.appendChild(doc.createTextNode("ViewLoadTestDiff.web"+queryParams));
			testElement.appendChild(testNameElement);
			testElement.appendChild(contextNameElement);
			testElement.appendChild(branchNameElement);
			testElement.appendChild(startTimeElement);
			testElement.appendChild(endTimeElement);
            testElement.appendChild(linkElement);
		}
		return testElement;
	}

			
    private void appendLoadTestCompare(Integer base, Integer compare, Document doc, Element loadTests)  {
		Connection con = null;
        try {
            Session session = HibernateUtil.currentSession();
            con = session.connection();
            PreparedStatement pstmt = con.prepareStatement(LOAD_TEST_COMPARISON_QUERY);
            pstmt.setInt(1,compare.intValue());
            pstmt.setInt(2,base.intValue());
            ResultSet rs = pstmt.executeQuery();
            Element testComparison = doc.createElement("ComparisonTest");
            Element baseTest = getTestDesc(base.intValue(),doc, con,"BaseTest");
            Element compareTest = getTestDesc(compare.intValue(),doc, con, "CompareTest");

			testComparison.appendChild(baseTest);
			testComparison.appendChild(compareTest);
			
            while(rs.next()){
            	Element transaction = doc.createElement("Transaction");
            	String scriptName    = rs.getString(1);
            	String avgDiff       = rs.getString("AVGDiff");
            	String NPDiff        = rs.getString("NPDiff");
            	String FPDiff        = rs.getString("FPDiff");
            	String baseAvg       = rs.getString("baseAvg");
            	String baseNP        = rs.getString("baseNP");
            	String baseFP        = rs.getString("baseFP");
            	String testAvg       = rs.getString("testAvg");
            	String testNP        = rs.getString("testNP");
            	String testFP        = rs.getString("testFP");
            	
            	Element scriptNameElement = doc.createElement("scriptName");
            	scriptNameElement.appendChild(doc.createTextNode(scriptName));
            	Element avgDiffElement = doc.createElement("AvgDiff");
            	avgDiffElement.appendChild(doc.createTextNode(avgDiff));
            	Element NPDiffElement = doc.createElement("NPDiff");
            	NPDiffElement.appendChild(doc.createTextNode(NPDiff));
            	Element FPDiffElement = doc.createElement("FPDiff");
            	FPDiffElement.appendChild(doc.createTextNode(FPDiff));
            	Element baseAvgElement = doc.createElement("baseAvg");
            	baseAvgElement.appendChild(doc.createTextNode(baseAvg));
            	Element baseNPElement = doc.createElement("baseNP");
            	baseNPElement.appendChild(doc.createTextNode(baseNP));
            	Element baseFPElement = doc.createElement("baseFP");
            	baseFPElement.appendChild(doc.createTextNode(baseFP));
            	Element testAvgElement = doc.createElement("testAvg");
            	testAvgElement.appendChild(doc.createTextNode(testAvg));
            	Element testNPElement = doc.createElement("testNP");
            	testNPElement.appendChild(doc.createTextNode(testNP));
            	Element testFPElement = doc.createElement("testFP");
            	testFPElement.appendChild(doc.createTextNode(testFP));
            	
            	transaction.appendChild(scriptNameElement);
            	transaction.appendChild(avgDiffElement);
            	transaction.appendChild(NPDiffElement);
            	transaction.appendChild(FPDiffElement);
            	transaction.appendChild(baseAvgElement);
            	transaction.appendChild(baseNPElement);
            	transaction.appendChild(baseFPElement);
            	transaction.appendChild(testAvgElement);
            	transaction.appendChild(testNPElement);
            	transaction.appendChild(testFPElement);
            
            	testComparison.appendChild(transaction);
            }
			loadTests.appendChild(testComparison);
        } catch (HibernateException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (DOMException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }finally{
            try {
                HibernateUtil.closeSession();
            } catch (HibernateException e1) {
                 e1.printStackTrace();
            }
        }
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
        return (Element)doc.importNode(calendarBean.toDOM().getFirstChild(), true);
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
     * @param selectedDate The selectedDate to set.
     */
    public void setSelectedDate(String selectedDate) {
        this.selectedDate = selectedDate;
    }
}
