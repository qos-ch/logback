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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.ServerSocket;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ch.qos.logback.core.net.mock.MockContext;
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

    @Before
    public void setUp() throws Exception {
        serverSocket = ServerSocketUtil.createServerSocket();
        appender = new InstrumentedServerSocketAppenderBase(serverSocket, listener, runner);
        appender.setContext(context);
    }

    @After
    public void tearDown() throws Exception {
        serverSocket.close();
    }

    @Test
    public void testStartStop() throws Exception {
        appender.start();
        assertTrue(runner.isContextInjected());
        assertTrue(runner.isRunning());
        assertSame(listener, appender.getLastListener());

        appender.stop();
        assertFalse(runner.isRunning());
    }

    @Test
    public void testStartWhenAlreadyStarted() throws Exception {
        appender.start();
        appender.start();
        assertEquals(1, runner.getStartCount());
    }

    @Test
    public void testStopThrowsException() throws Exception {
        appender.start();
        assertTrue(appender.isStarted());
        IOException ex = new IOException("test exception");
        runner.setStopException(ex);
        appender.stop();

        Status status = context.getLastStatus();
        assertNotNull(status);
        assertTrue(status instanceof ErrorStatus);
        assertTrue(status.getMessage().contains(ex.getMessage()));
        assertSame(ex, status.getThrowable());
    }

    @Test
    public void testStopWhenNotStarted() throws Exception {
        appender.stop();
        assertEquals(0, runner.getStartCount());
    }

}
