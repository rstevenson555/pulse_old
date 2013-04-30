/*
 * ClientProgram.java
 *
 * Created on June 29, 2001, 11:42 PM
 */

package com.bos.art.logServer.utils;

import java.net.Socket;
import java.io.PrintWriter;

/**
 * @author  root
 * @version
 */
public class ClientProgram extends Object {
    /** Creates new ClientProgram */
    public ClientProgram() {
    }

    /** @param args the command line arguments */
    public static void main(String args[]) throws java.net.UnknownHostException, java.io.IOException {
        Socket s = new Socket("127.0.0.1", 5555);
        PrintWriter out = new PrintWriter(s.getOutputStream());
        out.print(messageHead);
        out.print(queueConfig);
        for (int i = 0; i < 1000; ++i) {
            out.print(message);
        }
        out.print(cDataConfig);
        out.print(cDataTest);
        out.print(queueConfig2);
        out.print(cDataTest);
        out.print(queueConfig3);
        out.print(cDataTest);
        for (int i = 0; i < 1000; ++i) {
            out.print(message2);
            out.print(message3);
        }
        out.print(cDataTest);
        out.print(messageTail);
        out.print("</MESSAGES>");
        out.flush();
        out.close();
        s.close();
    }

    static String messageHead = "<HEADTAG>";
    static String message = "<EVENT  type=\"log\"  id=\"jsptiming\"  appname=\"orderpoint\"  servername=\"op-099/10.3.12.75\"><PAGE  name=\"cart\"  begin=\"true\"><DATE>06/29/2001</DATE><TIME> 10:49:33 AM</TIME><USERINFO  sessionid=\"dnupo6l2r2\"><IP>127.0.0.1</IP><USERKEY>014260uuser3501</USERKEY></USERINFO></PAGE></EVENT>";
    static String queueConfig = "<EVENT  type=\"config\"  id=\"jsptiming\"  appname=\"orderpoint\"  servername=\"op-099/10.3.12.75\"><FILENAME>TESTFILE.xml</FILENAME><DATE>06/29/2001</DATE><TIME> 10:49:33 AM</TIME><MAXSIZE>12</MAXSIZE><MAXAGE>2</MAXAGE></EVENT>";
    static String message2 = "<EVENT  type=\"log\"  id=\"timing1\"  appname=\"ot\"  servername=\"op-099/10.3.12.75\"><PAGE  name=\"cart\"  begin=\"true\"><DATE>06/29/2001</DATE><TIME> 10:49:33 AM</TIME><USERINFO  sessionid=\"dnupo6l2r2\"><IP>127.0.0.1</IP><USERKEY>014260uuser3501</USERKEY></USERINFO></PAGE></EVENT>";
    static String queueConfig2 = "<EVENT  type=\"config\"  id=\"timing1\"  appname=\"ot\"  servername=\"op-099/10.3.12.75\"><FILENAME>timing1Test.xml</FILENAME><DATE>06/29/2001</DATE><TIME> 10:49:33 AM</TIME><MAXSIZE>100</MAXSIZE><MAXAGE>48</MAXAGE></EVENT>";
    static String message3 = "<EVENT  type=\"log\"  id=\"timing2\"  appname=\"ot\"  servername=\"op-099/10.3.12.75\"><PAGE  name=\"cart\"  begin=\"true\"><DATE>06/29/2001</DATE><TIME> 10:49:33 AM</TIME><USERINFO  sessionid=\"dnupo6l2r2\"><IP>127.0.0.1</IP><USERKEY>014260uuser3501</USERKEY></USERINFO></PAGE></EVENT>";
    static String queueConfig3 = "<EVENT  type=\"config\"  id=\"timing2\"  appname=\"ot\"  servername=\"op-099/10.3.12.75\"><FILENAME>timing2Test.xml</FILENAME><DATE>06/29/2001</DATE><TIME> 10:49:33 AM</TIME><MAXSIZE>12</MAXSIZE><MAXAGE>1</MAXAGE></EVENT>";
    static String messageTail = "</HEADTAG>";
    static String cDataConfig = "<EVENT  type=\"config\"  id=\"stderr\"  appname=\"reliable\"  servername=\"op-099/10.3.12.75\"><FILENAME>reliableStderr.xml</FILENAME><DATE>06/29/2001</DATE><TIME> 10:49:33 AM</TIME><MAXSIZE>12</MAXSIZE><MAXAGE>1</MAXAGE></EVENT>";
    static String cDataTest = "<EVENT type=\"log\" id=\"stderr\" appname=\"reliable\" servername=\"127.1.1.10\"><![CDATA[<data>This  Some Change sample reliableMessage</data>]]> </EVENT>";
}
