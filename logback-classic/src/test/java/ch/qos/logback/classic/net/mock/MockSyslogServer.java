/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2013, QOS.ch. All rights reserved.
 *
 * This program and the accompanying materials are dual-licensed under
 * either the terms of the Eclipse Public License v1.0 as published by
 * the Eclipse Foundation
 *
 *   or (per the licensee's choosing)
 *
 * under the terms of the GNU Lesser General Public License version 2.1
 * as published by the Free Software Foundation.
 */
package ch.qos.logback.classic.net.mock;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author Ceki G&uuml;lc&uuml;
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
        byte[] buf = new byte[65536];
        DatagramPacket packet = new DatagramPacket(buf, buf.length);
        //System.out.println("Waiting for message");
        socket.receive(packet);
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
