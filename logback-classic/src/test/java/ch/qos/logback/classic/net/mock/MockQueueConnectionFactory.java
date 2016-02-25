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

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;

public class MockQueueConnectionFactory implements QueueConnectionFactory {

    MockQueueConnection cnx = new MockQueueConnection();

    public QueueConnection createQueueConnection() throws JMSException {
        return cnx;
    }

    public QueueConnection createQueueConnection(String user, String pass) throws JMSException {

        return cnx;
    }

    public Connection createConnection() throws JMSException {
        return null;
    }

    public Connection createConnection(String arg0, String arg1) throws JMSException {
        return null;
    }

}
