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
package ch.qos.logback.classic.net.server;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.net.mock.MockAppender;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.classic.spi.LoggingEventVO;

/**
 * Unit tests for {@link RemoteAppenderStreamClient}.
 *
 * @author Carl Harris
 */
public class RemoteAppenderStreamClientTest {

    private MockAppender appender;
    private Logger logger;
    private LoggingEvent event;
    private RemoteAppenderStreamClient client;

    @Before
    public void setUp() throws Exception {
        LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();

        appender = new MockAppender();
        appender.start();

        logger = lc.getLogger(getClass());
        logger.addAppender(appender);

        event = new LoggingEvent(logger.getName(), logger, Level.DEBUG, "test message", null, new Object[0]);

        LoggingEventVO eventVO = LoggingEventVO.build(event);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(eventVO);
        oos.close();

        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
        client = new RemoteAppenderStreamClient("some client ID", bis);
        client.setLoggerContext(lc);
    }

    @Test
    public void testWithEnabledLevel() throws Exception {
        logger.setLevel(Level.DEBUG);
        client.run();
        client.close();

        ILoggingEvent rcvdEvent = appender.getLastEvent();
        assertEquals(event.getLoggerName(), rcvdEvent.getLoggerName());
        assertEquals(event.getLevel(), rcvdEvent.getLevel());
        assertEquals(event.getMessage(), rcvdEvent.getMessage());
    }

    @Test
    public void testWithDisabledLevel() throws Exception {
        logger.setLevel(Level.INFO);
        client.run();
        client.close();
        assertNull(appender.getLastEvent());
    }

}
