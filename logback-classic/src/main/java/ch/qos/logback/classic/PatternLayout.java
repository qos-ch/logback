/**
 * LOGBack: the reliable, fast and flexible logging library for Java.
 *
 * Copyright (C) 1999-2006, QOS.ch
 *
 * This library is free software, you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation.
 */
package ch.qos.logback.classic;

import java.util.HashMap;
import java.util.Map;

import ch.qos.logback.classic.pattern.CallerDataConverter;
import ch.qos.logback.classic.pattern.ClassOfCallerConverter;
import ch.qos.logback.classic.pattern.DateConverter;
import ch.qos.logback.classic.pattern.FileOfCallerConverter;
import ch.qos.logback.classic.pattern.LevelConverter;
import ch.qos.logback.classic.pattern.LineOfCallerConverter;
import ch.qos.logback.classic.pattern.LineSeparatorConverter;
import ch.qos.logback.classic.pattern.LoggerConverter;
import ch.qos.logback.classic.pattern.MDCConverter;
import ch.qos.logback.classic.pattern.MessageConverter;
import ch.qos.logback.classic.pattern.MethodOfCallerConverter;
import ch.qos.logback.classic.pattern.NopThrowableInformationConverter;
import ch.qos.logback.classic.pattern.RelativeTimeConverter;
import ch.qos.logback.classic.pattern.ThreadConverter;
import ch.qos.logback.classic.pattern.ThrowableHandlingConverter;
import ch.qos.logback.classic.pattern.ThrowableInformationConverter;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.CoreGlobal;
import ch.qos.logback.core.pattern.Converter;
import ch.qos.logback.core.pattern.PatternLayoutBase;

/**
 * <p>
 * A flexible layout configurable with pattern string. The goal of this class is
 * to {@link #format format} a {@link LoggingEvent} and return the results in a
 * {#link String}. The format of the result depends on the
 * <em>conversion pattern</em>.
 * <p>
 * For more information about this layout, please refer to the online manual at
 * http://logback.qos.ch/manual/layouts.html#ClassicPatternLayout
 * 
 */

public class PatternLayout extends PatternLayoutBase<LoggingEvent> {

  // FIXME fix exception handling

  public static final Map<String, String> defaultConverterMap = new HashMap<String, String>();

  static {

    defaultConverterMap.put("d", DateConverter.class.getName());
    defaultConverterMap.put("date", DateConverter.class.getName());

    defaultConverterMap.put("r", RelativeTimeConverter.class.getName());
    defaultConverterMap.put("relative", RelativeTimeConverter.class.getName());

    defaultConverterMap.put("level", LevelConverter.class.getName());
    defaultConverterMap.put("le", LevelConverter.class.getName());
    defaultConverterMap.put("p", LevelConverter.class.getName());

    defaultConverterMap.put("t", ThreadConverter.class.getName());
    defaultConverterMap.put("thread", ThreadConverter.class.getName());

    defaultConverterMap.put("lo", LoggerConverter.class.getName());
    defaultConverterMap.put("logger", LoggerConverter.class.getName());
    defaultConverterMap.put("c", LoggerConverter.class.getName());

    defaultConverterMap.put("m", MessageConverter.class.getName());
    defaultConverterMap.put("msg", MessageConverter.class.getName());
    defaultConverterMap.put("message", MessageConverter.class.getName());

    defaultConverterMap.put("C", ClassOfCallerConverter.class.getName());
    defaultConverterMap.put("class", ClassOfCallerConverter.class.getName());

    defaultConverterMap.put("M", MethodOfCallerConverter.class.getName());
    defaultConverterMap.put("method", MethodOfCallerConverter.class.getName());

    defaultConverterMap.put("L", LineOfCallerConverter.class.getName());
    defaultConverterMap.put("line", LineOfCallerConverter.class.getName());

    defaultConverterMap.put("F", FileOfCallerConverter.class.getName());
    defaultConverterMap.put("file", FileOfCallerConverter.class.getName());

    defaultConverterMap.put("X", MDCConverter.class.getName());
    defaultConverterMap.put("mdc", MDCConverter.class.getName());

    defaultConverterMap
        .put("ex", ThrowableInformationConverter.class.getName());
    defaultConverterMap.put("exception", ThrowableInformationConverter.class
        .getName());

    defaultConverterMap.put("nopex", NopThrowableInformationConverter.class
        .getName());
    defaultConverterMap.put("nopexception",
        NopThrowableInformationConverter.class.getName());

    defaultConverterMap.put("caller", CallerDataConverter.class.getName());

    defaultConverterMap.put("n", LineSeparatorConverter.class.getName());
  }

  /**
   * This implementation checks if any of the converters in the chain handles
   * exceptions. If not, then this method adds a ThrowableInformationConverter
   * instance to the end of the chain.
   * <p>
   * This allows appenders using this layout to output exception information
   * event if the user forgets to add %ex to the pattern. Note that the
   * appenders defined in the Core package are not aware of exceptions nor
   * LoggingEvents.
   * <p>
   * If for some reason the user wishes to NOT print exceptions, then she can
   * add %nopex to the pattern.
   * 
   * 
   */
  protected void postCompileProcessing(Converter<LoggingEvent> head) {
    if (!chainHandlesThrowable(head)) {
      Converter<LoggingEvent> tail = findTail(head);
      Converter<LoggingEvent> exConverter = new ThrowableInformationConverter();
      if (tail == null) {
        head = exConverter;
      } else {
        tail.setNext(exConverter);
      }
    }
    setContextForConverters(head);
  }

  public Map<String, String> getDefaultConverterMap() {
    return defaultConverterMap;
  }

  /**
   * This method computes whether a chain of converters handles exceptions or
   * not.
   * 
   * @param head
   *          The first element of the chain
   * @return true if can handle throwables contained in logging events
   */
  public static boolean chainHandlesThrowable(Converter head) {
    Converter c = head;
    while (c != null) {
      if (c instanceof ThrowableHandlingConverter) {
        return true;
      }
      c = c.getNext();
    }
    return false;
  }

  public String doLayout(LoggingEvent event) {
    if (!isStarted()) {
      return CoreGlobal.EMPTY_STRING;
    }
    return writeLoopOnConverters(event);
  }

}
