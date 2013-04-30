package com.bos.applets;

import com.bos.applets.arch.AppletMessageListener;
import com.bos.applets.arch.jcookies.CookiesJar;
import com.bos.art.logParser.broadcast.beans.ChatBean;
import com.bos.art.logParser.broadcast.beans.TransferBean;
import com.bos.art.logParser.broadcast.beans.UserBean;
import com.bos.art.logParser.broadcast.beans.delegate.ChatDelegate;
import com.bos.art.logParser.broadcast.beans.delegate.UserDelegate;
import java.applet.AudioClip;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.*;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.util.HashMap;
import javax.swing.*;
import org.jgroups.Address;
import org.jgroups.Channel;
import org.jgroups.ChannelListener;
import org.jgroups.Message;

/**
 * A basic extension of the JApplet class
 */
public class MessagingApplet extends JApplet implements ChatDelegate, UserDelegate,ChannelListener
{
    private String localIp = "";
    private HashMap userMap = new HashMap();
	private JApplet applet;
    private String codeBase = null;
    // this is a test

    @Override
    public void destroy()
    {
        AppletMessageListener.getInstance().stop();
    }

    public void setLocalIp(String s){
        localIp =s;
    }

    @Override
	public void init()
	{		
        String webCodeBase = getCodeBase().toString();
        codeBase = webCodeBase;
        
	    initMainPanel(webCodeBase, null);
		getContentPane().setLayout(new BorderLayout(0,0));
		getContentPane().add(BorderLayout.CENTER,mainPanel);
	    
	}
	/**
	 * 
	 */
	private void initMainPanel(String webCodeBase, JApplet a ) {
        try {
            URL sendIconURL = new URL(webCodeBase+"images/send.gif");
            sendIcon = new ImageIcon( sendIconURL );
            
            if(a == null){
            	a = this;
            }
            applet = a;
            artMessage = a.getAudioClip(new URL(webCodeBase+"artMessage.wav"));
            firstConnect = a.getAudioClip(new URL(webCodeBase+"FirstConnect.wav"));
            memberLeaving = a.getAudioClip(new URL(webCodeBase+"MemberLeaving.wav"));
            newMember = a.getAudioClip(new URL(webCodeBase+"NewMember.wav"));
        }
        catch(MalformedURLException mfue)
        {
            System.out.println("Bad URL " + mfue);
        }	
		// This code is automatically generated by Visual Cafe when you add
		// components to the visual environment. It instantiates and initializes
		// the components. To modify the code, only use code syntax that matches
		// what Visual Cafe can generate, or Visual Cafe may be unable to back
		// parse your Java file into its visual environment.
		//{{INIT_CONTROLS
		setSize(599,284);
		mainPanel.setNextFocusableComponent(messageField);
		mainPanel.setLayout(new BorderLayout(5,5));
		mainPanel.setBounds(20,32,528,208);
		textHistoryScrollPane.setOpaque(true);
        liveFeedButton.setToolTipText("Click to start receiving the live feed data");
        liveFeedButton.setText("Click to start the Live feed");
		liveFeedButton.setBounds(465,0,63,25);
        mainPanel.add(BorderLayout.CENTER,liveFeedButton);
		//mainPanel.add(BorderLayout.CENTER,textHistoryScrollPane);
		textHistoryScrollPane.setBounds(0,0,528,208);
		textArea.setEditable(false);
		textArea.setTabSize(4);
		textHistoryScrollPane.getViewport().add(textArea);
        textHistoryScrollPane.setVisible(false);
        liveFeedButton.setVisible(true);
        liveFeedButton.setEnabled(true);
		textArea.setFont(new Font("Courier", Font.PLAIN, 11));
		textArea.setLineWrap(true);
		textArea.setWrapStyleWord(true);
		textArea.setColumns(50);
		textArea.setBounds(0,0,525,205);
		nameListScrollPane.setOpaque(true);
		mainPanel.add(BorderLayout.EAST,nameListScrollPane);
		nameListScrollPane.setBounds(269,0,259,178);		
		nameList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		nameListScrollPane.getViewport().add(nameList);
		nameList.setBounds(0,0,256,175);		
		textEntryPane.setLayout(new BorderLayout(5,0));
		mainPanel.add(BorderLayout.SOUTH,textEntryPane);
		textEntryPane.setBounds(0,183,528,25);
		messageField.setText("Not Connected...");
		textEntryPane.add(BorderLayout.CENTER,messageField);
		messageField.setEnabled(false);
		messageField.setBounds(0,0,460,25);
		sendButton.setToolTipText("Click to send your message");
		sendButton.setText("Send");
		sendButton.setMnemonic((int)'S');
		textEntryPane.add(BorderLayout.EAST,sendButton);
		sendButton.setEnabled(false);
		sendButton.setBounds(465,0,63,25);
		//$$ stringListModel1.move(0,285);
		//}}
        //
        //
        mainPanel.setBackground(Color.decode("#D8B478"));
        
        sendButton.setIcon( sendIcon );
        sendButton.setText( "" );
        sendButton.setBorderPainted(false);
        sendButton.setBackground(Color.white);

        liveFeedButton.setBackground(Color.green);
       
		nameList.setModel(listModel);
		
		nameList.setPrototypeCellValue("Index 1234567890123");
	
		//{{REGISTER_LISTENERS
		SymMouse aSymMouse = new SymMouse();
		messageField.addMouseListener(aSymMouse);
		SymAction lSymAction = new SymAction();
		sendButton.addActionListener(lSymAction);
		liveFeedButton.addActionListener(lSymAction);
		SymKey aSymKey = new SymKey();
		messageField.addKeyListener(aSymKey);
		//}}

	}

