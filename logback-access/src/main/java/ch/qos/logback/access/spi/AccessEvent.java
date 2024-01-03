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

import ch.qos.logback.access.AccessConstants;
import ch.qos.logback.access.pattern.AccessConverter;
import ch.qos.logback.access.servlet.Util;
import ch.qos.logback.core.Context;
import ch.qos.logback.core.spi.SequenceNumberGenerator;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

// Contributors:  Joern Huxhorn (see also bug #110)

/**
 * The Access module's internal representation of logging events. When the
 * logging component instance is called in the container to log then a
 * <code>AccessEvent</code> instance is created. This instance is passed around
 * to the different logback components.
 *
 * @author Ceki G&uuml;lc&uuml;
 * @author S&eacute;bastien Pennec
 */
public class AccessEvent implements Serializable, IAccessEvent {

    private static final String[] NA_STRING_ARRAY = new String[] { NA };

    private static final long serialVersionUID = 866718993618836343L;

    private static final String EMPTY = "";

    String queryString;
    String requestURI;
    String requestURL;
    String remoteHost;
    String remoteUser;
    String remoteAddr;
    String threadName;
    String protocol;
    String method;
    String serverName;
    String requestContent;
    String responseContent;
    String sessionID;
    long elapsedTime;

    Map<String, String> requestHeaderMap;
    Map<String, String[]> requestParameterMap;
    Map<String, String> responseHeaderMap;
    Map<String, Object> attributeMap;

    long contentLength = SENTINEL;
    int statusCode = SENTINEL;
    int localPort = SENTINEL;

    transient ServerAdapter serverAdapter;

    /**
     * The number of milliseconds elapsed from 1/1/1970 until logging event was
     * created.
     */
    private long timeStamp = 0;

    private long sequenceNumber = 0;

    public AccessEvent(Context context, ServerAdapter adapter) {
        this.timeStamp = System.currentTimeMillis();

        SequenceNumberGenerator sng = context.getSequenceNumberGenerator();
        if (sng != null) {
            this.sequenceNumber = sng.nextSequenceNumber();
        }
        this.serverAdapter = adapter;
        this.elapsedTime = calculateElapsedTime();
    }

    @Override
    public HttpServletRequest getRequest() {
        if (serverAdapter != null) {
            return serverAdapter.getRequest();
        }
        return null;
    }

    @Override
    public HttpServletResponse getResponse() {
        if (serverAdapter != null) {
            return serverAdapter.getResponse();
        }
        return null;
    }

    @Override
    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public long getSequenceNumber() {
        return sequenceNumber;
    }

    public void setSequenceNumber(long sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }

    /**
     * @param threadName The threadName to set.
     */
    public void setThreadName(String threadName) {
        this.threadName = threadName;
    }

    @Override
    public String getThreadName() {
        return threadName == null ? NA : threadName;
    }

    @Override
    public String getRequestURI() {
        if (requestURI == null) {
            if (serverAdapter != null) {
                requestURI = serverAdapter.getRequestURI();
            } else {
                requestURI = NA;
            }
        }
        return requestURI;
    }

    @Override
    public String getQueryString() {
        if (queryString == null) {
            if (serverAdapter != null) {
                StringBuilder buf = new StringBuilder();
                final String qStr = serverAdapter.getQueryString();
                if (qStr != null) {
                    buf.append(AccessConverter.QUESTION_CHAR);
                    buf.append(qStr);
                }
                queryString = buf.toString();
            } else {
                queryString = NA;
            }
        }
        return queryString;
    }

    /**
     * The first line of the request.
     */
    @Override
    public String getRequestURL() {
        if (requestURL == null) {
            if (serverAdapter != null) {
                StringBuilder buf = new StringBuilder();
                buf.append(serverAdapter.getMethod());
                buf.append(AccessConverter.SPACE_CHAR);
                buf.append(serverAdapter.getRequestURI());
                buf.append(getQueryString());
                buf.append(AccessConverter.SPACE_CHAR);
                buf.append(serverAdapter.getProtocol());
                requestURL = buf.toString();
            } else {
                requestURL = NA;
            }
        }
        return requestURL;
    }

    @Override
    public String getRemoteHost() {
        if (remoteHost == null) {
            if (serverAdapter != null) {
                // the underlying implementation of HttpServletRequest will
                // determine if remote lookup will be performed
                remoteHost = serverAdapter.getRemoteHost();
            }
            if (remoteHost == null) {
                remoteHost = NA;
            }
        }
        return remoteHost;
    }

