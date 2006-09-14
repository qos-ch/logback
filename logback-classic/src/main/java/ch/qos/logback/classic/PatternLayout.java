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
 * 
 * PatternLayout is a simple yet powerfull way to turn a LoggingEvent into
 * a String.
 * <p>
 * The returned String can be modified by specifying a pattern to the layout.
 * A pattern is composed of literal text and format control expressions called
 * conversion specifiers. Any literal can be inserted between the conversions
 * specifiers. When literals are inserted, PatternLayout is able to differentiate
 * which part of the pattern are conversion specifiers and which should be 
 * added as-is to the output string.
 * <p>
 * A conversion specifier starts with a percent sign (%) and is followed by
 * a letter or a word, describing the type of data that will be displayed.
 * A format modifier can be added to the conversion specifier. It will tweak
 * the way that the conversion specifier will display the data.
 * <p>
 * Here are a few quick examples:
 * <p>
 * <table cellpadding=10>
 *   <tr>
 *     <th>Conversion pattern</th>
 *     <th>Result</th>
 *     <th>Explanation</th>
 *   </tr>
 *   <tr>
 *     <td>%level [%thread]: %message</td>
 *     <td>DEBUG [main]: Message 1</td>
 *     <td>Simple pattern</td>
 *   </tr>
 *   <tr>
 *     <td>%level [%logger]: %message</td>
 *     <td>DEBUG [org.foo.bar.SampleClass]: Message 2</td>   
 *     <td>With <em>%logger</em>, the fully qualified name is displayed</td>
 *   </tr>
 *   <tr>
 *     <td>%level [%logger{10}]: %message</td>
 *     <td>DEBUG [o.f.b.SampleClass]: Message 2</td>  
 *     <td>A format modifier has been added to the conversion specifier, thus
 *     modifying the string displayed.</td> 
 *   </tr>        
 * </table>
 * <p>
 * When a conversion specifier is used in conjunction with a format modifier, 
 * the resulting string is modified. It is modified in a way that one can still
 * read the class name, and deduce its fully qualified name by looking at the
 * packages' first letter.
 * <p>
 * The format modifier can have a milder effect on the resulting string. If a 
 * larger number is specified, a longer part of the fully qualified name will be
 * displayed without modification. In that case, the name of deeper packages 
 * are displayed first.
 * <p>
 * Here are a few more examples of the format modifier behaviour.
 * <p>
 * <table cellpadding=10>
 *   <tr>
 *     <th>Conversion Pattern</th>
 *     <th>Class name</th>
 *     <th>Result</th>
 *   </tr>
 *   <tr>
 *     <td>%logger{10}</td>
 *     <td>mainPackage.sub.sample.Bar</td>
 *     <td>m.s.s.Bar</td>
 *   </tr>
 *   <tr>
 *     <td>%logger{15}</td>
 *     <td>mainPackage.sub.sample.Bar</td>
 *     <td>m.s.sample.Bar</td>
 *   </tr>
 *   <tr>
 *     <td>%logger{16}</td>
 *     <td>mainPackage.sub.sample.Bar</td>
 *     <td>m.sub.sample.Bar</td>
 *   </tr> 
 *   <tr>
 *     <td>%logger{26}</td>
 *     <td>mainPackage.sub.sample.Bar</td>
 *     <td>mainPackage.sub.sample.Bar</td>
 *   </tr>
 * </table>
 *<p>
 * 
 */
public class PatternLayout extends PatternLayoutBase implements ClassicLayout {

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
  protected void postCompileProcessing(Converter head) {
    if (!chainHandlesThrowable(head)) {
      Converter tail = findTail(head);
      Converter exConverter = new ThrowableInformationConverter();
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

  public String doLayout(Object event) {
    return doLayout((LoggingEvent) event);
  }

}
