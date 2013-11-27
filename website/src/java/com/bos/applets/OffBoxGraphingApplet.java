package com.bos.applets;

import com.bcop.arch.logger.time.Classifications;
import com.bos.applets.arch.AppletMessageListener;
import com.bos.art.logParser.broadcast.beans.ExternalAccessRecordsMinuteBean;
import com.bos.art.logParser.broadcast.beans.TransferBean;
import com.bos.art.logParser.broadcast.beans.delegate.ExternalAccessRecordsDelegate;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.MediaTracker;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import javax.swing.ImageIcon;
import javax.swing.JApplet;
import javax.swing.UIManager;

/**
 * A basic extension of the javax.swing.JApplet class
 */
public class OffBoxGraphingApplet extends JApplet implements ExternalAccessRecordsDelegate {

    private static final int[] pricingClassifications = {Classifications.PRICING_REQUEST_EAST,
        Classifications.PRICING_REQUEST_WEST,
        Classifications.PRICING_REQUEST_MIDW,
        Classifications.PRICING_REQUEST_TEST};
    private static final String[] pricingClassificationsTitle = {"Pricing to BCEAST","Pricing to BCWEST","Pricing to BCMIDW","Pricing to BCTST"};
    private static final int[] custValClassifications = {Classifications.CUSTVAL_REQUEST_EAST,
        Classifications.CUSTVAL_REQUEST_WEST,
        Classifications.CUSTVAL_REQUEST_MIDW};
    private static final String[] custValClassificationsTitle = {"CustVal to BCEAST","CustVal to BCWEST","CustVal to BCMIDW"};
    private static final int[] inventoryClassifications = {Classifications.INVENTORY_EAST,
        Classifications.INVENTORY_WEST,
        Classifications.INVENTORY_MIDW,
        Classifications.INVENTORY_TEST};
    private static final String[] inventoryClassificationsTitle = {"Inventory to BCEAST","Inventory to BCWEST","Inventory to BCMIDW","Inventory to BCTST"};

    private static final int[] campaignsClassifications = {
        Classifications.CAMPAIGN_WEBSERVICE_CALL};
    private static final String[] campaignsClassificationsTitle = {"Campaigns WebService"};

    private static final int[] mercadoClassificationsPostSR = {Classifications.MERCADO_SEARCH_EXECUTE,
        Classifications.MERCADO_PRICING,
        Classifications.MERCADO_INVENTORY};
    private static final String[] mercadoClassificationsTitle = {"Search","Search Pricing","Search Inventory"};
    
    private static final int[] mailClassifications = {
        Classifications.EMAIL_WEBSERVICE_CALL};
    private static final String[] mailClassificationsTitle = {"Email WebService"};

    private static final int[] podClassifications = {
        Classifications.POD_WEBSERVICE_CALL};
    private static final String[] podClassificationsTitle = {"POD WebService"};
    
    private static final int[] oiClassifications = {
        Classifications.ORDER_INVOICE_WEBSERVICE_CALL};
    private static final String[] oiClassificationsTitle = {"Order Invoice WebService"};
    
    private static final int[] wmClassifications = {Classifications.WM_CARD_AUTH_WEBSERVICE_CALL};
    private static final String[] wmClassificationsTitle = {"Auth WebService"};

