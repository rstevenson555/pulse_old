/*
 * LogParserGUI.java
 *
 * Created on March 26, 2001, 3:33 PM
 */

package logParser;

/**
 *
 * @author  i0360d3
 * @version 
 */
public class LogParserGUI extends javax.swing.JFrame {

    /** Creates new form LogParserGUI */
    public LogParserGUI() {
        initComponents ();
        pack ();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the FormEditor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        jPanel1 = new javax.swing.JPanel();
        panel5 = new java.awt.Panel();
        RunQuery = new javax.swing.JButton();
        panel6 = new java.awt.Panel();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        panel1 = new java.awt.Panel();
        panel2 = new java.awt.Panel();
        panel3 = new java.awt.Panel();
        panel4 = new java.awt.Panel();
        getContentPane().setLayout(new java.awt.GridLayout(1, 2));
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                exitForm(evt);
            }
        }
        );
        
        jPanel1.setLayout(new java.awt.GridLayout(2, 1));
        
        panel5.setLayout(new java.awt.GridLayout(2, 3));
          panel5.setFont(new java.awt.Font ("Dialog", 0, 11));
          panel5.setName("panel5");
          panel5.setBackground(new java.awt.Color (204, 204, 204));
          panel5.setForeground(java.awt.Color.black);
          
          RunQuery.setLabel("RunQuery");
            RunQuery.setText("jButton1");
            panel5.add(RunQuery);
            
            jPanel1.add(panel5);
          
          
        panel6.setFont(new java.awt.Font ("Dialog", 0, 11));
          panel6.setName("panel6");
          panel6.setBackground(new java.awt.Color (204, 204, 204));
          panel6.setForeground(java.awt.Color.black);
          jPanel1.add(panel6);
          
          
        getContentPane().add(jPanel1);
        
        
        
        panel1.setFont(new java.awt.Font ("Dialog", 0, 11));
          panel1.setName("panel1");
          panel1.setBackground(new java.awt.Color (153, 153, 153));
          panel1.setForeground(java.awt.Color.black);
          jTabbedPane1.addTab("panel1", panel1);
          
          
        panel2.setFont(new java.awt.Font ("Dialog", 0, 11));
          panel2.setName("panel2");
          panel2.setBackground(new java.awt.Color (153, 153, 153));
          panel2.setForeground(java.awt.Color.black);
          jTabbedPane1.addTab("panel2", panel2);
          
          
        panel3.setFont(new java.awt.Font ("Dialog", 0, 11));
          panel3.setName("panel3");
          panel3.setBackground(new java.awt.Color (153, 153, 153));
          panel3.setForeground(java.awt.Color.black);
          jTabbedPane1.addTab("panel3", panel3);
          
          
        panel4.setFont(new java.awt.Font ("Dialog", 0, 11));
          panel4.setName("panel4");
          panel4.setBackground(new java.awt.Color (153, 153, 153));
          panel4.setForeground(java.awt.Color.black);
          jTabbedPane1.addTab("panel4", panel4);
          
          
        getContentPane().add(jTabbedPane1);
        
    }//GEN-END:initComponents

    /** Exit the Application */
    private void exitForm(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_exitForm
        System.exit (0);
    }//GEN-LAST:event_exitForm

    /**
    * @param args the command line arguments
    */
    public static void main (String args[]) {
        new LogParserGUI ().show ();
    }


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel jPanel1;
    private java.awt.Panel panel5;
    private javax.swing.JButton RunQuery;
    private java.awt.Panel panel6;
    private javax.swing.JTabbedPane jTabbedPane1;
    private java.awt.Panel panel1;
    private java.awt.Panel panel2;
    private java.awt.Panel panel3;
    private java.awt.Panel panel4;
    // End of variables declaration//GEN-END:variables

}
