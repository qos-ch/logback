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

import java.util.ArrayList;
import java.util.List;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Queue;
import javax.jms.QueueSender;

public class MockQueueSender implements QueueSender {

    List<Message> messageList = new ArrayList<Message>();
    Queue queue;

    public MockQueueSender(Queue queue) {
        this.queue = queue;
    }

    public List<Message> getMessageList() {
        return messageList;
    }

    public Queue getQueue() throws JMSException {
        return queue;
    }

    public void send(Message message) throws JMSException {
        messageList.add(message);

    }

    public void send(Queue arg0, Message arg1) throws JMSException {

    }

    public void send(Message arg0, int arg1, int arg2, long arg3) throws JMSException {

    }

    public void send(Queue arg0, Message arg1, int arg2, int arg3, long arg4) throws JMSException {

    }

    public void close() throws JMSException {

    }

    public int getDeliveryMode() throws JMSException {

        return 0;
    }

    public Destination getDestination() throws JMSException {

        return null;
    }

    public boolean getDisableMessageID() throws JMSException {

        return false;
    }

    public boolean getDisableMessageTimestamp() throws JMSException {

        return false;
    }

    public int getPriority() throws JMSException {

        return 0;
    }

    public long getTimeToLive() throws JMSException {

        return 0;
    }

    public void send(Destination arg0, Message arg1) throws JMSException {

    }

    public void send(Destination arg0, Message arg1, int arg2, int arg3, long arg4) throws JMSException {

    }

    public void setDeliveryMode(int arg0) throws JMSException {

    }

    public void setDisableMessageID(boolean arg0) throws JMSException {

    }

    public void setDisableMessageTimestamp(boolean arg0) throws JMSException {

    }

    public void setPriority(int arg0) throws JMSException {

    }

    public void setTimeToLive(long arg0) throws JMSException {

    }

}
