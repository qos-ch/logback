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
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.net.ServerSocketFactory;

import ch.qos.logback.classic.net.server.RemoteAppenderClient;
import ch.qos.logback.classic.net.server.RemoteAppenderServerListener;
import ch.qos.logback.classic.net.server.SocketServer;
import ch.qos.logback.core.net.server.ServerListener;
import ch.qos.logback.core.net.server.ServerRunner;


/**
 * A {@link SocketServer} with instrumentation for unit testing.
 *
 * @author Carl Harris
 */
public class InstrumentedSocketServer extends SocketServer {
  
  private final ServerSocket serverSocket;
  private final ServerListener<RemoteAppenderClient> listener;
  private final ServerRunner<RemoteAppenderClient> runner;
  private final ExecutorService executorService;
  
  private ServerListener lastListener;
  
  public InstrumentedSocketServer(ServerSocket serverSocket) {
    this(serverSocket, new RemoteAppenderServerListener(serverSocket), null, 
        Executors.newCachedThreadPool());
  }
  
  public InstrumentedSocketServer(ServerSocket serverSocket,
      ServerListener<RemoteAppenderClient> listener, 
      ServerRunner<RemoteAppenderClient> runner, ExecutorService executorService) {
    this.serverSocket = serverSocket;
    this.listener = listener;
    this.runner = runner;
    this.executorService = executorService;
  }

  @Override
  protected ServerSocketFactory getServerSocketFactory() throws Exception {
    return new ServerSocketFactory() {

      @Override
      public ServerSocket createServerSocket(int port) throws IOException {
        return serverSocket;
      }

      @Override
      public ServerSocket createServerSocket(int port, int backlog)
          throws IOException {
        return serverSocket;
      }

      @Override
      public ServerSocket createServerSocket(int port, int backlog,
          InetAddress ifAddress) throws IOException {
        return serverSocket;
      }        
    };
  }

  @Override
  protected ServerRunner createServerRunner(
      ServerListener<RemoteAppenderClient> listener,
      Executor executor) {
    lastListener = listener;
    return runner != null ? runner : super.createServerRunner(listener, executor);
  }

  @Override
  protected ServerListener<RemoteAppenderClient> createServerListener(
      ServerSocket socket) {
    return listener;
  }

  @Override
  protected ExecutorService createExecutorService() {
    return executorService;
  }

  public ServerListener getLastListener() {
    return lastListener;
  }

}