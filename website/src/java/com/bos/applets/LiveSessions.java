/* ======================================
 * JFreeChart : a free Java chart library
 * ======================================
 *
 * Project Info:  http://www.object-refinery.com/jfreechart/index.html
 * Project Lead:  David Gilbert (david.gilbert@object-refinery.com);
 *
 * (C) Copyright 2000-2003, by Object Refinery Limited and Contributors.
 *
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation;
 * either version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * library; if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 * Boston, MA 02111-1307, USA.
 *
 * ----------------
 * MemoryUsage.java
 * ----------------
 * (C) Copyright 2002, 2003, by Object Refinery Limited and Contributors.
 *
 * Original Author:  Tony Bianchini;
 * Contributor(s):   David Gilbert;
 *
 * $Id$
 *
 * Changes
 * -------
 * 10-Sep-2002 : Version 1, based on code by Tony Bianchini (DG);
 * 11-Oct-2002 : Fixed errors reported by Checkstyle (DG);
 * 16-Oct-2002 : Removed redundant attributes (DG);
 * 18-Oct-2002 : Moved to com.jrefinery.chart.demo.premium package (DG);
 * 25-Apr-2003 : Updated for JFreeChart 0.9.8, and moved to com.jrefinery.chart.demo package (DG);
 *
 */
package com.bos.applets;

import EDU.oswego.cs.dl.util.concurrent.FutureResult;
import com.bos.applets.arch.AppletMessageListener;
import com.bos.art.logParser.broadcast.beans.MemoryStatBean;
import com.bos.art.logParser.broadcast.beans.QueryBean;
import com.bos.art.logParser.broadcast.beans.SessionDataBean;
import com.bos.art.logParser.broadcast.beans.TransferBean;
import com.bos.art.logParser.broadcast.beans.delegate.MemoryStatDelegate;
import com.bos.art.logParser.broadcast.beans.delegate.SessionDataDelegate;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashMap;
import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.DialShape;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jgroups.Message;

/**
 * A demo application showing a dynamically updated chart that displays the current JVM memory usage.
 *
 * @author Bryce Alcock
 * @author Bob Stevenson
 * @author Will Webb
 */
public class LiveSessions extends JApplet implements SessionDataDelegate, MemoryStatDelegate {

    private JPanel mainPanel;
    private DefaultCategoryDataset userDataSet;
    private String plot_bgcolor;
    private String chart_bgcolor;
    private long queryNum = 1;
    private JMenu byMachine;
    private JMenu byContext;
    private HashMap<String, JMenu> machineMap = new HashMap<String, JMenu>();
    private HashMap machineContextMap = new HashMap();
    private HashMap<String, JMenu> contextMap = new HashMap<String, JMenu>();
    private ChartPanel chartPanel;
    private static final String series1 = "1 Min Activity";
    private static final String series2 = "5 Min Activity";
    private static final String series3 = "10 Min Activity";
    private LiveSessions instance = null;
    private HashMap<String, MemoryGraph> memoryCharts = new HashMap<String, MemoryGraph>();
    private JPanel cards;
    private javax.swing.JMenuItem expandGraphItem = new javax.swing.JMenuItem();
    private JPopupMenu popupMenu;

    /**
     * Creates a new application.
     */
    public JPanel getMainPanel() {
        mainPanel = new JPanel(new GridLayout(1, 3, 0, 0));

        chartPanel = new ChartPanel(displayMeterChart(100.0, DialShape.CIRCLE));

        popupMenu = chartPanel.getPopupMenu();
        expandGraphItem.setText("Maximize Graph");
        expandGraphItem.setActionCommand("ExpandGraph");

        popupMenu.add(expandGraphItem);

        SymPopupMenu lSymPopupMenu = new SymPopupMenu();
        popupMenu.addPopupMenuListener(lSymPopupMenu);

        SymAction lSymAction = new SymAction();
        expandGraphItem.addActionListener(lSymAction);

        mainPanel.add(chartPanel);
        BrowserAndOS bos = new BrowserAndOS(BrowserAndOS.BROWSER_GRAPH);
        BrowserAndOS bos2 = new BrowserAndOS(BrowserAndOS.OS_GRAPH);

        mainPanel.add(bos);
        mainPanel.add(bos2);

        try {
            MediaTracker mt = new MediaTracker(this);

            java.awt.Image image = getImage(new java.net.URL(getCodeBase() + getParameter("plot_image")));
            mt.addImage(image, 0);
            try {
                mt.waitForAll();
            } catch (InterruptedException ie) {
                ;
            }

            bos.getChart().setBackgroundImage(image);
            bos2.getChart().setBackgroundImage(image);
        } catch (java.net.MalformedURLException mfue) {
            System.err.println(mfue);
        }
        return mainPanel;

    }

    class SymPopupMenu implements PopupMenuListener {

        public void popupMenuWillBecomeInvisible(PopupMenuEvent event) {
        }

        public void popupMenuWillBecomeVisible(PopupMenuEvent event) {
            Object object = event.getSource();
            if (object == popupMenu) {
                popupMenu_popupMenuWillBecomeVisible(event);
            }
        }

