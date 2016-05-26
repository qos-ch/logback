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

import java.io.Serializable;

import javax.jms.BytesMessage;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.QueueBrowser;
import javax.jms.QueueReceiver;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.StreamMessage;
import javax.jms.TemporaryQueue;
import javax.jms.TemporaryTopic;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.jms.TopicSubscriber;

public class MockQueueSession implements QueueSession {

    public ObjectMessage createObjectMessage() throws JMSException {
        return new MockObjectMessage();
    }

    public QueueSender createSender(Queue queue) throws JMSException {
        if (queue == null) {
            return null;
        }
        return new MockQueueSender(queue);
    }

    public QueueBrowser createBrowser(Queue arg0) throws JMSException {

        return null;
    }

    public QueueBrowser createBrowser(Queue arg0, String arg1) throws JMSException {

        return null;
    }

    public Queue createQueue(String arg0) throws JMSException {

        return null;
    }

    public QueueReceiver createReceiver(Queue arg0) throws JMSException {

        return null;
    }

    public QueueReceiver createReceiver(Queue arg0, String arg1) throws JMSException {

        return null;
    }

    public TemporaryQueue createTemporaryQueue() throws JMSException {

        return null;
    }

    public void close() throws JMSException {

    }

    public void commit() throws JMSException {

    }

    public BytesMessage createBytesMessage() throws JMSException {

        return null;
    }

    public MessageConsumer createConsumer(Destination arg0) throws JMSException {

        return null;
    }

    public MessageConsumer createConsumer(Destination arg0, String arg1) throws JMSException {

        return null;
    }

    public MessageConsumer createConsumer(Destination arg0, String arg1, boolean arg2) throws JMSException {

        return null;
    }

    public TopicSubscriber createDurableSubscriber(Topic arg0, String arg1) throws JMSException {

        return null;
    }

    public TopicSubscriber createDurableSubscriber(Topic arg0, String arg1, String arg2, boolean arg3) throws JMSException {

        return null;
    }

    public MapMessage createMapMessage() throws JMSException {

        return null;
    }

    public Message createMessage() throws JMSException {

        return null;
    }

    public ObjectMessage createObjectMessage(Serializable arg0) throws JMSException {

        return null;
    }

    public MessageProducer createProducer(Destination arg0) throws JMSException {

        return null;
    }

    public StreamMessage createStreamMessage() throws JMSException {

        return null;
    }

    public TemporaryTopic createTemporaryTopic() throws JMSException {

        return null;
    }

    public TextMessage createTextMessage() throws JMSException {

        return null;
    }

    public TextMessage createTextMessage(String arg0) throws JMSException {

        return null;
    }

    public Topic createTopic(String arg0) throws JMSException {

        return null;
    }

    public int getAcknowledgeMode() throws JMSException {

        return 0;
    }

    public MessageListener getMessageListener() throws JMSException {

        return null;
    }

    public boolean getTransacted() throws JMSException {

        return false;
    }

    public void recover() throws JMSException {

    }

    public void rollback() throws JMSException {

    }

    public void run() {

    }

    public void setMessageListener(MessageListener arg0) throws JMSException {

    }

    public void unsubscribe(String arg0) throws JMSException {

    }

}
