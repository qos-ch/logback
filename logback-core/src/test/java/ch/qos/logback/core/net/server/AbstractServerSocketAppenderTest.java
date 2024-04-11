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

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ch.qos.logback.core.net.mock.MockContext;
import ch.qos.logback.core.net.server.test.MockServerListener;
import ch.qos.logback.core.net.server.test.MockServerRunner;
import ch.qos.logback.core.net.server.test.ServerSocketUtil;
import ch.qos.logback.core.status.ErrorStatus;
import ch.qos.logback.core.status.Status;

/**
 * Unit tests for {@link AbstractServerSocketAppender}.
 *
 * @author Carl Harris
 */
public class AbstractServerSocketAppenderTest {

    private MockContext context = new MockContext();

    private MockServerRunner<RemoteReceiverClient> runner = new MockServerRunner<RemoteReceiverClient>();

    private MockServerListener<RemoteReceiverClient> listener = new MockServerListener<RemoteReceiverClient>();

    private ServerSocket serverSocket;
    private InstrumentedServerSocketAppenderBase appender;

    @BeforeEach
    public void setUp() throws Exception {
        serverSocket = ServerSocketUtil.createServerSocket();
        appender = new InstrumentedServerSocketAppenderBase(serverSocket, listener, runner);
        appender.setContext(context);
    }

    @AfterEach
    public void tearDown() throws Exception {
        serverSocket.close();
    }

    @Test
    public void testStartStop() throws Exception {
        appender.start();
        Assertions.assertTrue(runner.isContextInjected());
        Assertions.assertTrue(runner.isRunning());
        Assertions.assertSame(listener, appender.getLastListener());

        appender.stop();
        Assertions.assertFalse(runner.isRunning());
    }

    @Test
    public void testStartWhenAlreadyStarted() throws Exception {
        appender.start();
        appender.start();
        Assertions.assertEquals(1, runner.getStartCount());
    }

    @Test
    public void testStopThrowsException() throws Exception {
        appender.start();
        Assertions.assertTrue(appender.isStarted());
        IOException ex = new IOException("test exception");
        runner.setStopException(ex);
        appender.stop();

        Status status = context.getLastStatus();
        Assertions.assertNotNull(status);
        Assertions.assertTrue(status instanceof ErrorStatus);
        Assertions.assertTrue(status.getMessage().contains(ex.getMessage()));
        Assertions.assertSame(ex, status.getThrowable());
    }

    @Test
    public void testStopWhenNotStarted() throws Exception {
        appender.stop();
        Assertions.assertEquals(0, runner.getStartCount());
    }

}
