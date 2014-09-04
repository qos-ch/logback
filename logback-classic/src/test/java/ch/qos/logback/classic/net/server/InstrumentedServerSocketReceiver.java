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
package ch.qos.logback.classic.net.server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.util.concurrent.Executor;

import javax.net.ServerSocketFactory;

import ch.qos.logback.core.net.server.ServerListener;
import ch.qos.logback.core.net.server.ServerRunner;
import ch.qos.logback.core.net.server.ServerSocketListener;


/**
 * A {@link ServerSocketReceiver} with instrumentation for unit testing.
 *
 * @author Carl Harris
 */
public class InstrumentedServerSocketReceiver extends ServerSocketReceiver {
  
  private final ServerSocket serverSocket;
  private final ServerListener listener;
  private final ServerRunner<AppenderFacingClient> runner;
  
  private ServerListener lastListener;
  
  public InstrumentedServerSocketReceiver(ServerSocket serverSocket) {
    this(serverSocket, new ServerSocketListener(serverSocket), null);
  }
  
  public InstrumentedServerSocketReceiver(ServerSocket serverSocket,
      ServerListener listener,
      ServerRunner<AppenderFacingClient> runner) {
    this.serverSocket = serverSocket;
    this.listener = listener;
    this.runner = runner;
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
      ServerListener listener,
      Executor executor) {
    lastListener = listener;
    return runner != null ? runner : super.createServerRunner(listener, executor);
  }

  @Override
  protected ServerListener createServerListener(
      ServerSocket socket) {
    return listener;
  }

  public ServerListener getLastListener() {
    return lastListener;
  }

}