	public JPanel getMessagePanel(String urlbase, LiveSessions a){
		initMainPanel(urlbase, a);
		return mainPanel;
	}

	
	//{{DECLARE_CONTROLS
	JPanel mainPanel = new JPanel();
	JScrollPane textHistoryScrollPane = new JScrollPane();
	JTextArea textArea = new JTextArea();
	JScrollPane nameListScrollPane = new JScrollPane();
	JList nameList = new JList();
	JPanel textEntryPane = new JPanel();
	JTextField messageField = new JTextField();
	JButton sendButton = new JButton();
	JButton liveFeedButton = new JButton();
	//}}
	DefaultListModel listModel = new DefaultListModel();
    ImageIcon sendIcon = null;

    private AudioClip artMessage = null;;
    private AudioClip firstConnect = null;;
    private AudioClip memberLeaving = null;;
    private AudioClip newMember = null;;
    private AudioClip typing = null;;
	private boolean firstTimeClicked = false;
    private static boolean firstTime = true;

	class SymMouse extends MouseAdapter
	{
        @Override
		public void mouseClicked(MouseEvent event)
		{
			Object object = event.getSource();
			if (object == messageField)
				messageField_mouseClicked(event);
		}
	}

	void messageField_mouseClicked(MouseEvent event)
	{
		if (!firstTimeClicked) {
		    messageField.setText("");
		    firstTimeClicked = true;
		}
			 
	}

	class SymAction implements ActionListener
	{
		public void actionPerformed(ActionEvent event)
		{
			Object object = event.getSource();
			if (object == sendButton)
				sendButton_actionPerformed(event);
            else if ( object == liveFeedButton)
                liveFeedButton_actionPerformed(event);
		}
	}

