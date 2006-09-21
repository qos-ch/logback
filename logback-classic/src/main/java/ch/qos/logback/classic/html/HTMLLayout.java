/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * 
 * Copyright (C) 1999-2006, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
 */

package ch.qos.logback.classic.html;

import java.util.Map;

import ch.qos.logback.classic.ClassicLayout;
import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.html.HTMLLayoutBase;
import ch.qos.logback.core.pattern.Converter;

/**
 * 
 * HTMLLayout outputs events in an HTML table. 
 * <p>
 * The content of the table columns are specified using a conversion pattern. 
 * See {@link ch.qos.logback.classic.PatternLayout} for documentation on the
 * available patterns.
 * <p>
 * Note that the pattern <em>%ex</em> used to display an Exception is not the only way
 * to display an Exception with this layout. If you use this pattern, a table column will
 * be created to display the potential Exception's stacktrace.
 * <p>
 * However, a better solution is available in the form of implementations of the 
 * {@link ch.qos.logback.classic.html.IThrowableRenderer}  interface.
 * These implementations can be called and assigned to HTMLLayout to manage the display
 * of anything related to Exceptions.
 * <p>
 * By default, a {@link ch.qos.logback.classic.html.DefaultThrowableRenderer} 
 * is assigned to the HTMLLayout. It writes the Exception on a new table row, along
 * with its stacktrace, in a easily readable manner.
 * <p>
 * If one wants to use the <em>&ex</em> pattern anyway, then a NOPThrowableRenderer
 * can be specified in the configuration file.
 * <p>
 * A user-specified external CSS file can be linked to the html page. 
 * In case one does not want to customize the html output, an internal CSS style
 * is used.
 * 
 * The HTMLLayout is often used in conjunction with SMTPAppender, to
 * send a nicely formatted html email. Of course, it can be used with any
 * other Appender.
 * 
 * In case on wants to use the HTMLLayout with a SMTPAppender, here is a sample
 * configuration file that can be used.
 * 
 * <pre>
 * &lt;configuration&gt;
 *   &lt;appender name="SMTP" class="ch.qos.logback.classic.net.SMTPAppender"&gt;
 *     &lt;layout class="ch.qos.logback.classic.html.HTMLLayout"&gt;
 *       &lt;param name="pattern" value="%relative%thread%mdc%level%class%msg" /&gt;
 *     &lt;/layout&gt;
 *     &lt;throwableRenderer class="ch.qos.logback.classic.html.DefaultThrowableRenderer" /&gt;
 *    &lt;param name="From" value="sender.email@domain.net" /&gt;
 *    &lt;param name="SMTPHost" value="mail.domain.net" /&gt;
 *    &lt;param name="Subject" value="LastEvent: %class - %msg" /&gt;
 *    &lt;param name="To" value="destination.email@domain.net" /&gt;
 *   &lt;/appender&gt;
 *
 *   &lt;root&gt;
 *     &lt;level value="debug" /&gt;
 *     &lt;appender-ref ref="SMTP" /&gt;
 *   &lt;/root&gt;
 * &lt;/configuration&gt;
 *</pre>
 * <p>
 * In this configuration file, the <em>throwableRenderer</em> element specifies the default
 * implementation of IThrowableRenderer. It could be omitted, but is showed for educationnal
 * purposes.
 * <p>
 * @author Ceki G&uuml;lc&uuml;
 * @author S&eacute;bastien Pennec
 */
public class HTMLLayout extends HTMLLayoutBase implements ClassicLayout {

  /**
   * Default pattern string for log output. Currently set to the string <b>"%m"
   * </b> which just prints the application supplied message.
   */
  static final String DEFAULT_CONVERSION_PATTERN = "%date%thread%level%logger%mdc%msg";

  /**
   * Constructs a PatternLayout using the DEFAULT_LAYOUT_PATTERN.
   * 
   * The default pattern just produces the application supplied message.
   */
  public HTMLLayout() {
    pattern = DEFAULT_CONVERSION_PATTERN;
  }
  
  protected Map<String, String> getDefaultConverterMap() {
    return PatternLayout.defaultConverterMap;
  }

  public String doLayout(Object event) {
    return doLayout((LoggingEvent) event);
  }

  public String doLayout(LoggingEvent event) {
    StringBuffer buf = new StringBuffer();
    handleTableClosing(buf);

    boolean odd = true;
    if (((counter++) & 1) == 0) {
      odd = false;
    }

    String level = event.getLevel().toString().toLowerCase();

    buf.append(LINE_SEP);
    buf.append("<tr class=\"");
    buf.append(level);
    if (odd) {
      buf.append(" odd\">");
    } else {
      buf.append(" even\">");
    }
    buf.append(LINE_SEP);

    Converter c = head;
    while (c != null) {
      appendEventToBuffer(buf, c, event);
      c = c.getNext();
    }
    buf.append("</tr>");
    buf.append(LINE_SEP);

    if (event.getThrowableInformation() != null) {
      throwableRenderer.render(buf, event);
    }
    return buf.toString();
  }

  private void appendEventToBuffer(StringBuffer buf, Converter c,
      LoggingEvent event) {
    buf.append("<td class=\"");
    buf.append(computeConverterName(c));
    buf.append("\">");
    buf.append(c.convert(event));
    buf.append("</td>");
    buf.append(LINE_SEP);
  }
}
