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
import javax.jms.Session;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import javax.jms.TopicPublisher;
import javax.jms.TopicSession;
import javax.naming.Context;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import ch.qos.logback.core.net.JMSAppenderBase;
import ch.qos.logback.core.spi.PreSerializationTransformer;

/**
 * A simple appender that publishes events to a JMS Topic. The events are
 * serialized and transmitted as JMS message type {@link
 * javax.jms.ObjectMessage}.
 * 
 * For more information about this appender, please refer to
 * http://logback.qos.ch/manual/appenders.html#JMSTopicAppender
 * 
 * @author Ceki G&uuml;lc&uuml;
 */
public class JMSTopicAppender extends JMSAppenderBase<ILoggingEvent> {

    static int SUCCESSIVE_FAILURE_LIMIT = 3;

    String topicBindingName;
    String tcfBindingName;
    TopicConnection topicConnection;
    TopicSession topicSession;
    TopicPublisher topicPublisher;

    int successiveFailureCount = 0;

    private PreSerializationTransformer<ILoggingEvent> pst = new LoggingEventPreSerializationTransformer();

    /**
     * The <b>TopicConnectionFactoryBindingName</b> option takes a string value.
     * Its value will be used to lookup the appropriate
     * <code>TopicConnectionFactory</code> from the JNDI context.
     */
    public void setTopicConnectionFactoryBindingName(String tcfBindingName) {
        this.tcfBindingName = tcfBindingName;
    }

    /**
     * Returns the value of the <b>TopicConnectionFactoryBindingName</b> option.
     */
    public String getTopicConnectionFactoryBindingName() {
        return tcfBindingName;
    }

    /**
     * The <b>TopicBindingName</b> option takes a string value. Its value will be
     * used to lookup the appropriate <code>Topic</code> from the JNDI context.
     */
    public void setTopicBindingName(String topicBindingName) {
        this.topicBindingName = topicBindingName;
    }

    /**
     * Returns the value of the <b>TopicBindingName</b> option.
     */
    public String getTopicBindingName() {
        return topicBindingName;
    }

    /**
     * Options are activated and become effective only after calling this method.
     */
    public void start() {
        TopicConnectionFactory topicConnectionFactory;

        try {
            Context jndi = buildJNDIContext();

            // addInfo("Looking up [" + tcfBindingName + "]");
            topicConnectionFactory = (TopicConnectionFactory) lookup(jndi, tcfBindingName);
            // addInfo("About to create TopicConnection.");
            if (userName != null) {
                this.topicConnection = topicConnectionFactory.createTopicConnection(userName, password);
            } else {
                this.topicConnection = topicConnectionFactory.createTopicConnection();
            }

            // addInfo(
            // "Creating TopicSession, non-transactional, "
            // + "in AUTO_ACKNOWLEDGE mode.");
            this.topicSession = topicConnection.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);

            // addInfo("Looking up topic name [" + topicBindingName + "].");
            Topic topic = (Topic) lookup(jndi, topicBindingName);

            // addInfo("Creating TopicPublisher.");
            this.topicPublisher = topicSession.createPublisher(topic);

            // addInfo("Starting TopicConnection.");
            topicConnection.start();

            jndi.close();
        } catch (Exception e) {
            addError("Error while activating options for appender named [" + name + "].", e);
        }

        if (this.topicConnection != null && this.topicSession != null && this.topicPublisher != null) {
            super.start();
        }
    }

    /**
     * Close this JMSAppender. Closing releases all resources used by the
     * appender. A closed appender cannot be re-opened.
     */
    public synchronized void stop() {
        // The synchronized modifier avoids concurrent append and close operations
        if (!this.started) {
            return;
        }

        this.started = false;

        try {
            if (topicSession != null) {
                topicSession.close();
            }
            if (topicConnection != null) {
                topicConnection.close();
            }
        } catch (Exception e) {
            addError("Error while closing JMSAppender [" + name + "].", e);
        }

        // Help garbage collection
        topicPublisher = null;
        topicSession = null;
        topicConnection = null;
    }

    /**
     * This method called by {@link AppenderBase#doAppend} method to do most
     * of the real appending work.
     */
    public void append(ILoggingEvent event) {
        if (!isStarted()) {
            return;
        }

        try {
            ObjectMessage msg = topicSession.createObjectMessage();
            Serializable so = pst.transform(event);
            msg.setObject(so);
            topicPublisher.publish(msg);
            successiveFailureCount = 0;
        } catch (Exception e) {
            successiveFailureCount++;
            if (successiveFailureCount > SUCCESSIVE_FAILURE_LIMIT) {
                stop();
            }
            addError("Could not publish message in JMSTopicAppender [" + name + "].", e);
        }
    }

    /**
     * Returns the TopicConnection used for this appender. Only valid after
     * start() method has been invoked.
     */
    protected TopicConnection getTopicConnection() {
        return topicConnection;
    }

    /**
     * Returns the TopicSession used for this appender. Only valid after start()
     * method has been invoked.
     */
    protected TopicSession getTopicSession() {
        return topicSession;
    }

    /**
     * Returns the TopicPublisher used for this appender. Only valid after start()
     * method has been invoked.
     */
    protected TopicPublisher getTopicPublisher() {
        return topicPublisher;
    }
}
