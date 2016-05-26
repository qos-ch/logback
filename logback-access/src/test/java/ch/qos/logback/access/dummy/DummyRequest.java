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
package ch.qos.logback.access.dummy;

import ch.qos.logback.access.AccessConstants;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.*;

public class DummyRequest implements HttpServletRequest {

    public final static String DUMMY_CONTENT_STRING = "request contents";
    public final static byte[] DUMMY_CONTENT_BYTES = DUMMY_CONTENT_STRING.getBytes();

    public static final Map<String, Object> DUMMY_DEFAULT_ATTR_MAP = new HashMap<String, Object>();

    public static final String DUMMY_RESPONSE_CONTENT_STRING = "response contents";
    public static final byte[] DUMMY_RESPONSE_CONTENT_BYTES = DUMMY_RESPONSE_CONTENT_STRING.getBytes();

    Hashtable<String, String> headerNames;
    String uri;
    Map<String, Object> attributes;

    static {
        DUMMY_DEFAULT_ATTR_MAP.put("testKey", "testKey");
        DUMMY_DEFAULT_ATTR_MAP.put(AccessConstants.LB_INPUT_BUFFER, DUMMY_CONTENT_BYTES);
        DUMMY_DEFAULT_ATTR_MAP.put(AccessConstants.LB_OUTPUT_BUFFER, DUMMY_RESPONSE_CONTENT_BYTES);
    }

    public DummyRequest() {
        headerNames = new Hashtable<String, String>();
        headerNames.put("headerName1", "headerValue1");
        headerNames.put("headerName2", "headerValue2");

        attributes = new HashMap<String, Object>(DUMMY_DEFAULT_ATTR_MAP);
    }

    public String getAuthType() {
        return null;
    }

    public String getContextPath() {
        return null;
    }

    public Cookie[] getCookies() {
        Cookie cookie = new Cookie("testName", "testCookie");
        return new Cookie[] { cookie };
    }

    public long getDateHeader(String arg0) {
        return 0;
    }

    public String getHeader(String key) {
        return headerNames.get(key);
    }

    public Enumeration getHeaderNames() {
        return headerNames.keys();
    }

    public Enumeration getHeaders(String arg0) {
        return null;
    }

    public int getIntHeader(String arg0) {
        return 0;
    }

    public String getMethod() {
        return "testMethod";
    }

    public String getPathInfo() {
        return null;
    }

    public String getPathTranslated() {
        return null;
    }

    public String getQueryString() {
        return null;
    }

    public String getRemoteUser() {
        return "testUser";
    }

    public String getRequestURI() {
        return uri;
    }

    public StringBuffer getRequestURL() {
        return new StringBuffer(uri);
    }

    public String getRequestedSessionId() {
        return null;
    }

    public String getServletPath() {
        return null;
    }

    public HttpSession getSession() {
        return null;
    }

    public HttpSession getSession(boolean arg0) {
        return null;
    }

    public Principal getUserPrincipal() {
        return null;
    }

    public boolean isRequestedSessionIdFromCookie() {
        return false;
    }

    public boolean isRequestedSessionIdFromURL() {
        return false;
    }

    public boolean isRequestedSessionIdFromUrl() {
        return false;
    }

    public boolean authenticate(HttpServletResponse response) throws IOException, ServletException {
        return false; // To change body of implemented methods use File | Settings | File Templates.
    }

    public void login(String username, String password) throws ServletException {
        // To change body of implemented methods use File | Settings | File Templates.
    }

    public void logout() throws ServletException {
        // To change body of implemented methods use File | Settings | File Templates.
    }

    public Collection<Part> getParts() throws IOException, IllegalStateException, ServletException {
        return null; // To change body of implemented methods use File | Settings | File Templates.
    }

    public Part getPart(String name) throws IOException, IllegalStateException, ServletException {
        return null; // To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean isRequestedSessionIdValid() {
        return false;
    }

    public boolean isUserInRole(String arg0) {
        return false;
    }

    public Object getAttribute(String key) {
        return attributes.get(key);
    }

    public Enumeration getAttributeNames() {
        return Collections.enumeration(attributes.keySet());
    }

    public String getCharacterEncoding() {
        return null;
    }

    public int getContentLength() {
        return 0;
    }

    public String getContentType() {
        return null;
    }

    public ServletInputStream getInputStream() throws IOException {
        return null;
    }

    public String getLocalAddr() {
        return null;
    }

    public String getLocalName() {
        return null;
    }

    public int getLocalPort() {
        return 11;
    }

    public ServletContext getServletContext() {
        return null; // To change body of implemented methods use File | Settings | File Templates.
    }

    public AsyncContext startAsync() {
        return null; // To change body of implemented methods use File | Settings | File Templates.
    }

    public AsyncContext startAsync(ServletRequest servletRequest, ServletResponse servletResponse) {
        return null; // To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean isAsyncStarted() {
        return false; // To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean isAsyncSupported() {
        return false; // To change body of implemented methods use File | Settings | File Templates.
    }

    public AsyncContext getAsyncContext() {
        return null; // To change body of implemented methods use File | Settings | File Templates.
    }

    public DispatcherType getDispatcherType() {
        return null; // To change body of implemented methods use File | Settings | File Templates.
    }

    public Locale getLocale() {
        return null;
    }

    public Enumeration getLocales() {
        return null;
    }

    public String getParameter(String arg0) {
        return null;
    }

    public Map getParameterMap() {
        return null;
    }

    public Enumeration getParameterNames() {
        return null;
    }

    public String[] getParameterValues(String arg0) {
        return null;
    }

    public String getProtocol() {
        return "testProtocol";
    }

    public BufferedReader getReader() throws IOException {
        return null;
    }

    public String getRealPath(String arg0) {
        return null;
    }

    public String getRemoteAddr() {
        return "testRemoteAddress";
    }

    public String getRemoteHost() {
        return "testHost";
    }

    public int getRemotePort() {
        return 0;
    }

    public RequestDispatcher getRequestDispatcher(String arg0) {
        return null;
    }

    public String getScheme() {
        return null;
    }

    public String getServerName() {
        return "testServerName";
    }

    public int getServerPort() {
        return 0;
    }

    public boolean isSecure() {
        return false;
    }

    public void removeAttribute(String arg0) {
    }

    public void setAttribute(String name, Object value) {
        attributes.put(name, value);
    }

    public void setCharacterEncoding(String arg0) throws UnsupportedEncodingException {
    }

    public void setRequestUri(String uri) {
        this.uri = uri;
    }
}
