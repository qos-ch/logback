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
// Contributors: Dan MacDonald <dan@redknee.com>
package ch.qos.logback.access.net;

import java.net.InetAddress;

import ch.qos.logback.access.spi.AccessEvent;
import ch.qos.logback.core.net.SocketAppenderBase;
import ch.qos.logback.core.spi.PreSerializationTransformer;

/**
 * Sends {@link AccessEvent} objects to a remote a log server, usually a
 * {@link SocketNode}.
 * 
 * For more information about this appender, please refer to the online manual at
 * http://logback.qos.ch/manual/appenders.html#AccessSocketAppender
 *  
 * @author Ceki G&uuml;lc&uuml;
 * @author S&eacute;bastien Pennec
 * 
 */

public class SocketAppender extends SocketAppenderBase<AccessEvent> {
  
  PreSerializationTransformer<AccessEvent> pst = new AccessEventPreSerializationTransformer();
  
  public SocketAppender() {
  }

  /**
   * Connects to remote server at <code>address</code> and <code>port</code>.
   */
  public SocketAppender(InetAddress address, int port) {
    this.address = address;
    this.remoteHost = address.getHostName();
    this.port = port;
  }

  /**
   * Connects to remote server at <code>host</code> and <code>port</code>.
   */
  public SocketAppender(String host, int port) {
    this.port = port;
    this.address = getAddressByName(host);
    this.remoteHost = host;
  }
  
  @Override
  protected void postProcessEvent(AccessEvent event) {
    AccessEvent ae = (AccessEvent)event;
    ae.prepareForDeferredProcessing();
  }

  public PreSerializationTransformer<AccessEvent> getPST() {
    return pst;
  }
}