        public void popupMenuCanceled(PopupMenuEvent event) {
        }
    }

    private void popupMenu_popupMenuWillBecomeVisible(PopupMenuEvent event) {
        if (mainPanel.getComponentCount() == 0) {
            expandGraphItem.setText("Remove Graph");
            expandGraphItem.setActionCommand("RemoveGraph");
        } else {
            expandGraphItem.setText("Maximize Graph");
            expandGraphItem.setActionCommand("ExpandGraph");
        }

    }

    /**
     * Displays a meter chart.
     *
     * @param value the value.
     * @param shape the dial shape.
     */
    private JFreeChart displayMeterChart(double value, DialShape shape) {

        userDataSet = new DefaultCategoryDataset();

        JFreeChart chart = ChartFactory.createBarChart3D("Users By Context", null, null, userDataSet, PlotOrientation.HORIZONTAL, false, true, false);

        chart.getPlot().setNoDataMessage("No data received yet...");

        try {
            MediaTracker mt = new MediaTracker(this);

            java.awt.Image image = getImage(new java.net.URL(getCodeBase() + getParameter("plot_image")));
            mt.addImage(image, 0);
            try {
                mt.waitForAll();
            } catch (InterruptedException ie) {
                ;
            }

            chart.setBackgroundImage(image);
        } catch (java.net.MalformedURLException mfue) {
            System.err.println(mfue);
        }

        String chart_bgcolor = getParameter("chart_bgcolor");
        if (chart_bgcolor != null && chart_bgcolor.length() > 0) {
        } else {
            //
        }
        return chart;

    }

    public void start() {
        super.start();

        AppletMessageListener.getInstance().setSessionDataDelegate(this);
        AppletMessageListener.getInstance().setMemoryStatDelegate(this);
    }

    public void stop() {
        AppletMessageListener.getInstance().stop();
        super.stop();
    }

    public void didCompleteBagProcessing(org.jgroups.Message msg) {
    }

    /**
     * called by the receiver when objects are received we can then decide if we want to deal with this message or not
     *
     */
    public void didReceiveSessionDataBean(org.jgroups.Message msg, TransferBean obj) {
        SessionDataBean sdb = (SessionDataBean) obj;
        if (userDataSet == null) {
            userDataSet = new DefaultCategoryDataset();
        }
        if (sdb.getContext().equalsIgnoreCase("ALL_CONTEXTS") || sdb.getContext().equalsIgnoreCase("notinarttree")) {
            return;
        } else {
            userDataSet.setValue(new Integer(sdb.getFiveMinSessions()), series2, sdb.getContext());
        }

    }

