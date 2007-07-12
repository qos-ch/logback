/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * 
 * Copyright (C) 1999-2006, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
 */

package chapter7;

import java.io.IOException;
import java.security.Principal;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.slf4j.MDC;

/**
 * A simple servlet filter that puts the username
 * found either in the Principle or as a session attribute
 * in the MDC.
 * 
 * The value is removed from the MDC once the request has been
 * fully processed.
 *
 * To be used, add the following lines to a web.xml file
 * 
 * <filter>
 *   <filter-name>User Servlet Filter</filter-name>
 *   <filter-class>
 *     chapter7.UserServletFilter
 *   </filter-class>
 * </filter>
 * <filter-mapping>
 *   <filter-name>User Servlet Filter</filter-name>
 *   <url-pattern>/*</url-pattern>
 * </filter-mapping>
 *
 * @author S&eacute;bastien Pennec
 */
public class UserServletFilter implements Filter {

  boolean userRegistered = false;
  
  private final String userKey = "username";
  
  public void destroy() {
  }

  public void doFilter(ServletRequest request, ServletResponse response,
      FilterChain chain) throws IOException, ServletException {

    HttpServletRequest req = (HttpServletRequest) request;
    Principal principal = req.getUserPrincipal();

    if (principal != null) {
      String username = principal.getName();
      registerUsername(username);
    } else {
      HttpSession session = req.getSession();
      String username = (String)session.getAttribute(userKey);
      registerUsername(username);
    }
    
    try {
      chain.doFilter(request, response);
    } finally {
      if (userRegistered) {
        MDC.remove(userKey);
      }
    }
  }

  public void init(FilterConfig arg0) throws ServletException {
  }
  
  private void registerUsername(String username) {
    if (username != null && username.trim().length() > 0) {
      MDC.put(userKey, username);
      userRegistered = true;
    }
  }

}
