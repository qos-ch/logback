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

import javax.net.ServerSocketFactory;

import ch.qos.logback.classic.net.ReceiverBase;
import ch.qos.logback.core.net.AbstractSocketAppender;
import ch.qos.logback.core.net.server.ServerListener;
import ch.qos.logback.core.net.server.ServerRunner;
import ch.qos.logback.core.util.CloseUtil;

/**
 * A logging socket server that is configurable using Joran.
 *
 * @author Carl Harris
 */
public class ServerSocketReceiver extends ReceiverBase {

    /**
     * Default {@link ServerSocket} backlog
     */
    public static final int DEFAULT_BACKLOG = 50;

    private int port = AbstractSocketAppender.DEFAULT_PORT;
    private int backlog = DEFAULT_BACKLOG;

    private String address;

    private ServerSocket serverSocket;
    @SuppressWarnings("rawtypes")
    private ServerRunner runner;

    /**
     * Starts the server.
     */
    protected boolean shouldStart() {
        try {
            ServerSocket serverSocket = getServerSocketFactory().createServerSocket(getPort(), getBacklog(), getInetAddress());

            ServerListener<RemoteAppenderClient> listener = createServerListener(serverSocket);

            runner = createServerRunner(listener, getContext().getScheduledExecutorService());
            runner.setContext(getContext());
            return true;
        } catch (Exception ex) {
            addError("server startup error: " + ex, ex);
            CloseUtil.closeQuietly(serverSocket);
            return false;
        }
    }

    protected ServerListener<RemoteAppenderClient> createServerListener(ServerSocket socket) {
        return new RemoteAppenderServerListener(socket);
    }

    @SuppressWarnings("rawtypes")
    protected ServerRunner createServerRunner(ServerListener<RemoteAppenderClient> listener, Executor executor) {
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
            if (runner == null)
                return;
            runner.stop();
        } catch (IOException ex) {
            addError("server shutdown error: " + ex, ex);
        }
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
        if (getAddress() == null)
            return null;
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
