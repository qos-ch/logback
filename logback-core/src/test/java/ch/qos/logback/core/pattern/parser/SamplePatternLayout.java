/**
 * LOGBack: the reliable, fast and flexible logging library for Java.
 *
 * Copyright (C) 1999-2006, QOS.ch
 *
 * This library is free software, you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation.
 */
package ch.qos.logback.core.pattern.parser;

import java.util.HashMap;
import java.util.Map;

import ch.qos.logback.core.pattern.Converter123;
import ch.qos.logback.core.pattern.ConverterHello;
import ch.qos.logback.core.pattern.PatternLayoutBase;



public class SamplePatternLayout<E> extends PatternLayoutBase<E> {

  Map<String, String> converterMap = new HashMap<String, String>();

  public SamplePatternLayout() {
    converterMap.put("OTT", Converter123.class.getName());
    converterMap.put("hello", ConverterHello.class.getName());
  }
  
  public  Map<String, String> getDefaultConverterMap() {
    return converterMap;
  }

  public String doLayout(E event) {
    return writeLoopOnConverters(event);
  }

}
