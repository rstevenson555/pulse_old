/*
 * Created on Oct 22, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package com.bos.art.logParser.test;

import java.io.BufferedOutputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Calendar;

import com.bos.art.logParser.records.UserRequestTiming;

/**
 * @author I0360D3
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class FakeLoadTester {

	public static void main(String args[]) {
		String host = "art-app1.int.bcop.com";
		int port = 5557;
		try {
			Socket s = new Socket(host, port);
			ObjectOutputStream out =
				new ObjectOutputStream(new BufferedOutputStream(s.getOutputStream()));
			for (int i = 0; i < 100000; ++i) {
				if (i%10000 == 0){
					out.reset();
				}
				
				int sessionIndex = getRandomLoadTime();
				int pageID = getPageID();
				out.writeObject(
					new UserRequestTiming(
						"type",
						1,
						"eventId 1",
						"appname",
						"serverName",
						"context",
						"/contextrrr/page" +pageID,
						Calendar.getInstance(),
						getRandomLoadTime(),
						"sessionId" +sessionIndex,
						"ipAddress",
						"userKey" +sessionIndex,
						"browser",
                        "instancename"));
				sessionIndex = getRandomLoadTime();
						
				out.writeObject(
					new UserRequestTiming(
						"type",
						2,
						"eventId 2",
						"appname",
						"serverName",
						"context",
						"/contextr2/page"+pageID,
						Calendar.getInstance(),
						1001,
						"sessionId" +sessionIndex,
						"ipAddress",
						"userKey" +sessionIndex,
						"browser",
                        "instancename"));
				sessionIndex = getRandomLoadTime();
				out.writeObject(
					new UserRequestTiming(
						"type",
						3,
						"eventId 3",
						"appname",
						"serverName",
						"context",
						"/contextr3/page"+pageID,
						Calendar.getInstance(),
						1001,
						"sessionId" +sessionIndex,
						"ipAddress",
						"userKey" +sessionIndex,
						"browser",
                        "instancename"));
				sessionIndex = getRandomLoadTime();
				out.writeObject(
					new UserRequestTiming(
						"type",
						5,
						"eventId 2",
						"appname",
						"serverName",
						"/contextr4/context"+pageID,
						"page",
						Calendar.getInstance(),
						2000,
						"sessionId" +sessionIndex,
						"ipAddress",
						"userKey" +sessionIndex,
						"browser",
                        "instancename"));

			}
			out.flush();
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	private static int getRandomLoadTime(){
		return (int)(Math.random()*30000);
	}
	private static int getPageID(){
		return (int)(Math.random()*300);
	}

}
