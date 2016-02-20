/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2015, QOS.ch. All rights reserved.
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
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.net.ServerSocketFactory;

import ch.qos.logback.classic.net.ReceiverBase;
import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.net.AbstractSocketAppender;
import ch.qos.logback.core.net.server.ServerListener;
import ch.qos.logback.core.net.server.ServerRunner;

/**
 * A logging socket server that is configurable using Joran.
 *
 * @author Carl Harris
 * @author Sebastian Gr&ouml;bler
 */
public class ServerSocketReceiver extends ReceiverBase {
  
  /**
   * Default {@link ServerSocket} backlog
   */
  public static final int DEFAULT_BACKLOG = 50;

  private int port = AbstractSocketAppender.DEFAULT_PORT;
  private int backlog = DEFAULT_BACKLOG;
  
  private String address;

  private ServerRunner runner;

  private int corePoolSize = CoreConstants.CORE_POOL_SIZE;
  private int maxPoolSize = CoreConstants.MAX_POOL_SIZE;
  protected ExecutorService connectionPoolExecutorService;
  
  /**
   * Starts the server.
   */
  protected boolean shouldStart() {
    try {
      ServerSocket serverSocket = getServerSocketFactory().createServerSocket(
          getPort(), getBacklog(), getInetAddress());    

      ServerListener<RemoteAppenderClient> listener = 
          createServerListener(serverSocket);
      
      runner = createServerRunner(listener, getConnectionPoolExecutorService());
      runner.setContext(getContext());
      return true;
    }
    catch (Exception ex) {
      addError("server startup error: " + ex, ex);
      return false;
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
  
  @Override
  protected Runnable getRunnableTask() {
    return runner;
  }

  /**
   * {@inheritDoc}
   */
  protected void onStop() {
    try {
      if (runner == null) return;
      runner.stop();
    }
    catch (IOException ex) {
      addError("server shutdown error: " + ex, ex);
    } finally {
      shutDownExecutorService();
    }
  }

  private synchronized void shutDownExecutorService() {
    connectionPoolExecutorService.shutdownNow();
    connectionPoolExecutorService = null;
  }

  private ExecutorService getConnectionPoolExecutorService() {
    if (connectionPoolExecutorService == null) {
      synchronized (this) {
        if (connectionPoolExecutorService == null) {
          connectionPoolExecutorService = new ThreadPoolExecutor(
                  getCorePoolSize(),
                  getMaxPoolSize(),
                  0L, TimeUnit.MILLISECONDS,
                  new SynchronousQueue<Runnable>());
        }
      }
    }
    return connectionPoolExecutorService;
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

  /**
   * Gets the core pool size for the socket client connection pool.
   * The default value is {@link CoreConstants#CORE_POOL_SIZE}.
   * @return the core pool size
   */
  public int getCorePoolSize() {
    return corePoolSize;
  }

  /**
   * Sets the core number of threads for the socket client connection pool.
   * @param corePoolSize the core pool size
   */
  public void setCorePoolSize(int corePoolSize) {
    this.corePoolSize = corePoolSize;
  }

  /**
   * Gets the maximum pool size for the socket client connection pool.
   * The default value is {@link CoreConstants#MAX_POOL_SIZE}.
   * @return the maximum pool size
   */
  public int getMaxPoolSize() {
    return maxPoolSize;
  }

  /**
   * Sets the maximum allowed number of threads for the socket client connection pool.
   * @param maxPoolSize the maximum pool size
   */
  public void setMaxPoolSize(int maxPoolSize) {
    this.maxPoolSize = maxPoolSize;
  }
}
