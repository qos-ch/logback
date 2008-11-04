/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * 
 * Copyright (C) 1999-2006, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
 */

package ch.qos.logback.classic.selector.servlet;

import static ch.qos.logback.classic.ClassicGlobal.JNDI_CONTEXT_NAME;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.slf4j.Logger;
import org.slf4j.impl.StaticLoggerBinder;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.selector.ContextSelector;
import ch.qos.logback.classic.util.JNDIUtil;

public class ContextDetachingSCL implements ServletContextListener {

  public void contextDestroyed(ServletContextEvent servletContextEvent) {
    String loggerContextName = null;
    
    try {
      Context ctx = JNDIUtil.getInitialContext();
      loggerContextName = (String) JNDIUtil.lookup(ctx, JNDI_CONTEXT_NAME);
    } catch (NamingException ne) {
    }
    
    if (loggerContextName != null) {
      System.out.println("About to detach context named " + loggerContextName);
      
      ContextSelector selector = StaticLoggerBinder.SINGLETON.getContextSelector();
      LoggerContext context = selector.detachLoggerContext(loggerContextName);
      if (context != null) {
        Logger logger = context.getLogger(LoggerContext.ROOT_NAME);
        logger.warn("Shutting down context " + loggerContextName);
        context.reset();
      } else {
        System.out.println("No context named " + loggerContextName + " was found.");
      }
    }
  }

  public void contextInitialized(ServletContextEvent arg0) {
    // do nothing
  }

}
