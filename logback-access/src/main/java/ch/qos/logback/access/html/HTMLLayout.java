/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * 
 * Copyright (C) 1999-2006, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
 */

package ch.qos.logback.access.html;

import java.util.Map;

import ch.qos.logback.access.AccessLayout;
import ch.qos.logback.access.PatternLayout;
import ch.qos.logback.access.spi.AccessEvent;
import ch.qos.logback.core.html.HTMLLayoutBase;
import ch.qos.logback.core.html.NOPThrowableRenderer;
import ch.qos.logback.core.pattern.Converter;

/**
 * 
 * HTMLLayout outputs events in an HTML table. 
 * <p>
 * The content of the table columns are specified using a conversion pattern. 
 * See {@link ch.qos.logback.access.PatternLayout} for documentation on the
 * available patterns.
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
 *   &lt;appender name="SMTP" class="ch.qos.logback.access.net.SMTPAppender"&gt;
 *     &lt;layout class="ch.qos.logback.access.html.HTMLLayout"&gt;
 *       &lt;param name="pattern" value="%remoteIP%date%requestURL%statusCode%bytesSent" /&gt;
 *     &lt;/layout&gt;
 *    &lt;param name="From" value="sender.email@domain.net" /&gt;
 *    &lt;param name="SMTPHost" value="mail.domain.net" /&gt;
 *    &lt;param name="Subject" value="LastEvent: %statusCode %requestURL" /&gt;
 *    &lt;param name="To" value="destination.email@domain.net" /&gt;
 *   &lt;/appender&gt;
 *
 *   &lt;appender-ref ref="SMTP" /&gt;
 * &lt;/configuration&gt;
 *</pre>
 * <p>
 * @author Ceki G&uuml;lc&uuml;
 * @author S&eacute;bastien Pennec
 */
public class HTMLLayout extends HTMLLayoutBase implements AccessLayout {

  /**
   * Default pattern string for log output. Currently set to the string <b>"%m"
   * </b> which just prints the application supplied message.
   */
  static final String DEFAULT_CONVERSION_PATTERN = "%remoteIP%date%requestURL%statusCode%bytesSent";

  /**
   * Constructs a PatternLayout using the DEFAULT_LAYOUT_PATTERN.
   * 
   * The default pattern just produces the application supplied message.
   */
  public HTMLLayout() {
    pattern = DEFAULT_CONVERSION_PATTERN;
    throwableRenderer = new NOPThrowableRenderer();
  }
  
  @Override
  protected Map<String, String> getDefaultConverterMap() {
    return PatternLayout.defaultConverterMap;
  }

  public String doLayout(Object event) {
    return doLayout((AccessEvent) event);
  }

  public String doLayout(AccessEvent event) {
    StringBuffer buf = new StringBuffer();
    handleTableClosing(buf);

    boolean odd = true;
    if (((counter++) & 1) == 0) {
      odd = false;
    }

    buf.append(LINE_SEP);
    buf.append("<tr class=\"");
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

    return buf.toString();
  }

  private void appendEventToBuffer(StringBuffer buf, Converter c,
      AccessEvent event) {
    buf.append("<td class=\"");
    buf.append(computeConverterName(c));
    buf.append("\">");
    buf.append(c.convert(event));
    buf.append("</td>");
    buf.append(LINE_SEP);
  }
}