    /**
     * Entry point for the sample application.
     *
     * @param args ignored.
     */
    public static void main(String[] args) {

        JFrame frame = new JFrame("Memory Usage Demo");
        LiveSessions app = new LiveSessions();
        app.plot_bgcolor = "#A57B46";//getParameter("plot_bgcolor");
        app.chart_bgcolor = "#FFFFFF";

        JPanel panel = app.getMainPanel();
        frame.getContentPane().add(panel, BorderLayout.CENTER);
        frame.setBounds(200, 120, 600, 280);
        frame.setVisible(true);

        frame.addWindowListener(new WindowAdapter() {

            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
    }


    /*
     * (non-Javadoc) @see java.applet.Applet#init()
     */
    public void init() {
        super.init();

        instance = this;
        //tabbedPane = new JTabbedPane(JTabbedPane.TOP ,JTabbedPane.SCROLL_TAB_LAYOUT);
        cards = new JPanel(new CardLayout());
        plot_bgcolor = getParameter("plot_bgcolor");
        chart_bgcolor = getParameter("chart_bgcolor");

        cards.add(new MessagingApplet().getMessagePanel(getCodeBase().toString(), this), "ART Chat!");
        cards.add(getMainPanel(), "Sessions");

        System.out.println("remote host : " + this.getCodeBase().getHost());

        getContentPane().setLayout(new BorderLayout(0, 0));

        JMenuBar menuBar = new JMenuBar();

        // construct the view menu
        JMenu viewMenu = new JMenu("View");
        viewMenu.setMnemonic((int) 'V');
        JMenuItem viewSessions = new JMenuItem("Active Sessions");
        viewSessions.setBackground(Color.decode("#D8B478"));
        viewSessions.setMnemonic((int) 'S');
        JMenuItem chat = new JMenuItem("Chat Tool");
        chat.setBackground(Color.decode("#D8B478"));
        chat.setMnemonic((int) 'C');
        viewMenu.add(viewSessions);
        viewSessions.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                new Thread(new Runnable() {

                    public void run() {
                        try {
                            QueryBean qbean = new QueryBean();
                            qbean.setSerial(String.valueOf(queryNum++));
                            qbean.setQuery("select recordpk from accessrecords order by recordpk desc limit 1");

                            FutureResult result = new FutureResult();
                            AppletMessageListener.getInstance().registerQuery(qbean.getSerial(), result);

                            AppletMessageListener.getInstance().sendQueryBean(qbean);

                            QueryBean qbean2 = null;
                            try {
                                qbean2 = (QueryBean) result.timedGet(10000);
                            } catch (java.lang.reflect.InvocationTargetException ite) {
                                System.err.println(ite);
                            }
                            System.out.println("querybean 2: " + new String(qbean2.getResponse()));
                        } catch (InterruptedException ex) {
                        }
                    }
                }).start();

                CardLayout cl = (CardLayout) (cards.getLayout());
                cl.show(cards, "Sessions");
            }
        });
        viewMenu.add(chat);
        chat.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                CardLayout cl = (CardLayout) (cards.getLayout());
                cl.show(cards, "ART Chat!");
            }
        });

        // construct the memory menu
        JMenu memoryMenu = new JMenu("Memory Usage");
        memoryMenu.setMnemonic((int) 'M');
        byMachine = new JMenu("By Machine");
        byMachine.setBackground(Color.decode("#D8B478"));
        byMachine.setMnemonic((int) 'a');
        byContext = new JMenu("By Context");
        byContext.setMnemonic((int) 'C');
        byContext.setBackground(Color.decode("#D8B478"));

        memoryMenu.add(byMachine);
        memoryMenu.add(byContext);

        menuBar.add(viewMenu);
        menuBar.add(memoryMenu);
        setJMenuBar(menuBar);
        menuBar.setBackground(Color.decode("#D8B478"));
        viewMenu.setBackground(Color.decode("#D8B478"));
        memoryMenu.setBackground(Color.decode("#D8B478"));

        String chart_bgcolor = getParameter("chart_bgcolor");

        getContentPane().add(BorderLayout.CENTER, cards);
    }

    public void LiveSessions() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
        }
    }

    public void showDefaultView() {
        CardLayout cl = (CardLayout) (cards.getLayout());
        cl.show(cards, "Sessions");
    }

    class SymAction implements ActionListener {

        public void actionPerformed(ActionEvent event) {
            Object object = event.getSource();

            if (event.getActionCommand().equals("ExpandGraph")) {

                GraphingFrame.getInstance().addGraphPanel(chartPanel, mainPanel);
                GraphingFrame.getInstance().show();

            } else if (event.getActionCommand().equals("RemoveGraph")) {
                GraphingFrame.getInstance().returnPanel(mainPanel);
                mainPanel.updateUI();
            }
        }
    }

    /**
     * Java Doc comments go here.
     */
    public void didReceiveMemoryStatBean(Message msg, TransferBean obj) {
        MemoryStatBean bean = (MemoryStatBean) obj;
        final String server = bean.getServer();
        final String context = bean.getContext();
        String key = server + "~" + context;

        MemoryGraph mg = (MemoryGraph) memoryCharts.get(key);
        if (mg == null) {
            MemoryGraph memGraphPanel = new MemoryGraph(bean, this);
            mg = memGraphPanel;
            memoryCharts.put(key, memGraphPanel);
            JMenu machineMenu;
            if ((machineMenu = machineMap.get(server)) == null) {
                machineMenu = new JMenu(server);
                machineMenu.setBackground(Color.decode("#D8B478"));
                machineMap.put(server, machineMenu);
                byMachine.add(machineMenu);
            }
            if ((machineContextMap.get(server + context)) == null) {
                JMenuItem machineContextItem = new JMenuItem(context);
                machineContextItem.setBackground(Color.decode("#D8B478"));
                machineMenu.add(machineContextItem);
                machineContextItem.addActionListener(new ActionListener() {

                    public void actionPerformed(ActionEvent e) {
                        CardLayout cl = (CardLayout) (cards.getLayout());
                        cl.show(cards, server + " /" + context);
                    }
                });
            }

            JMenu contextMenu;
            if ((contextMenu = contextMap.get(context)) == null) {
                contextMenu = new JMenu(context);
                contextMenu.setBackground(Color.decode("#D8B478"));
                contextMap.put(context, contextMenu);
                byContext.add(contextMenu);
                JMenuItem allMachinesByContext = new JMenuItem("All");
                allMachinesByContext.setBackground(Color.decode("#D8B478"));
                contextMenu.add(allMachinesByContext);
                allMachinesByContext.addActionListener(new ActionListener() {

                    public void actionPerformed(ActionEvent e) {
                        for (String value : memoryCharts.keySet()) {
                            //System.out.println("memoryGraph value: " + value);
                            int tilde = value.lastIndexOf("~");
                            if (tilde != -1) {
                                if (value.substring(tilde + 1).equals(context)) {
                                    MemoryGraph graph = (MemoryGraph) memoryCharts.get(value);
                                    graph.expandGraph();
                                }
                            }
                        }
                    }
                });
            }

            JMenuItem contextMachineItem = new JMenuItem(server);
            contextMenu.add(contextMachineItem);
            contextMachineItem.setBackground(Color.decode("#D8B478"));
            contextMachineItem.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    CardLayout cl = (CardLayout) (cards.getLayout());
                    cl.show(cards, server + " /" + context);
                }
            });

            cards.add(memGraphPanel, server + " /" + context);
            //System.out.println("New Tabbed Pane added.  ");
        }
        mg.update(bean);
    }
}