    @Override
    public String getRemoteUser() {
        if (remoteUser == null) {
            if (serverAdapter != null) {
                remoteUser = serverAdapter.getRemoteUser();
            }
            if (remoteUser == null) {
                remoteUser = NA;
            }
        }
        return remoteUser;
    }

    @Override
    public String getProtocol() {
        if (protocol == null) {
            if (serverAdapter != null) {
                protocol = serverAdapter.getProtocol();
            }
            if (protocol == null) {
                protocol = NA;
            }
        }
        return protocol;
    }

    @Override
    public String getMethod() {
        if (method == null) {
            if (serverAdapter != null) {
                method = serverAdapter.getMethod();
            }
            if (method == null) {
                method = NA;
            }
        }
        return method;
    }

    @Override
    public String getSessionID() {
        if (sessionID == null) {
            if (serverAdapter != null) {
                sessionID = serverAdapter.getSessionId();
            }
            if (sessionID == null) {
                sessionID = NA;
            }
        }
        return sessionID;
    }

    @Override
    public String getServerName() {
        if (serverName == null) {
            if (serverAdapter != null) {
                serverName = serverAdapter.getServerName();
            }
            if (serverName == null) {
                serverName = NA;
            }
        }
        return serverName;
    }

    @Override
    public String getRemoteAddr() {
        if (remoteAddr == null) {
            if (serverAdapter != null) {
                remoteAddr = serverAdapter.getRemoteAddr();
            } else {
                remoteAddr = NA;
            }
        }
        return remoteAddr;
    }

    @Override
    public String getRequestHeader(String key) {
        key = key.toLowerCase();
        Map<String, String> headerMap = getRequestHeaderMap();
        String result = headerMap.get(key);
        if (result == null) {
            result = NA;
        }
        return result;
    }

    @Override
    public Enumeration<String> getRequestHeaderNames() {
        return Collections.enumeration(getRequestHeaderMap().keySet());
    }

    @Override
    public Map<String, String> getRequestHeaderMap() {
        if (requestHeaderMap == null) {
            // according to RFC 2616 header names are case-insensitive
            // latest versions of Tomcat return header names in lower-case
            requestHeaderMap = new TreeMap<String, String>(String.CASE_INSENSITIVE_ORDER);
            Enumeration<String> e = serverAdapter.getHeaderNames();
            if (e != null) {
                while (e.hasMoreElements()) {
                    String key = e.nextElement();
                    requestHeaderMap.put(key, serverAdapter.getHeader(key));
                }
            }
        }
        return requestHeaderMap;
    }

    @Override
    public Map<String, String[]> getRequestParameterMap() {
        if (requestParameterMap == null) {
            requestParameterMap = new HashMap<String, String[]>();
            Enumeration<String> e = serverAdapter.getParameterNames();
            while (e.hasMoreElements()) {
                String key = e.nextElement();
                requestParameterMap.put(key, serverAdapter.getParameterValues(key));
            }
        }
        return requestParameterMap;
    }

    @Override
    public String getAttribute(String key) {
        Object value = null;
        if (attributeMap != null) {
            // Event was prepared for deferred processing so we have a copy of attribute map
            // and must use that copy
            value = attributeMap.get(key);
        } else if (serverAdapter != null) {
            // We have original request so take attribute from it
            value = serverAdapter.getAttribute(key);
        }

        return value != null ? value.toString() : NA;
    }

    private void copyAttributeMap() {

        if (serverAdapter == null) {
            return;
        }

        // attributeMap has been copied already. See also LOGBACK-1189
        if (attributeMap != null) {
            return;
        }

        attributeMap = new HashMap<String, Object>();

        Enumeration<String> names = serverAdapter.getAttributeNames();
        while (names.hasMoreElements()) {
            String name = names.nextElement();

            Object value = serverAdapter.getAttribute(name);
            if (shouldCopyAttribute(name, value)) {
                attributeMap.put(name, value);
            }
        }
    }

    private boolean shouldCopyAttribute(String name, Object value) {
        if (AccessConstants.LB_INPUT_BUFFER.equals(name) || AccessConstants.LB_OUTPUT_BUFFER.equals(name)) {
            // Do not copy attributes used by logback internally - these are available via
            // other getters anyway
            return false;
        } else if (value == null) {
            // No reasons to copy nulls - Map.get() will return null for missing keys and
            // the list of attribute
            // names is not available through IAccessEvent
            return false;
        } else {
            // Only copy what is serializable
            return value instanceof Serializable;
        }
    }

    @Override
    public String[] getRequestParameter(String key) {
        Map<String, String[]> parameterMap = getRequestParameterMap();
        String[] value = parameterMap.get(key);
        return (value != null) ? value : NA_STRING_ARRAY;
    }

