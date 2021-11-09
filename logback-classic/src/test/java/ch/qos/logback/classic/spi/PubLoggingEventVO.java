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
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.slf4j.Marker;
import org.slf4j.event.KeyValuePair;
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
	public List<Marker> markerList;
	public List<KeyValuePair> kvpList;
	public Map<String, String> mdcPropertyMap;
	public long timeStamp;
	public long sequenceNumber;

	@Override
	public String getThreadName() {
		return threadName;
	}

	@Override
	public LoggerContextVO getLoggerContextVO() {
		return loggerContextVO;
	}

	@Override
	public String getLoggerName() {
		return loggerName;
	}

	@Override
	public Level getLevel() {
		return level;
	}

	@Override
	public String getMessage() {
		return message;
	}

	@Override
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

	@Override
	public Object[] getArgumentArray() {
		return argumentArray;
	}

	@Override
	public IThrowableProxy getThrowableProxy() {
		return throwableProxy;
	}

	@Override
	public StackTraceElement[] getCallerData() {
		return callerDataArray;
	}

	@Override
	public boolean hasCallerData() {
		return callerDataArray != null;
	}

	@Override
	public List<Marker> getMarkerList() {
		return markerList;
	}

	@Override
	public long getTimeStamp() {
		return timeStamp;
	}

	@Override
	public long getSequenceNumber() {
		return sequenceNumber;
	}

	public void setSequenceNumber(final long sequenceNumber) {
		this.sequenceNumber = sequenceNumber;
	}

	public long getContextBirthTime() {
		return loggerContextVO.getBirthTime();
	}

	public LoggerContextVO getContextLoggerRemoteView() {
		return loggerContextVO;
	}

	@Override
	public Map<String, String> getMDCPropertyMap() {
		return mdcPropertyMap;
	}

	@Override
	public Map<String, String> getMdc() {
		return mdcPropertyMap;
	}

	@Override
	public void prepareForDeferredProcessing() {
	}

	@Override
	public List<KeyValuePair> getKeyValuePairs() {
		return kvpList;
	}

	private void writeObject(final ObjectOutputStream out) throws IOException {
		out.defaultWriteObject();
		out.writeInt(level.levelInt);
		if (argumentArray != null) {
			final int len = argumentArray.length;
			out.writeInt(len);
			for (final Object element : argumentArray) {
				if (element != null) {
					out.writeObject(element.toString());
				} else {
					out.writeObject(NULL_ARGUMENT_ARRAY_ELEMENT);
				}
			}
		} else {
			out.writeInt(NULL_ARGUMENT_ARRAY);
		}

	}

	private void readObject(final ObjectInputStream in) throws IOException, ClassNotFoundException {
		in.defaultReadObject();
		final int levelInt = in.readInt();
		level = Level.toLevel(levelInt);

		final int argArrayLen = in.readInt();
		if (argArrayLen != NULL_ARGUMENT_ARRAY) {
			argumentArray = new String[argArrayLen];
			for (int i = 0; i < argArrayLen; i++) {
				final Object val = in.readObject();
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
		result = prime * result + (message == null ? 0 : message.hashCode());
		result = prime * result + (threadName == null ? 0 : threadName.hashCode());
		return prime * result + (int) (timeStamp ^ timeStamp >>> 32);
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if ((obj == null) || (getClass() != obj.getClass())) {
			return false;
		}
		final PubLoggingEventVO other = (PubLoggingEventVO) obj;
		if (!Objects.equals(message, other.message)) {
			return false;
		}

		if (!Objects.equals(loggerName, other.loggerName)) {
			return false;
		}

		if (!Objects.equals(threadName, other.threadName)) {
			return false;
		}
		if (timeStamp != other.timeStamp) {
			return false;
		}

		if (!Objects.equals(markerList, other.markerList)) {
			return false;
		}

		if (!Objects.equals(mdcPropertyMap, other.mdcPropertyMap)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
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
