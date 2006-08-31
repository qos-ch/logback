/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * 
 * Copyright (C) 1999-2006, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
 */
package ch.qos.logback.classic.net;

import java.io.BufferedInputStream;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * 
 * 
 * @author S&eacute;bastien Pennec
 */
public class MockSocketServer extends Thread {

	static final String LOGGINGEVENT = "LoggingEvent";
	static final String LOGGINGEVENT2 = "LoggingEvent2";
	static final String MINIMALEXT = "MinimalExt";
	static final String MINIMALSER = "MinimalSer";

	static final int PORT = 4560;

	final int loopLen;

	List<String> msgList = new ArrayList<String>();
	boolean finished = false;

	String className = LOGGINGEVENT;

	MockSocketServer(int loopLen) {
		super();
		this.loopLen = loopLen;
	}

	@Override
	public void run() {
		ObjectInputStream ois;
		Object readObject;
		try {
			//System.out.println("Listening on port " + PORT);
			ServerSocket serverSocket = new ServerSocket(PORT);
			//System.out.println("Waiting to accept a new client.");
			Socket socket = serverSocket.accept();
			//System.out.println("Connected to client at " + socket.getInetAddress());
			ois = new ObjectInputStream(new BufferedInputStream(socket
					.getInputStream()));
			for (int i = 0; i < loopLen; i++) {
				readObject = ois.readObject();
				msgList.add(readObject.toString());
			}
			ois.close();
			serverSocket.close();
		} catch (Exception se) {
			se.printStackTrace();
		}
		finished = true;
	}
}