    @Override
    public String getCookie(String key) {
        if (serverAdapter != null) {
            Cookie[] cookieArray = serverAdapter.getCookies();
            if (cookieArray == null) {
                return NA;
            }

            for (Cookie cookie : cookieArray) {
                if (key.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return NA;
    }

    @Override
    public long getContentLength() {
        if (contentLength == SENTINEL && serverAdapter != null) {
            contentLength = serverAdapter.getResponseContentLength();
        }
        return contentLength;
    }

    public int getStatusCode() {
        if (statusCode == SENTINEL && serverAdapter != null) {
            statusCode = serverAdapter.getStatusCode();
        }
        return statusCode;
    }

    public long getElapsedSeconds() {
        return elapsedTime < 0 ? elapsedTime : elapsedTime / 1000;
    }

    public long getElapsedTime() {
        return elapsedTime;
    }

    private long calculateElapsedTime() {
        if (serverAdapter == null || serverAdapter.getRequestTimestamp() < 0) {
            return -1;
        }
        return getTimeStamp() - serverAdapter.getRequestTimestamp();
    }

    public String getRequestContent() {
        if (requestContent != null) {
            return requestContent;
        }

        if (serverAdapter != null) {
            if (Util.isFormUrlEncoded(serverAdapter.getContentType(), serverAdapter.getMethod())) {
                StringBuilder buf = new StringBuilder();
                Enumeration<String> paramEnumeration = serverAdapter.getParameterNames();

                // example: id=1234&user=cgu
                // number=1233&x=1
                int count = 0;
                while (paramEnumeration.hasMoreElements()) {
                    String key = paramEnumeration.nextElement();
                    if (count++ != 0) {
                        buf.append("&");
                    }
                    buf.append(key);
                    buf.append("=");
                    String val = serverAdapter.getParameterValues(key)[0];
                    if (val != null) {
                        buf.append(val);
                    } else {
                        buf.append("");
                    }
                }
                requestContent = buf.toString();
            } else {
                // retrieve the byte array placed by TeeFilter
                byte[] inputBuffer = (byte[]) serverAdapter.getAttribute(AccessConstants.LB_INPUT_BUFFER);
                if (inputBuffer != null) {
                    requestContent = new String(inputBuffer);
                }
            }
        }

        if (requestContent == null || requestContent.isEmpty()) {
            requestContent = EMPTY;
        }

        return requestContent;
    }

    public String getResponseContent() {
        if (responseContent != null) {
            return responseContent;
        }

        if (serverAdapter != null) {
            if (Util.isImageResponse(serverAdapter.getResponseType())) {
                responseContent = "[IMAGE CONTENTS SUPPRESSED]";
            } else {
                // retrieve the byte array previously placed by TeeFilter
                byte[] outputBuffer = (byte[]) serverAdapter.getAttribute(AccessConstants.LB_OUTPUT_BUFFER);

                if (outputBuffer != null) {
                    responseContent = new String(outputBuffer);
                }
            }
        }

        if (responseContent == null || responseContent.isEmpty()) {
            responseContent = EMPTY;
        }

        return responseContent;
    }

    public int getLocalPort() {
        if (localPort == SENTINEL && serverAdapter != null) {
            localPort = serverAdapter.getLocalPort();
        }
        return localPort;
    }

    @Override
    public ServerAdapter getServerAdapter() {
        return serverAdapter;
    }

    public String getResponseHeader(String key) {
        return getResponseHeaderMap().get(key);
    }

    public Map<String, String> getResponseHeaderMap() {
        if (responseHeaderMap == null) {
            responseHeaderMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
            responseHeaderMap.putAll(serverAdapter.getResponseHeaderMap());
        }
        return responseHeaderMap;
    }

    public List<String> getResponseHeaderNameList() {
        return new ArrayList<String>(getResponseHeaderMap().keySet());
    }

    @Override
    public Object getSessionAttribute(String key) {
        return serverAdapter.getSessionAttribute(key);
    }

    public void prepareForDeferredProcessing() {
        getRequestHeaderMap();
        getRequestParameterMap();
        getResponseHeaderMap();
        getLocalPort();
        getMethod();
        getProtocol();
        getRemoteAddr();
        getRemoteHost();
        getRemoteUser();
        getRequestURI();
        getRequestURL();
        getServerName();
        getTimeStamp();
        getElapsedTime();

        getStatusCode();
        getContentLength();
        getRequestContent();
        getResponseContent();

        copyAttributeMap();
    }
}
