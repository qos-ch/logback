/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2009, QOS.ch. All rights reserved.
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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ch.qos.logback.access.AccessConstants;
import ch.qos.logback.access.pattern.AccessConverter;
import ch.qos.logback.access.servlet.Util;

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
public class AccessEvent implements Serializable {

  
  private static final long serialVersionUID = 866718993618836343L;
  
  public final static String NA = "-";
  public final static String[] NA_STRING_ARRAY = new String[] { AccessEvent.NA };

  public final static String EMPTY = "";
  public static final int SENTINEL = -1;

  private transient final HttpServletRequest httpRequest;
  private transient final HttpServletResponse httpResponse;

  String requestURI;
  String requestURL;
  String remoteHost;
  String remoteUser;
  String remoteAddr;
  String protocol;
  String method;
  String serverName;
  String requestContent;
  String responseContent;

  Map<String, String> requestHeaderMap;
  Map<String, String[]> requestParameterMap;
  Map<String, String> responseHeaderMap;

  long contentLength = SENTINEL;
  int statusCode = SENTINEL;
  int localPort = SENTINEL;

  transient ServerAdapter serverAdapter;

  /**
   * The number of milliseconds elapsed from 1/1/1970 until logging event was
   * created.
   */
  private long timeStamp = 0;

  public AccessEvent(HttpServletRequest httpRequest,
      HttpServletResponse httpResponse, ServerAdapter adapter) {
    this.httpRequest = httpRequest;
    this.httpResponse = httpResponse;
    this.timeStamp = System.currentTimeMillis();
    this.serverAdapter = adapter;
  }

  /**
   * Returns the underlying HttpServletRequest. After serialization the returned 
   * value will be null. 
   * 
   * @return
   */
  public HttpServletRequest getRequest() {
    return httpRequest;
  }

  /**
   * Returns the underlying HttpServletResponse. After serialization the returned 
   * value will be null. 
   * 
   * @return
   */
  public HttpServletResponse getResponse() {
    return httpResponse;
  }

  public long getTimeStamp() {
    return timeStamp;
  }

  public void setTimeStamp(long timeStamp) {
    if (this.timeStamp != 0) {
      throw new IllegalStateException(
          "timeStamp has been already set for this event.");
    } else {
      this.timeStamp = timeStamp;
    }
  }

  public String getRequestURI() {
    if (requestURI == null) {
      if (httpRequest != null) {
        requestURI = httpRequest.getRequestURI();
      } else {
        requestURI = AccessEvent.NA;
      }
    }
    return requestURI;
  }

  /**
   * The first line of the request.
   */
  public String getRequestURL() {
    if (requestURL == null) {
      if (httpRequest != null) {
        StringBuffer buf = new StringBuffer();
        buf.append(httpRequest.getMethod());
        buf.append(AccessConverter.SPACE_CHAR);
        buf.append(httpRequest.getRequestURI());
        final String qStr = httpRequest.getQueryString();
        if (qStr != null) {
          buf.append(AccessConverter.QUESTION_CHAR);
          buf.append(qStr);
        }
        buf.append(AccessConverter.SPACE_CHAR);
        buf.append(httpRequest.getProtocol());
        requestURL = buf.toString();
      } else {
        requestURL = AccessEvent.NA;
      }
    }
    return requestURL;
  }

  public String getRemoteHost() {
    if (remoteHost == null) {
      if (httpRequest != null) {
        // the underlying implementation of HttpServletRequest will
        // determine if remote lookup will be performed
        remoteHost = httpRequest.getRemoteHost();
      } else {
        remoteHost = AccessEvent.NA;
      }
    }
    return remoteHost;
  }

  public String getRemoteUser() {
    if (remoteUser == null) {
      if (httpRequest != null) {
        remoteUser = httpRequest.getRemoteUser();
      } else {
        remoteUser = AccessEvent.NA;
      }
    }
    return remoteUser;
  }

  public String getProtocol() {
    if (protocol == null) {
      if (httpRequest != null) {
        protocol = httpRequest.getProtocol();
      } else {
        protocol = AccessEvent.NA;
      }
    }
    return protocol;
  }

