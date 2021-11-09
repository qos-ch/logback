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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Map;

import ch.qos.logback.access.AccessConstants;
import jakarta.servlet.AsyncContext;
import jakarta.servlet.DispatcherType;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpUpgradeHandler;
import jakarta.servlet.http.Part;

public class DummyRequest implements HttpServletRequest {

    public final static String DUMMY_CONTENT_STRING = "request contents";
    public final static byte[] DUMMY_CONTENT_BYTES = DUMMY_CONTENT_STRING.getBytes();

    public static final Map<String, Object> DUMMY_DEFAULT_ATTR_MAP = new HashMap<>();

    public static final String DUMMY_RESPONSE_CONTENT_STRING = "response contents";
    public static final byte[] DUMMY_RESPONSE_CONTENT_BYTES = DUMMY_RESPONSE_CONTENT_STRING.getBytes();

    Hashtable<String, String> headerMap;
    Hashtable<String, String[]> parameterMap;

    String uri;
    Map<String, Object> attributes;

    static {
        DUMMY_DEFAULT_ATTR_MAP.put("testKey", "testKey");
        DUMMY_DEFAULT_ATTR_MAP.put(AccessConstants.LB_INPUT_BUFFER, DUMMY_CONTENT_BYTES);
        DUMMY_DEFAULT_ATTR_MAP.put(AccessConstants.LB_OUTPUT_BUFFER, DUMMY_RESPONSE_CONTENT_BYTES);
    }

    public DummyRequest() {
        headerMap = new Hashtable<>();
        headerMap.put("headerName1", "headerValue1");
        headerMap.put("headerName2", "headerValue2");

        parameterMap =  new Hashtable<>();
        parameterMap.put("param1", new String[] {"value1"});

        attributes = new HashMap<>(DUMMY_DEFAULT_ATTR_MAP);
    }

    @Override
    public String getAuthType() {
        return null;
    }

    @Override
    public String getContextPath() {
        return null;
    }

    @Override
    public Cookie[] getCookies() {
        final Cookie cookie = new Cookie("testName", "testCookie");
        return new Cookie[] { cookie };
    }

    @Override
    public long getDateHeader(final String arg0) {
        return 0;
    }

    @Override
    public String getHeader(final String key) {
        return headerMap.get(key);
    }


    @Override
    public Enumeration<String>  getHeaderNames() {
        return headerMap.keys();
    }

    @Override
    public Enumeration<String> getHeaders(final String arg) {
        return null;
    }

    public Map<String, String> getHeaders() {
        return headerMap;
    }


    @Override
    public int getIntHeader(final String arg0) {
        return 0;
    }

    @Override
    public String getMethod() {
        return "testMethod";
    }

    @Override
    public String getPathInfo() {
        return null;
    }

    @Override
    public String getPathTranslated() {
        return null;
    }

    @Override
    public String getQueryString() {
        return null;
    }

    @Override
    public String getRemoteUser() {
        return "testUser";
    }

    @Override
    public String getRequestURI() {
        return uri;
    }

    @Override
    public StringBuffer getRequestURL() {
        return new StringBuffer(uri);
    }

    @Override
    public String getRequestedSessionId() {
        return null;
    }

    @Override
    public String getServletPath() {
        return null;
    }

    @Override
    public HttpSession getSession() {
        return null;
    }

    @Override
    public HttpSession getSession(final boolean arg0) {
        return null;
    }

    @Override
    public Principal getUserPrincipal() {
        return null;
    }

    @Override
    public boolean isRequestedSessionIdFromCookie() {
        return false;
    }

    @Override
    public boolean isRequestedSessionIdFromURL() {
        return false;
    }

    @Override
    public boolean isRequestedSessionIdFromUrl() {
        return false;
    }

    @Override
    public boolean authenticate(final HttpServletResponse response) throws IOException, ServletException {
        return false; // To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void login(final String username, final String password) throws ServletException {
        // To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void logout() throws ServletException {
        // To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Collection<Part> getParts() throws IOException, IllegalStateException, ServletException {
        return null; // To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Part getPart(final String name) throws IOException, IllegalStateException, ServletException {
        return null; // To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean isRequestedSessionIdValid() {
        return false;
    }

    @Override
    public boolean isUserInRole(final String arg0) {
        return false;
    }

    @Override
    public Object getAttribute(final String key) {
        return attributes.get(key);
    }

    @Override
    public Enumeration<String> getAttributeNames() {
        return Collections.enumeration(attributes.keySet());
    }

    @Override
    public String getCharacterEncoding() {
        return null;
    }

    @Override
    public int getContentLength() {
        return 0;
    }

    @Override
    public String getContentType() {
        return null;
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        return null;
    }

    @Override
    public String getLocalAddr() {
        return null;
    }

    @Override
    public String getLocalName() {
        return null;
    }

    @Override
    public int getLocalPort() {
        return 11;
    }

    @Override
    public ServletContext getServletContext() {
        return null; // To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public AsyncContext startAsync() {
        return null; // To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public AsyncContext startAsync(final ServletRequest servletRequest, final ServletResponse servletResponse) {
        return null; // To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean isAsyncStarted() {
        return false; // To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean isAsyncSupported() {
        return false; // To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public AsyncContext getAsyncContext() {
        return null; // To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public DispatcherType getDispatcherType() {
        return null; // To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Locale getLocale() {
        return null;
    }

    @Override
    public Enumeration<Locale> getLocales() {
        return null;
    }

    @Override
    public String getParameter(final String arg) {
        final String[] stringArray = parameterMap.get(arg);
        if(stringArray == null || stringArray.length == 0) {
            return null;
        }
        return stringArray[0];
    }

    @Override
    public Map<String, String[]> getParameterMap() {
        return parameterMap;
    }

    @Override
    public Enumeration<String> getParameterNames() {
        return parameterMap.keys();
        //eturn Collections.enumeration(parameterMap.keySet());
    }

    @Override
    public String[] getParameterValues(final String arg) {
        return parameterMap.get(arg);
    }

    @Override
    public String getProtocol() {
        return "testProtocol";
    }

    @Override
    public BufferedReader getReader() throws IOException {
        return null;
    }

    @Override
    public String getRealPath(final String arg0) {
        return null;
    }

    @Override
    public String getRemoteAddr() {
        return "testRemoteAddress";
    }

    @Override
    public String getRemoteHost() {
        return "testHost";
    }

    @Override
    public int getRemotePort() {
        return 0;
    }

    @Override
    public RequestDispatcher getRequestDispatcher(final String arg0) {
        return null;
    }

    @Override
    public String getScheme() {
        return null;
    }

    @Override
    public String getServerName() {
        return "testServerName";
    }

    @Override
    public int getServerPort() {
        return 0;
    }

    @Override
    public boolean isSecure() {
        return false;
    }

    @Override
    public void removeAttribute(final String arg0) {
    }

    @Override
    public void setAttribute(final String name, final Object value) {
        attributes.put(name, value);
    }

    @Override
    public void setCharacterEncoding(final String arg0) throws UnsupportedEncodingException {
    }

    public void setRequestUri(final String uri) {
        this.uri = uri;
    }

    @Override
    public long getContentLengthLong() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public String changeSessionId() {
        return null;
    }

    @Override
    public <T extends HttpUpgradeHandler> T upgrade(final Class<T> httpUpgradeHandlerClass) throws IOException, ServletException {
        return null;
    }
}
