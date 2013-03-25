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

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.spi.ContextAwareBase;
import ch.qos.logback.core.spi.LifeCycle;

/**
 * A logging socket server that is configurable using Joran.
 *
 * @author Carl Harris
 */
public class SocketServer extends ContextAwareBase implements LifeCycle {

  private int port;
  private int backlog = SimpleSocketServer.DEFAULT_BACKLOG;
  private String address;
  private SimpleSocketServer server;
  private boolean started;
  
  /**
   * Starts the server.
   */
  public final void start() {
    if (isStarted()) return;
    LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
    server = createSocketServer(lc);
    server.start();
    started = true;
  }

  /**
   * Stops the server.
   */
  public final void stop() {
    if (!isStarted()) return;
    server.interrupt();
    try {
      server.join(5000);
      started = server.isAlive();
      if (started) {
        addWarn("server is still running");
      }
    }
    catch (InterruptedException ex) {
      addWarn("interrupted while stopping server");
    }
  }

  /**
   * Gets a flag indicating whether the server is running.
   * @return flag state
   */
  public final boolean isStarted() {
    return started;
  }

  /**
   * Creates an ordinary (non-SSL) socket server.
   * @param lc logger context for the server
   * @return socket server or {@code null} if a server socket could not
   *    be created due to an error
   */
  protected SimpleSocketServer createSocketServer(LoggerContext lc) {
    try {
      return new SimpleSocketServer(lc, getPort(), getBacklog(), 
          getInetAddress());
    }
    catch (UnknownHostException ex) {
      addError(getAddress() + ": unknown host");
      return null;
    }
  }
  
  /**
   * Gets the local address for the listener.
   * @return an {@link InetAddress} representation of the local address.
   * @throws UnknownHostException
   */
  protected InetAddress getInetAddress() throws UnknownHostException {
    if (getAddress() == null) return null;
    return InetAddress.getByName(getAddress());
  }
  
  /**
   * Gets the local port for the listener.
   * @return local port
   */
  public int getPort() {
    return port;
  }

  /**
   * Sets the local port for the listener.
   * @param port the local port to set
   */
  public void setPort(int port) {
    this.port = port;
  }

  /**
   * Gets the listener queue depth.
   * <p>
   * This represents the number of connected clients whose connections 
   * have not yet been accepted.
   * @return queue depth
   * @see java.net.ServerSocket
   */
  public int getBacklog() {
    return backlog;
  }

  /**
   * Sets the listener queue depth.
   * <p>
   * This represents the number of connected clients whose connections 
   * have not yet been accepted.
   * @param backlog the queue depth to set
   * @see java.net.ServerSocket
   */
  public void setBacklog(int backlog) {
    this.backlog = backlog;
  }

  /**
   * Gets the local address for the listener.
   * @return a string representation of the local address
   */
  public String getAddress() {
    return address;
  }

  /**
   * Sets the local address for the listener.
   * @param address a host name or a string representation of an IP address
   */
  public void setAddress(String address) {
    this.address = address;
  }

}
