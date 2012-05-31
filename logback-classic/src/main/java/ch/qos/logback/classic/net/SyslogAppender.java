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
  static final public String DEFAULT_STACKTRACE_SUFFIX_PATTERN = ""+CoreConstants.TAB;

  PatternLayout prefixLayout = new PatternLayout();
  PatternLayout stacktraceLayout = new PatternLayout();

  public Layout<ILoggingEvent> buildLayout(String facilityStr) {
    String prefixPattern = "%syslogStart{" + facilityStr + "}%nopex";
    setupPrefixLayout(prefixPattern);
    setupStacktraceLayout(prefixPattern);
    return setupFullLayout(prefixPattern);
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
    ILoggingEvent event = (ILoggingEvent) eventObject;

    String layout = stacktraceLayout.doLayout(event);

    IThrowableProxy tp = event.getThrowableProxy();
    while (tp != null) {
      StackTraceElementProxy[] stepArray = tp.getStackTraceElementProxyArray();
      try {
        for (StackTraceElementProxy step : stepArray) {
          StringBuilder sb = new StringBuilder();
          sb.append(layout).append(step);
          sw.write(sb.toString().getBytes());
          sw.flush();
        }
      } catch (IOException e) {
        break;
      }
      tp = tp.getCause();
    }
  }

  private Layout<ILoggingEvent> setupFullLayout(String prefixPattern) {
	PatternLayout fullLayout = new PatternLayout();
	fullLayout.getInstanceConverterMap().put("syslogStart",
	  SyslogStartConverter.class.getName());
	if (suffixPattern == null) {
	  suffixPattern = DEFAULT_SUFFIX_PATTERN;
	}
	fullLayout.setPattern(prefixPattern + suffixPattern);
	fullLayout.setContext(getContext());
	fullLayout.start();
	return fullLayout;
  }

  private void setupStacktraceLayout(String prefixPattern) {
	stacktraceLayout.getInstanceConverterMap().put("syslogStart",
	  SyslogStartConverter.class.getName());
	if (stacktraceSuffixPattern == null) {
	  stacktraceSuffixPattern = DEFAULT_STACKTRACE_SUFFIX_PATTERN;
	}          
	stacktraceLayout.setPattern(prefixPattern + stacktraceSuffixPattern);
	stacktraceLayout.setContext(getContext());
	stacktraceLayout.start();
  }

  private void setupPrefixLayout(String prefixPattern) {
	prefixLayout.getInstanceConverterMap().put("syslogStart",
	  SyslogStartConverter.class.getName());
	prefixLayout.setPattern(prefixPattern);
    prefixLayout.setContext(getContext());
    prefixLayout.start();
  }

}