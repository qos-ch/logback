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

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;

import javax.net.ServerSocketFactory;

import ch.qos.logback.core.net.SocketAppenderBase;
import ch.qos.logback.core.net.server.ServerListener;
import ch.qos.logback.core.net.server.ServerRunner;
import ch.qos.logback.core.net.server.ThreadPoolFactoryBean;
import ch.qos.logback.core.spi.ContextAwareBase;
import ch.qos.logback.core.spi.LifeCycle;

/**
 * A logging socket server that is configurable using Joran.
 *
 * @author Carl Harris
 */
public class SocketServer extends ContextAwareBase implements LifeCycle {
  
  /**
   * Default {@link ServerSocket} backlog
   */
  public static final int DEFAULT_BACKLOG = 50;

  private int port = SocketAppenderBase.DEFAULT_PORT;
  private int backlog = DEFAULT_BACKLOG;
  
  private String address;
  private ThreadPoolFactoryBean threadPool;

  private ServerRunner runner;
  private ExecutorService executor;
  
  /**
   * Starts the server.
   */
  public final void start() {
    if (isStarted()) return;
    try {
      ServerSocket socket = getServerSocketFactory().createServerSocket(
          getPort(), getBacklog(), getInetAddress());    
      ServerListener<RemoteAppenderClient> listener = createServerListener(socket);
      executor = getThreadPool().createExecutor();
      runner = createServerRunner(listener, executor);
      runner.setContext(getContext());
      runner.start();
    }
    catch (Exception ex) {
      addError("server startup error: " + ex, ex);
    }
  }

  protected ServerListener<RemoteAppenderClient> createServerListener(
      ServerSocket socket) {
    return new RemoteAppenderServerListener(socket);
  }
  
  protected ServerRunner createServerRunner(
      ServerListener<RemoteAppenderClient> listener,
      Executor executor) {
    return new RemoteAppenderServerRunner(listener, executor);
  }
  
  /**
   * Stops the server.
   */
  public final void stop() {
    if (!isStarted()) return;
    try {
      runner.stop();
      executor.shutdownNow();
    }
    catch (IOException ex) {
      addError("server shutdown error: " + ex, ex);
    }
  }

  /**
   * Gets a flag indicating whether the server is running.
   * @return flag state
   */
  public final boolean isStarted() {
    return runner != null && runner.isStarted();
  }

  /**
   * Gets the server socket factory.
   * <p>
   * Subclasses may override to provide a custom factory.
   * @return server socket factory
   * @throws Exception
   */
  protected ServerSocketFactory getServerSocketFactory() throws Exception {
    return ServerSocketFactory.getDefault();
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
  public Integer getBacklog() {
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
  public void setBacklog(Integer backlog) {
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

  /**
   * Gets the server's thread pool configuration.
   * @return thread pool configuration; if no thread pool configuration was
   *    provided, a default configuration is returned
   */
  public ThreadPoolFactoryBean getThreadPool() {
    if (threadPool == null) {
      return new ThreadPoolFactoryBean();
    }
    return threadPool;
  }

  /**
   * Sets the server's thread pool configuration.
   * @param threadPool the configuration to set
   */
  public void setThreadPool(ThreadPoolFactoryBean threadPool) {
    this.threadPool = threadPool;
  }

}
