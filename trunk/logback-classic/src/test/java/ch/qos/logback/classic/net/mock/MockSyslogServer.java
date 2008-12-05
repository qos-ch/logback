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

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author Ceki G&uumllc&uuml;
 */
public class MockSyslogServer extends Thread {

  final int loopLen;
  final int port;
  
  List<String> msgList = new ArrayList<String>();
  boolean finished = false;
  
  public MockSyslogServer(int loopLen, int port) {
    super();
    this.loopLen = loopLen;
    this.port = port;
  }

  @Override
  public void run() {
    //System.out.println("MockSyslogServer listening on port "+port);
    DatagramSocket socket = null;
    try {
      socket = new DatagramSocket(port);

      for (int i = 0; i < loopLen; i++) {
        byte[] buf = new byte[2048];
        DatagramPacket packet = new DatagramPacket(buf, buf.length);
        //System.out.println("Waiting for message");
        socket.receive(packet);
        //System.out.println("Got message");
        String msg = new String(buf, 0, packet.getLength());
        msgList.add(msg);
      }
    } catch (Exception se) {
      se.printStackTrace();
    } finally {
      if(socket != null) {
	  try {socket.close();} catch(Exception e) {}
      }
    }
    finished = true;
  }
  
  public boolean isFinished() {
    return finished;
  }
  
  public List<String> getMessageList() {
    return msgList;
  }
}
