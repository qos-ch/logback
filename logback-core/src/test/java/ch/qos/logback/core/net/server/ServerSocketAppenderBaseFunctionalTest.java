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
import static org.junit.Assert.assertTrue;

import java.io.ObjectInputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import ch.qos.logback.core.net.mock.MockContext;
import ch.qos.logback.core.net.server.test.ServerSocketUtil;
import ch.qos.logback.core.util.ExecutorServiceUtil;

/**
 * A functional test for {@link AbstractServerSocketAppender}.
 *
 * @author Carl Harris
 */
@Ignore
public class ServerSocketAppenderBaseFunctionalTest {

    private static final String TEST_EVENT = "test event";

    private static final int EVENT_COUNT = 10;

    private ScheduledExecutorService executor = ExecutorServiceUtil.newScheduledExecutorService();
    private MockContext context = new MockContext(executor);
    private ServerSocket serverSocket;
    private InstrumentedServerSocketAppenderBase appender;

    @Before
    public void setUp() throws Exception {

        serverSocket = ServerSocketUtil.createServerSocket();

        appender = new InstrumentedServerSocketAppenderBase(serverSocket);
        appender.setContext(context);
    }

    @After
    public void tearDown() throws Exception {
        executor.shutdownNow();
        executor.awaitTermination(10000, TimeUnit.MILLISECONDS);
        assertTrue(executor.isTerminated());
    }

    @Test
    public void testLogEventClient() throws Exception {
        appender.start();
        Socket socket = new Socket(InetAddress.getLocalHost(), serverSocket.getLocalPort());

        socket.setSoTimeout(1000);
        ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());

        for (int i = 0; i < EVENT_COUNT; i++) {
            appender.append(TEST_EVENT + i);
            assertEquals(TEST_EVENT + i, ois.readObject());
        }

        socket.close();
        appender.stop();
    }

}
