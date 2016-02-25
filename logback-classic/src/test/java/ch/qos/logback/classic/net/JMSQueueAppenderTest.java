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

import java.io.Serializable;

import javax.jms.ObjectMessage;

import junit.framework.TestCase;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.net.mock.MockQueue;
import ch.qos.logback.classic.net.mock.MockQueueConnectionFactory;
import ch.qos.logback.classic.net.mock.MockQueueSender;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.classic.util.MockInitialContext;
import ch.qos.logback.classic.util.MockInitialContextFactory;
import ch.qos.logback.core.ContextBase;
import ch.qos.logback.core.spi.PreSerializationTransformer;

public class JMSQueueAppenderTest extends TestCase {

    ch.qos.logback.core.Context context;
    JMSQueueAppender appender;
    PreSerializationTransformer<ILoggingEvent> pst = new LoggingEventPreSerializationTransformer();

    @Override
    protected void setUp() throws Exception {
        context = new ContextBase();
        appender = new JMSQueueAppender();
        appender.setContext(context);
        appender.setName("jmsQueue");
        appender.qcfBindingName = "queueCnxFactory";
        appender.queueBindingName = "testQueue";
        appender.setProviderURL("url");
        appender.setInitialContextFactoryName(MockInitialContextFactory.class.getName());

        MockInitialContext mic = MockInitialContextFactory.getContext();
        mic.map.put(appender.qcfBindingName, new MockQueueConnectionFactory());
        mic.map.put(appender.queueBindingName, new MockQueue(appender.queueBindingName));

        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        appender = null;
        context = null;
        super.tearDown();
    }

    public void testAppendOk() {
        appender.start();

        ILoggingEvent le = createLoggingEvent();
        appender.append(le);

        MockQueueSender qs = (MockQueueSender) appender.queueSender;
        assertEquals(1, qs.getMessageList().size());
        ObjectMessage message = (ObjectMessage) qs.getMessageList().get(0);
        try {
            Serializable witness = pst.transform(le);
            assertEquals(witness, message.getObject());
        } catch (Exception e) {
            fail();
        }
    }

    public void testAppendFailure() {
        appender.start();

        // make sure the append method does not work
        appender.queueSender = null;

        ILoggingEvent le = createLoggingEvent();
        for (int i = 1; i <= 3; i++) {
            appender.append(le);
            assertEquals(i, context.getStatusManager().getCount());
            assertTrue(appender.isStarted());
        }
        appender.append(le);
        assertEquals(4, context.getStatusManager().getCount());
        assertFalse(appender.isStarted());
    }

    public void testStartMinimalInfo() {
        // let's leave only what's in the setup()
        // method, minus the providerURL
        appender.setProviderURL(null);
        appender.start();

        assertTrue(appender.isStarted());

        try {
            assertEquals(appender.queueBindingName, appender.queueSender.getQueue().getQueueName());
        } catch (Exception e) {
            fail();
        }
    }

    public void testStartUserPass() {
        appender.setUserName("test");
        appender.setPassword("test");

        appender.start();

        assertTrue(appender.isStarted());

        try {
            assertEquals(appender.queueBindingName, appender.queueSender.getQueue().getQueueName());
        } catch (Exception e) {
            fail();
        }
    }

    public void testStartFails() {
        appender.queueBindingName = null;

        appender.start();

        assertFalse(appender.isStarted());
    }

    private ILoggingEvent createLoggingEvent() {
        LoggingEvent le = new LoggingEvent();
        le.setLevel(Level.DEBUG);
        le.setMessage("test message");
        le.setTimeStamp(System.currentTimeMillis());
        le.setThreadName(Thread.currentThread().getName());
        return le;
    }
}
