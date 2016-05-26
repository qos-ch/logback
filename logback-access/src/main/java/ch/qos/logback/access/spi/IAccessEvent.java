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

import ch.qos.logback.core.spi.DeferredProcessingAware;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

// Contributors:  Joern Huxhorn (see also bug #110)

/**
 * The Access module's internal representation of logging events. When the
 * logging component instance is called in the container to log then a
 * <code>AccessEvent</code> instance is created. This instance is passed
 * around to the different logback components.
 *
 * @author Ceki G&uuml;lc&uuml;
 * @author S&eacute;bastien Pennec
 * @author J&ouml;rn Huxhorn
 */
public interface IAccessEvent extends DeferredProcessingAware {

    String NA = "-";
    int SENTINEL = -1;

    /**
     * Returns the underlying HttpServletRequest. After serialization the returned
     * value will be null.
     *
     * @return
     */
    HttpServletRequest getRequest();

    /**
     * Returns the underlying HttpServletResponse. After serialization the returned
     * value will be null.
     *
     * @return
     */
    HttpServletResponse getResponse();

    /**
     * The number of milliseconds elapsed from 1/1/1970 until logging event was
     * created.
     */
    long getTimeStamp();

    /**
     * The time elapsed between receiving the request and logging it in milliseconds.
     */
    long getElapsedTime();

    /**
    * The number of seconds elapsed between receiving the request and logging it.
    */
    long getElapsedSeconds();

    String getRequestURI();

    /**
     * The first line of the request.
     */
    String getRequestURL();

    String getRemoteHost();

    String getRemoteUser();

    String getProtocol();

    String getMethod();

    String getServerName();

    String getSessionID();

    void setThreadName(String threadName);
    String getThreadName();
    
    String getQueryString();
    
    String getRemoteAddr();

    String getRequestHeader(String key);

    Enumeration getRequestHeaderNames();

    Map<String, String> getRequestHeaderMap();

    Map<String, String[]> getRequestParameterMap();

    String getAttribute(String key);

    String[] getRequestParameter(String key);

    String getCookie(String key);

    long getContentLength();

    int getStatusCode();

    String getRequestContent();

    String getResponseContent();

    int getLocalPort();

    ServerAdapter getServerAdapter();

    String getResponseHeader(String key);

    Map<String, String> getResponseHeaderMap();

    List<String> getResponseHeaderNameList();
}
