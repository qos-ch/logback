/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2024, QOS.ch. All rights reserved.
 * <p>
 * This program and the accompanying materials are dual-licensed under
 * either the terms of the Eclipse Public License v1.0 as published by
 * the Eclipse Foundation
 * <p>
 * or (per the licensee's choosing)
 * <p>
 * under the terms of the GNU Lesser General Public License version 2.1
 * as published by the Free Software Foundation.
 */
package ch.qos.logback.access.spi;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * Base class for ServerAdapter implementations where the server provides access to
 * HttpServletRequest and HttpServletResponse
 */
public abstract class ServletApiServerAdapter implements ServerAdapter {

    private final HttpServletRequest request;
    private final HttpServletResponse response;

    protected ServletApiServerAdapter(HttpServletRequest servletRequest, HttpServletResponse servletResponse) {
        this.request = servletRequest;
        this.response = servletResponse;
    }

    @Override
    public int getStatusCode() {
        return response.getStatus();
    }

    @Override
    public String getContentType() {
        return request.getContentType();
    }

    @Override
    public String getRequestURI() {
        return request.getRequestURI();
    }

    @Override
    public String getQueryString() {
        return request.getQueryString();
    }

    @Override
    public String getMethod() {
        return request.getMethod();
    }

    @Override
    public String getProtocol() {
        return request.getProtocol();
    }

    @Override
    public String getRemoteHost() {
        return request.getRemoteHost();
    }

    @Override
    public String getRemoteUser() {
        return request.getRemoteUser();
    }

    @Override
    public String getSessionId() {
        final HttpSession session = request.getSession(false);
        if (session != null) {
            return session.getId();
        }
        return null;
    }

    @Override
    public Object getSessionAttribute(String key) {
        final HttpSession session = request.getSession(false);
        if (session != null) {
            return session.getId();
        }
        return null;
    }

    @Override
    public String getServerName() {
        return request.getServerName();
    }

    @Override
    public String getRemoteAddr() {
        return request.getRemoteAddr();
    }

    @Override
    public Enumeration<String> getHeaderNames() {
        Enumeration<String> headerNames = request.getHeaderNames();
        if (headerNames == null) {
            headerNames = Collections.emptyEnumeration();
        }
        return headerNames;
    }

    @Override
    public String getHeader(String key) {
        return request.getHeader(key);
    }

    @Override
    public Map<String, String> getResponseHeaderMap() {
        Map<String, String> responseHeaderMap = new HashMap<>();
        for (String key : response.getHeaderNames()) {
            String value = response.getHeader(key);
            responseHeaderMap.put(key, value);
        }
        return responseHeaderMap;
    }

    @Override
    public Enumeration<String> getParameterNames() {
        try {
            Enumeration<String> parameterNames = request.getParameterNames();
            if (parameterNames != null) {
                return parameterNames;
            }
        } catch (Exception t) {
            // The use of HttpServletRequest.getParameterNames() can cause
            // a READ of the Request body content.  This can fail with various
            // Throwable failures depending on the state of the Request
            // at the time this method is called.
            // We don't want to fail the logging due to these types of requests
            t.printStackTrace();
        }
        return Collections.emptyEnumeration();
    }

    @Override
    public String[] getParameterValues(String key) {
        return request.getParameterValues(key);
    }

    @Override
    public Enumeration<String> getAttributeNames() {
        Enumeration<String> attributeNames = request.getAttributeNames();
        if (attributeNames == null) {
            attributeNames = Collections.emptyEnumeration();
        }
        return attributeNames;
    }

    @Override
    public Object getAttribute(String key) {
        return request.getAttribute(key);
    }

    @Override
    public Cookie[] getCookies() {
        return request.getCookies();
    }

    @Override
    public int getLocalPort() {
        return request.getLocalPort();
    }

    @Override
    public String getResponseType() {
        return response.getContentType();
    }

    @Override
    public HttpServletRequest getRequest() {
        return request;
    }

    @Override
    public HttpServletResponse getResponse() {
        return response;
    }
}
