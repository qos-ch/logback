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
package ch.qos.logback.classic.spi;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Map;

import org.slf4j.Marker;
import org.slf4j.helpers.MessageFormatter;

import ch.qos.logback.classic.Level;

/**
 * A read/write and serializable implementation of {@link ILoggingEvent}.
 * 
 * @author Ceki G&uuml;lc&uuml;
 */
public class PubLoggingEventVO implements ILoggingEvent, Serializable {

    private static final long serialVersionUID = -3385765861078946218L;

    private static final int NULL_ARGUMENT_ARRAY = -1;
    private static final String NULL_ARGUMENT_ARRAY_ELEMENT = "NULL_ARGUMENT_ARRAY_ELEMENT";

    public String threadName;
    public String loggerName;
    public LoggerContextVO loggerContextVO;

    public transient Level level;
    public String message;

    private transient String formattedMessage;

    public Object[] argumentArray;

    public IThrowableProxy throwableProxy;
    public StackTraceElement[] callerDataArray;
    public Marker marker;
    public Map<String, String> mdcPropertyMap;
    public long timeStamp;
    public long sequenceNumber;

    public String getThreadName() {
        return threadName;
    }

    public LoggerContextVO getLoggerContextVO() {
        return loggerContextVO;
    }

    public String getLoggerName() {
        return loggerName;
    }

    public Level getLevel() {
        return level;
    }

    public String getMessage() {
        return message;
    }

    public String getFormattedMessage() {
        if (formattedMessage != null) {
            return formattedMessage;
        }

        if (argumentArray != null) {
            formattedMessage = MessageFormatter.arrayFormat(message, argumentArray).getMessage();
        } else {
            formattedMessage = message;
        }

        return formattedMessage;
    }

    public Object[] getArgumentArray() {
        return argumentArray;
    }

    public IThrowableProxy getThrowableProxy() {
        return throwableProxy;
    }

    public StackTraceElement[] getCallerData() {
        return callerDataArray;
    }

    public boolean hasCallerData() {
        return callerDataArray != null;
    }

    public Marker getMarker() {
        return marker;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public long getSequenceNumber() {
        return sequenceNumber;
    }

    public void setSequenceNumber(long sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }
    
    public long getContextBirthTime() {
        return loggerContextVO.getBirthTime();
    }

    public LoggerContextVO getContextLoggerRemoteView() {
        return loggerContextVO;
    }

    public Map<String, String> getMDCPropertyMap() {
        return mdcPropertyMap;
    }

    public Map<String, String> getMdc() {
        return mdcPropertyMap;
    }

    public void prepareForDeferredProcessing() {
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
        out.writeInt(level.levelInt);
        if (argumentArray != null) {
            int len = argumentArray.length;
            out.writeInt(len);
            for (int i = 0; i < argumentArray.length; i++) {
                if (argumentArray[i] != null) {
                    out.writeObject(argumentArray[i].toString());
                } else {
                    out.writeObject(NULL_ARGUMENT_ARRAY_ELEMENT);
                }
            }
        } else {
            out.writeInt(NULL_ARGUMENT_ARRAY);
        }

    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        int levelInt = in.readInt();
        level = Level.toLevel(levelInt);

        int argArrayLen = in.readInt();
        if (argArrayLen != NULL_ARGUMENT_ARRAY) {
            argumentArray = new String[argArrayLen];
            for (int i = 0; i < argArrayLen; i++) {
                Object val = in.readObject();
                if (!NULL_ARGUMENT_ARRAY_ELEMENT.equals(val)) {
                    argumentArray[i] = val;
                }
            }
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((message == null) ? 0 : message.hashCode());
        result = prime * result + ((threadName == null) ? 0 : threadName.hashCode());
        result = prime * result + (int) (timeStamp ^ (timeStamp >>> 32));
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final PubLoggingEventVO other = (PubLoggingEventVO) obj;
        if (message == null) {
            if (other.message != null)
                return false;
        } else if (!message.equals(other.message))
            return false;

        if (loggerName == null) {
            if (other.loggerName != null)
                return false;
        } else if (!loggerName.equals(other.loggerName))
            return false;

        if (threadName == null) {
            if (other.threadName != null)
                return false;
        } else if (!threadName.equals(other.threadName))
            return false;
        if (timeStamp != other.timeStamp)
            return false;

        if (marker == null) {
            if (other.marker != null)
                return false;
        } else if (!marker.equals(other.marker))
            return false;

        if (mdcPropertyMap == null) {
            if (other.mdcPropertyMap != null)
                return false;
        } else if (!mdcPropertyMap.equals(other.mdcPropertyMap))
            return false;
        return true;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(timeStamp);
        sb.append(" ");
        sb.append(level);
        sb.append(" [");
        sb.append(threadName);
        sb.append("] ");
        sb.append(loggerName);
        sb.append(" - ");
        sb.append(getFormattedMessage());
        return sb.toString();
    }

}
