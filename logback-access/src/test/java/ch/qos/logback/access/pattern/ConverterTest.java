package ch.qos.logback.access.pattern;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import junit.framework.TestCase;
import ch.qos.logback.access.pattern.helpers.DummyRequest;
import ch.qos.logback.access.pattern.helpers.DummyResponse;
import ch.qos.logback.access.spi.AccessEvent;

public class ConverterTest extends TestCase {

  AccessEvent event;
  HttpServletRequest request;
  HttpServletResponse response;

  public void setUp() throws Exception {
    super.setUp();
    request = new DummyRequest();
    response = new DummyResponse();
    event = createEvent();
  }

  public void tearDown() throws Exception {
    super.tearDown();
    event = null;
    request = null;
    response = null;
  }

  public void testContentLengthConverter() {
    // TODO when AccessEvent has been modified
  }

  public void testDateConverter() {
    DateConverter converter = new DateConverter();
    converter.start();
    String result = converter.convert(event);
    assertEquals(converter.simpleFormat.format(event.getTimeStamp()), result);
  }

  public void testLineLocalPortConverter() {
    LocalPortConverter converter = new LocalPortConverter();
    converter.start();
    String result = converter.convert(event);
    assertEquals(Integer.toString(request.getLocalPort()), result);
  }

  public void testRemoteHostConverter() {
    RemoteHostConverter converter = new RemoteHostConverter();
    converter.start();
    String result = converter.convert(event);
    assertEquals(request.getRemoteHost(), result);
  }

  public void testRemoteIPAddressConverter() {
    RemoteIPAddressConverter converter = new RemoteIPAddressConverter();
    converter.start();
    String result = converter.convert(event);
    assertEquals(request.getRemoteAddr(), result);
  }

  public void testRemoteUserConverter() {
    RemoteUserConverter converter = new RemoteUserConverter();
    converter.start();
    String result = converter.convert(event);
    assertEquals(request.getRemoteUser(), result);
  }

  public void testRequestAttributeConverter() {
    RequestAttributeConverter converter = new RequestAttributeConverter();
    List<String> optionList = new ArrayList<String>();
    optionList.add("testKey");
    converter.setOptionList(optionList);
    converter.start();
    String result = converter.convert(event);
    assertEquals(request.getAttribute("testKey"), result);
  }

  public void testRequestCookieConverter() {
    RequestCookieConverter converter = new RequestCookieConverter();
    List<String> optionList = new ArrayList<String>();
    optionList.add("testName");
    converter.setOptionList(optionList);
    converter.start();
    String result = converter.convert(event);
    Cookie cookie = request.getCookies()[0];
    assertEquals(cookie.getValue(), result);
  }

  public void testRequestHeaderConverter() {
    RequestHeaderConverter converter = new RequestHeaderConverter();
    List<String> optionList = new ArrayList<String>();
    optionList.add("headerName1");
    converter.setOptionList(optionList);
    converter.start();
    String result = converter.convert(event);
    assertEquals(request.getHeader("headerName1"), result);
  }

  public void testRequestMethodConverter() {
    RequestMethodConverter converter = new RequestMethodConverter();
    converter.start();
    String result = converter.convert(event);
    assertEquals(request.getMethod(), result);
  }

  public void testRequestProtocolConverter() {
    RequestProtocolConverter converter = new RequestProtocolConverter();
    converter.start();
    String result = converter.convert(event);
    assertEquals(request.getProtocol(), result);
  }

  public void testRequestURIConverter() {
    RequestURIConverter converter = new RequestURIConverter();
    converter.start();
    String result = converter.convert(event);
    assertEquals(request.getRequestURI(), result);
  }

  public void testRequestURLConverter() {
    RequestURLConverter converter = new RequestURLConverter();
    converter.start();
    String result = converter.convert(event);
    String expected = request.getMethod() + " " + request.getRequestURI() + " "
        + request.getProtocol();
    assertEquals(expected, result);
  }

  public void testResponseHeaderConverter() {
    // TODO
    // ResponseHeaderConverter converter = new ResponseHeaderConverter();
    // List<String> optionList = new ArrayList<String>();
    // optionList.add("headerName1");
    // converter.setOptionList(optionList);
    // converter.start();
    // String result = converter.convert(event);
    // assertEquals(request.getHeader("headerName1"), result);
  }

  public void testServerNameConverter() {
    ServerNameConverter converter = new ServerNameConverter();
    converter.start();
    String result = converter.convert(event);
    assertEquals(request.getServerName(), result);
  }
  
  public void testStatusCodeConverter() {
    //TODO 
  }

  private AccessEvent createEvent() {
    AccessEvent ae = new AccessEvent(request, response);
    return ae;
  }

}