    @Override
    public void init() {
        String plot_bgcolor = getParameter("plot_bgcolor");
        String chart_bgcolor = getParameter("chart_bgcolor");
        java.awt.Image image = null;
        ImageIcon homeIcon = null;
        ImageIcon expandIcon = null;
        try {
            String webCodeBase = getCodeBase().toString();
            image =
                    getImage(
                    new URL(getCodeBase() + getParameter("plot_image")));

            URL homeIconURL = new URL(webCodeBase + "images/home.gif");
            URL expandIconURL = new URL(webCodeBase + "images/expand.gif");
            homeIcon = new ImageIcon(homeIconURL);
            expandIcon = new ImageIcon(expandIconURL);

        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        MediaTracker mt = new MediaTracker(this);
        mt.addImage(image, 0);
        try {
            mt.waitForAll();
        } catch (InterruptedException ie) {
            ;
        }
        chart_bgcolor = "#8D8D8D";
        pricingChart = new ExternalMinuteGraph(plot_bgcolor, chart_bgcolor, image, expandIcon, homeIcon, pricingClassifications,pricingClassificationsTitle);
        custValChart = new ExternalMinuteGraph(plot_bgcolor, chart_bgcolor, image, expandIcon, homeIcon, custValClassifications,custValClassificationsTitle);
        inventoryChart = new ExternalMinuteGraph(plot_bgcolor, chart_bgcolor, image, expandIcon, homeIcon, inventoryClassifications,inventoryClassificationsTitle);
        campaignsChart = new ExternalMinuteGraph(plot_bgcolor, chart_bgcolor, image, expandIcon, homeIcon, campaignsClassifications,campaignsClassificationsTitle);
        searchEngineChartPostSR = new ExternalMinuteGraph(plot_bgcolor, chart_bgcolor, image, expandIcon, homeIcon, mercadoClassificationsPostSR,mercadoClassificationsTitle);
        mailChart = new ExternalMinuteGraph(plot_bgcolor, chart_bgcolor, image, expandIcon, homeIcon, mailClassifications,mailClassificationsTitle);
        podChart = new ExternalMinuteGraph(plot_bgcolor, chart_bgcolor, image, expandIcon, homeIcon, podClassifications,podClassificationsTitle);
        oiChart = new ExternalMinuteGraph(plot_bgcolor, chart_bgcolor, image, expandIcon, homeIcon, oiClassifications,oiClassificationsTitle);
        wmChart = new ExternalMinuteGraph(plot_bgcolor, chart_bgcolor, image, expandIcon, homeIcon, wmClassifications,wmClassificationsTitle);

        // This code is automatically generated by Visual Cafe when you add
        // components to the visual environment. It instantiates and initializes
        // the components. To modify the code, only use code syntax that matches
        // what Visual Cafe can generate, or Visual Cafe may be unable to back
        // parse your Java file into its visual environment.
        //{{INIT_CONTROLS
        getContentPane().setLayout(new BorderLayout(0, 0));
        //setSize(457,310);
        getContentPane().add(BorderLayout.CENTER, graphingTabbedPane);
        graphingTabbedPane.setBounds(0, 0, 457, 310);
        graphingTabbedPane.add(pricingChart);
        graphingTabbedPane.add(inventoryChart);
        graphingTabbedPane.add(custValChart);
        graphingTabbedPane.add(campaignsChart);
        //graphingTabbedPane.add(searchEngineChart);
        graphingTabbedPane.add(searchEngineChartPostSR);
        //graphingTabbedPane.add(creditCardChart);
        graphingTabbedPane.add(mailChart);
        graphingTabbedPane.add(podChart);
        graphingTabbedPane.add(oiChart);
        graphingTabbedPane.add(wmChart);
        graphingTabbedPane.setSelectedIndex(4);
        graphingTabbedPane.setTitleAt(0, "Pricing (AS400)");
        graphingTabbedPane.setTitleAt(1, "Inventory (AS400)");
        graphingTabbedPane.setTitleAt(2, "CustVal (AS400)");
        graphingTabbedPane.setTitleAt(3, "Campaigns");
        //graphingTabbedPane.setTitleAt(2,"Search (Mercado) Pre-SR");
        graphingTabbedPane.setTitleAt(4, "Search");
        //graphingTabbedPane.setTitleAt(5,"Credit Card (Chase)");
        graphingTabbedPane.setTitleAt(5, "Email");
        graphingTabbedPane.setTitleAt(6, "Proof Of Delivery");
        graphingTabbedPane.setTitleAt(7, "Order Inquiry");
        graphingTabbedPane.setTitleAt(8, "Credit Card Auth");

        UIManager.put("TabbedPane.selected", Color.decode("#000000"));
        
        for (int i = 0; i < 9; i++) {
        	graphingTabbedPane.setBackgroundAt(i, Color.decode("#464646"));
        	graphingTabbedPane.setForegroundAt(i, Color.decode("#FFFFFF"));
        }
        
//	    graphingTabbedPane.getComponentAt(3).set
        //}}
        //
        if (chart_bgcolor != null && chart_bgcolor.length() > 0) {
            getContentPane().setBackground(Color.decode(chart_bgcolor));
        }
        invalidate();
        update(getGraphics());
    }

    public OffBoxGraphingApplet() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
        }
    }

    public void didCompleteBagProcessing(org.jgroups.Message msg) {
    }

    /**
     * called by the receiver when objects are received we can then decide if we want to deal with this message or not
     *
     */
    public void didReceiveExternalAccessRecordsBean(org.jgroups.Message msg, TransferBean obj) {
        ExternalAccessRecordsMinuteBean bean = (ExternalAccessRecordsMinuteBean) obj;
        int classify = bean.getClassificationID();

        if (classify >= Classifications.BEGIN_PRICING && classify <= Classifications.END_PRICING || classify == Classifications.PRICING_REQUEST_TEST) {
            for (int i = 0; i < pricingClassifications.length; i++) {
                if (bean.getClassificationID() == pricingClassifications[i]) {
                    pricingChart.didReceiveExternalAccessRecordsBean(msg, obj);
                }
            }
        } else if (classify >= Classifications.BEGIN_CHASE && classify <= Classifications.END_CHASE) {
            /*
             * for(int i=0;i<chaseClassifications.length;i++) { if ( bean.getClassificationID()==chaseClassifications[i] ) {
             * creditCardChart.didReceiveExternalAccessRecordsBean(msg,obj); } }
             */
        } else if (classify >= Classifications.BEGIN_CAMPAIGN && classify <= Classifications.END_CAMPAIGN) {
            for (int i = 0; i < campaignsClassifications.length; i++) {
                if (bean.getClassificationID() == campaignsClassifications[i]) {
                    campaignsChart.didReceiveExternalAccessRecordsBean(msg, obj);
                }
            }
        } else if (classify >= Classifications.BEGIN_POD && classify <= Classifications.END_POD) {
            for (int i = 0; i < podClassifications.length; i++) {
                if (bean.getClassificationID() == podClassifications[i]) {
                    podChart.didReceiveExternalAccessRecordsBean(msg, obj);
                }
            }
        } else if (classify >= Classifications.BEGIN_ORDER_INVOICE && classify <= Classifications.END_ORDER_INVOICE) {
            for (int i = 0; i < oiClassifications.length; i++) {
                if (bean.getClassificationID() == oiClassifications[i]) {
                    oiChart.didReceiveExternalAccessRecordsBean(msg, obj);
                }
            }
        } else if (classify >= Classifications.WM_CARD_AUTH_WEBSERVICE_CALL && classify <= Classifications.WM_CARD_AUTH_WEBSERVICE_CALL) {
            for (int i = 0; i < wmClassifications.length; i++) {
                if (bean.getClassificationID() == wmClassifications[i]) {
                    wmChart.didReceiveExternalAccessRecordsBean(msg, obj);
                }
            }
        } else if (classify >= Classifications.BEGIN_EMAIL && classify <= Classifications.END_EMAIL) {
            for (int i = 0; i < mailClassifications.length; i++) {
                if (bean.getClassificationID() == mailClassifications[i]) {
                    mailChart.didReceiveExternalAccessRecordsBean(msg, obj);
                }
            }
        } else if (classify >= Classifications.BEGIN_MERCADO && classify <= Classifications.END_MERCADO) {
            for (int i = 0; i < mercadoClassificationsPostSR.length; i++) {
                if (bean.getClassificationID() == mercadoClassificationsPostSR[i]) {
                    searchEngineChartPostSR.didReceiveExternalAccessRecordsBean(msg, obj);
                }
            }
        } else if (classify >= Classifications.CUSTVAL_REQUEST_EAST && classify <= Classifications.CUSTVAL_REQUEST_CENT) {
            for (int i = 0; i < custValClassifications.length; i++) {
                if (bean.getClassificationID() == custValClassifications[i]) {
                    custValChart.didReceiveExternalAccessRecordsBean(msg, obj);
                }
            }
        } else if (classify >= Classifications.INVENTORY_EAST && classify <= Classifications.INVENTORY_TEST) {
            for (int i = 0; i < inventoryClassifications.length; i++) {
                if (bean.getClassificationID() == inventoryClassifications[i]) {
                    inventoryChart.didReceiveExternalAccessRecordsBean(msg, obj);
                }
            }
        }
    }

    @Override
    public void start() {
        super.start();
        AppletMessageListener.getInstance().setExternalAccessRecordsDelegate(this);
    }

    @Override
    public void stop() {
        AppletMessageListener.getInstance().stop();
        super.stop();
    }
    //{{DECLARE_CONTROLS
    javax.swing.JTabbedPane graphingTabbedPane = new javax.swing.JTabbedPane();
    //}}
    //
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmm");
    private String plot_bgcolor;
    private String chart_bgcolor;
    private ExternalMinuteGraph pricingChart;
    private ExternalMinuteGraph custValChart;
    private ExternalMinuteGraph inventoryChart;
    private ExternalMinuteGraph campaignsChart;
    private ExternalMinuteGraph creditCardChart;
    private ExternalMinuteGraph searchEngineChartPostSR;
    private ExternalMinuteGraph mailChart;
    private ExternalMinuteGraph podChart;
    private ExternalMinuteGraph oiChart;
    private ExternalMinuteGraph wmChart;
}
