/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2009, QOS.ch. All rights reserved.
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
package ch.qos.logback.core.net;

import ch.qos.logback.core.AppenderBase;

public class TelnetAppender extends AppenderBase {

  int port; 
  
  @Override
  public void start() {
    int errorCount = 0;
    if (port == 0) {
      errorCount++;
      addError("No port was configured for appender"
          + name
          + " For more information, please visit http://logback.qos.ch/codes.html#socket_no_port");
    }

    //ServerSocket serverSocket = new ServerSocket(port);
    
//    connect(address, port);

    if (errorCount == 0) {
      this.started = true;
    }
  }

  @Override
  protected void append(Object eventObject) {
    // TODO Auto-generated method stub

  }
  
  

}
