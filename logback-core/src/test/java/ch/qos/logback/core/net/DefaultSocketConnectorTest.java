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

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;

import ch.qos.logback.core.net.SocketConnector.ExceptionHandler;
import ch.qos.logback.core.net.server.test.ServerSocketUtil;
import ch.qos.logback.core.testUtil.EnvUtilForTests;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

/**
 * Unit tests for {@link DefaultSocketConnector}.
 *
 * @author Carl Harris
 */
public class DefaultSocketConnectorTest {

    private static final int DELAY = 2000;
    private static final int SHORT_DELAY = 10;
    private static final int RETRY_DELAY = 10;

    private MockExceptionHandler exceptionHandler = new MockExceptionHandler();

    private ServerSocket serverSocket;
    private DefaultSocketConnector connector;

    ExecutorService executor = Executors.newSingleThreadExecutor();

    @BeforeEach
    public void setUp() throws Exception {
        if(EnvUtilForTests.isGithubAction())
            return;
        
        serverSocket = ServerSocketUtil.createServerSocket();
        connector = new DefaultSocketConnector(serverSocket.getInetAddress(), serverSocket.getLocalPort(), 0,
                RETRY_DELAY);
        connector.setExceptionHandler(exceptionHandler);
    }

    @AfterEach
    public void tearDown() throws Exception {
        if(EnvUtilForTests.isGithubAction())
            return;
        
        if (serverSocket != null) {
            serverSocket.close();
        }
    }

    @Test
    public void testConnect() throws Exception {
        if(EnvUtilForTests.isGithubAction())
            return;
        
        Future<Socket> connectorTask = executor.submit(connector);

        Socket socket = connectorTask.get(DELAY, TimeUnit.MILLISECONDS);
        Assertions.assertNotNull(socket);
        connectorTask.cancel(true);

        Assertions.assertTrue(connectorTask.isDone());
        socket.close();
    }

    @Test
    public void testConnectionFails() throws Exception {
        if(EnvUtilForTests.isGithubAction())
            return;
        
        serverSocket.close();
        Future<Socket> connectorTask = executor.submit(connector);

        // this connection attempt will always time out
        try {
            connectorTask.get(SHORT_DELAY, TimeUnit.MILLISECONDS);
            Assertions.fail();
        } catch (TimeoutException e) {
        }
        Exception lastException = exceptionHandler.awaitConnectionFailed();
        Assertions.assertTrue(lastException instanceof ConnectException);
        Assertions.assertFalse(connectorTask.isDone());
        connectorTask.cancel(true);

        // thread.join(4 * DELAY);
        Assertions.assertTrue(connectorTask.isCancelled());
    }

    @Disabled
    @Test
    @Timeout(value=5, unit = TimeUnit.SECONDS)
    public void testConnectEventually() throws Exception {
        if(EnvUtilForTests.isGithubAction())
            return;
        
        serverSocket.close();

        Future<Socket> connectorTask = executor.submit(connector);
        // this connection attempt will always time out
        try {
            connectorTask.get(SHORT_DELAY, TimeUnit.MILLISECONDS);
            Assertions.fail();
        } catch (TimeoutException e) {
        }

        // the following call requires over 1000 millis
        Exception lastException = exceptionHandler.awaitConnectionFailed();
        Assertions.assertNotNull(lastException);
        Assertions.assertTrue(lastException instanceof ConnectException);

        // now rebind to the same local address
        SocketAddress address = serverSocket.getLocalSocketAddress();
        serverSocket = new ServerSocket();
        serverSocket.setReuseAddress(true);
        serverSocket.bind(address);

        // now we should be able to connect
        Socket socket = connectorTask.get(2 * DELAY, TimeUnit.MILLISECONDS);

        Assertions.assertNotNull(socket);

        Assertions.assertFalse(connectorTask.isCancelled());
        socket.close();
    }

    private static class MockExceptionHandler implements ExceptionHandler {

        private final CyclicBarrier failureBarrier = new CyclicBarrier(2);

        private Exception lastException;

        public void connectionFailed(SocketConnector connector, Exception ex) {
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