	void liveFeedButton_actionPerformed(ActionEvent event)
    {
        String host = applet.getCodeBase().getHost();

        try {
            URL origin= applet.getCodeBase(); //this is the Applet-class
            String hostName= origin.getHost();
            // first try port 80
            Socket local= null;
            try {
                local = new Socket(hostName, 80);
            }catch(Exception e) {
                try {
                    local = new Socket(hostName, 7209);
                }catch(Exception e1) {
                    e1.printStackTrace();
                }
            }
            InetAddress l3= local.getLocalAddress();
            System.out.println("first try - Local IP:"+l3.getHostAddress());
            localIp = l3.getHostAddress();
            local.close();

            if ( localIp==null ) {
                // if that's null, try the port we connected to in the browser
                local= new Socket(hostName, origin.getPort());
                l3= local.getLocalAddress();
                System.out.println("second try - Local IP:"+l3.getHostAddress());
                localIp = l3.getHostAddress();
                local.close();
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }

		messageField.setText("Connecting to Server...");
        System.out.println("host: " + host);
        System.out.println("localIP: " + localIp);

        AppletMessageListener.getInstance().start(host, localIp,new CookiesJar(applet));

        AppletMessageListener.getInstance().setUserDelegate(this);
        AppletMessageListener.getInstance().setChatDelegate(this);
        AppletMessageListener.getInstance().setChannelListener(this);

        liveFeedButton.setVisible(false);
        mainPanel.remove(liveFeedButton);

		mainPanel.add(BorderLayout.CENTER,textHistoryScrollPane);
        textHistoryScrollPane.setVisible(true);

        SwingUtilities.updateComponentTreeUI(this);

        ((LiveSessions)applet).showDefaultView();
    }

	void sendButton_actionPerformed(ActionEvent event)
	{
        //textArea.setText( textArea.getText() + messageField.getText() + "\n");
        ChatBean cbean = new ChatBean();
        cbean.setMessage( messageField.getText() );

        AppletMessageListener.getInstance().sendChatMessage( cbean );
		messageField.setText("");
	}

	class SymKey extends KeyAdapter
	{
        @Override
		public void keyTyped(KeyEvent event)
		{
			Object object = event.getSource();
			if (object == messageField)
				messageField_keyTyped(event);
		}
	}

	void messageField_keyTyped(KeyEvent event)
	{
		if ( event.getKeyChar() == KeyEvent.VK_ENTER) {
            String message = messageField.getText();
            int spacePos = message.indexOf(" ");
            if ( spacePos!=-1) {
                String word = message.substring(0,spacePos);
                if ( word.trim().equalsIgnoreCase("art")) {
                    String command = message.substring(message.indexOf("art")+3).trim();
                    AppletMessageListener.getInstance().sendCommandToServer(command);
		            messageField.setText("");
                    return;
                }
            }
            ChatBean cbean = new ChatBean();
            cbean.setMessage( messageField.getText() );

            AppletMessageListener.getInstance().sendChatMessage( cbean );
		    messageField.setText("");
		}
			 
	}

    public void didCompleteBagProcessing(Message msg)
    {
    }
    
    /**
     * called by the receiver when objects are received
     * we can then decide if we want to deal with this message or not
     **/
    public void didReceiveChatBean(Message msg,TransferBean obj)
    {
        ChatBean bean = (ChatBean)obj;
	    textArea.append( (String)bean.getMessage() + "\n");
        artMessage.play();

        textArea.setCaretPosition(textArea.getDocument().getLength());
    }

    @Override
    public void start() {
        super.start();
        System.out.println("MessagingApplet.start() .. called ");

        try {
            URL origin= this.getCodeBase(); //this is the Applet-class
            String hostName= origin.getHost();
            Socket local= null;
            try {
                local = new Socket(hostName, 80);
            }catch(Exception e) {
                try {
                    local = new Socket(hostName, 7209);
                }catch(Exception e1) {
                    e1.printStackTrace();
                }
            }
            InetAddress l3= local.getLocalAddress();
            System.out.println("Local IP3:"+l3.getHostAddress());
            localIp = l3.getHostAddress();
            local.close();

        }
        catch(Exception e)
        {
            e.printStackTrace();
        }

        firstTimeClicked = false;
    }

    @Override
    public void stop() {
        AppletMessageListener.getInstance().stop();
        super.stop();
    } 

    public void membersJoined()
    {
        newMember.play();
    }

    /**
     * this gets called for every member that leaves the cluster
     * @param hostname of the member that's leaving
     * @param port port number of member that's leaving
     **/
    public void memberLeft(String hostname,int port,String localhost)
    {
        listModel.removeElement( hostname );
    }

    /**
     * @param hostname of member that joined
     * @param port port number of member that joined
     * @param localhost, always the localhost name
     **/
    public void memberJoined(String hostname,int port,String localhost)
    {
        listModel.addElement( hostname );
    }

    public void didReceiveUserBean(Message msg, TransferBean obj)
    {
        UserBean bean = (UserBean)obj;
        String user = bean.getName();
        String address = bean.getIpAddress();
        if ( address.charAt(0)=='/')
            address= address.substring(1);

        String ipaddress = address;

        System.out.println("user joined: " + bean.getName() );
        System.out.println("user address: " + address );
        
        if ( userMap.get(ipaddress)==null) {
            listModel.addElement( user );
            userMap.put( ipaddress, user );
        }
    }


    public void userLeft(String ipaddress)
    {
        String username = (String)userMap.get( ipaddress );
        userMap.remove( ipaddress );
        listModel.removeElement( username );
    }

    public void channelConnected(Channel channel) {
       // do nothing
        firstConnect.play();
        listModel.clear();

        System.out.println("got connect message: " + channel);

        // switch to the session graph view
        if ( applet instanceof LiveSessions)
            ((LiveSessions)applet).showDefaultView();

        messageField.setText("Enter your message here and press [ENTER] or [Send]");
        messageField.setEnabled(true);
        sendButton.setEnabled(true);
    }

    public void channelDisconnected(Channel channel) {
        // do nothing
        System.err.println("Channel Disconnected");
    }

    public void channelClosed(Channel channel) {
        // do nothing
        System.err.println("Channel Closed");
    }

    public void channelShunned() {
        // do nothing
        System.err.println("Channel Shunned");
    }

    public void channelReconnected(Address addr) {
        // do nothing
        System.err.println("Channel ReConnected");
    }
    
}
