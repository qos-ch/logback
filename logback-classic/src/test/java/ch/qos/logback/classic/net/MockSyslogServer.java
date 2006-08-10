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

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author Ceki G&uumllc&uuml;
 */
public class MockSyslogServer extends Thread {

  static final int PORT = 14805;

  final int loopLen;

  List<String> msgList = new ArrayList<String>();
  boolean finished = false;
  
  MockSyslogServer(int loopLen) {
    super();
    this.loopLen = loopLen;
  }

  @Override
  public void run() {
    DatagramSocket socket = null;
    try {
      socket = new DatagramSocket(PORT);

      for (int i = 0; i < loopLen; i++) {
        byte[] buf = new byte[1024];
        DatagramPacket packet = new DatagramPacket(buf, buf.length);
        socket.receive(packet);
        String msg = new String(buf, 0, packet.getLength());
        msgList.add(msg);
      }
    } catch (Exception se) {
      se.printStackTrace();
    } finally {
      if(socket != null) {
        socket.close();
      }
    }
    finished = true;
  }
}
