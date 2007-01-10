/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * 
 * Copyright (C) 1999-2006, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
 */
package ch.qos.logback.classic.net.mock;

import java.io.BufferedInputStream;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import ch.qos.logback.classic.spi.LoggingEvent;

/**
 * 
 * 
 * @author S&eacute;bastien Pennec
 */
public class MockSocketServer extends Thread {

  static final int PORT = 4560;

  final int loopLen;

  List<LoggingEvent> loggingEventList = new ArrayList<LoggingEvent>();
  boolean finished = false;

  public MockSocketServer(int loopLen) {
    super();
    this.loopLen = loopLen;
  }

  @Override
  public void run() {
    ObjectInputStream ois = null;
    ServerSocket serverSocket = null;
    // Object readObject;
    LoggingEvent event;
    try {
      // System.out.println("Listening on port " + PORT);
      serverSocket = new ServerSocket(PORT);
      // System.out.println("Waiting to accept a new client.");
      Socket socket = serverSocket.accept();
      // System.out.println("Connected to client at " +
      // socket.getInetAddress());
      ois = new ObjectInputStream(new BufferedInputStream(socket
          .getInputStream()));
      for (int i = 0; i < loopLen; i++) {
        event = (LoggingEvent) ois.readObject();
        // System.out.println("* LoggerName:" + event.getLogger().getName());
        // System.out.println("* Context Name: " +
        // event.getLogger().getLoggerContext().getName());
        loggingEventList.add(event);
      }
    } catch (Exception se) {
      se.printStackTrace();
    } finally {

      if (ois != null) {
        try {
          ois.close();
        } catch (Exception e) {
        }
      }
      if (serverSocket != null) {
        try {
          serverSocket.close();
        } catch (Exception e) {
        }
      }
    }
    finished = true;
  }
  
  public boolean isFinished() {
    return finished;
  }
  
  public List<LoggingEvent> getEventsList() {
    return loggingEventList;
  }
}
