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

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;

// Contributors:  Joern Huxhorn (see also bug #110)

/**
 * The Access module's internal representation of logging events. When the
 * logging component instance is called in the container to log then a
 * <code>AccessEvent</code> instance is created. This instance is passed
 * around to the different logback components.
 *
 * @author Ceki G&uuml;lc&uuml;
 * @author S&eacute;bastien Pennec
 */
public class AccessEvent implements Serializable, IAccessEvent {

    private static final String[] NA_STRING_ARRAY = new String[] { NA };

    private static final long serialVersionUID = 866718993618836343L;

    private static final String EMPTY = "";

    private transient final HttpServletRequest httpRequest;
    private transient final HttpServletResponse httpResponse;

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

    public AccessEvent(Context context, HttpServletRequest httpRequest, HttpServletResponse httpResponse, ServerAdapter adapter) {
        this.httpRequest = httpRequest;
        this.httpResponse = httpResponse;
        this.timeStamp = System.currentTimeMillis();

        SequenceNumberGenerator sng = context.getSequenceNumberGenerator();
        if (sng != null) {
            this.sequenceNumber = sng.nextSequenceNumber();
        }
        this.serverAdapter = adapter;
        this.elapsedTime = calculateElapsedTime();
    }

    /**
     * Returns the underlying HttpServletRequest. After serialization the returned
     * value will be null.
     *
     * @return
     */
    @Override
    public HttpServletRequest getRequest() {
        return httpRequest;
    }

    /**
     * Returns the underlying HttpServletResponse. After serialization the returned
     * value will be null.
     *
     * @return
     */
    @Override
    public HttpServletResponse getResponse() {
        return httpResponse;
    }

