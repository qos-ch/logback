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
package ch.qos.logback.access.net;

import java.io.BufferedInputStream;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

//import ch.qos.logback.access.spi.AccessEvent;
import ch.qos.logback.access.spi.IAccessEvent;


/**
 * @author S&eacute;bastien Pennec
 */
public class MockSocketServer extends Thread {

  static final int PORT = 4560;

  final int loopLen;

  List<IAccessEvent> accessEventList = new ArrayList<IAccessEvent>();
  boolean finished = false;

  MockSocketServer(int loopLen) {
    super();
    this.loopLen = loopLen;
  }

  @Override
  public void run() {
    ObjectInputStream ois = null;
    ServerSocket serverSocket = null;
    // Object readObject;
    try {
      serverSocket = new ServerSocket(PORT);
      Socket socket = serverSocket.accept();
      ois = new ObjectInputStream(new BufferedInputStream(socket
          .getInputStream()));
      for (int i = 0; i < loopLen; i++) {
        IAccessEvent event = (IAccessEvent) ois.readObject();
        accessEventList.add(event);
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
}
