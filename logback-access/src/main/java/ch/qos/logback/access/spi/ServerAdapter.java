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
package ch.qos.logback.access.spi;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.Enumeration;
import java.util.Map;

/**
 * An interface to access server-specific methods from the server-independent
 * AccessEvent.
 *
 * @author Ceki G&uuml;lc&uuml;
 * @author S&eacute;bastien Pennec
 */
public interface ServerAdapter {

    long getRequestTimestamp();

    long getResponseContentLength();

    int getStatusCode();

    String getContentType();

    String getRequestURI();

    String getQueryString();

    String getMethod();

    String getProtocol();

    String getRemoteHost();

    String getRemoteUser();

    String getSessionId();

    Object getSessionAttribute(String key);

    int getLocalPort();

    String getServerName();

    String getRemoteAddr();

    Enumeration<String> getHeaderNames();

    String getHeader(String key);

    Map<String, String> getResponseHeaderMap();

    Enumeration<String> getParameterNames();

    String[] getParameterValues(String key);

    Enumeration<String> getAttributeNames();

    Object getAttribute(String key);

    Cookie[] getCookies();

    String getResponseType();

    /**
     * Will be null for some server implementations. Don't rely on it being available!
     */
    HttpServletRequest getRequest();

    /**
     * Will be null for some server implementations. Don't rely on it being available!
     */
    HttpServletResponse getResponse();
}
