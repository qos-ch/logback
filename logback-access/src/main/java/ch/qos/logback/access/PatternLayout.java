/**
 * LOGBack: the reliable, fast and flexible logging library for Java.
 *
 * Copyright (C) 1999-2006, QOS.ch
 *
 * This library is free software, you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation.
 */
package ch.qos.logback.access;

import java.util.HashMap;
import java.util.Map;

import ch.qos.logback.access.pattern.ContentLengthConverter;
import ch.qos.logback.access.pattern.DateConverter;
import ch.qos.logback.access.pattern.LineSeparatorConverter;
import ch.qos.logback.access.pattern.LocalIPAddressConverter;
import ch.qos.logback.access.pattern.LocalPortConverter;
import ch.qos.logback.access.pattern.NAConverter;
import ch.qos.logback.access.pattern.RemoteHostConverter;
import ch.qos.logback.access.pattern.RemoteIPAddressConverter;
import ch.qos.logback.access.pattern.RemoteUserConverter;
import ch.qos.logback.access.pattern.RequestAttributeConverter;
import ch.qos.logback.access.pattern.RequestCookieConverter;
import ch.qos.logback.access.pattern.RequestHeaderConverter;
import ch.qos.logback.access.pattern.RequestMethodConverter;
import ch.qos.logback.access.pattern.RequestParameterConverter;
import ch.qos.logback.access.pattern.RequestProtocolConverter;
import ch.qos.logback.access.pattern.RequestURIConverter;
import ch.qos.logback.access.pattern.RequestURLConverter;
import ch.qos.logback.access.pattern.ResponseHeaderConverter;
import ch.qos.logback.access.pattern.ServerNameConverter;
import ch.qos.logback.access.pattern.StatusCodeConverter;
import ch.qos.logback.access.spi.AccessEvent;
import ch.qos.logback.core.pattern.Converter;
import ch.qos.logback.core.pattern.PatternLayoutBase;

/**
 * <p>
 * This class is a module-specific implementation of
 * {@link ch.qos.logback.classic.PatternLayout} to allow http-specific patterns
 * to be used. The <code>ch.qos.logback.access.PatternLayout</code> provides a
 * way to format the logging output that is just as easy and flexible as the
 * usual <code>PatternLayout</code>.
 * </p>
 * <p>
 * For more information about the general use of a <code>PatternLayout</code>,
 * please refer to logback classic's
 * <code>ch.qos.logback.classic.PatternLayout</code>.
 * </p>
 * <p>
 * Logback access' <code>PatternLayout</code> offers the following
 * possibilities:
 * </p>
 * <table border="1" CELLPADDING="8">
 * <th>Conversion Character or Word</th>
 * <th>Effect</th>
 * 
 * <tr>
 * <td align="center"><b>a / remoteIP</b></td>
 * <td>
 * <p>
 * Remote IP address.
 * </p>
 * </td>
 * </tr>
 * <tr>
 * <td align="center"><b>A / localIP</b></td>
 * <td>
 * <p>
 * Local IP address.
 * </p>
 * </td>
 * </tr>
 * <tr>
 * <td align="center"><b>b / B / byteSent</b></td>
 * <td>
 * <p>
 * Response's content length.
 * </p>
 * </td>
 * </tr>
 * <tr>
 * <td align="center"><b>h / clientHost</b></td>
 * <td>
 * <p>
 * Remote host.
 * </p>
 * </td>
 * </tr>
 * <tr>
 * <td align="center"><b>H / protocol</b></td>
 * <td>
 * <p>
 * Request protocol.
 * </p>
 * </td>
 * </tr>
 * <tr>
 * <td align="center"><b>i / header</b></td>
 * <td>
 * <p>
 * Request header. This conversion word can be followed by a key whose
 * corresponding data will be extracted from the header information.
 * </p>
 * </td>
 * </tr>
 * <tr>
 * <td align="center"><b>m / requestMethod</b></td>
 * <td>
 * <p>
 * Request method.
 * </p>
 * </td>
 * </tr>
 * <tr>
 * <td align="center"><b>r / requestURL</b></td>
 * <td>
 * <p>
 * URL requested.
 * </p>
 * </td>
 * </tr>
 * <tr>
 * <td align="center"><b>s / statusCode</b></td>
 * <td>
 * <p>
 * Status code of the request.
 * </p>
 * </td>
 * </tr>
 * <tr>
 * <td align="center"><b>t / date</b></td>
 * <td>
 * <p>
 * Date of the event.
 * </p>
 * </td>
 * </tr>
 * <tr>
 * <td align="center"><b>u / user</b></td>
 * <td>
 * <p>
 * Remote user.
 * </p>
 * </td>
 * </tr>
 * <tr>
 * <td align="center"><b>U / requestURI</b></td>
 * <td>
 * <p>
 * Requested URI.
 * </p>
 * </td>
 * </tr>
 * <tr>
 * <td align="center"><b>v / server</b></td>
 * <td>
 * <p>
 * Server name.
 * </p>
 * </td>
 * </tr>
 * <tr>
 * <td align="center"><b>localPort</b></td>
 * <td>
 * <p>
 * Local port.
 * </p>
 * </td>
 * </tr>
 * <tr>
 * <td align="center"><b>reqAttribute</b></td>
 * <td>
 * <p>
 * Attribute of the request. Just like the request header conversion word,
 * reqAttribute can be followed by a key.
 * </p>
 * </td>
 * </tr>
 * <tr>
 * <td align="center"><b>reqCookie</b></td>
 * <td>
 * <p>
 * Request cookie.
 * </p>
 * </td>
 * </tr>
 * <tr>
 * <td align="center"><b>responseHeader</b></td>
 * <td>
 * <p>
 * Header of the response. Just like the request header conversion word,
 * responseHeader can be followed by a key.
 * </p>
 * </td>
 * </tr>
 * <tr>
 * <td align="center"><b>reqParameter</b></td>
 * <td>
 * <p>
 * Parameter of the response. Just like the request header conversion word,
 * reqParameter can be followed by a key.
 * </p>
 * </td>
 * </tr>
 * </table>
 * 
 * @author Ceki G&uuml;lc&uuml;
 * @author S&eacute;bastien Pennec
 */
