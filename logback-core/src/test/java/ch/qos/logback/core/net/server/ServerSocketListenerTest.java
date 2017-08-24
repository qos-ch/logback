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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import org.junit.Before;
import org.junit.Test;

import ch.qos.logback.core.net.server.Client;
import ch.qos.logback.core.net.server.ServerSocketListener;

/**
 * Unit tests for {@link ServerSocketListener}.
 *
 * @author Carl Harris
 */
public class ServerSocketListenerTest {

    private ServerSocket serverSocket;
    private ServerSocketListener listener;

    @Before
    public void setUp() throws Exception {
        serverSocket = ServerSocketUtil.createServerSocket();
        assertNotNull(serverSocket);
        listener = new InstrumentedServerSocketListener(serverSocket);
    }

    @Test
    public void testAcceptClient() throws Exception {
        RunnableClient localClient = new RunnableClient(InetAddress.getLocalHost(), serverSocket.getLocalPort());
        Thread thread = new Thread(localClient);
        thread.start();
        synchronized (localClient) {
            int retries = 200;
            while (retries-- > 0 && !localClient.isConnected()) {
                localClient.wait(10);
            }
        }
        assertTrue(localClient.isConnected());
        localClient.close();

        serverSocket.setSoTimeout(5000);
        Client client = listener.acceptClient();
        assertNotNull(client);
        client.close();
    }

    private static class InstrumentedServerSocketListener extends ServerSocketListener<RemoteClient> {

        public InstrumentedServerSocketListener(ServerSocket serverSocket) {
            super(serverSocket);
        }

        @Override
        protected RemoteClient createClient(String id, Socket socket) throws IOException {
            return new RemoteClient(socket);
        }

    }

    private static class RemoteClient implements Client {

        private final Socket socket;

        public RemoteClient(Socket socket) {
            this.socket = socket;
        }

        public void run() {
        }

        public void close() {
            try {
                socket.close();
            } catch (IOException ex) {
                ex.printStackTrace(System.err);
            }
        }

    }

    private static class RunnableClient implements Client {

        private final InetAddress inetAddress;
        private final int port;
        private boolean connected;
        private boolean closed;

        public RunnableClient(InetAddress inetAddress, int port) {
            super();
            this.inetAddress = inetAddress;
            this.port = port;
        }

        public synchronized boolean isConnected() {
            return connected;
        }

        public synchronized void setConnected(boolean connected) {
            this.connected = connected;
        }

        public void run() {
            try {
                Socket socket = new Socket(inetAddress, port);
                synchronized (this) {
                    setConnected(true);
                    notifyAll();
                    while (!closed && !Thread.currentThread().isInterrupted()) {
                        try {
                            wait();
                        } catch (InterruptedException ex) {
                            Thread.currentThread().interrupt();
                        }
                    }
                    socket.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace(System.err);
            }
        }

        public synchronized void close() {
            closed = true;
            notifyAll();
        }

    }
}
