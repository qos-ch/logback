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
package ch.qos.logback.classic.net;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;

import javax.net.SocketFactory;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.net.ConnectionRunner;
import ch.qos.logback.core.net.DefaultSocketConnector;
import ch.qos.logback.core.net.AbstractSocketAppender;
import ch.qos.logback.core.net.SocketConnector;
import ch.qos.logback.core.util.CloseUtil;
import ch.qos.logback.core.util.Duration;

/**
 * A component that receives serialized {@link ILoggingEvent} objects from a
 * remote appender over a {@link Socket}.
 *
 * @author Carl Harris
 */
public class SocketReceiver extends AbstractReceiver
        implements Runnable {

  static String NO_PORT_ERROR_URL = CoreConstants.CODES_URL + "#receiver_no_port";
  static String NO_HOST_ERROR_URL = CoreConstants.CODES_URL + "#receiver_no_host";

  private static final int DEFAULT_ACCEPT_CONNECTION_DELAY = 5000;

  private String remoteHost;
  private InetAddress address;
  private int port;
  private int reconnectionDelay;
  private int acceptConnectionTimeout = DEFAULT_ACCEPT_CONNECTION_DELAY;

  private String receiverId;
  private volatile Socket socket;
  Duration reconnectionDuration = null;
  ConnectionRunner connectionRunner;

  /**
   * {@inheritDoc}
   */
  protected boolean shouldStart() {
    int errorCount = 0;
    if (port == 0) {
      errorCount++;
      addError("No port was configured for receiver. ");
      addError("For more information, please visit "+NO_PORT_ERROR_URL);
    }

    if (remoteHost == null) {
      errorCount++;
      addError("No host name or address was configured for receiver. ");
      addError("For more information, please visit "+NO_HOST_ERROR_URL);
    }

    if (reconnectionDelay == 0) {
      reconnectionDelay = AbstractSocketAppender.DEFAULT_RECONNECTION_DELAY;
    }

    if (errorCount == 0) {
      connectionRunner = new ConnectionRunner(this, remoteHost, port, reconnectionDuration);
      receiverId = "receiver " + remoteHost + ":" + port + ": ";
    }

    return errorCount == 0;
  }

  /**
   * {@inheritDoc}
   */
  protected void onStop() {
    if (socket != null) {
      CloseUtil.closeQuietly(socket);
    }
    if(connectionRunner != null) {
      connectionRunner.stop();
    }
  }

  @Override
  protected Runnable getRunnableTask() {
    return this;
  }

  /**
   * {@inheritDoc}
   */
  public void run() {
    try {
      LoggerContext lc = (LoggerContext) getContext();
      while (!Thread.currentThread().isInterrupted()) {
        socket = connectionRunner.connect();
        if (socket == null)  {
          addWarn("null socket");
          break;
        }  else {
          addInfo("Connected to "+remoteHost+":"+port);
        }
        dispatchEvents(lc);
      }
    } catch (InterruptedException ex) {
      assert true;    // ok... we'll exit now
    }
    addInfo("shutting down");
  }

  private void dispatchEvents(LoggerContext lc) {
    try {
      socket.setSoTimeout(acceptConnectionTimeout);
      addInfo("About to create input stream for reading events from "+remoteHost+":"+port);
      ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
      // wait indefinitely for incoming data
      socket.setSoTimeout(0);
      addInfo(receiverId + "connection established");
      while (true) {
        ILoggingEvent event = (ILoggingEvent) ois.readObject();
        Logger remoteLogger = lc.getLogger(event.getLoggerName());
        if (remoteLogger.isEnabledFor(event.getLevel())) {
          remoteLogger.callAppenders(event);
        }
      }
    } catch (EOFException ex) {
      addInfo(receiverId + "end-of-stream detected");
    } catch (IOException ex) {
      addInfo(receiverId + "connection failed: " + ex);
    } catch (ClassNotFoundException ex) {
      addInfo(receiverId + "unknown event class: " + ex);
    } finally {
      CloseUtil.closeQuietly(socket);
      socket = null;
      addInfo(receiverId + "connection closed");
    }
  }

  protected SocketFactory getSocketFactory() {
    return SocketFactory.getDefault();
  }

  public void setRemoteHost(String remoteHost) {
    this.remoteHost = remoteHost;
  }

  public void setPort(int port) {
    this.port = port;
  }

  public void setReconnectionDelay(Duration duration) {
    this.reconnectionDuration = duration;
  }

  public void setAcceptConnectionTimeout(int acceptConnectionTimeout) {
    this.acceptConnectionTimeout = acceptConnectionTimeout;
  }

}