public class PatternLayout extends PatternLayoutBase implements AccessLayout {

  static final Map<String, String> defaultConverterMap = new HashMap<String, String>();

  public static String CLF_PATTERN = "%h %l %u %t \"%r\" %s %b";

  static {

    defaultConverterMap.put("a", RemoteIPAddressConverter.class.getName());
    defaultConverterMap.put("remoteIP", RemoteIPAddressConverter.class
        .getName());

    defaultConverterMap.put("A", LocalIPAddressConverter.class.getName());
    defaultConverterMap.put("localIP", LocalIPAddressConverter.class.getName());

    defaultConverterMap.put("b", ContentLengthConverter.class.getName());
    defaultConverterMap.put("B", ContentLengthConverter.class.getName());
    defaultConverterMap
        .put("bytesSent", ContentLengthConverter.class.getName());

    defaultConverterMap.put("h", RemoteHostConverter.class.getName());
    defaultConverterMap.put("clientHost", RemoteHostConverter.class.getName());

    defaultConverterMap.put("H", RequestProtocolConverter.class.getName());
    defaultConverterMap.put("protocol", RequestProtocolConverter.class
        .getName());

    defaultConverterMap.put("i", RequestHeaderConverter.class.getName());
    defaultConverterMap.put("header", RequestHeaderConverter.class.getName());

    defaultConverterMap.put("l", NAConverter.class.getName());

    defaultConverterMap.put("m", RequestMethodConverter.class.getName());
    defaultConverterMap.put("requestMethod", RequestMethodConverter.class
        .getName());

    defaultConverterMap.put("r", RequestURLConverter.class.getName());
    defaultConverterMap.put("requestURL", RequestURLConverter.class.getName());

    defaultConverterMap.put("s", StatusCodeConverter.class.getName());
    defaultConverterMap.put("statusCode", StatusCodeConverter.class.getName());

    defaultConverterMap.put("t", DateConverter.class.getName());
    defaultConverterMap.put("date", DateConverter.class.getName());

    defaultConverterMap.put("u", RemoteUserConverter.class.getName());
    defaultConverterMap.put("user", RemoteUserConverter.class.getName());

    defaultConverterMap.put("U", RequestURIConverter.class.getName());
    defaultConverterMap.put("requestURI", RequestURIConverter.class.getName());

    defaultConverterMap.put("v", ServerNameConverter.class.getName());
    defaultConverterMap.put("server", ServerNameConverter.class.getName());

    defaultConverterMap.put("localPort", LocalPortConverter.class.getName());
    defaultConverterMap.put("reqAttribute", RequestAttributeConverter.class
        .getName());
    defaultConverterMap
        .put("reqCookie", RequestCookieConverter.class.getName());
    defaultConverterMap.put("responseHeader", ResponseHeaderConverter.class
        .getName());
    defaultConverterMap.put("reqParameter", RequestParameterConverter.class
        .getName());

    defaultConverterMap.put("n", LineSeparatorConverter.class.getName());
  }

  public PatternLayout() {
    // set a default value for pattern
    setPattern(CLF_PATTERN);
  }

  /**
   * Returns the default converter map for this instance.
   */
  public Map<String, String> getDefaultConverterMap() {
    return defaultConverterMap;
  }

  /**
   * Add a line separator so that each line is on a separate line.
   */
  protected void postCompileProcessing(Converter head) {
    Converter tail = findTail(head);
    Converter newLineConverter = new LineSeparatorConverter();
    if (tail == null) {
      head = newLineConverter;
    } else {
      if (!(tail instanceof LineSeparatorConverter)) {
        tail.setNext(newLineConverter);
      }
    }
    setContextForConverters(head);
  }

  public String doLayout(AccessEvent event) {
    if (!isStarted()) {
      return null;
    }
    return writeLoopOnConverters(event);
  }

  public String doLayout(Object o) {
    return doLayout((AccessEvent) o);
  }

}
