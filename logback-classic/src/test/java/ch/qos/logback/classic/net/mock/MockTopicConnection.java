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
package ch.qos.logback.classic.net.mock;

import javax.jms.ConnectionConsumer;
import javax.jms.ConnectionMetaData;
import javax.jms.Destination;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.ServerSessionPool;
import javax.jms.Session;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicSession;

public class MockTopicConnection implements TopicConnection {

    MockTopicSession session = new MockTopicSession();

    public TopicSession createTopicSession(boolean arg0, int arg1) throws JMSException {
        return session;
    }

    public ConnectionConsumer createConnectionConsumer(Topic arg0, String arg1, ServerSessionPool arg2, int arg3) throws JMSException {

        return null;
    }

    public ConnectionConsumer createDurableConnectionConsumer(Topic arg0, String arg1, String arg2, ServerSessionPool arg3, int arg4) throws JMSException {

        return null;
    }

    public void close() throws JMSException {

    }

    public ConnectionConsumer createConnectionConsumer(Destination arg0, String arg1, ServerSessionPool arg2, int arg3) throws JMSException {

        return null;
    }

    public Session createSession(boolean arg0, int arg1) throws JMSException {

        return null;
    }

    public String getClientID() throws JMSException {

        return null;
    }

    public ExceptionListener getExceptionListener() throws JMSException {

        return null;
    }

    public ConnectionMetaData getMetaData() throws JMSException {

        return null;
    }

    public void setClientID(String arg0) throws JMSException {

    }

    public void setExceptionListener(ExceptionListener arg0) throws JMSException {

    }

    public void start() throws JMSException {

    }

    public void stop() throws JMSException {

    }

}
