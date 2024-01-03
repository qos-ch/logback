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
package ch.qos.logback.access.jetty;

import ch.qos.logback.access.spi.ServerAdapter;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.eclipse.jetty.http.HttpField;
import org.eclipse.jetty.http.HttpFields;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.server.Session;
import org.eclipse.jetty.util.Fields;

import java.util.Collections;
import java.util.Enumeration;
import java.util.Map;
import java.util.stream.Collectors;

public class JettyNoServletApiServerAdapter implements ServerAdapter {

    private final Request request;
    private final Response response;

    public JettyNoServletApiServerAdapter(Request jettyRequest, Response jettyResponse) {
        this.request = jettyRequest;
        this.response = jettyResponse;
    }

    @Override
    public long getResponseContentLength() {
        return Response.getContentBytesWritten(response);
    }

    @Override
    public int getStatusCode() {
        return Response.getOriginalResponse(response).getStatus();
    }

    @Override
    public String getContentType() {
        return request.getHeaders().get(HttpHeader.CONTENT_TYPE);
    }

    @Override
    public String getRequestURI() {
        return request.getHttpURI().getCanonicalPath();
    }

    @Override
    public String getQueryString() {
        return request.getHttpURI().getQuery();
    }

    @Override
    public String getMethod() {
        return request.getMethod();
    }

    @Override
    public String getProtocol() {
        return request.getConnectionMetaData().getProtocol();
    }

    @Override
    public String getRemoteHost() {
        return Request.getRemoteAddr(request);
    }

    @Override
    public String getRemoteUser() {
        return request.getHttpURI().getUser();
    }

    @Override
    public String getSessionId() {
        Session session = request.getSession(false);
        if (session != null) {
            return session.getId();
        }
        return null;
    }

    @Override
    public Object getSessionAttribute(String key) {
        Session session = request.getSession(false);
        if (session != null) {
            return session.getAttribute(key);
        }
        return null;
    }

    @Override
    public int getLocalPort() {
        return Request.getLocalPort(request);
    }

    @Override
    public String getServerName() {
        return Request.getServerName(request);
    }

    @Override
    public String getRemoteAddr() {
        return Request.getRemoteAddr(request);
    }

    @Override
    public Enumeration<String> getHeaderNames() {
        return Collections.enumeration(request.getHeaders().stream().map(HttpField::getName).collect(Collectors.toList()));
    }

    @Override
    public String getHeader(String key) {
        return request.getHeaders().get(key);
    }

    @Override
    public Map<String, String> getResponseHeaderMap() {
        HttpFields headers = response.getHeaders().asImmutable();
        return headers.stream().collect(Collectors.toMap(HttpField::getName, HttpField::getValue));
    }

    @Override
    public Enumeration<String> getParameterNames() {
        try {
            return Collections.enumeration(Request.getParameters(request).getNames());
        } catch (Exception e) {
            // We don't want to fail the logging due to exceptions on getting the parameters
            e.printStackTrace();
            return Collections.emptyEnumeration();
        }
    }

    @Override
    public String[] getParameterValues(String key) {
        try {
            return Request.getParameters(request).get(key).getValues().toArray(new String[0]);
        } catch (Exception e) {
            // We don't want to fail the logging due to exceptions on getting the parameters
            e.printStackTrace();
            return new String[0];
        }
    }

    @Override
    public Enumeration<String> getAttributeNames() {
        return Collections.enumeration(request.getConnectionMetaData().getAttributeNameSet());
    }

    @Override
    public Object getAttribute(String key) {
        return request.getConnectionMetaData().getAttribute(key);
    }

    @Override
    public Cookie[] getCookies() {
        return Request.getCookies(request).stream().map(cookie -> new Cookie(cookie.getName(), cookie.getValue())).toArray(Cookie[]::new);
    }

    @Override
    public String getResponseType() {
        return response.getHeaders().asImmutable().get(HttpHeader.CONTENT_TYPE);
    }

    @Override
    public long getRequestTimestamp() {
        return Request.getTimeStamp(request);
    }

    @Override
    public HttpServletRequest getRequest() {
        // not available for recent jetty versions
        return null;
    }

    @Override
    public HttpServletResponse getResponse() {
        // not available for recent jetty versions
        return null;
    }
}
