/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2014, QOS.ch. All rights reserved.
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
import java.util.concurrent.CountDownLatch;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.net.server.ServerListener;

/**
 * Extension of {@link ServerSocketReceiver} which allows to observe how many client connections have been closed.
 *
 * @author Sebastian Gr&ouml;bler
 */
public class ConnectionDropCountingServerSocketReceiver extends ServerSocketReceiver {

    protected final CountDownLatch closeCounter;

    public ConnectionDropCountingServerSocketReceiver(final CountDownLatch closeCounter) {
        this.closeCounter = closeCounter;
    }

    @Override
    protected ServerListener<RemoteAppenderClient> createServerListener(final ServerSocket socket) {
        return new DropCountingListener(super.createServerListener(socket));
    }

    private class DropCountingListener implements ServerListener<RemoteAppenderClient> {

        private final ServerListener<RemoteAppenderClient> decoratedListener;

        private DropCountingListener(final ServerListener<RemoteAppenderClient> decoratedListener) {
            this.decoratedListener = decoratedListener;
        }

        public RemoteAppenderClient acceptClient() throws IOException, InterruptedException {
            return new DropCountingClient(decoratedListener.acceptClient());
        }

        public void close() {
            decoratedListener.close();
        }
    }

    private class DropCountingClient implements RemoteAppenderClient {

        private final RemoteAppenderClient decoratedClient;

        private DropCountingClient(final RemoteAppenderClient decoratedClient) {
            this.decoratedClient = decoratedClient;
        }

        public void setLoggerContext(final LoggerContext lc) {
            decoratedClient.setLoggerContext(lc);
        }

        public void close() {
            closeCounter.countDown();
            decoratedClient.close();
        }

        public void run() {
            decoratedClient.run();
        }
    }
}
