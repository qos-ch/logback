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

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.slf4j.LoggerFactory;
import org.slf4j.impl.StaticLoggerBinder;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.selector.ContextJNDISelector;
import ch.qos.logback.classic.selector.ContextSelector;

/**
 * A servlet filter that puts the environment-dependend
 * LoggerContext in a Threadlocal variable.
 * 
 * It removes it after the request is processed.
 *
 * To use it, add the following lines to a web.xml file
 * 
 * <filter>
 *   <filter-name>LoggerContextFilter</filter-name>
 *   <filter-class>
 *     ch.qos.userApp.LoggerContextFilter
 *   </filter-class>
 * </filter>
 * <filter-mapping>
 *   <filter-name>LoggerContextFilter</filter-name>
 *   <url-pattern>/*</url-pattern>
 * </filter-mapping>
 * 
 * @author S&eacute;bastien Pennec
 */
public class LoggerContextFilter implements Filter {

  public void destroy() {
    //do nothing
  }

  public void doFilter(ServletRequest request, ServletResponse response,
      FilterChain chain) throws IOException, ServletException {

    LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
    ContextSelector selector = StaticLoggerBinder.SINGLETON.getContextSelector();
    ContextJNDISelector sel = null;

    if (selector instanceof ContextJNDISelector) {
      sel = (ContextJNDISelector)selector;
      sel.setLocalContext(lc);
    }

    try {
      chain.doFilter(request, response);
    } finally {
      if (sel != null) {
        sel.removeLocalContext();
      }
    }
  }

  public void init(FilterConfig arg0) throws ServletException {
    //do nothing
  }
}
