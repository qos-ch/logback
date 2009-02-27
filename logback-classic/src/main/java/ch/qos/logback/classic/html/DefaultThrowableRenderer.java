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

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.ThrowableDataPoint;
import ch.qos.logback.classic.spi.ThrowableProxyUtil;
import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.helpers.Transform;
import ch.qos.logback.core.html.IThrowableRenderer;

public class DefaultThrowableRenderer implements IThrowableRenderer<ILoggingEvent> {
  
  static final String TRACE_PREFIX = "<br />&nbsp;&nbsp;&nbsp;&nbsp;";
  
  public DefaultThrowableRenderer() {
  }
  
  void render(StringBuilder sbuf, IThrowableProxy tp) {
    StringBuilder firstLine = new StringBuilder();
    ThrowableProxyUtil.printFirstLine(firstLine, tp);
    sbuf.append(Transform.escapeTags(firstLine.toString()));
    
    int commonFrames = tp.getCommonFrames();
    ThrowableDataPoint[] tdpa = tp.getThrowableDataPointArray();
    
    for (int i = 0; i < tdpa.length - commonFrames; i++) {
      ThrowableDataPoint tdp = tdpa[i];
      sbuf.append(TRACE_PREFIX);
      sbuf.append(Transform.escapeTags(tdp.toString()));
      sbuf.append(CoreConstants.LINE_SEPARATOR);
    }
    
    if (commonFrames > 0) {
      sbuf.append(TRACE_PREFIX);
      sbuf.append("\t... " + commonFrames).append(" common frames omitted")
          .append(CoreConstants.LINE_SEPARATOR);
    }
  }
  
  public void render(StringBuilder sbuf, ILoggingEvent event) {
    IThrowableProxy tp = event.getThrowableProxy();
    sbuf.append("<tr><td class=\"Exception\" colspan=\"6\">");
    while(tp != null) {
      render(sbuf, tp);
      tp = tp.getCause();
    }
    sbuf.append("</td></tr>");
  }
}
