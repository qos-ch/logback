/**
 * Logback: the generic, reliable, fast and flexible logging framework.
 * 
 * Copyright (C) 2000-2008, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
 */
package ch.qos.logback.classic;

import org.slf4j.LoggerFactory;

import ch.qos.logback.core.status.StatusManager;
import ch.qos.logback.core.status.ViewStatusMessagesServletBase;

public class ViewStatusMessageServlet extends ViewStatusMessagesServletBase {

  private static final long serialVersionUID = 443878494348593337L;

  //

  @Override
  protected StatusManager getStatusManager() {
    LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
    return lc.getStatusManager();
  }

  @Override
  protected String getPageTitle() {
    LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
    return "<h2>Status messages for LoggerContext named ["
        + lc.getName() + "]</h2>\r\n";
  }

}
