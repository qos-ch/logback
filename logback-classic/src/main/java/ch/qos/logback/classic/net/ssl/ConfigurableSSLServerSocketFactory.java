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
package ch.qos.logback.classic.net.ssl;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;

import javax.net.ServerSocketFactory;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;

/**
 * An {@link SSLServerSocketFactory} that configures SSL parameters 
 * (those covered by {@link SSLParameters}) on each newly created socket. 
 * <p>
 * When any of this factory's {@code createServerSocket} methods are invoked, 
 * it calls on a delegate {@link SSLServerSocketFactory} to create the socket, 
 * and then sets the SSL parameters of the socket (using the provided 
 * configuration) before returning the socket to the caller.
 *
 * @author Carl Harris
 */
class ConfigurableSSLServerSocketFactory extends ServerSocketFactory {

  private final SSLParameters parameters;
  private final SSLServerSocketFactory delegate;

  /**
   * Creates a new factory.
   * @param parameters parameters that will be configured on each
   *    socket created by the factory
   * @param delegate socket factory that will be called upon to create
   *    server sockets before configuration
   */
  public ConfigurableSSLServerSocketFactory(SSLParameters parameters,
      SSLServerSocketFactory delegate) {
    this.parameters = parameters;
    this.delegate = delegate;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public ServerSocket createServerSocket(int port, int backlog, InetAddress ifAddress)
      throws IOException {
    SSLServerSocket socket = (SSLServerSocket) delegate.createServerSocket(
        port, backlog, ifAddress);
    configureSocket(socket);
    return socket;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public ServerSocket createServerSocket(int port, int backlog)
      throws IOException {
    SSLServerSocket socket = (SSLServerSocket) delegate.createServerSocket(
        port, backlog);
    configureSocket(socket);
    return socket;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public ServerSocket createServerSocket(int port) throws IOException {
    SSLServerSocket socket = (SSLServerSocket) delegate.createServerSocket(
        port);
    configureSocket(socket);
    return socket;
  }

  /**
   * Configures a server socket using the parameters associated with
   * this factory.
   * @param socket the socket to configure
   */
  private void configureSocket(SSLServerSocket socket) {
    socket.setEnabledCipherSuites(parameters.getCipherSuites());
    socket.setEnabledProtocols(parameters.getProtocols());
    socket.setNeedClientAuth(parameters.getNeedClientAuth());
    socket.setWantClientAuth(parameters.getWantClientAuth());
  }
  
}
