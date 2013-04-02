/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2011, QOS.ch. All rights reserved.
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

package ch.qos.logback.classic.net.server;

import java.util.concurrent.Executor;

import org.slf4j.ILoggerFactory;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.net.server.ConcurrentServerRunner;
import ch.qos.logback.core.net.server.ServerListener;
import ch.qos.logback.core.net.server.ServerRunner;

/**
 * A {@link ServerRunner} that receives logging events from remote appender
 * clients.
 *
 * @author Carl Harris
 */
class RemoteAppenderServerRunner
    extends ConcurrentServerRunner<RemoteAppenderClient> {

  private LoggerContext lc;
  private Logger logger;

  /**
   * Constructs a new server runner.
   * @param listener the listener from which the server will accept new
   *    clients
   * @param executor that will be used to execute asynchronous tasks 
   *    on behalf of the runner.
   */
  public RemoteAppenderServerRunner(
      ServerListener<RemoteAppenderClient> listener, Executor executor) {
    super(listener, executor);
  }

  @Override
  protected boolean configureClient(RemoteAppenderClient client) {
    LoggerContext lc = getLoggerContext();
    if (lc == null) {
      logError("logger context not yet available");
      return false;
    }
    
    client.setLoggerContext(lc);
    return true;
  }

  protected void logInfo(String message) {
    Logger logger = getLogger();
    if (logger != null) {
      logger.info(message);
    }
    else {
      addInfo(message);
    }
  }
  
  protected void logError(String message) {
    Logger logger = getLogger();
    if (logger != null) {
      logger.error(message);
    }
    else {
      addError(message);
    }
  }
  
  protected Logger getLogger() {
    if (logger == null) {
      LoggerContext lc = getLoggerContext();
      if (lc != null) {
        logger = lc.getLogger(getClass().getPackage().getName());
      }
    }
    return logger;
  }
  
  protected LoggerContext getLoggerContext() {
    if (lc == null) {   
      ILoggerFactory factory = LoggerFactory.getILoggerFactory();
      if (factory instanceof LoggerContext) {
        lc = (LoggerContext) factory;
      }
    }
    return lc;
  }

}
