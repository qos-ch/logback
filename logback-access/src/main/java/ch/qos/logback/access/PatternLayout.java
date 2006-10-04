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
import ch.qos.logback.access.pattern.PostContentConverter;
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


public class PatternLayout extends PatternLayoutBase implements AccessLayout {

  static final Map<String, String> defaultConverterMap = new HashMap<String, String>();

  public static String CLF_PATTERN = "%h %l %u %t \"%r\" %s %b";
  
  static {

    defaultConverterMap.put("a", RemoteIPAddressConverter.class.getName());
    defaultConverterMap.put("remoteIP", RemoteIPAddressConverter.class.getName());

    defaultConverterMap.put("A", LocalIPAddressConverter.class.getName());
    defaultConverterMap.put("localIP", LocalIPAddressConverter.class.getName());

    defaultConverterMap.put("b", ContentLengthConverter.class.getName());
    defaultConverterMap.put("B", ContentLengthConverter.class.getName());
    defaultConverterMap.put("bytesSent", ContentLengthConverter.class.getName());

    defaultConverterMap.put("h", RemoteHostConverter.class.getName());
    defaultConverterMap.put("clientHost", RemoteHostConverter.class.getName());

    defaultConverterMap.put("H", RequestProtocolConverter.class.getName());
    defaultConverterMap.put("protocol", RequestProtocolConverter.class.getName());
    
    defaultConverterMap.put("i", RequestHeaderConverter.class.getName());
    defaultConverterMap.put("header", RequestHeaderConverter.class.getName());
    
    defaultConverterMap.put("l", NAConverter.class.getName());
    
    defaultConverterMap.put("m", RequestMethodConverter.class.getName());
    defaultConverterMap.put("requestMethod", RequestMethodConverter.class.getName());
    
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
    defaultConverterMap.put("reqAttribute", RequestAttributeConverter.class.getName());
    defaultConverterMap.put("reqCookie", RequestCookieConverter.class.getName());
    defaultConverterMap.put("responseHeader", ResponseHeaderConverter.class.getName());
    defaultConverterMap.put("reqParameter", RequestParameterConverter.class.getName());
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
    if(tail == null) {
      head = newLineConverter;
    } else {
      tail.setNext(newLineConverter);
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
