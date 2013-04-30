/*
 * Created on December 7, 2012
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package com.bos.helper;

import com.bos.arch.HibernateUtil;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Session;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.DefaultXYItemRenderer;
import org.jfree.chart.plot.SeriesRenderingOrder;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
/**
 * @author I081299
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class DollarsAndOrdersHelper {
	private static final String DAILY_SUMMARY_QUERY = "SELECT AccumulatorStat_ID, a.Time, c.contextName, a.Value, a.Count "
			+ "from AccumulatorStats a, Contexts c where a.Context_ID=c.Context_ID and Time >= ? and Time <= ? "
			+ "order by Time desc";	
	
    public static final int ARIBA_DIRECT_DOLLARS = 2002;
    public static final int SAP_DIRECT_DOLLARS = 2004;
    public static final int WM_DIRECT_DOLLARS = 2006;
    public static final int BOS_CHECKOUT_DOLLARS = 2008;
    public static final int BOS_GUEST_DOLLARS = 2010;

	public JFreeChart generateDollarsAndOrdersGraph(String startDateStr, String endDateStr) {		
		TimeSeries dollarsSeries = new TimeSeries("Dollars");
		TimeSeries ordersSeries = new TimeSeries("Orders");
		
		TimeSeriesCollection dollarsDataSet = new TimeSeriesCollection();
		TimeSeriesCollection ordersDataSet = new TimeSeriesCollection();
		dollarsDataSet.addSeries(dollarsSeries);
		ordersDataSet.addSeries(ordersSeries);
		
		DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyyMMdd");
		DateTime startDateTime = formatter.parseDateTime(startDateStr);
		DateTime endDateTime = formatter.parseDateTime(endDateStr);
		String formattedStartDate = startDateTime.toString("dd MMMMM yyyy");
		String formattedEndDate = endDateTime.toString("dd MMMMM yyyy");
		
		DateAxis dateAxis = new DateAxis(formattedStartDate + " - " + formattedEndDate);
		dateAxis.setLabelPaint(Color.white);
		dateAxis.setTickLabelPaint(Color.white);
		dateAxis.setLabelFont(new Font("Arial", Font.BOLD, 12));
		dateAxis.setDateFormatOverride(new SimpleDateFormat("yyyy-MMM"));
		dateAxis.setLowerMargin(0.0);
        dateAxis.setUpperMargin(0.0);
        dateAxis.setTickLabelsVisible(true);
        
        NumberAxis rangeAxis = new NumberAxis("Dollars");
        rangeAxis.setLabelPaint(Color.white);
        rangeAxis.setLabelFont(new Font("Arial", Font.BOLD, 12));
        rangeAxis.setTickLabelPaint(Color.white);
        NumberFormat df = DecimalFormat.getCurrencyInstance(); //new DecimalFormat("##,##0");
        rangeAxis.setAutoTickUnitSelection(true);
        rangeAxis.setAutoRangeIncludesZero(true);
        rangeAxis.setNumberFormatOverride(df);
        rangeAxis.setLowerBound(0.0);
        rangeAxis.setUpperBound(14000000.0);
        
        XYItemRenderer renderer = new DefaultXYItemRenderer();
        renderer.setSeriesPaint(0, new Color(0, 143, 255));
        renderer.setSeriesStroke(0, new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        for (int j = 1; j < 10; j++)
            renderer.setSeriesStroke(
                j,
                new BasicStroke(1f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 0, new float[] { 10.0f, 5.0f }, 0));
		
        NumberAxis rangeAxis2 = new NumberAxis("Orders");
        rangeAxis2.setLabelPaint(Color.white);
        rangeAxis2.setLabelFont(new Font("Arial", Font.BOLD, 12));
        rangeAxis2.setTickLabelPaint(Color.white);
        java.text.DecimalFormat df2 = new java.text.DecimalFormat("#000");
        rangeAxis2.setNumberFormatOverride(df2);
        rangeAxis2.setLowerMargin(0.00);
        rangeAxis2.setUpperMargin(1.0); // to leave room for dollars line 
        XYBarRenderer renderer2 = new XYBarRenderer(0.20);
        renderer2.setSeriesPaint(0, new Color(43, 217, 0));
        renderer2.setSeriesOutlinePaint(0, Color.black);
        renderer2.setShadowVisible(false);
        renderer2.setDrawBarOutline(false);
        
        XYPlot xyplot = new XYPlot(ordersDataSet, dateAxis, rangeAxis2, renderer2);
        
        String plot_bgcolor = "#000000";
        if (plot_bgcolor != null && plot_bgcolor.length() > 0) {
            xyplot.setBackgroundPaint(Color.decode(plot_bgcolor));
        } else {
            xyplot.setBackgroundPaint(Color.black);
        }
        xyplot.setRangeAxisLocation(AxisLocation.BOTTOM_OR_RIGHT);
        xyplot.setRangeAxis(1, rangeAxis);
        xyplot.setRangeAxisLocation(AxisLocation.BOTTOM_OR_LEFT);
        xyplot.setDataset(1, dollarsDataSet);
        xyplot.mapDatasetToRangeAxis(1, 1);
        xyplot.setRenderer(1, renderer);
        xyplot.setDatasetRenderingOrder(org.jfree.chart.plot.DatasetRenderingOrder.FORWARD);
        xyplot.setSeriesRenderingOrder(SeriesRenderingOrder.REVERSE);
        
        TextTitle chartTitle = new TextTitle("Dollars and Orders");
        chartTitle.setPaint(Color.white);
        chartTitle.setFont(new Font("Verdana", Font.BOLD, 20));
        
        JFreeChart chart = new JFreeChart("", JFreeChart.DEFAULT_TITLE_FONT, xyplot, true);
        chart.setBackgroundPaint(new Color(70, 70, 70));
        chart.setTitle(chartTitle);        
        
        
        Session session = null;
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            SimpleDateFormat sdf2 = new SimpleDateFormat("yyyyMMddHHmmss");
            SimpleDateFormat sdf3 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            java.util.Date startDate = sdf2.parse(startDateStr + "000000");
            java.util.Date endDate   = sdf2.parse(endDateStr + "235959");
            
            session = HibernateUtil.currentSession();
            con = session.connection();
            System.out.println("ChartGeneratorHelper connection: " + con);
            
            java.sql.Date startTimeSQL = new java.sql.Date(startDate.getTime());
            java.sql.Date endTimeSQL = new java.sql.Date(endDate.getTime());
            
            session = HibernateUtil.currentSession();
            con = session.connection();
            System.out.println("ChartGeneratorHelper connection: " + con);
            pstmt = con.prepareStatement(DAILY_SUMMARY_QUERY);
            pstmt.setDate(1, startTimeSQL);
            pstmt.setDate(2, endTimeSQL);
            
            double totalDollars = 0.0;
            int totalOrders = 0;
            long prevDate = 0;
            boolean firstRecord = true;
            rs = pstmt.executeQuery();
            
            while (rs.next()) {
                int accumulatorID = rs.getInt("AccumulatorStat_ID");
                Date currDate = rs.getDate("Time");
                int value = rs.getInt("Value");
                int count = rs.getInt("Count");                
                Day day = new Day(currDate);
                
                // Calculate the sum of all forms of revenue for the day. If a new date is retrieved, reset 
                // the sum values to calculate the sum for the new day.
                if (currDate.getTime() == prevDate || firstRecord) {
                	// Only add data if we are dealing with dollars and orders records
	                if (accumulatorID == ARIBA_DIRECT_DOLLARS || accumulatorID == SAP_DIRECT_DOLLARS ||
	            		accumulatorID == WM_DIRECT_DOLLARS || accumulatorID == BOS_CHECKOUT_DOLLARS || accumulatorID == BOS_GUEST_DOLLARS) {
	                	totalDollars += value;
	                	totalOrders += count;
	                	
	                	// Update graph with dollars and orders values 
	                	dollarsSeries.addOrUpdate(day, totalDollars/100.0);
	                	ordersSeries.addOrUpdate(day, totalOrders);
	                }
	                firstRecord = false;
                } else {
                	totalDollars = 0.0;
                	totalOrders = 0;
                }
                
                prevDate = currDate.getTime();
            }
        } catch (HibernateException e) {
	        e.printStackTrace();
	    } catch (java.text.ParseException e){
	        e.printStackTrace();
	    } catch (SQLException se){
              se.printStackTrace();
        } finally {
	    	try {
                pstmt.close();
                rs.close();
	    		con.close();
		    	session.disconnect();
		    	session.close();
		    	HibernateUtil.closeSession();		    	
	    	} 
	    	catch (SQLException sqle) { sqle.printStackTrace(); } 
    	     catch (HibernateException he) { he.printStackTrace(); }
	    }
        		
		return chart;
	}
	
	public JFreeChart generateAvgDollarsPerOrderGraph(String startDateStr, String endDateStr) {		
		TimeSeries avgDollarsSeries = new TimeSeries("Avg Dollars Per Order");
		
		TimeSeriesCollection dollarsDataSet = new TimeSeriesCollection();
		dollarsDataSet.addSeries(avgDollarsSeries);
		
		DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyyMMdd");
		DateTime startDateTime = formatter.parseDateTime(startDateStr);
		DateTime endDateTime = formatter.parseDateTime(endDateStr);
		String formattedStartDate = startDateTime.toString("dd MMMMM yyyy");
		String formattedEndDate = endDateTime.toString("dd MMMMM yyyy");
		
		DateAxis dateAxis = new DateAxis(formattedStartDate + " - " + formattedEndDate);
		dateAxis.setLabelPaint(Color.white);
		dateAxis.setTickLabelPaint(Color.white);
		dateAxis.setLabelFont(new Font("Arial", Font.BOLD, 12));
		dateAxis.setDateFormatOverride(new SimpleDateFormat("yyyy-MMM"));
		dateAxis.setLowerMargin(0.0);
		dateAxis.setUpperMargin(0.0);
		dateAxis.setTickLabelsVisible(true);
		
		NumberAxis rangeAxis = new NumberAxis("Dollars");
		rangeAxis.setLabelPaint(Color.white);
		rangeAxis.setLabelFont(new Font("Arial", Font.BOLD, 12));
		rangeAxis.setTickLabelPaint(Color.white);
		NumberFormat df = DecimalFormat.getCurrencyInstance(); //new DecimalFormat("##,##0");
		rangeAxis.setAutoRange(true);
		rangeAxis.setAutoTickUnitSelection(true);
		rangeAxis.setAutoRangeIncludesZero(true);
		rangeAxis.setNumberFormatOverride(df);
		rangeAxis.setLowerBound(0.0);
		rangeAxis.setUpperBound(800.0);
		
		XYItemRenderer renderer = new DefaultXYItemRenderer();
		renderer.setSeriesPaint(0, Color.decode("#FF4949"));
		renderer.setSeriesStroke(0, new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
		for (int j = 1; j < 10; j++)
			renderer.setSeriesStroke(
					j,
					new BasicStroke(1f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 0, new float[] { 10.0f, 5.0f }, 0));
		
		XYPlot xyplot = new XYPlot(dollarsDataSet, dateAxis, rangeAxis,  renderer);
		
		String plot_bgcolor = "#000000";
		if (plot_bgcolor != null && plot_bgcolor.length() > 0) {
			xyplot.setBackgroundPaint(Color.decode(plot_bgcolor));
		} else {
			xyplot.setBackgroundPaint(Color.black);
		}
		xyplot.setRangeAxisLocation(AxisLocation.BOTTOM_OR_RIGHT);
		xyplot.setRangeAxisLocation(AxisLocation.BOTTOM_OR_LEFT);
		xyplot.setDatasetRenderingOrder(org.jfree.chart.plot.DatasetRenderingOrder.FORWARD);
		xyplot.setSeriesRenderingOrder(SeriesRenderingOrder.REVERSE);
		
		TextTitle chartTitle = new TextTitle("Average Dollars per Order");
		chartTitle.setPaint(Color.white);
		chartTitle.setFont(new Font("Verdana", Font.BOLD, 20));
		
		JFreeChart chart = new JFreeChart("", JFreeChart.DEFAULT_TITLE_FONT, xyplot, true);
		chart.setBackgroundPaint(new Color(70, 70, 70));
		chart.setTitle(chartTitle);        
		
		
		Session session = null;
		Connection con = null;
		ResultSet rs = null;
        PreparedStatement pstmt = null;
		try {
			SimpleDateFormat sdf2 = new SimpleDateFormat("yyyyMMddHHmmss");
			SimpleDateFormat sdf3 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			java.util.Date startDate = sdf2.parse(startDateStr + "000000");
			java.util.Date endDate   = sdf2.parse(endDateStr + "235959");
			
			session = HibernateUtil.currentSession();
			con = session.connection();
			System.out.println("ChartGeneratorHelper connection: " + con);
			
			java.sql.Date startTimeSQL = new java.sql.Date(startDate.getTime());
			java.sql.Date endTimeSQL = new java.sql.Date(endDate.getTime());
			
			session = HibernateUtil.currentSession();
			con = session.connection();
			System.out.println("ChartGeneratorHelper connection: " + con);
			pstmt = con.prepareStatement(DAILY_SUMMARY_QUERY);
			pstmt.setDate(1, startTimeSQL);
			pstmt.setDate(2, endTimeSQL);
			
			double totalDollars = 0.0;
			int totalOrders = 0;
			long prevDate = 0;
			boolean firstRecord = true;
			rs = pstmt.executeQuery();
			
			while (rs.next()) {
				int accumulatorID = rs.getInt("AccumulatorStat_ID");
				Date currDate = rs.getDate("Time");
				int value = rs.getInt("Value");
				int count = rs.getInt("Count");                
				Day day = new Day(currDate);
				
				// Calculate the sum of all forms of revenue for the day. If a new date is retrieved, reset 
				// the sum values to calculate the sum for the new day.
				if (currDate.getTime() == prevDate || firstRecord) {
					// Only add data if we are dealing with dollars and orders records
					if (accumulatorID == ARIBA_DIRECT_DOLLARS || accumulatorID == SAP_DIRECT_DOLLARS ||
							accumulatorID == WM_DIRECT_DOLLARS || accumulatorID == BOS_CHECKOUT_DOLLARS || accumulatorID == BOS_GUEST_DOLLARS) {
						totalDollars += value;
						totalOrders += count;
						
						// Update graph with dollars and orders values 
						avgDollarsSeries.addOrUpdate(day, (totalDollars/100.0)/totalOrders);
					}
					firstRecord = false;
				} else {
					totalDollars = 0.0;
					totalOrders = 0;
				}
				
				prevDate = currDate.getTime();
			}
		} catch (HibernateException e) {
			e.printStackTrace();
		} catch (java.text.ParseException e){
			e.printStackTrace();
		} catch (SQLException se){
			se.printStackTrace();
		} finally {
			try {
                rs.close();
                pstmt.close();
				con.close();
				session.disconnect();
				session.close();
				HibernateUtil.closeSession();		    	
			} 
			catch (SQLException sqle) { sqle.printStackTrace(); } 
			catch (HibernateException he) { he.printStackTrace(); }
		}
		
		return chart;
	}
	
}
