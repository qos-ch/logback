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
import java.net.ServerSocket;
import java.net.Socket;

import ch.qos.logback.core.net.server.ServerListener;
import ch.qos.logback.core.net.server.ServerSocketListener;

/**
 * A {@link ServerListener} for remote appenders.
 *
 * @author Carl Harris
 */
class RemoteAppenderServerListener extends ServerSocketListener<RemoteAppenderClient> {

    /**
     * Constructs a new listener.
     * @param serverSocket the {@link ServerSocket} from which to accept
     *    new client connections
     */
    public RemoteAppenderServerListener(ServerSocket serverSocket) {
        super(serverSocket);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected RemoteAppenderClient createClient(String id, Socket socket) throws IOException {
        return new RemoteAppenderStreamClient(id, socket);
    }

}
