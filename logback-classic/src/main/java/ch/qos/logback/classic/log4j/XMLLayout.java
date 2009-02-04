/**
 * Logback: the generic, reliable, fast and flexible logging framework.
 * 
 * Copyright (C) 2000-2009, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
 */
package ch.qos.logback.classic.log4j;

import ch.qos.logback.classic.spi.CallerData;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.classic.spi.ThrowableDataPoint;
import ch.qos.logback.classic.spi.ThrowableProxy;
import ch.qos.logback.core.LayoutBase;
import ch.qos.logback.core.helpers.Transform;

// Code is based on revision 309623 of org.apache.log4j.xml.XMLLayout dated "Wed
// Jul 31 09:25:14 2002 UTC" as authored by Ceki Gulcu.
// See also http://tinyurl.com/dch9mr

/**
 * 
 * Generates log4j.dtd compliant XML documents.
 * 
 * 
 * @author Ceki G&uuml;lc&uuml;
 * @author Thorbjørn Ravn Andersen
 */
public class XMLLayout extends LayoutBase<LoggingEvent> {

  private final int DEFAULT_SIZE = 256;
  private final int UPPER_LIMIT = 2048;

  private StringBuffer buf = new StringBuffer(DEFAULT_SIZE);
  private boolean locationInfo = false;

  /**
   * The <b>LocationInfo</b> option takes a boolean value. By default, it is
   * set to false which means there will be no location information output by
   * this layout. If the the option is set to true, then the file name and line
   * number of the statement at the origin of the log statement will be output.
   * 
   * <p>If you are embedding this layout within an {@link
   * org.apache.log4j.net.SMTPAppender} then make sure to set the
   * <b>LocationInfo</b> option of that appender as well.
   */
  public void setLocationInfo(boolean flag) {
    locationInfo = flag;
  }

  /**
   * Returns the current value of the <b>LocationInfo</b> option.
   */
  public boolean getLocationInfo() {
    return locationInfo;
  }

  /** No options to activate. */
  public void activateOptions() {
  }

  /**
   * Formats a {@link LoggingEvent} in conformity with the log4j.dtd.
   */
  public String doLayout(LoggingEvent event) {
  
    // Reset working buffer. If the buffer is too large, then we need a new
    // one in order to avoid the penalty of creating a large array.
    if (buf.capacity() > UPPER_LIMIT) {
      buf = new StringBuffer(DEFAULT_SIZE);
    } else {
      buf.setLength(0);
    }

    // We yield to the \r\n heresy.

    buf.append("<log4j:event logger=\"");
    buf.append(event.getLoggerRemoteView().getName());
    buf.append("\" timestamp=\"");
    buf.append(event.getTimeStamp());
    buf.append("\" level=\"");
    buf.append(event.getLevel());
    buf.append("\" thread=\"");
    buf.append(event.getThreadName());
    buf.append("\">\r\n");

    buf.append("<log4j:message><![CDATA[");
    // Append the rendered message. Also make sure to escape any
    // existing CDATA sections.
    Transform.appendEscapingCDATA(buf, event.getFormattedMessage());
    buf.append("]]></log4j:message>\r\n");

    // logback does not support NDC
    // String ndc = event.getNDC();
    
    ThrowableProxy tp = event.getThrowableProxy();
    
    if (tp != null) {
      buf.append("<log4j:throwable><![CDATA[");
      ThrowableDataPoint[] tdpArray = tp.getThrowableDataPointArray();
      for (ThrowableDataPoint tdp: tdpArray) {
        buf.append(tdp.toString());
        buf.append("\r\n");
      }
      buf.append("]]></log4j:throwable>\r\n");
    }

    if (locationInfo) {
      CallerData[] callerDataArray = event.getCallerData();
      if (callerDataArray != null && callerDataArray.length > 0) {
        CallerData  immediateCallerData = callerDataArray[0];
        buf.append("<log4j:locationInfo class=\"");
        buf.append(immediateCallerData.getClassName());
        buf.append("\" method=\"");
        buf.append(Transform.escapeTags(immediateCallerData.getMethodName()));
        buf.append("\" file=\"");
        buf.append(immediateCallerData.getFileName());
        buf.append("\" line=\"");
        buf.append(immediateCallerData.getLineNumber());
        buf.append("\"/>\r\n");
      }
    }

    buf.append("</log4j:event>\r\n\r\n");

    return buf.toString();
  }

}
