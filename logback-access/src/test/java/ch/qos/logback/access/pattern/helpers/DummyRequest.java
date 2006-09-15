package ch.qos.logback.access.pattern.helpers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletInputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

public class DummyRequest implements HttpServletRequest {

  Hashtable<String, String> headerNames;
  
  public DummyRequest() {
    headerNames = new Hashtable<String, String>();
    headerNames.put("headerName1", "headerValue1");
    headerNames.put("headerName2", "headerValue2");
  }
  
  public String getAuthType() {
    return null;
  }

  public String getContextPath() {
    return null;
  }

  public Cookie[] getCookies() {
    Cookie cookie = new Cookie("testName", "testCookie");
    return new Cookie[] {cookie};
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
    return "testURI";
  }

  public StringBuffer getRequestURL() {
    return null;
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

  public boolean isRequestedSessionIdValid() {
    return false;
  }

  public boolean isUserInRole(String arg0) {
    return false;
  }

  public Object getAttribute(String key) {
    if (key.equals("testKey")) {
      return "testKey";
    } else {
      return null;
    }
  }

  public Enumeration getAttributeNames() {
    return null;
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

  public void setAttribute(String arg0, Object arg1) {
  }

  public void setCharacterEncoding(String arg0)
      throws UnsupportedEncodingException {
  }
}