  public String getMethod() {
    if (method == null) {
      if (httpRequest != null) {
        method = httpRequest.getMethod();
      } else {
        method = AccessEvent.NA;
      }
    }
    return method;
  }

  public String getServerName() {
    if (serverName == null) {
      if (httpRequest != null) {
        serverName = httpRequest.getServerName();
      } else {
        serverName = AccessEvent.NA;
      }
    }
    return serverName;
  }

  public String getRemoteAddr() {
    if (remoteAddr == null) {
      if (httpRequest != null) {
        remoteAddr = httpRequest.getRemoteAddr();
      } else {
        remoteAddr = AccessEvent.NA;
      }
    }
    return remoteAddr;
  }

  public String getRequestHeader(String key) {
    String result = null;
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
      return AccessEvent.NA;
    }
  }

  public Enumeration getRequestHeaderNames() {
    // post-serialization
    if (httpRequest == null) {
      Vector<String> list = new Vector<String>(getRequestHeaderMap().keySet());
      return list.elements();
    }
    return httpRequest.getHeaderNames();

  }

  public Map<String, String> getRequestHeaderMap() {
    if (requestHeaderMap == null) {
      buildRequestHeaderMap();
    }
    return requestHeaderMap;
  }

  public void buildRequestHeaderMap() {
    requestHeaderMap = new HashMap<String, String>();
    Enumeration e = httpRequest.getHeaderNames();
    if (e == null) {
      return;
    }
    while (e.hasMoreElements()) {
      String key = (String) e.nextElement();
      requestHeaderMap.put(key, httpRequest.getHeader(key));
    }
  }

  public void buildRequestParameterMap() {
    requestParameterMap = new HashMap<String, String[]>();
    Enumeration e = httpRequest.getParameterNames();
    if (e == null) {
      return;
    }
    while (e.hasMoreElements()) {
      String key = (String) e.nextElement();
      requestParameterMap.put(key, httpRequest.getParameterValues(key));
    }
  }

  public Map<String, String[]> getRequestParameterMap() {
    if (requestParameterMap == null) {
      buildRequestParameterMap();
    }
    return requestParameterMap;
  }

  /**
   * Attributes are not serialized
   * 
   * @param key
   */
  public String getAttribute(String key) {
    if (httpRequest != null) {
      Object value = httpRequest.getAttribute(key);
      if (value == null) {
        return AccessEvent.NA;
      } else {
        return value.toString();
      }
    } else {
      return AccessEvent.NA;
    }
  }

  public String[] getRequestParameter(String key) {
    if (httpRequest != null) {
      String[] value = httpRequest.getParameterValues(key);
      if (value == null) {
        return NA_STRING_ARRAY;
      } else {
        return value;
      }
    } else {
      return NA_STRING_ARRAY;
    }
  }

  public String getCookie(String key) {

    if (httpRequest != null) {
      Cookie[] cookieArray = httpRequest.getCookies();
      if (cookieArray == null) {
        return AccessEvent.NA;
      }

      for (Cookie cookie : cookieArray) {
        if (key.equals(cookie.getName())) {
          return cookie.getValue();
        }
      }
    }
    return AccessEvent.NA;
  }

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

  public String getRequestContent() {
    if (requestContent != null) {
      return requestContent;
    }

    if (Util.isFormUrlEncoded(httpRequest)) {
      StringBuffer buf = new StringBuffer();

      Enumeration pramEnumeration = httpRequest.getParameterNames();

      // example: id=1234&user=cgu
      // number=1233&x=1
      int count = 0;
      try {
        while (pramEnumeration.hasMoreElements()) {

          String key = (String) pramEnumeration.nextElement();
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

      // retreive the byte array placed by TeeFilter
      byte[] inputBuffer = (byte[]) httpRequest
          .getAttribute(AccessConstants.LB_INPUT_BUFFER);

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
      byte[] outputBuffer = (byte[]) httpRequest
          .getAttribute(AccessConstants.LB_OUTPUT_BUFFER);

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
    buildRequestHeaderMap();
    buildRequestParameterMap();
    buildResponseHeaderMap();
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

    getStatusCode();
    getContentLength();
    getRequestContent();
    getResponseContent();
  }
}