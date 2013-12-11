package com.bos.applets;

import java.awt.*;
import java.util.ArrayList;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public class GraphingFrame extends JFrame
{
    private ArrayList panelList = new ArrayList();
    private ArrayList returnList = new ArrayList();

	private JPanel mainPanel;
    private JPanel buttonPanel;
    private JPanel titlePanel;

    private JButton oneAcross;
    private JButton twoAcross;
    private JButton threeAcross;

    private static GraphingFrame instance = new GraphingFrame("Live Graphs:1");

    private static final int ONE_BY_X = 1;
    private static final int TWO_BY_X = 2;
    private static final int THREE_BY_X = 3;

    private static int graphsAcross = TWO_BY_X;

    public static GraphingFrame getInstance()
    {
        return instance;
    }

    public GraphingFrame(String title)
    {
        super(title);

        SymWindow aSymWindow = new SymWindow();
		this.addWindowListener(aSymWindow);

        setSize(640,480);
    	mainPanel = new JPanel(new GridLayout(1,1,5,5));
        buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        titlePanel = new JPanel(new GridLayout(1,1));
        
        getContentPane().setLayout(new BorderLayout(0,0));
		getContentPane().add(BorderLayout.CENTER,mainPanel);
        getContentPane().add(BorderLayout.SOUTH, buttonPanel);
        getContentPane().add(BorderLayout.NORTH, titlePanel);
        
        javax.swing.JLabel chart_title = new javax.swing.JLabel("solutions.com (Live)");
        chart_title.setFont(new Font("Verdana", Font.BOLD, 20));
        titlePanel.add(chart_title);

        oneAcross = new javax.swing.JButton();
        oneAcross.setText("1 Column");

        twoAcross = new javax.swing.JButton();
        twoAcross.setText("2 Columns");

        threeAcross = new javax.swing.JButton();
        threeAcross.setText("3 Columns");

        buttonPanel.add(oneAcross);
        buttonPanel.add(twoAcross);
        buttonPanel.add(threeAcross);

        buttonPanel.setBackground(Color.decode("#000000"));
        mainPanel.setBackground(Color.decode("#000000"));

        SymAction lSymAction = new SymAction();

        oneAcross.addActionListener(lSymAction);
        twoAcross.addActionListener(lSymAction);
        threeAcross.addActionListener(lSymAction);
    }

    class SymAction implements java.awt.event.ActionListener {
        public void actionPerformed(java.awt.event.ActionEvent event) {
            Object object = event.getSource();

            if (object == oneAcross) {
                    
                graphsAcross = ONE_BY_X;
                adjustLayout();
                mainPanel.updateUI();

            } else if ( object == twoAcross) {
                graphsAcross = TWO_BY_X;
                adjustLayout();
                mainPanel.updateUI();

            } else if ( object == threeAcross ) {
                graphsAcross = THREE_BY_X;
                adjustLayout();
                mainPanel.updateUI();
            }
        }
    }


    public void addGraphPanel(JPanel chartPanel,JPanel returnPanel)
    {
        // first remove panel from it's original location
        returnPanel.remove( chartPanel );        
        returnPanel.updateUI();

        panelList.add( chartPanel );
        returnList.add( returnPanel );

        adjustLayout();
        
        for(int i = 0;i<panelList.size();i++) {
            mainPanel.add( (JPanel)panelList.get(i));
        }
        mainPanel.updateUI();
        SwingUtilities.updateComponentTreeUI(this);
    }

    private void adjustLayout()
    {
        // special case for 1 chart in the view
        if ( panelList.size()==1) {
            mainPanel.setLayout(new GridLayout(1,1,5,5));
        } 
        else {
            double across = graphsAcross;
            double panelListSize = panelList.size();
            int rows = (int)Math.ceil(panelListSize / across);
            //System.out.println("rows: " + rows );
            //System.out.println("grphsAcross: " + across );
            //System.out.println("panellist size: " + panelList.size() );
            mainPanel.setLayout(new GridLayout(rows,graphsAcross,5,5));
        }
    }
    
    public void returnPanel(JPanel returnPanel)
    {
        for(int i = 0,tot=returnList.size();i<tot;i++) {
            if ( returnPanel == (JPanel)returnList.get(i)) {
                JPanel chart = (JPanel)panelList.get(i);
                mainPanel.remove( chart );

                // and put them back into the main realtime applet page, tabbed pane
                returnPanel.add( BorderLayout.CENTER, chart );
                returnPanel.updateUI();

                returnList.remove( returnPanel );
                panelList.remove( chart );

                adjustLayout();
                mainPanel.updateUI();

                break;
            }                
        }

        if (returnList.size()==0) {
            hide();
            dispose();
        }
    }
    
    private void returnPanels()
    {
        int tot = panelList.size();
        for(int i=0;i<tot;i++) {
            JPanel returnPanel = (JPanel)returnList.get(i);

            // remove the charts from this frame 
            //ChartPanel chart = (ChartPanel)panelList.get(i);
            JPanel chart = (JPanel)panelList.get(i);
            mainPanel.remove( chart );
            mainPanel.updateUI();
            
            // and put them back into the main realtime applet page, tabbed pane
            returnPanel.add( BorderLayout.CENTER, chart );
            returnPanel.updateUI();

        }
    }
   
    class SymWindow extends java.awt.event.WindowAdapter
	{
		public void windowClosing(java.awt.event.WindowEvent event)
		{
            try {
			    Object object = event.getSource();

                returnPanels();

                panelList.clear();
                returnList.clear();
            }
            catch(Exception e)
            {
                System.err.println("Error closing window " + e);
            }
		}
	}

	void GraphingFrame_windowClosing(java.awt.event.WindowEvent event)
	{
		// to do: code goes here.
			 
		GraphingFrame_windowClosing_Interaction1(event);
	}

	void GraphingFrame_windowClosing_Interaction1(java.awt.event.WindowEvent event) {
		try {
			//this.exitApplication();
            //System.out.println("window closing");
            returnPanels();
		} catch (Exception e) {
            System.err.println("Error in GraphingFrame_windowClosing: " +e);
		}
	}

}
