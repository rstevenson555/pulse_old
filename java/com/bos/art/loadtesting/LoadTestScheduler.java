/*
 * Created on Feb 22, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.bos.art.loadtesting;

import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JCheckBox;
import javax.swing.JList;
import javax.swing.JTextField;
import javax.swing.JComboBox;
import javax.swing.JButton;
/**
 * @author I0360D3
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class LoadTestScheduler {

	private JPanel jPanel = null;  //  @jve:decl-index=0:visual-constraint="141,9"
	private JLabel jLabel = null;
	private JCheckBox jCheckBox = null;
	private JList jList = null;
	private JLabel jLabel1 = null;
	private JTextField jTextField = null;
	private JLabel jLabel2 = null;
	private JTextField jTextField1 = null;
	private JLabel jLabel3 = null;
	private JTextField jTextField2 = null;
	private JLabel jLabel4 = null;
	private JTextField jTextField3 = null;
	private JLabel jLabel5 = null;
	private JTextField jTextField4 = null;
	private JLabel jLabel6 = null;
	private JLabel jLabel7 = null;
	private JComboBox jComboBox = null;
	private JLabel jLabel8 = null;
	private JTextField jTextField5 = null;
	private JLabel jLabel9 = null;
	private JTextField jTextField6 = null;
	private JLabel jLabel10 = null;
	private JButton jButton = null;
	private JLabel jLabel11 = null;
	private JTextField jTextField7 = null;
	/**
	 * This method initializes jPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */    
	private JPanel getJPanel() {
		if (jPanel == null) {
			jPanel = new JPanel();
			jLabel = new JLabel();
			jLabel1 = new JLabel();
			jLabel2 = new JLabel();
			jLabel3 = new JLabel();
			jLabel4 = new JLabel();
			jLabel5 = new JLabel();
			jLabel6 = new JLabel();
			jLabel7 = new JLabel();
			jLabel8 = new JLabel();
			jLabel9 = new JLabel();
			jLabel10 = new JLabel();
			jLabel11 = new JLabel();
			jPanel.setLayout(null);
			jPanel.setSize(515, 396);
			jLabel.setBounds(24, 32, 135, 31);
			jLabel.setText("Use Existing User Data");
			jLabel1.setBounds(24, 94, 137, 39);
			jLabel1.setText("Import New Data");
			jLabel2.setBounds(288, 92, 217, 23);
			jLabel2.setText("From Context (e.g. shop, shopping)");
			jLabel3.setBounds(288, 116, 217, 22);
			jLabel3.setText("machine list coma separated");
			jLabel4.setBounds(288, 139, 216, 23);
			jLabel4.setText("Start Time (yyyy-MM-dd HH24:MI:SS)");
			jLabel5.setBounds(288, 162, 216, 24);
			jLabel5.setText("End Time (yyyy-MM-dd HH24:MI:SS)");
			jLabel6.setBounds(288, 187, 215, 26);
			jLabel6.setText("To Context (e.g. shop )");
			jLabel7.setBounds(30, 227, 456, 19);
			jLabel7.setText("Actual Load test Parameters Go In the Section Below:");
			jLabel8.setBounds(14, 257, 258, 26);
			jLabel8.setText("LoadRunner Senario File");
			jLabel9.setBounds(14, 293, 258, 27);
			jLabel9.setText("TestStart Time (YYYY-MM-DD HH24:MI:SS)");
			jLabel10.setBounds(12, 324, 261, 27);
			jLabel10.setText("Context (will not work if using prod-playback)");
			jLabel11.setBounds(12, 360, 263, 24);
			jLabel11.setText("Machine (test ONLY!!!)");
			jPanel.add(jLabel, null);
			jPanel.add(getJCheckBox(), null);
			jPanel.add(getJList(), null);
			jPanel.add(jLabel1, null);
			jPanel.add(getJTextField(), null);
			jPanel.add(jLabel2, null);
			jPanel.add(getJTextField1(), null);
			jPanel.add(jLabel3, null);
			jPanel.add(getJTextField2(), null);
			jPanel.add(jLabel4, null);
			jPanel.add(getJTextField3(), null);
			jPanel.add(jLabel5, null);
			jPanel.add(getJTextField4(), null);
			jPanel.add(jLabel6, null);
			jPanel.add(jLabel7, null);
			jPanel.add(getJComboBox(), null);
			jPanel.add(jLabel8, null);
			jPanel.add(getJTextField5(), null);
			jPanel.add(jLabel9, null);
			jPanel.add(getJTextField6(), null);
			jPanel.add(jLabel10, null);
			jPanel.add(getJButton(), null);
			jPanel.add(jLabel11, null);
			jPanel.add(getJTextField7(), null);
		}
		return jPanel;
	}
	/**
	 * This method initializes jCheckBox	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */    
	private JCheckBox getJCheckBox() {
		if (jCheckBox == null) {
			jCheckBox = new JCheckBox();
			jCheckBox.setBounds(168, 36, 24, 25);
			jCheckBox.addChangeListener(new javax.swing.event.ChangeListener() { 
				public void stateChanged(javax.swing.event.ChangeEvent e) {    
					System.out.println("stateChanged()"); // TODO Auto-generated Event stub stateChanged()
				}
			});
		}
		return jCheckBox;
	}
	/**
	 * This method initializes jList	
	 * 	
	 * @return javax.swing.JList	
	 */    
	private JList getJList() {
		if (jList == null) {
			jList = new JList();
			jList.setBounds(206, 12, 279, 73);
		}
		return jList;
	}
	/**
	 * This method initializes jTextField	
	 * 	
	 * @return javax.swing.JTextField	
	 */    
	private JTextField getJTextField() {
		if (jTextField == null) {
			jTextField = new JTextField();
			jTextField.setBounds(185, 93, 92, 20);
		}
		return jTextField;
	}
	/**
	 * This method initializes jTextField1	
	 * 	
	 * @return javax.swing.JTextField	
	 */    
	private JTextField getJTextField1() {
		if (jTextField1 == null) {
			jTextField1 = new JTextField();
			jTextField1.setBounds(185, 118, 92, 19);
		}
		return jTextField1;
	}
	/**
	 * This method initializes jTextField2	
	 * 	
	 * @return javax.swing.JTextField	
	 */    
	private JTextField getJTextField2() {
		if (jTextField2 == null) {
			jTextField2 = new JTextField();
			jTextField2.setBounds(185, 140, 91, 18);
		}
		return jTextField2;
	}
	/**
	 * This method initializes jTextField3	
	 * 	
	 * @return javax.swing.JTextField	
	 */    
	private JTextField getJTextField3() {
		if (jTextField3 == null) {
			jTextField3 = new JTextField();
			jTextField3.setBounds(184, 163, 92, 19);
		}
		return jTextField3;
	}
	/**
	 * This method initializes jTextField4	
	 * 	
	 * @return javax.swing.JTextField	
	 */    
	private JTextField getJTextField4() {
		if (jTextField4 == null) {
			jTextField4 = new JTextField();
			jTextField4.setBounds(185, 188, 90, 20);
		}
		return jTextField4;
	}
	/**
	 * This method initializes jComboBox	
	 * 	
	 * @return javax.swing.JComboBox	
	 */    
	private JComboBox getJComboBox() {
		if (jComboBox == null) {
			jComboBox = new JComboBox();
			jComboBox.setBounds(288, 257, 162, 26);
		}
		return jComboBox;
	}
	/**
	 * This method initializes jTextField5	
	 * 	
	 * @return javax.swing.JTextField	
	 */    
	private JTextField getJTextField5() {
		if (jTextField5 == null) {
			jTextField5 = new JTextField();
			jTextField5.setBounds(290, 292, 161, 24);
		}
		return jTextField5;
	}
	/**
	 * This method initializes jTextField6	
	 * 	
	 * @return javax.swing.JTextField	
	 */    
	private JTextField getJTextField6() {
		if (jTextField6 == null) {
			jTextField6 = new JTextField();
			jTextField6.setBounds(292, 327, 158, 22);
		}
		return jTextField6;
	}

	/**
	 * This method initializes jButton	
	 * 	
	 * @return javax.swing.JButton	
	 */    
	private JButton getJButton() {
		if (jButton == null) {
			jButton = new JButton();
			jButton.setBounds(21, 155, 155, 56);
			jButton.setText("Submit Button");
			jButton.addActionListener(new java.awt.event.ActionListener() { 
				public void actionPerformed(java.awt.event.ActionEvent e) {    
					System.out.println("actionPerformed()"); // TODO Auto-generated Event stub actionPerformed()
				}
			});
		}
		return jButton;
	}
	/**
	 * This method initializes jTextField7	
	 * 	
	 * @return javax.swing.JTextField	
	 */    
	private JTextField getJTextField7() {
		if (jTextField7 == null) {
			jTextField7 = new JTextField();
			jTextField7.setBounds(292, 362, 157, 20);
		}
		return jTextField7;
	}
             }
