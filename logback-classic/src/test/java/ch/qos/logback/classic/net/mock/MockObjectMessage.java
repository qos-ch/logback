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
import java.util.Enumeration;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.ObjectMessage;

public class MockObjectMessage implements ObjectMessage {

    Serializable object;

    public Serializable getObject() throws JMSException {
        return object;
    }

    public void setObject(Serializable object) throws JMSException {
        this.object = object;
    }

    public void acknowledge() throws JMSException {

    }

    public void clearBody() throws JMSException {

    }

    public void clearProperties() throws JMSException {

    }

    public boolean getBooleanProperty(String arg0) throws JMSException {

        return false;
    }

    public byte getByteProperty(String arg0) throws JMSException {

        return 0;
    }

    public double getDoubleProperty(String arg0) throws JMSException {

        return 0;
    }

    public float getFloatProperty(String arg0) throws JMSException {

        return 0;
    }

    public int getIntProperty(String arg0) throws JMSException {

        return 0;
    }

    public String getJMSCorrelationID() throws JMSException {

        return null;
    }

    public byte[] getJMSCorrelationIDAsBytes() throws JMSException {

        return null;
    }

    public int getJMSDeliveryMode() throws JMSException {

        return 0;
    }

    public Destination getJMSDestination() throws JMSException {

        return null;
    }

    public long getJMSExpiration() throws JMSException {

        return 0;
    }

    public String getJMSMessageID() throws JMSException {

        return null;
    }

    public int getJMSPriority() throws JMSException {

        return 0;
    }

    public boolean getJMSRedelivered() throws JMSException {

        return false;
    }

    public Destination getJMSReplyTo() throws JMSException {

        return null;
    }

    public long getJMSTimestamp() throws JMSException {

        return 0;
    }

    public String getJMSType() throws JMSException {

        return null;
    }

    public long getLongProperty(String arg0) throws JMSException {

        return 0;
    }

    public Object getObjectProperty(String arg0) throws JMSException {

        return null;
    }

    public Enumeration getPropertyNames() throws JMSException {

        return null;
    }

    public short getShortProperty(String arg0) throws JMSException {

        return 0;
    }

    public String getStringProperty(String arg0) throws JMSException {

        return null;
    }

    public boolean propertyExists(String arg0) throws JMSException {

        return false;
    }

    public void setBooleanProperty(String arg0, boolean arg1) throws JMSException {

    }

    public void setByteProperty(String arg0, byte arg1) throws JMSException {

    }

    public void setDoubleProperty(String arg0, double arg1) throws JMSException {

    }

    public void setFloatProperty(String arg0, float arg1) throws JMSException {

    }

    public void setIntProperty(String arg0, int arg1) throws JMSException {

    }

    public void setJMSCorrelationID(String arg0) throws JMSException {

    }

    public void setJMSCorrelationIDAsBytes(byte[] arg0) throws JMSException {

    }

    public void setJMSDeliveryMode(int arg0) throws JMSException {

    }

    public void setJMSDestination(Destination arg0) throws JMSException {

    }

    public void setJMSExpiration(long arg0) throws JMSException {

    }

    public void setJMSMessageID(String arg0) throws JMSException {

    }

    public void setJMSPriority(int arg0) throws JMSException {

    }

    public void setJMSRedelivered(boolean arg0) throws JMSException {

    }

    public void setJMSReplyTo(Destination arg0) throws JMSException {

    }

    public void setJMSTimestamp(long arg0) throws JMSException {

    }

    public void setJMSType(String arg0) throws JMSException {

    }

    public void setLongProperty(String arg0, long arg1) throws JMSException {

    }

    public void setObjectProperty(String arg0, Object arg1) throws JMSException {

    }

    public void setShortProperty(String arg0, short arg1) throws JMSException {

    }

    public void setStringProperty(String arg0, String arg1) throws JMSException {

    }

}
