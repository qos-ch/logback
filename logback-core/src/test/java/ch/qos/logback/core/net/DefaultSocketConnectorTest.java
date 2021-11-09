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
package ch.qos.logback.core.net;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.net.ConnectException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import ch.qos.logback.core.net.SocketConnector.ExceptionHandler;
import ch.qos.logback.core.net.server.test.ServerSocketUtil;

/**
 * Unit tests for {@link DefaultSocketConnector}.
 *
 * @author Carl Harris
 */
@Ignore
public class DefaultSocketConnectorTest {

    private static final int DELAY = 1000;
    private static final int SHORT_DELAY = 10;
    private static final int RETRY_DELAY = 10;

    private final MockExceptionHandler exceptionHandler = new MockExceptionHandler();

    private ServerSocket serverSocket;
    private DefaultSocketConnector connector;

    ExecutorService executor = Executors.newSingleThreadExecutor();

    @Before
    public void setUp() throws Exception {
        serverSocket = ServerSocketUtil.createServerSocket();
        connector = new DefaultSocketConnector(serverSocket.getInetAddress(), serverSocket.getLocalPort(), 0,
                        RETRY_DELAY);
        connector.setExceptionHandler(exceptionHandler);
    }

    @After
    public void tearDown() throws Exception {
        if (serverSocket != null) {
            serverSocket.close();
        }
    }

    @Test
    public void testConnect() throws Exception {
        final Future<Socket> connectorTask = executor.submit(connector);

        final Socket socket = connectorTask.get(DELAY, TimeUnit.MILLISECONDS);
        assertNotNull(socket);
        connectorTask.cancel(true);

        assertTrue(connectorTask.isDone());
        socket.close();
    }

    @Test
    public void testConnectionFails() throws Exception {
        serverSocket.close();
        final Future<Socket> connectorTask = executor.submit(connector);

        // this connection attempt will always timeout
        try {
            connectorTask.get(SHORT_DELAY, TimeUnit.MILLISECONDS);
            fail();
        } catch (final TimeoutException e) {
        }
        final Exception lastException = exceptionHandler.awaitConnectionFailed();
        assertTrue(lastException instanceof ConnectException);
        assertFalse(connectorTask.isDone());
        connectorTask.cancel(true);

        // thread.join(4 * DELAY);
        assertTrue(connectorTask.isCancelled());
    }

    @Test(timeout = 5000)
    public void testConnectEventually() throws Exception {
        serverSocket.close();

        final Future<Socket> connectorTask = executor.submit(connector);
        // this connection attempt will always timeout
        try {
            connectorTask.get(SHORT_DELAY, TimeUnit.MILLISECONDS);
            fail();
        } catch (final TimeoutException e) {
        }

        // the following call requries over 1000 millis
        final Exception lastException = exceptionHandler.awaitConnectionFailed();
        assertNotNull(lastException);
        assertTrue(lastException instanceof ConnectException);

        // now rebind to the same local address
        final SocketAddress address = serverSocket.getLocalSocketAddress();
        serverSocket = new ServerSocket();
        serverSocket.setReuseAddress(true);
        serverSocket.bind(address);

        // now we should be able to connect
        final Socket socket = connectorTask.get(2 * DELAY, TimeUnit.MILLISECONDS);

        assertNotNull(socket);

        assertFalse(connectorTask.isCancelled());
        socket.close();
    }

    private static class MockExceptionHandler implements ExceptionHandler {

        private final CyclicBarrier failureBarrier = new CyclicBarrier(2);

        private Exception lastException;

        @Override
        public void connectionFailed(final SocketConnector connector, final Exception ex) {
            lastException = ex;
            try {
                failureBarrier.await();
            } catch (InterruptedException | BrokenBarrierException e) {
                e.printStackTrace();
            }
        }

        public Exception awaitConnectionFailed() throws InterruptedException {
            if (lastException != null) {
                return lastException;
            }

            try {
                failureBarrier.await();
            } catch (InterruptedException | BrokenBarrierException e) {
                e.printStackTrace();
            }
            return lastException;
        }

    }

}
