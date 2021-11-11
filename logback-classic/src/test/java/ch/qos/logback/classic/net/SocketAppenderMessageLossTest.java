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
package ch.qos.logback.classic.net;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.junit.Ignore;
import org.junit.Test;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import ch.qos.logback.core.testUtil.RandomUtil;
import ch.qos.logback.core.util.Duration;

@Ignore
public class SocketAppenderMessageLossTest {
    int runLen = 100;
    Duration reconnectionDelay = new Duration(1000);

    static final int TIMEOUT = 3000;

    @Test // (timeout = TIMEOUT)
    public void synchronousSocketAppender() throws Exception {

        final SocketAppender socketAppender = new SocketAppender();
        socketAppender.setReconnectionDelay(reconnectionDelay);
        socketAppender.setIncludeCallerData(true);

        runTest(socketAppender);
    }

    @Test(timeout = TIMEOUT)
    public void smallQueueSocketAppender() throws Exception {

        final SocketAppender socketAppender = new SocketAppender();
        socketAppender.setReconnectionDelay(reconnectionDelay);
        socketAppender.setQueueSize(runLen / 10);

        runTest(socketAppender);
    }

    @Test(timeout = TIMEOUT)
    public void largeQueueSocketAppender() throws Exception {
        final SocketAppender socketAppender = new SocketAppender();
        socketAppender.setReconnectionDelay(reconnectionDelay);
        socketAppender.setQueueSize(runLen * 5);

        runTest(socketAppender);
    }

    // appender used to signal when the N'th event (as set in the latch) is received by the server
    // this allows us to have test which are both more robust and quicker.
    static public class ListAppenderWithLatch extends AppenderBase<ILoggingEvent> {
        public List<ILoggingEvent> list = new ArrayList<>();
        CountDownLatch latch;

        ListAppenderWithLatch(final CountDownLatch latch) {
            this.latch = latch;
        }

        @Override
        protected void append(final ILoggingEvent e) {
            list.add(e);
            latch.countDown();
        }
    }

    public void runTest(final SocketAppender socketAppender) throws Exception {
        final int port = RandomUtil.getRandomServerPort();

        final LoggerContext serverLoggerContext = new LoggerContext();
        serverLoggerContext.setName("serverLoggerContext");

        final CountDownLatch allMessagesReceivedLatch = new CountDownLatch(runLen);
        final ListAppenderWithLatch listAppender = new ListAppenderWithLatch(allMessagesReceivedLatch);
        listAppender.setContext(serverLoggerContext);
        listAppender.start();

        final Logger serverRootLogger = serverLoggerContext.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
        serverRootLogger.setAdditive(false);
        serverRootLogger.addAppender(listAppender);

        final LoggerContext loggerContext = new LoggerContext();
        loggerContext.setName("clientLoggerContext");
        socketAppender.setContext(loggerContext);

        final CountDownLatch latch = new CountDownLatch(1);
        final SimpleSocketServer simpleSocketServer = new SimpleSocketServer(serverLoggerContext, port);
        simpleSocketServer.start();
        simpleSocketServer.setLatch(latch);

        latch.await();

        socketAppender.setPort(port);
        socketAppender.setRemoteHost("localhost");
        socketAppender.setReconnectionDelay(reconnectionDelay);
        socketAppender.start();
        assertTrue(socketAppender.isStarted());

        final Logger logger = loggerContext.getLogger(getClass());
        logger.setAdditive(false);
        logger.addAppender(socketAppender);

        for (int i = 0; i < runLen; ++i) {
            logger.info("hello");
        }

        allMessagesReceivedLatch.await();
        loggerContext.stop();
        simpleSocketServer.close();

    }
}
