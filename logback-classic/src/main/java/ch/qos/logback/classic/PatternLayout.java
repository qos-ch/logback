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
 * 
 * <p>
 * The conversion pattern is closely related to the conversion pattern of the
 * printf function in C. A conversion pattern is composed of literal text and
 * format control expressions called <em>conversion specifiers</em>.
 * 
 * <p>
 * <i>Note that you are free to insert any literal text within the conversion
 * pattern.</i>
 * </p>
 * 
 * <p>
 * Each conversion specifier starts with a percent sign (%) and is followed by
 * optional <em>format modifiers</em> and a <em>conversion character</em> or
 * <em>conversion word</em>. The conversion character or word specifies the
 * type of data, e.g. logger, level, date, thread name. The format modifiers
 * control such things as field width, padding, left and right justification.
 * The following is a simple example.
 * 
 * <p>
 * Let the conversion pattern be <b>"%-5level [%thread]: %message%n"</b> and
 * assume that the logback environment was set to use a PatternLayout. Then the
 * statements
 * 
 * <pre>
 * Logger logger = LoggerFactory.getLogger(&quot;root&quot;);
 * root.debug(&quot;Message 1&quot;);
 * root.warn(&quot;Message 2&quot;);
 * </pre>
 * 
 * would yield the output
 * 
 * <pre>
 *          DEBUG [main]: Message 1
 *          WARN  [main]: Message 2
 * </pre>
 * 
 * <p>
 * Note that there is no explicit separator between text and conversion
 * specifiers. The pattern parser knows when it has reached the end of a
 * conversion specifier when it reads a conversion character. In the example
 * above the conversion specifier <b>%-5level</b> means the level of the
 * logging event should be left justified to a width of five characters.
 * 
 * The recognized conversion characters and words are
 * 
 * <p>
 * <table border="1" CELLPADDING="8">
 * <th>Conversion Character or Word</th>
 * <th>Effect</th>
 * 
 * <tr>
 * <td align=center><b>c / l / lo / logger</b></td>
 * 
 * <td>Used to output the logger of the logging event. The logger conversion
 * specifier can be optionally followed by <em>precision specifier</em>, that
 * is a decimal constant in brackets.
 * 
 * <p>
 * If a precision specifier is given, then only the corresponding number of
 * right most components of the logger name will be printed. By default the
 * logger name is printed in full.
 * 
 * <p>
 * For example, for the category name "a.b.c" the pattern <b>%logger{2}</b>
 * will output "b.c". See more examples of name abbreviations further down this
 * document.
 * 
 * </td>
 * </tr>
 * 
 * <tr>
 * <td align=center><b>C / class</b></td>
 * 
 * <td>Used to output the fully qualified class name of the caller issuing the
 * logging request. This conversion specifier can be optionally followed by
 * <em>precision specifier</em>, that is a decimal constant in brackets.
 * 
 * <p>
 * If a precision specifier is given, then only the corresponding number of
 * right most components of the class name will be printed. By default the class
 * name is output in fully qualified form.
 * 
 * <p>
 * For example, for the class name "org.apache.xyz.SomeClass", the pattern
 * <b>%class{1}</b> will output "SomeClass". See more examples of name
 * abbreviations further down this document.
 * 
 * <p>
 * <b>WARNING</b> Generating the caller class information is slow. Thus, it's
 * use should be avoided unless execution speed is not an issue.
 * 
 * </td>
 * </tr>
 * 
 * <tr>
 * <td align=center><b>d / date</b></td>
 * <td>Used to output the date of the logging event. The date conversion
 * specifier may be followed by a set of braces containing a date and time
 * pattern strings {@link java.text.SimpleDateFormat}, <em>ABSOLUTE</em>,
 * <em>DATE</em> or <em>ISO8601</em>. For example, <b>%d{HH:mm:ss,SSS}</b>,
 * <b>%d{dd&nbsp;MMM&nbsp;yyyy&nbsp;HH:mm:ss,SSS}</b> or <b>%d{DATE}</b>. If
 * no date format specifier is given then ISO8601 format is assumed. </td>
 * </tr>
 * 
 * <tr>
 * <td align=center><b>F / file</b></td>
 * 
 * <td>Used to output the file name where the logging request was issued.
 * 
 * <p>
 * <b>WARNING</b> Generating caller file information is extremely slow. Its use
 * should be avoided unless execution speed is not an issue.
 * 
 * </tr>
 * 
 * <tr>
 * <td align=center><b>caller</b></td>
 * 
 * <td>Used to output location information of the caller which generated the
 * logging event.
 * 
 * <p>
 * The location information depends on the JVM implementation but usually
 * consists of the fully qualified name of the calling method followed by the
 * callers source the file name and line number between parentheses.
 * 
 * <p>
 * The location information can be very useful. However, it's generation is
 * <em>extremely</em> slow. It's use should be avoided unless execution speed
 * is not an issue.
 * 
 * </td>
 * </tr>
 * 
 * <tr>
 * <td align=center><b>L / line</b></td>
 * 
 * <td>Used to output the line number from where the logging request was
 * issued.
 * 
 * <p>
 * <b>WARNING</b> Generating caller location information is extremely slow.
 * It's use should be avoided unless execution speed is not an issue.
 * 
 * </tr>
 * 
 * 
 * <tr>
 * <td align=center><b>m / msg / message</b></td>
 * <td>Used to output the application supplied message associated with the
 * logging event.</td>
 * </tr>
 * 
 * <tr>
 * <td align=center><b>M / method</b></td>
 * 
 * <td>Used to output the method name where the logging request was issued.
 * 
 * <p>
 * <b>WARNING</b> Generating caller location information is extremely slow.
 * It's use should be avoided unless execution speed is not an issue.
 * 
 * </tr>
 * 
 * <tr>
 * <td align=center><b>n</b></td>
 * 
 * <td>Outputs the platform dependent line separator character or characters.
 * 
 * <p>
 * This conversion character offers practically the same performance as using
 * non-portable line separator strings such as "\n", or "\r\n". Thus, it is the
 * preferred way of specifying a line separator.
 * 
 * 
 * </tr>
 * 
 * <tr>
 * <td align=center><b>p / le / level</b></td>
 * <td>Used to output the level of the logging event.</td>
 * </tr>
 * 
 * <tr>
 * 
 * <td align=center><b>r / relative</b></td>
 * 
 * <td>Used to output the number of milliseconds elapsed since the start of the
 * application until the creation of the logging event.</td>
 * </tr>
 * 
 * 
 * <tr>
 * <td align=center><b>t / thread</b></td>
 * 
 * <td>Used to output the name of the thread that generated the logging event.</td>
 * 
 * </tr>
 * 
 * <tr>
 * <td align=center><b>X</b></td>
 * 
 * <td>
 * 
 * <p>
 * Used to output the MDC (mapped diagnostic context) associated with the thread
 * that generated the logging event. The <b>X</b> conversion character can be
 * followed by the key for the map placed between braces, as in
 * <b>%X{clientNumber}</b> where <code>clientNumber</code> is the key. The
 * value in the MDC corresponding to the key will be output. If no additional
 * sub-option is specified, then the entire contents of the MDC key value pair
 * set is output using a format key1=val1, key2=val2
 * </p>
 * 
 * <p>
 * See {@link MDC} class for more details.
 * </p>
 * 
 * </td>
 * </tr>
 * <tr>
 * <td align=center><b>throwable</b></td>
 * 
 * <td>
 * <p>
 * Used to output the Throwable trace that has been bound to the LoggingEvent,
 * by default this will output the full trace as one would normally find by a
 * call to Throwable.printStackTrace(). The throwable conversion word can be
 * followed by an option in the form <b>%throwable{short}</b> which will only
 * output the first line of the ThrowableInformation.
 * </p>
 * </td>
 * </tr>
 * 
 * <tr>
 * 
 * <td align=center><b>%</b></td>
 * 
 * <td>The sequence %% outputs a single percent sign. </td>
 * </tr>
 * 
 * </table>
 * 
 * <p>
 * By default the relevant information is output as is. However, with the aid of
 * format modifiers it is possible to change the minimum field width, the
 * maximum field width and justification.
 * 
 * <p>
 * The optional format modifier is placed between the percent sign and the
 * conversion character or word.
 * 
 * <p>
 * The first optional format modifier is the <em>left justification flag</em>
 * which is just the minus (-) character. Then comes the optional
 * <em>minimum field width</em> modifier. This is a decimal constant that
 * represents the minimum number of characters to output. If the data item
 * requires fewer characters, it is padded on either the left or the right until
 * the minimum width is reached. The default is to pad on the left (right
 * justify) but you can specify right padding with the left justification flag.
 * The padding character is space. If the data item is larger than the minimum
 * field width, the field is expanded to accommodate the data. The value is
 * never truncated.
 * 
 * <p>
 * This behavior can be changed using the <em>maximum field width</em>
 * modifier which is designated by a period followed by a decimal constant. If
 * the data item is longer than the maximum field, then the extra characters are
 * removed from the <em>beginning</em> of the data item and not from the end.
 * For example, it the maximum field width is eight and the data item is ten
 * characters long, then the first two characters of the data item are dropped.
 * This behavior deviates from the printf function in C where truncation is done
 * from the end.
 * 
 * <p>
 * Below are various format modifier examples for the logger conversion
 * specifier.
 * 
 * <p>
 * <TABLE BORDER=1 CELLPADDING=8>
 * <th>Format modifier
 * <th>Left justify
 * <th>Minimum width
 * <th>Maximum width
 * <th>Comment
 * 
 * <tr>
 * <td align=center>%20c</td>
 * <td align=center>false</td>
 * <td align=center>20</td>
 * <td align=center>none</td>
 * 
 * <td>Left pad with spaces if the category name is less than 20 characters
 * long.
 * 
 * <tr>
 * <td align=center>%-20c</td>
 * <td align=center>true</td>
 * <td align=center>20</td>
 * <td align=center>none</td>
 * <td>Right pad with spaces if the logger name is less than 20 characters
 * long.
 * 
 * <tr>
 * <td align=center>%.30c</td>
 * <td align=center>NA</td>
 * <td align=center>none</td>
 * <td align=center>30</td>
 * 
 * <td>Truncate from the beginning if the logger name is longer than 30
 * characters.
 * 
 * <tr>
 * <td align=center>%20.30c</td>
 * <td align=center>false</td>
 * <td align=center>20</td>
 * <td align=center>30</td>
 * 
 * <td>Left pad with spaces if the logger name is shorter than 20 characters.
 * However, if logger name is longer than 30 characters, then truncate from the
 * beginning.
 * 
 * <tr>
 * <td align=center>%-20.30c</td>
 * <td align=center>true</td>
 * <td align=center>20</td>
 * <td align=center>30</td>
 * 
 * <td>Right pad with spaces if the logger name is shorter than 20 characters.
 * However, if logger name is longer than 30 characters, then truncate from the
 * beginning.
 * 
 * </table>
 * 
 * <p>
 * Below are some examples of conversion patterns.
 * 
 * <dl>
 * 
 * <p>
 * <dt><b>%relative [%thread] %-5level %logger %mdc - %message\n</b>
 * <p>
 * <dd>This is essentially the TTCC layout.
 * 
 * <p>
 * <dt><b>%-6relative [%15.15thread] %-5level %30.30logger %mdc - %message\n</b>
 * 
 * <p>
 * <dd>Similar to the TTCC layout except that the relative time is right padded
 * if less than 6 digits, thread name is right padded if less than 15 characters
 * and truncated if longer and the logger name is left padded if shorter than 30
 * characters and truncated if longer.
 * 
 * </dl>
 * <p> Here are a few more examples of the format modifier behaviour, with emphasis on 
 * the way it affects class names.
 * <p>
 * 
 * <table BORDER=1 CELLPADDING=8>
 * 
 * <tr>
 * <th>Conversion Pattern</th>
 * <th>Class name</th>
 * <th>Result</th>
 * </tr>
 * 
 * <tr>
 * <td>%logger{10}</td>
 * <td>mainPackage.sub.sample.Bar</td>
 * <td>m.s.s.Bar</td>
 * </tr>
 * 
 * <tr>
 * <td>%logger{15}</td>
 * <td>mainPackage.sub.sample.Bar</td>
 * <td>m.s.sample.Bar</td>
 * </tr>
 * 
 * <tr>
 * <td>%logger{16}</td>
 * <td>mainPackage.sub.sample.Bar</td>
 * <td>m.sub.sample.Bar</td>
 * </tr>
 * 
 * <tr>
 * <td>%logger{26}</td>
 * <td>mainPackage.sub.sample.Bar</td>
 * <td>mainPackage.sub.sample.Bar</td>
 * </tr>
 * 
 * </table>
 * 
 * <p>
 * The above text is largely inspired from Peter A. Darnell and Philip E.
 * Margolis' highly recommended book "C -- a Software Engineering Approach",
 * ISBN 0-387-97389-3.
 * 
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
