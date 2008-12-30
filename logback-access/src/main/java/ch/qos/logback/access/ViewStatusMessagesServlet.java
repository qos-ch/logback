/**
 * Logback: the generic, reliable, fast and flexible logging framework.
 * 
 * Copyright (C) 2000-2008, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
 */
package ch.qos.logback.access;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ch.qos.logback.core.status.StatusManager;
import ch.qos.logback.core.status.ViewStatusMessagesServletBase;

public class ViewStatusMessagesServlet extends ViewStatusMessagesServletBase {

  private static final long serialVersionUID = 443878494348593337L;

  @Override
  protected StatusManager getStatusManager(HttpServletRequest req, HttpServletResponse resp) {
    return (StatusManager) req.getAttribute(AccessConstants.LOGBACK_STATUS_MANAGER_KEY);
  }

  @Override
  protected String getPageTitle(HttpServletRequest req, HttpServletResponse resp) {
    return "<h2>Status messages for logback-access</h2>\r\n";
  }
}
