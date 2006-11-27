package ch.qos.logback.access.spi;

import java.io.Serializable;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ch.qos.logback.access.pattern.AccessConverter;

/**
 * The Access module's internal representation of logging events. When the
 * logging component instance is called in the container to log then a 
 * <code>AccessEvent</code>
 * instance is created. This instance is passed around to the different logback
 * components.
 * 
 * @author Ceki G&uuml;lc&uuml;
 * @author S&eacute;bastien Pennec
 */
public class AccessEvent implements Serializable {

  private static final long serialVersionUID = -3118194368414470960L;

  public final static String NA = "-";
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
  String postContent;

  Map<String, Object> requestHeaderMap;

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
  
  public HttpServletRequest getRequest() {
    return httpRequest;
  }
  
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
        result = (String) requestHeaderMap.get(key);
      }
    } else {
      result = (String) requestHeaderMap.get(key);
    }

    if (result != null) {
      return result;
    } else {
      return AccessEvent.NA;
    }
  }

  public void buildRequestHeaderMap() {
    requestHeaderMap = new HashMap<String, Object>();
    Enumeration e = httpRequest.getHeaderNames();
    while (e.hasMoreElements()) {
      String key = (String) e.nextElement();
      requestHeaderMap.put(key, httpRequest.getHeader(key));
    }
  }

  public String getResponseHeader(String key) {
    return serverAdapter.getResponseHeader(key);
  }

  /**
   * Attributes are not serialized
   * 
   * @param key
   * @return
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

  public String getRequestParameter(String key) {
    if (httpRequest != null) {
      Object value = httpRequest.getParameter(key);
      if (value == null) {
        return AccessEvent.NA;
      } else {
        return value.toString();
      }
    } else {
      return AccessEvent.NA;
    }
  }

  public String getCookie(String key) {

    if (httpRequest != null) {
      Cookie[] cookieArray = httpRequest.getCookies();
      if (cookieArray == null) {
        return AccessEvent.NA;
      }

      for (int i = 0; cookieArray != null && i < cookieArray.length; i++) {
        if (key.equals(cookieArray[i].getName())) {
          return cookieArray[i].getValue();
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

  public String getPostContent() {
    if (postContent != null) {
      return postContent;
    }

//    try {
//      InputStream in = httpRequest.getInputStream();
//      postContent = Util.readToString(in);
//    } catch (Exception ex) {
//      // do nothing
//    }
    if (postContent == null || postContent.length() == 0) {
      postContent = NA;
    }

    return postContent;
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
  
  public void prepareForSerialization() {
    getStatusCode();
    getContentLength();
    //getPostContent();
  }
}