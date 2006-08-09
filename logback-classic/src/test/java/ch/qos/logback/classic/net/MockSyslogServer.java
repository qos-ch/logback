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

import java.net.DatagramSocket;

/**
 *
 * @author Ceki G&uumllc&uuml;
 */
public class MockSyslogServer extends Thread {

  final int loopLen;
  DatagramSocket socket;
  
  MockSyslogServer(int loopLen) {
    super();
    this.loopLen = loopLen;
    
  }
  
  @Override
  public void run() {
     for(int i = 0; i < loopLen; i++) {
       
     }
  }
}
