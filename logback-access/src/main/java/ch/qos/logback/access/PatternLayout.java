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
 * For more informations about this layout, please refer to the online manual at
 * http://logback.qos.ch/manual/layouts.html#AccessPatternLayout
 * 
 * @author Ceki G&uuml;lc&uuml;
 * @author S&eacute;bastien Pennec
 */
public class PatternLayout extends PatternLayoutBase<AccessEvent> {

  public static final Map<String, String> defaultConverterMap = new HashMap<String, String>();

  public static String CLF_PATTERN = "%h %l %u %t \"%r\" %s %b";
  public static String CLF_PATTERN_NAME = "common";
  public static String CLF_PATTERN_NAME_2 = "clf";
  public static String COMBINED_PATTERN = "%h %l %u %t \"%r\" %s %b \"%i{Referer}\" \"%i{User-Agent}\"";
  public static String COMBINED_PATTERN_NAME = "combined";

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

  @Override
  public void start() {
    if (getPattern().equalsIgnoreCase(CLF_PATTERN_NAME)
        || getPattern().equalsIgnoreCase(CLF_PATTERN_NAME_2)) {
      setPattern(CLF_PATTERN);
    } else if (getPattern().equalsIgnoreCase(COMBINED_PATTERN_NAME)) {
      setPattern(COMBINED_PATTERN);
    }
    super.start();
  }

}
