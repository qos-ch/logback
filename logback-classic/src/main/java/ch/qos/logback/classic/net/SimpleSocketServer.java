/*
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2026, QOS.ch. All rights reserved.
 *
 * This program and the accompanying materials are dual-licensed under
 * either the terms of the Eclipse Public License v2.0 as published by
 * the Eclipse Foundation
 *
 *   or (per the licensee's choosing)
 *
 * under the terms of the GNU Lesser General Public License version 2.1
 * as published by the Free Software Foundation.
 */
package ch.qos.logback.classic.net;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import javax.net.ServerSocketFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.util.IpAddressMatcher;

/**
 * A simple {@link SocketNode} based server.
 * 
 * <pre>
 *      &lt;b&gt;Usage:&lt;/b&gt; java ch.qos.logback.classic.net.SimpleSocketServer port configFile
 * </pre>
 * 
 * where <em>port</em> is a port number where the server listens and
 * <em>configFile</em> is an XML configuration file fed to
 * {@link JoranConfigurator}.
 * 
 * <p>
 * Client access can be restricted by registering allowed addresses with
 * {@link #addAllowedClientAddress(String)}. Supported forms are single IPs
 * (e.g. {@code 192.168.1.10}) and CIDR network ranges (e.g.
 * {@code 192.168.1.0/24}). If no allowed addresses are configured, every client
 * is accepted (backward compatible default).
 * </p>
 * 
 * @author Ceki G&uuml;lc&uuml;
 * @author S&eacute;bastien Pennec
 * 
 * @since 0.8.4
 */
public class SimpleSocketServer extends Thread {

    Logger logger = LoggerFactory.getLogger(SimpleSocketServer.class);

    private final int port;
    private final LoggerContext lc;
    private boolean closed = false;
    private ServerSocket serverSocket;
    private List<SocketNode> socketNodeList = new ArrayList<SocketNode>();

    /**
     * When non-empty, only clients whose remote address matches one of these
     * matchers are accepted. Empty means all clients are allowed.
     */
    private final List<IpAddressMatcher> allowedClientAddresses = new ArrayList<IpAddressMatcher>();

    // used for testing purposes
    private CountDownLatch latch;

    public static void main(String argv[]) throws Exception {
        doMain(SimpleSocketServer.class, argv);
    }

    protected static void doMain(Class<? extends SimpleSocketServer> serverClass, String argv[]) throws Exception {
        int port = -1;
        if (argv.length == 2) {
            port = parsePortNumber(argv[0]);
        } else {
            usage("Wrong number of arguments.");
        }

        String configFile = argv[1];
        LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
        configureLC(lc, configFile);

        SimpleSocketServer sss = new SimpleSocketServer(lc, port);

        // start the server in a separate thread
        sss.start();
    }

    public SimpleSocketServer(LoggerContext lc, int port) {
        this.lc = lc;
        this.port = port;
    }

    /**
     * Authorize a single client IP address or a CIDR network range.
     * <p>
     * If at least one allowed address is registered, only matching clients are
     * accepted; others are closed immediately after {@code accept()}. When no
     * allowed addresses are registered, all clients are accepted.
     * </p>
     *
     * @param addressOrCidr a single IP (e.g. {@code 10.0.0.5}) or CIDR range
     *                      (e.g. {@code 192.168.1.0/24})
     * @throws IllegalArgumentException if the specification is invalid
     * @since 1.6.2
     */
    public void addAllowedClientAddress(String addressOrCidr) {
        allowedClientAddresses.add(new IpAddressMatcher(addressOrCidr));
    }

    /**
     * Replace the set of authorized client addresses with the given collection.
     * Each entry must be a single IP or CIDR range. Passing an empty collection
     * (or {@code null}) clears the restriction so that all clients are allowed.
     *
     * @param addresses allowed addresses / CIDR ranges, or {@code null}
     * @throws IllegalArgumentException if any specification is invalid
     * @since 1.6.2
     */
    public void setAllowedClientAddresses(Collection<String> addresses) {
        allowedClientAddresses.clear();
        if (addresses == null) {
            return;
        }
        for (String addressOrCidr : addresses) {
            addAllowedClientAddress(addressOrCidr);
        }
    }

    /**
     * Returns {@code true} if the client is allowed to connect.
     * <p>
     * When no allowed addresses are configured, every client is allowed.
     * </p>
     *
     * @param clientAddress the remote address of the connecting client
     * @return {@code true} if the connection should be accepted
     * @since 1.6.2
     */
    protected boolean isClientAllowed(InetAddress clientAddress) {
        if (allowedClientAddresses.isEmpty()) {
            return true;
        }
        if (clientAddress == null) {
            return false;
        }
        for (IpAddressMatcher matcher : allowedClientAddresses) {
            if (matcher.matches(clientAddress)) {
                return true;
            }
        }
        return false;
    }

