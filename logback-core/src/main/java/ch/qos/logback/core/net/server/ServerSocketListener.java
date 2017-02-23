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
package ch.qos.logback.core.net.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;

import ch.qos.logback.core.util.CloseUtil;

/**
 * A {@link ServerListener} that accepts connections on a {@link ServerSocket}.
 *
 * @author Carl Harris
 */
public abstract class ServerSocketListener<T extends Client> implements ServerListener<T> {

    private final ServerSocket serverSocket;

    /**
     * Constructs a new listener.
     * @param serverSocket server socket delegate
     */
    public ServerSocketListener(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
    }

    /**
     * {@inheritDoc}
     */
    public T acceptClient() throws IOException {
        Socket socket = serverSocket.accept();
        return createClient(socketAddressToString(socket.getRemoteSocketAddress()), socket);
    }

    /**
     * Creates the client object for a new socket connection
     * @param id identifier string for the client
     * @param socket client's socket connection
     * @return client object
     * @throws IOException
     */
    protected abstract T createClient(String id, Socket socket) throws IOException;

    /**
     * {@inheritDoc}
     */
    public void close() {
        CloseUtil.closeQuietly(serverSocket);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return socketAddressToString(serverSocket.getLocalSocketAddress());
    }

    /**
     * Converts a socket address to a reasonable display string.
     * @param address the subject socket address
     * @return display string
     */
    private String socketAddressToString(SocketAddress address) {
        String addr = address.toString();
        int i = addr.indexOf("/");
        if (i >= 0) {
            addr = addr.substring(i + 1);
        }
        return addr;
    }

}
