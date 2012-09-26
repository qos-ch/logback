/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2011, QOS.ch. All rights reserved.
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

import java.io.IOException;
import java.io.OutputStream;

import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.pattern.SyslogStartConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.StackTraceElementProxy;
import ch.qos.logback.classic.util.LevelToSyslogSeverity;
import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.Layout;
import ch.qos.logback.core.net.SyslogAppenderBase;

/**
 * This appender can be used to send messages to a remote syslog daemon. <p> For
 * more information about this appender, please refer to the online manual at
 * http://logback.qos.ch/manual/appenders.html#SyslogAppender
 *
 * @author Ceki G&uuml;lc&uuml;
 */
public class SyslogAppender extends SyslogAppenderBase<ILoggingEvent> {

  static final public String DEFAULT_SUFFIX_PATTERN = "[%thread] %logger %msg";
  static final public String DEFAULT_STACKTRACE_PATTERN = "" + CoreConstants.TAB;
  static final public String DEFAULT_STACKTRACE_HEADER_PATTERN = "";

  PatternLayout stackTraceHeaderLayout = new PatternLayout();
  String stackTraceHeaderPattern = DEFAULT_STACKTRACE_HEADER_PATTERN;
  
  PatternLayout stackTraceLayout = new PatternLayout();
  String stackTracePattern = DEFAULT_STACKTRACE_PATTERN;

  boolean throwableExcluded = false;

  
  public void start() {
    super.start();
    setupStackTraceHeaderLayout();
    setupStackTraceLayout();
  }

  String getPrefixPattern() {
    return "%syslogStart{" + getFacility() + "}%nopex";
  }

  /*
   * Convert a level to equivalent syslog severity. Only levels for printing
   * methods i.e DEBUG, WARN, INFO and ERROR are converted.
   * 
   * @see ch.qos.logback.core.net.SyslogAppenderBase#getSeverityForEvent(java.lang.Object)
   */
  @Override
  public int getSeverityForEvent(Object eventObject) {
    ILoggingEvent event = (ILoggingEvent) eventObject;
    return LevelToSyslogSeverity.convert(event);
  }

  @Override
  protected void postProcess(Object eventObject, OutputStream sw) {
	    if (throwableExcluded)
	      return;

	    ILoggingEvent event = (ILoggingEvent) eventObject;
	    IThrowableProxy tp = event.getThrowableProxy();

	    if(tp == null)
	      return;

	    String stackTracePrefix = stackTraceLayout.doLayout(event);
	    String stackTraceHeaderPrefix = stackTraceHeaderLayout.doLayout(event);
	    boolean topException = true;
	    while (tp != null) {
	      StackTraceElementProxy[] stepArray = tp.getStackTraceElementProxyArray();
	      try {
	    	StringBuilder stackTraceHeaderLine = new StringBuilder().append(stackTraceHeaderPrefix);
	    	if (topException) {
	    		topException = false;
	    	} else {
	    		stackTraceHeaderLine.append(CoreConstants.CAUSED_BY);
	    	}
	    	stackTraceHeaderLine.append(tp.getClassName()).append(": ").append(tp.getMessage());
	    	sw.write(stackTraceHeaderLine.toString().getBytes());
	    	sw.flush();
	        for (StackTraceElementProxy step : stepArray) {
	          StringBuilder sb = new StringBuilder();
	          sb.append(stackTracePrefix).append(step);
	          sw.write(sb.toString().getBytes());
	          sw.flush();
	        }
	      } catch (IOException e) {
	        break;
	      }
	      tp = tp.getCause();
	    }
	  }

  public Layout<ILoggingEvent> buildLayout() {
    PatternLayout layout = new PatternLayout();
    layout.getInstanceConverterMap().put("syslogStart",
            SyslogStartConverter.class.getName());
    if (suffixPattern == null) {
      suffixPattern = DEFAULT_SUFFIX_PATTERN;
    }
    layout.setPattern(getPrefixPattern() + suffixPattern);
    layout.setContext(getContext());
    layout.start();
    return layout;
   }

  private void setupStackTraceLayout() {
    stackTraceLayout.getInstanceConverterMap().put("syslogStart",
            SyslogStartConverter.class.getName());

    stackTraceLayout.setPattern(getPrefixPattern() + stackTracePattern + " ");
    stackTraceLayout.setContext(getContext());
    stackTraceLayout.start();
  }

  private void setupStackTraceHeaderLayout() {
	  stackTraceHeaderLayout.getInstanceConverterMap().put("syslogStart",
	            SyslogStartConverter.class.getName());

	  stackTraceHeaderLayout.setPattern(getPrefixPattern() + stackTraceHeaderPattern + " ");
	  stackTraceHeaderLayout.setContext(getContext());
	  stackTraceHeaderLayout.start();
  }

  public boolean isThrowableExcluded() {
    return throwableExcluded;
  }

  /**
   * Setting throwableExcluded to true causes no Throwable's stack trace data to be sent to
   * the syslog daemon. By default, stack trace data is sent to syslog daemon.
   *
   * @param throwableExcluded
   * @since 1.0.4
   */
  public void setThrowableExcluded(boolean throwableExcluded) {
    this.throwableExcluded = throwableExcluded;
  }

  /**
   * See {@link #setStackTraceHeaderPattern(String).
   *
   * @return the stackTraceHeaderPattern
   * @since 1.0.x
   */
  public String getStackTraceHeaderPattern() {
    return stackTraceHeaderPattern;
  }

  /**
   * Stack trace lines are sent to the syslog server separately from the main message
   * Preceding each block of stack trace lines, there is a special header line for that stacktrace block
   * When stacktraces are nested, each nested block of stacktraces is headed by a special header line.
   * Nested block header lines differ slightly from the initial stracktrace header line, they contain the additional text "Caused by: "

   * For stacktrace header lines, the stackTraceHeaderPattern is used instead of {@link #suffixPattern} or {@link #stackTracePattern}.
   * The <b>stackTraceHeaderPattern</b> option allows specification of a separate format for the
   * non-standardized part of these header lines.
   *
   * @param stackTraceHeaderPattern
   * @since 1.0.x
   */
  public void setStackTraceHeaderPattern(String stackTraceHeaderPattern) {
    this.stackTraceHeaderPattern = stackTraceHeaderPattern;
  }

  /**
   * See {@link #setStackTracePattern(String).
   *
   * @return the stackTraceSuffixPattern
   * @since 1.0.4
   */
  public String getStackTracePattern() {
    return stackTracePattern;
  }

  /**
   * Stack trace lines are sent to the syslog server separately from the main message
   * For stack trace lines, the stackTracePattern is used instead of {@link #suffixPattern}.
   * The <b>stackTracePattern</b> option allows specification of a separately format for the
   * non-standardized part of stack trace lines.
   *
   * @param stackTracePattern
   * @since 1.0.4
   */
  public void setStackTracePattern(String stackTracePattern) {
    this.stackTracePattern = stackTracePattern;
  }
}