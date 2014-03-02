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

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import ch.qos.logback.classic.net.SocketAppender;
import ch.qos.logback.core.Context;
import ch.qos.logback.core.net.AbstractSocketAppender;
import org.junit.After;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.LoggerFactory;

/**
 * Implicitly tests configurable max pool size of {@link ServerSocketReceiver} and
 * that each additional client connection is being dropped.
 *
 * @author Sebastian Gr&ouml;bler
 */
public class ServerSocketReceiverExecutorServiceIntegrationTest {

    /**
     * Default host for this integration test.
     */
    private static final String DEFAULT_HOST = "localhost";

    /**
     * Default port for this integration test.
     */
    private static final String DEFAULT_PORT = String.valueOf(AbstractSocketAppender.DEFAULT_PORT);

    /**
     * Configurable host for this integration test.
     */
    private static final String HOST = System.getProperty("integration.test.host", DEFAULT_HOST);

    /**
     * Configurable port for this integration test.
     */
    private static final int PORT = Integer.parseInt(System.getProperty("integration.test.port", DEFAULT_PORT));

    /**
     * The timeout for this test in case the expectations are not met.
     */
    private static final int TEST_TIMEOUT = 2;

    /**
     * The unit of the timeout for this test
     */
    private static final TimeUnit TEST_TIMEOUT_UNIT = TimeUnit.SECONDS;

    /**
     * The maximum pool size to test.
     */
    private final static int MAX_POOL_SIZE = 2;

    /**
     * The number of clients which can not connect because of pool size limitation.
     */
    private final static int TOO_MANY_CLIENTS = 2;

    /**
     * The total of all clients trying to establish a connection.
     */
    private final static int CLIENT_COUNT = MAX_POOL_SIZE + TOO_MANY_CLIENTS;

    private final Context context = (Context) LoggerFactory.getILoggerFactory();
    private final CountDownLatch dropCounter = new CountDownLatch(TOO_MANY_CLIENTS);
    private final ServerSocketReceiver serverSocketReceiver = new ConnectionDropCountingServerSocketReceiver(dropCounter);

    @Before
    public void beforeTest() {
        serverSocketReceiver.setContext(context);
        serverSocketReceiver.setAddress(HOST);
        serverSocketReceiver.setPort(PORT);
        serverSocketReceiver.setMaxPoolSize(MAX_POOL_SIZE);
        serverSocketReceiver.start();
    }

    @After
    public void afterTest() {
        serverSocketReceiver.stop();
    }

    @Test
    public void dropsClientsWhenMaxPoolSizeIsReached() throws InterruptedException {
        // when
        for (int i = 0; i < CLIENT_COUNT; i++) {
            final SocketAppender socketAppender = new SocketAppender();
            socketAppender.setContext(context);
            socketAppender.setRemoteHost(HOST);
            socketAppender.setPort(PORT);
            socketAppender.start();
        }

        // then
        final boolean allTooManyClientsGotDroppedBeforeTimeout = dropCounter.await(TEST_TIMEOUT, TEST_TIMEOUT_UNIT);
        assertTrue(allTooManyClientsGotDroppedBeforeTimeout);
    }
}
