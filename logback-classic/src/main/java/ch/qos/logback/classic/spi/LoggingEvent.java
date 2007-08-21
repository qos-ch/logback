/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * 
 * Copyright (C) 1999-2006, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
 */

package ch.qos.logback.classic.spi;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Map;

import org.slf4j.MDC;
import org.slf4j.Marker;
import org.slf4j.helpers.MessageFormatter;
import org.slf4j.impl.LogbackMDCAdapter;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

/**
 * The internal representation of logging events. When an affirmative decision
 * is made to log then a <code>LoggingEvent</code> instance is created. This
 * instance is passed around to the different Logback components.
 * 
 * <p>
 * Writers of Logback components such as appenders should be aware of that some
 * of the LoggingEvent fields are initialized lazily. Therefore, an appender
 * wishing to output data to be later correctly read by a receiver, must
 * initialize "lazy" fields prior to writing them out. See the
 * {@link #prepareForDeferredProcessing()} method for the exact list.
 * </p>
 * 
 * @author Ceki G&uuml;lc&uuml;
 * @author S&eacute;bastien Pennec
 */
public class LoggingEvent implements Serializable {

  /**
   * 
   */
  private static final long serialVersionUID = 3022264832697160750L;

  /**
   * 
   */
  private static long startTime = System.currentTimeMillis();

  /**
   * Fully qualified name of the calling Logger class. This field does not
   * survive serialization.
   * 
   * <p>
   * Note that the getCallerInformation() method relies on this fact.
   */
  transient String fqnOfLoggerClass;

  /**
   * The name of thread in which this logging event was generated.
   */
  private String threadName;

  /**
   * Level of logging event.
   * 
   * <p>
   * This field should not be accessed directly. You shoud use the {@link
   * #getLevel} method instead.
   * </p>
   * 
   */
  private transient Level level;

  private String message;
  private String formattedMessage;

  private Object[] argumentArray;

  private ThrowableInformation throwableInfo;

  private CallerData[] callerDataArray;
  private LoggerRemoteView loggerRemoteView;

  private Marker marker;

  private Map<String, String> mdcPropertyMap;

  /**
   * The number of milliseconds elapsed from 1/1/1970 until logging event was
   * created.
   */
  private long timeStamp;

  public LoggingEvent() {
  }

  public LoggingEvent(String fqcn, Logger logger, Level level, String message,
      Throwable throwable, Object[] argArray) {
    this.fqnOfLoggerClass = fqcn;
    this.loggerRemoteView = logger.getLoggerRemoteView();
    this.level = level;
    this.message = message;

    if (throwable != null) {
      this.throwableInfo = new ThrowableInformation(throwable);
    }

    // bug 85 (we previously failed to set this.argumentArray)
    this.argumentArray = argArray;
    
    if (argArray != null) {
      formattedMessage = MessageFormatter.arrayFormat(message, argArray);
    } else {
      formattedMessage = message;
    }
    timeStamp = System.currentTimeMillis();

    // the case is ugly but under the circumstances acceptable
    LogbackMDCAdapter logbackMDCAdapter = (LogbackMDCAdapter) MDC.getMDCAdapter();
    mdcPropertyMap = logbackMDCAdapter.getPropertyMap();
  }

  public void setArgumentArray(Object[] argArray) {
    if (this.argumentArray != null) {
      throw new IllegalStateException("argArray has been already set");
    }
    this.argumentArray = argArray;
  }

  public Object[] getArgumentArray() {
    return this.argumentArray;
  }

  public Level getLevel() {
    return level;
  }

  public String getThreadName() {
    if (threadName == null) {
      threadName = (Thread.currentThread()).getName();
    }
    return threadName;
  }

  /**
   * @param threadName
   *          The threadName to set.
   * @throws IllegalStateException
   *           If threadName has been already set.
   */
  public void setThreadName(String threadName) throws IllegalStateException {
    if (this.threadName != null) {
      throw new IllegalStateException("threadName has been already set");
    }
    this.threadName = threadName;
  }

  /**
   * Returns the throwable information contained within this event. May be
   * <code>null</code> if there is no such information.
   */
  public ThrowableInformation getThrowableInformation() {
    return throwableInfo;
  }

  /**
   * Set this event's throwable information.
   */
  public void setThrowableInformation(ThrowableInformation ti) {
    if (throwableInfo != null) {
      throw new IllegalStateException(
          "ThrowableInformation has been already set.");
    } else {
      throwableInfo = ti;
    }
  }

  /**
   * This method should be called prior to serializing an event. It should also
   * be called when using asynchronous logging.
   * 
   * <p>Note that due to performance concerns, this method does NOT extract 
   * caller data. It is the responsability of the calller to extract caller
   * information.
   */
  public void prepareForDeferredProcessing() {
    this.getThreadName();
    
  }

  public LoggerRemoteView getLoggerRemoteView() {
    return loggerRemoteView;
  }

  public void setLoggerRemoteView(LoggerRemoteView loggerRemoteView) {
    this.loggerRemoteView = loggerRemoteView;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    if (this.message != null) {
      throw new IllegalStateException(
          "The message for this event has been set already.");
    }
    this.message = message;
  }

  public long getTimeStamp() {
    return timeStamp;
  }

  public void setTimeStamp(long timeStamp) {
    this.timeStamp = timeStamp;
  }

  public void setLevel(Level level) {
    if (this.level != null) {
      throw new IllegalStateException(
          "The level has been already set for this event.");
    }
    this.level = level;
  }

  /**
   * The time at which this class was loaded into memory, expressed in
   * millisecond elapsed since the epoch (1.1.1970).
   * 
   * @return The time as measured when this class was loaded into memory.
   */
  public static final long getStartTime() {
    return startTime;
  }

  /**
   * Get the caller information for this logging event. If caller information is
   * null at the time of its invocation, this method extracts location
   * information. The collected information is cached for future use.
   * 
   * <p>
   * Note that after serialization it is impossible to correctly extract caller
   * information.
   * </p>
   */
  public CallerData[] getCallerData() {
    // we rely on the fact that fqnOfLoggerClass does not survive
    // serialization
    if (callerDataArray == null && fqnOfLoggerClass != null) {
      callerDataArray = CallerData.extract(new Throwable(), fqnOfLoggerClass);
    }
    return callerDataArray;
  }

  public void setCallerInformation(CallerData[] callerDataArray) {
    this.callerDataArray = callerDataArray;
  }

  public Marker getMarker() {
    return marker;
  }

  public void setMarker(Marker marker) {
    if (this.marker != null) {
      throw new IllegalStateException(
          "The marker has been already set for this event.");
    }
    this.marker = marker;
  }

  public String getFormattedMessage() {
    return formattedMessage;
  }

  public Map<String, String> getMDCPropertyMap() {
    return mdcPropertyMap;
  }

  private void writeObject(ObjectOutputStream out) throws IOException {
    out.defaultWriteObject();
    out.writeInt(level.levelInt);
  }

  private void readObject(ObjectInputStream in) throws IOException,
      ClassNotFoundException {
    in.defaultReadObject();
    int levelInt = in.readInt();
    level = Level.toLevel(levelInt);
  }
  
  @Override
  public String toString() {
    StringBuffer sb = new StringBuffer('[');
    sb.append(level).append("] ");
    sb.append(getFormattedMessage());
    sb.append("\n");
    return sb.toString();
  }

}
