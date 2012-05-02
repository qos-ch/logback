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
import ch.qos.logback.classic.spi.ThrowableProxyUtil;
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

  PatternLayout prefixLayout = new PatternLayout();

  public Layout<ILoggingEvent> buildLayout(String facilityStr) {
    String prefixPattern = "%syslogStart{" + facilityStr + "}%nopex{}";

    prefixLayout.getInstanceConverterMap().put("syslogStart",
        SyslogStartConverter.class.getName());
    prefixLayout.setPattern(prefixPattern);
    prefixLayout.setContext(getContext());
    prefixLayout.start();

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
    IThrowableProxy tp = event.getThrowableProxy();
    if (tp == null)
      return;

    // Although line-separator-splitting is not very pretty, this
    // avoids duplicating quite complex logic from ThrowableProxyUtil
    // (such as omitting common stack frames, dealing with suppressed
    // exceptions, etc).

    String prefix = prefixLayout.doLayout(event);
    String lines[] = ThrowableProxyUtil.asString(tp).split(CoreConstants.LINE_SEPARATOR);
    for (String line : lines) {
      try {
	StringBuilder sb = new StringBuilder();
        sb.append(prefix).append(line);
        sw.write(sb.toString().getBytes());
        sw.flush();	
      } catch (IOException e) {
        break;
      }
    }
  }
}
