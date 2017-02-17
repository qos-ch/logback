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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.ContextBase;
import ch.qos.logback.core.net.mock.MockSocketAppender;
import ch.qos.logback.core.net.mock.MockSocketConnector;
import ch.qos.logback.core.net.server.ServerSocketUtil;
import ch.qos.logback.core.read.ListAppender;
import ch.qos.logback.core.spi.PreSerializationTransformer;
import ch.qos.logback.core.status.Status;
import ch.qos.logback.core.status.StatusChecker;
import ch.qos.logback.core.testUtil.RandomUtil;
import ch.qos.logback.core.util.Duration;

/**
 * Unit tests for {@link AbstractSocketAppender}.
 *
 * @author Carl Harris
 * @author Sebastian Gr&ouml;bler
 * @author Ceki G&uuml;lc&uuml;
 */
public class AbstractSocketAppenderTest2 {

    /**
     * Timeout used for all blocking operations in multi-threading contexts.
     */
    private static final int TIMEOUT = 1000;

    Context context = new ContextBase();
    StatusChecker statusChecker = new StatusChecker(context);
    // private ScheduledExecutorService executorService = spy((ScheduledExecutorService)
    // ExecutorServiceUtil.newScheduledExecutorService());
    // private MockContext mockContext = new MockContext(executorService);
    PreSerializationTransformer<String> preSerializationTransformer = new StringPreSerializationTransformer();
    ServerSocket serverSocket;
    
    Socket socket;
    SocketConnector socketConnector;
    // private AutoFlushingObjectWriter objectWriter = mock(AutoFlushingObjectWriter.class);
    // private LinkedBlockingDeque<String> deque = spy(new LinkedBlockingDeque<String>(1));
    // private InstrumentedSocketAppender appender = spy(new InstrumentedSocketAppender(preSerializationTransformer,
    // queueFactory, objectWriterFactory,
    // socketConnector));

    MockSocketAppender appender;
    static final String LOCALHOST_STR = "localhost";
    
    
    @Before
    public void setupValidAppenderWithMockDependencies() throws Exception {
        serverSocket = ServerSocketUtil.createServerSocket();
        socket =  new Socket(serverSocket.getInetAddress(), serverSocket.getLocalPort());
        //socketConnector = new MockSocketConnector(socket);
        appender = new MockSocketAppender(preSerializationTransformer, socketConnector);
        appender.setContext(context);
        appender.setRemoteHost(LOCALHOST_STR);
    }

    @After
    public void tearDown() throws Exception {
        appender.stop();
        assertFalse(appender.isStarted());
    }

    @Test
    public void failsToStartWithoutValidPort() throws Exception {

        // given
        appender.setPort(-1);

        // when
        appender.start();

        // then
        assertFalse(appender.isStarted());
    }

    @Test
    public void failsToStartWithoutValidRemoteHost() throws Exception {

        // given
        appender.setRemoteHost(null);

        // when
        appender.start();

        // then
        assertFalse(appender.isStarted());
        statusChecker.containsMatch(Status.ERROR, "remote host");
    }

    @Test
    public void failsToStartWithNegativeQueueSize() throws Exception {

        // given
        appender.setQueueSize(-1);

        // when
        appender.start();

        // then
        assertFalse(appender.isStarted());
        statusChecker.containsMatch(Status.ERROR, "Queue size must be greater than zero");
    }

    @Test
    public void failsToStartWithUnresolvableRemoteHost() throws Exception {

        // given
        appender.setRemoteHost("NOT.A.VALID.REMOTE.HOST.NAME");

        // when
        appender.start();

        // then
        assertFalse(appender.isStarted());
        statusChecker.containsMatch(Status.ERROR, "unknown host");
    }

    @Test
    public void startsButOutputsWarningWhenQueueSizeIsZero() throws Exception {

        // given
        appender.setQueueSize(0);

        // when
        appender.start();

        // then
        assertTrue(appender.isStarted());
        statusChecker.containsMatch(Status.ERROR, "Queue size of zero is deprecated, use a size of one to indicate synchronous processing");
    }

    @Test
    public void startsWithValidParameters() throws Exception {

        // when
        appender.start();

        // then
        assertTrue(appender.isStarted());
        statusChecker.isErrorFree(0);
    }
    
    @Test
    public void createsSocketConnectorWithConfiguredParameters() throws Exception {
        int retryDelay = 42;
        int port = 21;
        InetAddress localhost = InetAddress.getByName(LOCALHOST_STR);
        
        // given
        appender.setReconnectionDelay(new Duration(retryDelay));
        appender.setPort(port);

        // when
        appender.start();

        // then
        assertEquals(new DefaultSocketConnector(localhost, port, 0, retryDelay), appender.getSocketConnector());
    }
    
    @Test
    public void addsInfoMessageWhenSocketConnectionWasEstablished() throws Exception {

        // when
        mockOneSuccessfulSocketConnection();
        appender.start();

        // then
        statusChecker.containsMatch("connection established");
    }
    
    void startServer() {
        Context serverLoggerContext = new ContextBase();
        serverLoggerContext.setName("serverLoggerContext");
        final int port = RandomUtil.getRandomServerPort();
        SimpleSocketServer simpleSocketServer = new SimpleSocketServer(serverLoggerContext, port);
        simpleSocketServer.start();
    }
    
    private static class StringPreSerializationTransformer implements PreSerializationTransformer<String> {

        @Override
        public Serializable transform(String event) {
            return event;
        }
    }
}
