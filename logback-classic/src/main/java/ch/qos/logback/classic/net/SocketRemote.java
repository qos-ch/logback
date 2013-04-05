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

package ch.qos.logback.classic.net;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;

import javax.net.SocketFactory;

import org.slf4j.ILoggerFactory;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.net.SocketAppenderBase;
import ch.qos.logback.core.net.SocketConnector;
import ch.qos.logback.core.net.SocketConnectorBase;
import ch.qos.logback.core.spi.ContextAwareBase;
import ch.qos.logback.core.spi.LifeCycle;
import ch.qos.logback.core.util.CloseUtil;

/**
 * A component that receives serialized {@link ILoggingEvent} objects from a
 * remote appender over a {@link Socket}.
 * 
 * @author Carl Harris
 */
public class SocketRemote extends ContextAwareBase
    implements LifeCycle, SocketConnector.ExceptionHandler, Runnable {

  private static final int DEFAULT_ACCEPT_CONNECTION_DELAY = 5000;
  
  private String host;
  private InetAddress address;
  private int port;
  private int reconnectionDelay;
  private int acceptConnectionTimeout = DEFAULT_ACCEPT_CONNECTION_DELAY;

  private ExecutorService executor;
  private boolean started;
  private String remoteId;
  private volatile Socket socket;
  
  /**
   * {@inheritDoc}
   */
  public void start() {
    if (isStarted()) return;
    
    if (getContext() == null) {
      throw new IllegalStateException("context not set");
    }
    
    int errorCount = 0;
    if (port == 0) {
      errorCount++;
      addError("No port was configured for remote. "
          + "For more information, please visit http://logback.qos.ch/codes.html#receiver_no_port");
    }

    if (host == null) {
      errorCount++;
      addError("No host name or address was configured for remote. " 
          + "For more information, please visit http://logback.qos.ch/codes.html#receiver_no_host");
    }
    
    if (reconnectionDelay == 0) {
      reconnectionDelay = SocketAppenderBase.DEFAULT_RECONNECTION_DELAY;
    }
    
    if (errorCount == 0) {
      try {
        address = InetAddress.getByName(host);
      }
      catch (UnknownHostException ex) {
        addError("unknown host: " + host);
        errorCount++;
      }
    }
        
    if (errorCount == 0) {
      remoteId = "remote " + host + ":" + port + ": ";
      executor = createExecutorService();
      executor.execute(this);
      started = true;
    }
  }

  /**
   * {@inheritDoc}
   */
  public void stop() {
    if (!isStarted()) return;
    if (socket != null) {
      CloseUtil.closeQuietly(socket);
    }
    executor.shutdownNow();
    started = false;
  }

  /**
   * {@inheritDoc}
   */
  public boolean isStarted() {
    return started;
  }

  /**
   * {@inheritDoc}
   */
  public void run() {
    try {
      LoggerContext lc = awaitConfiguration();      
      SocketConnector connector = createConnector(address, port, 0, 
          reconnectionDelay);
      while (!executor.isShutdown() && 
          !Thread.currentThread().isInterrupted()) {
        try {
          executor.execute(connector);
        }
        catch (RejectedExecutionException ex) {
          // executor is shutting down... 
          continue;
        }
        socket = connector.awaitConnection();
        dispatchEvents(lc);
        connector = createConnector(address, port, reconnectionDelay);
      }
    }
    catch (InterruptedException ex) {
      assert true;    // ok... we'll exit now
    }
    addInfo("shutting down");
  }
  
  private LoggerContext awaitConfiguration() throws InterruptedException {
    ILoggerFactory factory = LoggerFactory.getILoggerFactory();
    while (!(factory instanceof LoggerContext)
        && !Thread.currentThread().isInterrupted()) {
      Thread.sleep(500);
      factory = LoggerFactory.getILoggerFactory();
    }
    return (LoggerContext) factory;
  }

  private void dispatchEvents(LoggerContext lc) {
    try {
      socket.setSoTimeout(acceptConnectionTimeout);
      ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
      socket.setSoTimeout(0);
      addInfo(remoteId + "connection established");
      while (true) {
        ILoggingEvent event = (ILoggingEvent) ois.readObject();
        Logger remoteLogger = lc.getLogger(event.getLoggerName());
        if (remoteLogger.isEnabledFor(event.getLevel())) {
          remoteLogger.callAppenders(event);
        }
      }     
    }
    catch (EOFException ex) {
      addInfo(remoteId + "end-of-stream detected");
    }
    catch (IOException ex) {
      addInfo(remoteId + "connection failed: " + ex);
    }
    catch (ClassNotFoundException ex) {
      addInfo(remoteId + "unknown event class: " + ex);
    }
    finally {
      CloseUtil.closeQuietly(socket);
      socket = null;
      addInfo(remoteId + "connection closed");
    }
  }
   
  /**
   * {@inheritDoc}
   */
  public void connectionFailed(SocketConnector connector, Exception ex) {
    if (ex instanceof InterruptedException) {
      addInfo("connector interrupted");
    }
    else if (ex instanceof ConnectException) {
      addInfo(remoteId + "connection refused");
    }
    else {
      addInfo(remoteId + ex);
    }
  }

  private SocketConnector createConnector(InetAddress address, int port, 
      int delay) {
    return createConnector(address, port, delay, delay);
  }

  private SocketConnector createConnector(InetAddress address, int port,
      int initialDelay, int retryDelay) {
    SocketConnector connector = newConnector(address, port, initialDelay, 
        retryDelay);
    connector.setExceptionHandler(this);
    connector.setSocketFactory(getSocketFactory());
    return connector;
  }
  
  protected SocketConnector newConnector(InetAddress address, 
      int port, int initialDelay, int retryDelay) {
    return new SocketConnectorBase(address, port, initialDelay, retryDelay);
  }
  
  protected SocketFactory getSocketFactory() {
    return SocketFactory.getDefault();
  }

  protected ExecutorService createExecutorService() {
    return Executors.newCachedThreadPool();
  }
  
  public void setHost(String host) {
    this.host = host;
  }

  public void setRemoteHost(String host) {
    setHost(host);
  }
  
  public void setPort(int port) {
    this.port = port;
  }

  public void setReconnectionDelay(int reconnectionDelay) {
    this.reconnectionDelay = reconnectionDelay;
  }

  public void setAcceptConnectionTimeout(int acceptConnectionTimeout) {
    this.acceptConnectionTimeout = acceptConnectionTimeout;
  }
    
}
