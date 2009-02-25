/**
 * Logback: the generic, reliable, fast and flexible logging framework.
 * 
 * Copyright (C) 2000-2008, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
 */
package ch.qos.logback.classic.html;

import static ch.qos.logback.core.CoreConstants.LINE_SEPARATOR;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.ThrowableDataPoint;
import ch.qos.logback.classic.spi.ThrowableProxy;
import ch.qos.logback.core.helpers.Transform;
import ch.qos.logback.core.html.IThrowableRenderer;

public class DefaultThrowableRenderer implements IThrowableRenderer {
  
  static final String TRACE_PREFIX = "<br />&nbsp;&nbsp;&nbsp;&nbsp;";
  
  Throwable throwable;
  
  public DefaultThrowableRenderer() {
    
  }
  
  public void setThrowable(Throwable t) {
    this.throwable = t;
  }
  
  public void render(StringBuilder sbuf, ThrowableDataPoint[] tdpArray) {
    if (tdpArray != null) {
      int len = tdpArray.length;
      if (len == 0) {
        return;
      }
      sbuf.append("<tr><td class=\"Exception\" colspan=\"6\">");
      sbuf.append(Transform.escapeTags(tdpArray[0].toString()));
      sbuf.append(LINE_SEPARATOR);
      for (int i = 1; i < len; i++) {
        sbuf.append(TRACE_PREFIX);
        sbuf.append(Transform.escapeTags(tdpArray[i].toString()));
        sbuf.append(LINE_SEPARATOR);
      }
      sbuf.append("</td></tr>");
    }
  }
  
  public void render(StringBuilder sbuf, Object eventObject) {
    ILoggingEvent event = (ILoggingEvent)eventObject;
    ThrowableProxy tp = event.getThrowableProxy();
    if (tp != null) {
      render(sbuf, tp.getThrowableDataPointArray());
    }
  }
}