    @Override
    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        if (this.timeStamp != 0) {
            throw new IllegalStateException("timeStamp has been already set for this event.");
        } else {
            this.timeStamp = timeStamp;
        }
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
            if (httpRequest != null) {
                requestURI = httpRequest.getRequestURI();
            } else {
                requestURI = NA;
            }
        }
        return requestURI;
    }

    @Override
    public String getQueryString() {
        if (queryString == null) {
            if (httpRequest != null) {
                StringBuilder buf = new StringBuilder();
                final String qStr = httpRequest.getQueryString();
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
            if (httpRequest != null) {
                StringBuilder buf = new StringBuilder();
                buf.append(httpRequest.getMethod());
                buf.append(AccessConverter.SPACE_CHAR);
                buf.append(httpRequest.getRequestURI());
                buf.append(getQueryString());
                buf.append(AccessConverter.SPACE_CHAR);
                buf.append(httpRequest.getProtocol());
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
            if (httpRequest != null) {
                // the underlying implementation of HttpServletRequest will
                // determine if remote lookup will be performed
                remoteHost = httpRequest.getRemoteHost();
            } else {
                remoteHost = NA;
            }
        }
        return remoteHost;
    }

    @Override
    public String getRemoteUser() {
        if (remoteUser == null) {
            if (httpRequest != null) {
                remoteUser = httpRequest.getRemoteUser();
            } else {
                remoteUser = NA;
            }
        }
        return remoteUser;
    }

    @Override
    public String getProtocol() {
        if (protocol == null) {
            if (httpRequest != null) {
                protocol = httpRequest.getProtocol();
            } else {
                protocol = NA;
            }
        }
        return protocol;
    }

    @Override
    public String getMethod() {
        if (method == null) {
            if (httpRequest != null) {
                method = httpRequest.getMethod();
            } else {
                method = NA;
            }
        }
        return method;
    }

    @Override
    public String getSessionID() {
        if (sessionID == null) {
            if (httpRequest != null) {
                final HttpSession session = httpRequest.getSession();
                if (session != null) {
                    sessionID = session.getId();
                }
            } else {
                sessionID = NA;
            }
        }
        return sessionID;
    }

    @Override
    public String getServerName() {
        if (serverName == null) {
            if (httpRequest != null) {
                serverName = httpRequest.getServerName();
            } else {
                serverName = NA;
            }
        }
        return serverName;
    }

    @Override
    public String getRemoteAddr() {
        if (remoteAddr == null) {
            if (httpRequest != null) {
                remoteAddr = httpRequest.getRemoteAddr();
            } else {
                remoteAddr = NA;
            }
        }
        return remoteAddr;
    }

    @Override
    public String getRequestHeader(String key) {
        String result = null;
        key = key.toLowerCase();
        if (requestHeaderMap == null) {
            if (httpRequest != null) {
                buildRequestHeaderMap();
                result = requestHeaderMap.get(key);
            }
        } else {
            result = requestHeaderMap.get(key);
        }

        if (result != null) {
            return result;
        } else {
            return NA;
        }
    }

    @Override
    public Enumeration<String> getRequestHeaderNames() {
        // post-serialization
        if (httpRequest == null) {
            Vector<String> list = new Vector<String>(getRequestHeaderMap().keySet());
            return list.elements();
        }
        return httpRequest.getHeaderNames();
    }

    @Override
    public Map<String, String> getRequestHeaderMap() {
        if (requestHeaderMap == null) {
            buildRequestHeaderMap();
        }
        return requestHeaderMap;
    }

    public void buildRequestHeaderMap() {
        // according to RFC 2616 header names are case insensitive
        // latest versions of Tomcat return header names in lower-case
        requestHeaderMap = new TreeMap<String, String>(String.CASE_INSENSITIVE_ORDER);
        Enumeration<String> e = httpRequest.getHeaderNames();
        if (e == null) {
            return;
        }
        while (e.hasMoreElements()) {
            String key = e.nextElement();
            requestHeaderMap.put(key, httpRequest.getHeader(key));
        }
    }

    public void buildRequestParameterMap() {
        requestParameterMap = new HashMap<String, String[]>();
        Enumeration<String> e = httpRequest.getParameterNames();
        if (e == null) {
            return;
        }
        while (e.hasMoreElements()) {
            String key = e.nextElement();
            requestParameterMap.put(key, httpRequest.getParameterValues(key));
        }
    }

    @Override
    public Map<String, String[]> getRequestParameterMap() {
        if (requestParameterMap == null) {
            buildRequestParameterMap();
        }
        return requestParameterMap;
    }

    @Override
    public String getAttribute(String key) {
        Object value = null;
        if (attributeMap != null) {
            // Event was prepared for deferred processing so we have a copy of attribute map and must use that copy
            value = attributeMap.get(key);
        } else if (httpRequest != null) {
            // We have original request so take attribute from it
            value = httpRequest.getAttribute(key);
        }

        return value != null ? value.toString() : NA;
    }

    private void copyAttributeMap() {

        if (httpRequest == null) {
            return;
        }

        // attributeMap has been copied already. See also LOGBACK-1189
        if (attributeMap != null) {
            return;
        }

        attributeMap = new HashMap<String, Object>();

        Enumeration<String> names = httpRequest.getAttributeNames();
        while (names.hasMoreElements()) {
            String name = names.nextElement();

            Object value = httpRequest.getAttribute(name);
            if (shouldCopyAttribute(name, value)) {
                attributeMap.put(name, value);
            }
        }
    }

    private boolean shouldCopyAttribute(String name, Object value) {
        if (AccessConstants.LB_INPUT_BUFFER.equals(name) || AccessConstants.LB_OUTPUT_BUFFER.equals(name)) {
            // Do not copy attributes used by logback internally - these are available via other getters anyway
            return false;
        } else if (value == null) {
            // No reasons to copy nulls - Map.get() will return null for missing keys and the list of attribute
            // names is not available through IAccessEvent
            return false;
        } else {
            // Only copy what is serializable
            return value instanceof Serializable;
        }
    }

    @Override
    public String[] getRequestParameter(String key) {
        String[] value = null;

        if (requestParameterMap != null) {
            value = requestParameterMap.get(key);
        } else if (httpRequest != null) {
            value = httpRequest.getParameterValues(key);
        }

        return (value != null) ? value : NA_STRING_ARRAY;
    }

    @Override
    public String getCookie(String key) {

        if (httpRequest != null) {
            Cookie[] cookieArray = httpRequest.getCookies();
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
        if (contentLength == SENTINEL) {
            if (httpResponse != null) {
                contentLength = serverAdapter.getContentLength();
                return contentLength;
            }
        }
        return contentLength;
    }

    public int getStatusCode() {
        if (statusCode == SENTINEL) {
            if (httpResponse != null) {
                statusCode = serverAdapter.getStatusCode();
            }
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
        if (serverAdapter.getRequestTimestamp() < 0) {
            return -1;
        }
        return getTimeStamp() - serverAdapter.getRequestTimestamp();
    }

    public String getRequestContent() {
        if (requestContent != null) {
            return requestContent;
        }

        if (Util.isFormUrlEncoded(httpRequest)) {
            StringBuilder buf = new StringBuilder();

            Enumeration<String> pramEnumeration = httpRequest.getParameterNames();

            // example: id=1234&user=cgu
            // number=1233&x=1
            int count = 0;
            try {
                while (pramEnumeration.hasMoreElements()) {

                    String key = pramEnumeration.nextElement();
                    if (count++ != 0) {
                        buf.append("&");
                    }
                    buf.append(key);
                    buf.append("=");
                    String val = httpRequest.getParameter(key);
                    if (val != null) {
                        buf.append(val);
                    } else {
                        buf.append("");
                    }
                }
            } catch (Exception e) {
                // FIXME Why is try/catch required?
                e.printStackTrace();
            }
            requestContent = buf.toString();
        } else {
            // retrieve the byte array placed by TeeFilter
            byte[] inputBuffer = (byte[]) httpRequest.getAttribute(AccessConstants.LB_INPUT_BUFFER);

            if (inputBuffer != null) {
                requestContent = new String(inputBuffer);
            }

            if (requestContent == null || requestContent.length() == 0) {
                requestContent = EMPTY;
            }
        }

        return requestContent;
    }

    public String getResponseContent() {
        if (responseContent != null) {
            return responseContent;
        }

        if (Util.isImageResponse(httpResponse)) {
            responseContent = "[IMAGE CONTENTS SUPPRESSED]";
        } else {

            // retreive the byte array previously placed by TeeFilter
            byte[] outputBuffer = (byte[]) httpRequest.getAttribute(AccessConstants.LB_OUTPUT_BUFFER);

            if (outputBuffer != null) {
                responseContent = new String(outputBuffer);
            }
            if (responseContent == null || responseContent.length() == 0) {
                responseContent = EMPTY;
            }
        }

        return responseContent;
    }

    public int getLocalPort() {
        if (localPort == SENTINEL) {
            if (httpRequest != null) {
                localPort = httpRequest.getLocalPort();
            }

        }
        return localPort;
    }

    public ServerAdapter getServerAdapter() {
        return serverAdapter;
    }

    public String getResponseHeader(String key) {
        buildResponseHeaderMap();
        return responseHeaderMap.get(key);
    }

    void buildResponseHeaderMap() {
        if (responseHeaderMap == null) {
            responseHeaderMap = serverAdapter.buildResponseHeaderMap();
        }
    }

    public Map<String, String> getResponseHeaderMap() {
        buildResponseHeaderMap();
        return responseHeaderMap;
    }

    public List<String> getResponseHeaderNameList() {
        buildResponseHeaderMap();
        return new ArrayList<String>(responseHeaderMap.keySet());
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
