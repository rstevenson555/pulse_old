/*
 * Created on Jul 6, 2004
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package com.bos.helper;

import com.bos.arch.HibernateUtil;
import com.bos.art.model.jdo.ExternalMinuteStatisticBean;
import com.bos.art.model.jdo.ExternalStatBean;
import java.awt.BasicStroke;
import java.awt.Color;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import net.sf.hibernate.Databinder;
import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Query;
import net.sf.hibernate.Session;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.DefaultXYItemRenderer;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.time.Minute;
import org.jfree.data.time.Month;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
/**
 * @author I0360D3
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class ChartGeneratorHelper {
    private static final String EXTERNAL_SELECT =
        "  Select externalRecord  from ExternalMinuteStatisticBean as externalRecord"
            + "    where externalRecord.time > :startTime and externalRecord.time < :endTime "
            + "        and externalRecord.machineID = :machine "
            + "        and externalRecord.classificationID = :classification ";
    private static final String EXTERNAL_SELECT_STATEMENT =
        "Select * from ExternalMinuteStatistics where Classification_ID=? and Time>? and Time<?";

    private static final String MINUTE_STATS_QUERY = 
        "  Select * from minutestatistics where time>? and time<? and machine_id=? order by time";

    public static void main(String[] args) {

    }
    public JFreeChart generateExternalAccessTimeSliceReport(int classificationID, String startTime, String endTime) {
        //////////////////////////////////////////////////
        /////////////////////////////////////////////////
        long startTimeLong;
        boolean isAutoRanging = false;
        ChartPanel chartPanel;
        DateAxis dateAxis = null;
        /** Time series for total memory used. */
        TimeSeries pagesServed;
        TimeSeries i90percent;
        /** Time series for free memory. */
        TimeSeries averageSeries;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmm");
        //		private AvgLoadTime instance = null;
        /**
         * Creates a new application.
         */
        // create two series that automatically discard data more than 30 seconds old...
        i90percent = new TimeSeries("90 %ile", Minute.class);
        pagesServed = new TimeSeries("Total Pages", Minute.class);
        averageSeries = new TimeSeries("Average", Minute.class);
        TimeSeriesCollection dataset = new TimeSeriesCollection();
        TimeSeriesCollection volumeDataSet = new TimeSeriesCollection();
        volumeDataSet.addSeries(pagesServed);
        dataset.addSeries(averageSeries);
        dataset.addSeries(i90percent);

        dateAxis = new DateAxis("Time (HH:MM)");
        dateAxis.setDateFormatOverride(new SimpleDateFormat("HH:mm"));
        dateAxis.setLowerMargin(0.0);
        dateAxis.setUpperMargin(0.0);
        dateAxis.setTickLabelsVisible(true);

        NumberAxis rangeAxis = new NumberAxis("Time (Sec)");
        java.text.DecimalFormat format = new java.text.DecimalFormat("0.000");
        rangeAxis.setAutoRange(true);
        rangeAxis.setAutoTickUnitSelection(true);
        rangeAxis.setAutoRangeIncludesZero(true);
        rangeAxis.setNumberFormatOverride(format);
        rangeAxis.setLowerMargin(0.40);
        XYItemRenderer renderer = new DefaultXYItemRenderer();
        //renderer.setToolTipGenerator(
            //new StandardXYLabelGenerator(new SimpleDateFormat("HH:mm"), new java.text.DecimalFormat("#0.00")));
        renderer.setSeriesPaint(0, Color.black);
        renderer.setSeriesStroke(0, new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        renderer.setSeriesPaint(1, Color.magenta);
        for (int j = 1; j < 10; j++)
            renderer.setSeriesStroke(
                j,
                new BasicStroke(1f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 0, new float[] { 10.0f, 5.0f }, 0));

        NumberAxis rangeAxis2 = new NumberAxis("Page Volume");
        java.text.DecimalFormat format2 = new java.text.DecimalFormat("#000");
        rangeAxis2.setNumberFormatOverride(format2);
        rangeAxis2.setUpperMargin(1.00); // to leave room for price line       
        XYBarRenderer renderer2 = new XYBarRenderer(0.20);
        /*renderer2.setToolTipGenerator(
            new StandardXYLabelGenerator(new SimpleDateFormat("HH:mm"), new java.text.DecimalFormat("#000")));
            */
        renderer2.setSeriesPaint(0, new Color(200, 200, 240, 200));
        XYPlot xyplot = new XYPlot(volumeDataSet, dateAxis, rangeAxis2, renderer2);
        String plot_bgcolor = "#FFFFFF";
        if (plot_bgcolor != null && plot_bgcolor.length() > 0) {
            xyplot.setBackgroundPaint(Color.decode(plot_bgcolor));
        } else {
            xyplot.setBackgroundPaint(Color.lightGray);
        }
        xyplot.setRangeAxisLocation(org.jfree.chart.axis.AxisLocation.BOTTOM_OR_RIGHT);
        xyplot.setRangeAxis(1, rangeAxis);
        xyplot.setRangeAxisLocation(org.jfree.chart.axis.AxisLocation.BOTTOM_OR_LEFT);
        xyplot.setDataset(1, dataset);
        xyplot.mapDatasetToRangeAxis(0, 0);
        xyplot.setRenderer(1, renderer);
        xyplot.setDatasetRenderingOrder(org.jfree.chart.plot.DatasetRenderingOrder.REVERSE);
        JFreeChart chart = new JFreeChart(getClassificationName(classificationID), JFreeChart.DEFAULT_TITLE_FONT, xyplot, true);
        //////////////////////////////////////////////////
        //////////////////////////////////////////////////
		Session session = null;
        try {
            session = HibernateUtil.currentSession();
            Query query = session.createQuery(EXTERNAL_SELECT);
			System.out.println(EXTERNAL_SELECT);
			System.out.println("startTime:"+startTime);
			System.out.println("endTime:"+endTime);
			System.out.println("machine:"+2);
			System.out.println("classification:"+classificationID);
            
            query.setParameter("startTime", startTime);
            query.setParameter("endTime", endTime);
            query.setParameter("machine", new Integer(2));
            query.setParameter("classification", new Integer(classificationID));
            List sessionSummaryList = query.list();
            System.out.println("starting to append the stats for ExternalSelect...");
            Databinder db = HibernateUtil.getDataBinder();
            db.setInitializeLazy(true);
            Iterator iter = sessionSummaryList.iterator();
            for (; iter.hasNext();) {
                Object o = iter.next();
                ExternalMinuteStatisticBean ems = (ExternalMinuteStatisticBean)o;
                System.out.println("Type : " + o.getClass().getName());
                System.out.println("Type : " + o.toString());
                ems.getTime();
                /////////////////////////////////////////////////////////////
                /////////////////////////////////////////////////////////////
                Date dpdate = ems.getTime();
                Minute minute = new Minute(dpdate);
                averageSeries.addOrUpdate(minute, new Double(ems.getAverageLoadTime() / 1000.00));
                i90percent.addOrUpdate(minute, new Double(ems.getNinetiethPercentile() / 1000.00));
                pagesServed.addOrUpdate(minute, new Double(ems.getTotalLoads()));
                //////////////////////////////////////////////////////////////
                //////////////////////////////////////////////////////////////
            }
		//	session.close();
		HibernateUtil.closeSession();
        	
        } catch (HibernateException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return chart; //generateSimpleDemoChart();
    }


    private static final String CLASSIFICATION_QUERY =
        "from ExternalStatBean as externalStat where externalStat.classificationId= :classification";
        
        
    private static String getClassificationName(int classify) {
        Session session = null;
        String returnValue = "Classification " + classify;
        try {
            session = HibernateUtil.currentSession();
            Query query = session.createQuery(CLASSIFICATION_QUERY);
            query.setParameter("classification", new Integer(classify));
            ExternalStatBean esb = (ExternalStatBean)query.uniqueResult();
            if (esb != null) {
                returnValue = esb.getDescription();
            }

			HibernateUtil.closeSession();
		//	session.close();
        } catch (HibernateException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();

        }
        return returnValue;
    }


    public JFreeChart generateMonthlyVolAvg(String title, String context, String startTime, String endTime){
        //////////////////////////////////////////////////
        /////////////////////////////////////////////////
        String contextname = context;
        long startTimeLong;
        boolean isAutoRanging = false;
        ChartPanel chartPanel;
        DateAxis dateAxis = null;
        /** Time series for total memory used. */
        TimeSeries pagesServed;
        /** Time series for free memory. */
        TimeSeries averageSeries;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmm");
        //		private AvgLoadTime instance = null;
        /**
         * Creates a new application.
         */
        // create two series that automatically discard data more than 30 seconds old...
        pagesServed = new TimeSeries("Total Pages", Month.class);
        //pagesServed.setHistoryCount(60);
        averageSeries = new TimeSeries("Average", Month.class);

        TimeSeriesCollection dataset = new TimeSeriesCollection();
        TimeSeriesCollection volumeDataSet = new TimeSeriesCollection();
        volumeDataSet.addSeries(pagesServed);
        dataset.addSeries(averageSeries);
        dateAxis = new DateAxis("YYYY-MMM");
        dateAxis.setDateFormatOverride(new SimpleDateFormat("yyyy-MMM"));
        dateAxis.setLowerMargin(0.0);
        dateAxis.setUpperMargin(0.0);
        dateAxis.setTickLabelsVisible(true);
        NumberAxis rangeAxis = new NumberAxis("Time (Sec)");
        java.text.DecimalFormat format = new java.text.DecimalFormat("0.000");
        rangeAxis.setRange(0.0,1.25);
        rangeAxis.setAutoRange(true);
        rangeAxis.setAutoTickUnitSelection(true);
        rangeAxis.setAutoRangeIncludesZero(true);
        rangeAxis.setNumberFormatOverride(format);
        rangeAxis.setLowerMargin(0.40);
        XYItemRenderer renderer = new DefaultXYItemRenderer();
        //renderer.setToolTipGenerator(
            //new StandardXYLabelGenerator(new SimpleDateFormat("HH:mm"), new java.text.DecimalFormat("#0.00")));
        renderer.setSeriesPaint(0, Color.black);
        renderer.setSeriesStroke(0, new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        renderer.setSeriesPaint(1, Color.magenta);
        for (int j = 1; j < 10; j++)
            renderer.setSeriesStroke(
                j,
                new BasicStroke(1f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 0, new float[] { 10.0f, 5.0f }, 0));
        NumberAxis rangeAxis2 = new NumberAxis("Page Volume");
        java.text.DecimalFormat format2 = new java.text.DecimalFormat("#000");
        rangeAxis2.setNumberFormatOverride(format2);
        rangeAxis2.setUpperMargin(1.00); // to leave room for price line       
        XYBarRenderer renderer2 = new XYBarRenderer(0.20);
        /*renderer2.setToolTipGenerator(
            new StandardXYLabelGenerator(new SimpleDateFormat("HH:mm"), new java.text.DecimalFormat("#000")));
            */
        renderer2.setSeriesPaint(0, new Color(200, 200, 240, 200));
        XYPlot xyplot = new XYPlot(volumeDataSet, dateAxis, rangeAxis2, renderer2);
        String plot_bgcolor = "#FFFFFF";
        if (plot_bgcolor != null && plot_bgcolor.length() > 0) {
            xyplot.setBackgroundPaint(Color.decode(plot_bgcolor));
        } else {
            xyplot.setBackgroundPaint(Color.lightGray);
        }
        xyplot.setRangeAxisLocation(org.jfree.chart.axis.AxisLocation.BOTTOM_OR_RIGHT);
        xyplot.setRangeAxis(1, rangeAxis);
        xyplot.setRangeAxisLocation(org.jfree.chart.axis.AxisLocation.BOTTOM_OR_LEFT);
        xyplot.setDataset(1, dataset);
        xyplot.mapDatasetToRangeAxis(1, 1);
        xyplot.setRenderer(1, renderer);
        xyplot.setDatasetRenderingOrder(org.jfree.chart.plot.DatasetRenderingOrder.FORWARD);
        JFreeChart chart = new JFreeChart(title, JFreeChart.DEFAULT_TITLE_FONT, xyplot, true);
        //////////////////////////////////////////////////
        //////////////////////////////////////////////////
		Session session = null;

        try {
            SimpleDateFormat sdf2 = new SimpleDateFormat("yyyyMMddHHmmss");
            SimpleDateFormat sdf3 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            java.util.Date startDate = sdf2.parse(startTime);
            java.util.Date endDate   = sdf2.parse(endTime);

            session = HibernateUtil.currentSession();
            Connection con = session.connection();
            System.out.println("ChartGeneratorHelper connection: " + con);
            PreparedStatement pstmt = con.prepareStatement(MONTHLY_VOL_AVG);
            java.sql.Date startTimeSQL = new java.sql.Date(startDate.getTime());
            java.sql.Date endTimeSQL = new java.sql.Date(endDate.getTime());
            

            pstmt.setString(1,contextname);
            pstmt.setDate(2,startTimeSQL);
            pstmt.setDate(3,endTimeSQL);
            
			System.out.println(MONTHLY_VOL_AVG );
			System.out.println("startTime:"+startTime);
			System.out.println("endTime:"+endTime);
            ResultSet rs = pstmt.executeQuery(); 

            while(rs.next()) {

                String monthString = rs.getString("month");
                java.util.Date monthDate = sdf3.parse(monthString+"-01 00:00:00");
                
                Month month = new Month(monthDate);

                averageSeries.addOrUpdate(month, new Double(rs.getInt("average_response")  / 1000.00));
                pagesServed.addOrUpdate(month, new Double(rs.getInt("loads")));
            }
            rangeAxis.setAutoRange(true);
            //dateAxis.setAutoRange(true);
            dateAxis.setRange(startDate,endDate);
            rs.close();
            pstmt.close();
            con.close();
		HibernateUtil.closeSession();
        	
        } catch (HibernateException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }catch (SQLException e){
            e.printStackTrace();
        }catch (java.text.ParseException e){
            e.printStackTrace();
        }
        return chart; //generateSimpleDemoChart();

    }

    private static String MONTHLY_VOL_AVG =
        " select sum(averageloadtime*totalloads)/sum(totalloads) as average_response, sum(totalloads) as loads, to_char(day,'yyyy-MM') as month " +
        " from dailypageloadtimes d " +
        " where context_id = (select context_id from contexts where contextname=?) " +
        " and day>? and day<? " +
        " group by to_char(day,'yyyy-MM') " +
        " order by to_char(day,'yyyy-MM') ";



    public JFreeChart generateMinuteStatsReport(String title, int MachineId, String startTime, String endTime) {
        //////////////////////////////////////////////////
        /////////////////////////////////////////////////
        long startTimeLong;
        boolean isAutoRanging = false;
        ChartPanel chartPanel;
        DateAxis dateAxis = null;
        /** Time series for total memory used. */
        TimeSeries pagesServed;
        TimeSeries i90percent;
        /** Time series for free memory. */
        TimeSeries averageSeries;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmm");
        //		private AvgLoadTime instance = null;
        /**
         * Creates a new application.
         */
        // create two series that automatically discard data more than 30 seconds old...
        i90percent = new TimeSeries("90 %ile", Minute.class);
        pagesServed = new TimeSeries("Total Pages", Minute.class);
        //pagesServed.setHistoryCount(60);
        averageSeries = new TimeSeries("Average", Minute.class);

        TimeSeriesCollection dataset = new TimeSeriesCollection();
        TimeSeriesCollection volumeDataSet = new TimeSeriesCollection();
        volumeDataSet.addSeries(pagesServed);
        dataset.addSeries(averageSeries);
        dataset.addSeries(i90percent);
        dateAxis = new DateAxis("Time (HH:MM)");
        dateAxis.setDateFormatOverride(new SimpleDateFormat("HH:mm"));
        dateAxis.setLowerMargin(0.0);
        dateAxis.setUpperMargin(0.0);
        dateAxis.setTickLabelsVisible(true);
        NumberAxis rangeAxis = new NumberAxis("Time (Sec)");
        java.text.DecimalFormat format = new java.text.DecimalFormat("0.000");
        rangeAxis.setAutoRange(false);
        rangeAxis.setRange(0.0,1.25);
        rangeAxis.setAutoTickUnitSelection(true);
        rangeAxis.setAutoRangeIncludesZero(true);
        rangeAxis.setNumberFormatOverride(format);
        rangeAxis.setLowerMargin(0.40);
        XYItemRenderer renderer = new DefaultXYItemRenderer();
        //renderer.setToolTipGenerator(
            //new StandardXYLabelGenerator(new SimpleDateFormat("HH:mm"), new java.text.DecimalFormat("#0.00")));
        renderer.setSeriesPaint(0, Color.black);
        renderer.setSeriesStroke(0, new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        renderer.setSeriesPaint(1, Color.magenta);
        for (int j = 1; j < 10; j++)
            renderer.setSeriesStroke(
                j,
                new BasicStroke(1f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 0, new float[] { 10.0f, 5.0f }, 0));
        NumberAxis rangeAxis2 = new NumberAxis("Page Volume");
        java.text.DecimalFormat format2 = new java.text.DecimalFormat("#000");
        rangeAxis2.setNumberFormatOverride(format2);
        rangeAxis2.setUpperMargin(1.00); // to leave room for price line       
        XYBarRenderer renderer2 = new XYBarRenderer(0.20);
        /*renderer2.setToolTipGenerator(
            new StandardXYLabelGenerator(new SimpleDateFormat("HH:mm"), new java.text.DecimalFormat("#000")));
            */
        renderer2.setSeriesPaint(0, new Color(200, 200, 240, 200));
        XYPlot xyplot = new XYPlot(volumeDataSet, dateAxis, rangeAxis2, renderer2);
        String plot_bgcolor = "#FFFFFF";
        if (plot_bgcolor != null && plot_bgcolor.length() > 0) {

            xyplot.setBackgroundPaint(Color.decode(plot_bgcolor));
        } else {
            xyplot.setBackgroundPaint(Color.lightGray);
        }
        xyplot.setRangeAxisLocation(org.jfree.chart.axis.AxisLocation.BOTTOM_OR_RIGHT);
        xyplot.setRangeAxis(1, rangeAxis);
        xyplot.setRangeAxisLocation(org.jfree.chart.axis.AxisLocation.BOTTOM_OR_LEFT);
        xyplot.setDataset(1, dataset);
        xyplot.mapDatasetToRangeAxis(1, 1);
        xyplot.setRenderer(1, renderer);
        xyplot.setDatasetRenderingOrder(org.jfree.chart.plot.DatasetRenderingOrder.FORWARD);
        //TODO  Fix this Machine 14 Chart statement.
        JFreeChart chart = new JFreeChart(title, JFreeChart.DEFAULT_TITLE_FONT, xyplot, true);
        //////////////////////////////////////////////////
        //////////////////////////////////////////////////
		Session session = null;

        try {
            SimpleDateFormat sdf2 = new SimpleDateFormat("yyyyMMddHHmmss");
            java.util.Date startDate = sdf2.parse(startTime);
            java.util.Date endDate   = sdf2.parse(endTime);

            session = HibernateUtil.currentSession();
            Connection con = session.connection();
            PreparedStatement pstmt = con.prepareStatement(MINUTE_STATS_QUERY);
            java.sql.Timestamp startTimeSQL = new java.sql.Timestamp(startDate.getTime());
            java.sql.Timestamp endTimeSQL = new java.sql.Timestamp(endDate.getTime());

            pstmt.setTimestamp(1,startTimeSQL);
            pstmt.setTimestamp(2,endTimeSQL);
            pstmt.setInt(3,MachineId);
            
			System.out.println(MINUTE_STATS_QUERY );
			System.out.println("startTime:"+startTime);
			System.out.println("endTime:"+endTime);
            ResultSet rs = pstmt.executeQuery(); 

            while(rs.next()) {
                /////////////////////////////////////////////////////////////
                /////////////////////////////////////////////////////////////
                java.sql.Timestamp ts = rs.getTimestamp("time");
                System.out.println("rs.getTimestamp: "+sdf2.format(new java.util.Date(ts.getTime())));
                Minute minute = new Minute(new java.util.Date(ts.getTime()));

                averageSeries.addOrUpdate(minute, new Double(rs.getInt("averageloadtime")  / 1000.00));
                i90percent.addOrUpdate(minute, new Double(rs.getInt("ninetiethpercentile") / 1000.00));
                pagesServed.addOrUpdate(minute, new Double(rs.getInt("totalloads")));
                //////////////////////////////////////////////////////////////
                //////////////////////////////////////////////////////////////
            }
		//	session.close();
		HibernateUtil.closeSession();
        	
        } catch (HibernateException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }catch (SQLException e){
            e.printStackTrace();
        }catch (java.text.ParseException e){
            e.printStackTrace();
        }
        return chart; //generateSimpleDemoChart();
    }


}