    public void run() {

        final String oldThreadName = Thread.currentThread().getName();

        try {

            final String newThreadName = getServerThreadName();
            Thread.currentThread().setName(newThreadName);

            logger.info("Listening on port " + port);
            if (!allowedClientAddresses.isEmpty()) {
                logger.info("Client IP restrictions are in effect ({} allowed address pattern(s))",
                        allowedClientAddresses.size());
            }
            serverSocket = getServerSocketFactory().createServerSocket(port);
            while (!closed) {
                logger.info("Waiting to accept a new client.");
                signalAlmostReadiness();
                Socket socket = serverSocket.accept();
                InetAddress clientAddress = socket.getInetAddress();
                logger.info("Connected to client at " + clientAddress);
                if (!isClientAllowed(clientAddress)) {
                    logger.warn("Denying connection from unauthorized client " + clientAddress);
                    closeSocketQuietly(socket);
                    continue;
                }
                logger.info("Starting new socket node.");
                SocketNode newSocketNode = new SocketNode(this, socket, lc);
                synchronized (socketNodeList) {
                    socketNodeList.add(newSocketNode);
                }
                final String clientThreadName = getClientThreadName(socket);
                new Thread(newSocketNode, clientThreadName).start();
            }
        } catch (Exception e) {
            if (closed) {
                logger.info("Exception in run method for a closed server. This is normal.");
            } else {
                logger.error("Unexpected failure in run method", e);
            }
        }

        finally {
            Thread.currentThread().setName(oldThreadName);
        }
    }

    private void closeSocketQuietly(Socket socket) {
        try {
            socket.close();
        } catch (IOException e) {
            logger.debug("Failed to close unauthorized client socket", e);
        }
    }

    /**
     * Returns the name given to the server thread.
     */
    protected String getServerThreadName() {
        return String.format("Logback %s (port %d)", getClass().getSimpleName(), port);
    }

    /**
     * Returns a name to identify each client thread.
     */
    protected String getClientThreadName(Socket socket) {
        return String.format("Logback SocketNode (client: %s)", socket.getRemoteSocketAddress());
    }

    /**
     * Gets the platform default {@link ServerSocketFactory}.
     * <p>
     * Subclasses may override to provide a custom server socket factory.
     */
    protected ServerSocketFactory getServerSocketFactory() {
        return ServerSocketFactory.getDefault();
    }

    /**
     * Signal another thread that we have established a connection This is useful
     * for testing purposes.
     */
    void signalAlmostReadiness() {
        if (latch != null && latch.getCount() != 0) {
            // System.out.println("signalAlmostReadiness() with latch "+latch);
            latch.countDown();
        }
    }

    /**
     * Used for testing purposes
     * 
     * @param latch
     */
    void setLatch(CountDownLatch latch) {
        this.latch = latch;
    }

    /**
     * Used for testing purposes
     */
    public CountDownLatch getLatch() {
        return latch;
    }

    public boolean isClosed() {
        return closed;
    }

    public void close() {
        closed = true;
        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                logger.error("Failed to close serverSocket", e);
            } finally {
                serverSocket = null;
            }
        }

        logger.info("closing this server");
        synchronized (socketNodeList) {
            for (SocketNode sn : socketNodeList) {
                sn.close();
            }
        }
        if (socketNodeList.size() != 0) {
            logger.warn("Was expecting a 0-sized socketNodeList after server shutdown");
        }

    }

    public void socketNodeClosing(SocketNode sn) {
        logger.debug("Removing {}", sn);

        // don't allow simultaneous access to the socketNodeList
        // (e.g. removal whole iterating on the list causes
        // java.util.ConcurrentModificationException)
        synchronized (socketNodeList) {
            socketNodeList.remove(sn);
        }
    }

    static void usage(String msg) {
        System.err.println(msg);
        System.err.println("Usage: java " + SimpleSocketServer.class.getName() + " port configFile");
        System.exit(1);
    }

    static int parsePortNumber(String portStr) {
        try {
            return Integer.parseInt(portStr);
        } catch (java.lang.NumberFormatException e) {
            e.printStackTrace();
            usage("Could not interpret port number [" + portStr + "].");
            // we won't get here
            return -1;
        }
    }

    static public void configureLC(LoggerContext lc, String configFile) throws JoranException {
        JoranConfigurator configurator = new JoranConfigurator();
        lc.reset();
        configurator.setContext(lc);
        configurator.doConfigure(configFile);
    }
}
