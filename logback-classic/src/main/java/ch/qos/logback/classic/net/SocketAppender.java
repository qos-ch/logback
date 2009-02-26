/**
 * Logback: the generic, reliable, fast and flexible logging framework.
 * 
 * Copyright (C) 1999-2006, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
 */

// Contributors: Dan MacDonald <dan@redknee.com>
package ch.qos.logback.classic.net;

import java.net.InetAddress;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.net.SocketAppenderBase;
import ch.qos.logback.core.spi.PreSerializationTransformer;

/**
 * Sends {@link ILoggingEvent} objects to a remote a log server, usually a
 * {@link SocketNode}.
 * 
 * For more information on this appender, please refer to the online manual
 * at http://logback.qos.ch/manual/appenders.html#SocketAppender
 * 
 * @author Ceki G&uuml;lc&uuml;
 * @author S&eacute;bastien Pennec
 */

public class SocketAppender extends SocketAppenderBase<ILoggingEvent> {

  boolean includeCallerData = false;

  PreSerializationTransformer<ILoggingEvent> pst = new LoggingEventPreSerializationTransformer();
  
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
  protected void postProcessEvent(ILoggingEvent event) {
    if (includeCallerData) {
      event.getCallerData();
    }
  }

  public void setIncludeCallerData(boolean includeCallerData) {
    this.includeCallerData = includeCallerData;
  }
  
  public PreSerializationTransformer<ILoggingEvent> getPST() {
    return pst;
  }
  
}
