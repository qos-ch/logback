/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2015, QOS.ch. All rights reserved.
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
package ch.qos.logback.classic.selector.servlet;

import java.io.IOException;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.selector.ContextJNDISelector;
import ch.qos.logback.classic.selector.ContextSelector;
import ch.qos.logback.classic.util.ContextSelectorStaticBinder;

/**
 * A servlet filter that puts the environment dependent LoggerContext in a
 * ThreadLocal variable, removing it after the request is processed.
 * 
 * <p>To use it, add the following lines to a web.xml file
 *  
 * <filter>
 *   <filter-name>LoggerContextFilter</filter-name>
 *   <filter-class>
 *     ch.qos.logback.classic.selector.servlet.LoggerContextFilter
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
        // do nothing
    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
        ContextSelector selector = ContextSelectorStaticBinder.getSingleton().getContextSelector();
        ContextJNDISelector sel = null;

        if (selector instanceof ContextJNDISelector) {
            sel = (ContextJNDISelector) selector;
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
        // do nothing
    }
}
