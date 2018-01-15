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
package ch.qos.logback.core.net.ssl;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;

import javax.net.ServerSocketFactory;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;

/**
 * An {@link SSLServerSocketFactory} that configures SSL parameters 
 * (those specified in {@link SSLParametersConfiguration} on each newly 
 * created socket. 
 * <p>
 * When any of this factory's {@code createServerSocket} methods are invoked, 
 * it calls on a delegate {@link SSLServerSocketFactory} to create the socket, 
 * and then sets the SSL parameters of the socket (using the provided 
 * configuration) before returning the socket to the caller.
 *
 * @author Carl Harris
 */
public class ConfigurableSSLServerSocketFactory extends ServerSocketFactory {

    private final SSLParametersConfiguration parameters;
    private final SSLServerSocketFactory delegate;

    /**
     * Creates a new factory.
     * @param parameters parameters that will be configured on each
     *    socket created by the factory
     * @param delegate socket factory that will be called upon to create
     *    server sockets before configuration
     */
    public ConfigurableSSLServerSocketFactory(SSLParametersConfiguration parameters, SSLServerSocketFactory delegate) {
        this.parameters = parameters;
        this.delegate = delegate;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ServerSocket createServerSocket(int port, int backlog, InetAddress ifAddress) throws IOException {
        SSLServerSocket socket = (SSLServerSocket) delegate.createServerSocket(port, backlog, ifAddress);
        parameters.configure(new SSLConfigurableServerSocket(socket));
        return socket;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ServerSocket createServerSocket(int port, int backlog) throws IOException {
        SSLServerSocket socket = (SSLServerSocket) delegate.createServerSocket(port, backlog);
        parameters.configure(new SSLConfigurableServerSocket(socket));
        return socket;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ServerSocket createServerSocket(int port) throws IOException {
        SSLServerSocket socket = (SSLServerSocket) delegate.createServerSocket(port);
        parameters.configure(new SSLConfigurableServerSocket(socket));
        return socket;
    }

}
