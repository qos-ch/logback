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

import ch.qos.logback.classic.ClassicLayout;
import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.helpers.CssBuilder;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.LayoutBase;
import ch.qos.logback.core.pattern.Converter;
import ch.qos.logback.core.pattern.DynamicConverter;
import ch.qos.logback.core.pattern.parser.Node;
import ch.qos.logback.core.pattern.parser.Parser;
import ch.qos.logback.core.pattern.parser.ScanException;

/**
 * 
 * HTMLLayout outputs events in an HTML table. The content of the table columns
 * are specified using a conversion pattern. See
 * {@link ch.qos.logback.classic.PatternLayout} for documentation on the
 * available patterns.
 * 
 * @author Ceki G&uuml;lc&uuml;
 * @author S&eacute;bastien Pennec
 */
public class HTMLLayout extends LayoutBase implements ClassicLayout {

  /**
   * Default pattern string for log output. Currently set to the string <b>"%m"
   * </b> which just prints the application supplied message.
   */
  static final String DEFAULT_CONVERSION_PATTERN = "%date%thread%level%logger%mdc%msg";

  protected final int BUF_SIZE = 256;
  protected final int MAX_CAPACITY = 1024;

  private String pattern;

  private Converter head;

  // private String timezone;
  private String title = "Logback Log Messages";

  private CssBuilder cssBuilder;

  ThrowableRenderer throwableRenderer = new ThrowableRenderer();

  // counter keeping track of the rows output
  private long counter = 0;
  // max number of rows before we close the table and create a new one
  private static final int ROW_LIMIT = 10000;

  /**
   * Constructs a PatternLayout using the DEFAULT_LAYOUT_PATTERN.
   * 
   * The default pattern just produces the application supplied message.
   */
  public HTMLLayout() {
    pattern = DEFAULT_CONVERSION_PATTERN;
  }

  /**
   * Set the <b>ConversionPattern </b> option. This is the string which controls
   * formatting and consists of a mix of literal content and conversion
   * specifiers.
   */
  public void setPattern(String conversionPattern) {
    pattern = conversionPattern;
  }

  /**
   * Returns the value of the <b>ConversionPattern </b> option.
   */
  public String getPattern() {
    return pattern;
  }

  public CssBuilder getCssBuilder() {
    return cssBuilder;
  }

  public void setCssBuilder(CssBuilder cssBuilder) {
    this.cssBuilder = cssBuilder;
  }

  /**
   * Parses the pattern and creates the Converter linked list.
   */
  public void start() {
    try {
      Parser p = new Parser(pattern);
      if (getContext() != null) {
        p.setStatusManager(getContext().getStatusManager());
      }
      Node t = p.parse();
      this.head = p.compile(t, PatternLayout.defaultConverterMap);
      DynamicConverter.startConverters(this.head);
    } catch (ScanException ex) {
      addError("Incorrect pattern found", ex);
    }

    started = true;
  }

  /**
   * The <b>Title </b> option takes a String value. This option sets the
   * document title of the generated HTML document.
   * 
   * <p>
   * Defaults to 'Logback Log Messages'.
   */
  public void setTitle(String title) {
    this.title = title;
  }

  /**
   * Returns the current value of the <b>Title </b> option.
   */
  public String getTitle() {
    return title;
  }

  /**
   * Returns the content type output by this layout, i.e "text/html".
   */
  @Override
  public String getContentType() {
    return "text/html";
  }

  /**
   * Returns appropriate HTML headers.
   */
  public String getHeader() {
    StringBuffer sbuf = new StringBuffer();
    // PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"
    // SYSTEM "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd"
    sbuf.append("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\"");
    sbuf.append(" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">");
    sbuf.append(LINE_SEP);
    sbuf.append("<html>");
    sbuf.append(LINE_SEP);
    sbuf.append("<head>");
    sbuf.append(LINE_SEP);
    sbuf.append("<title>");
    sbuf.append(title);
    sbuf.append("</title>");
    sbuf.append(LINE_SEP);
    if (cssBuilder == null) {
      CssBuilder.addDefaultCSS(sbuf);
    } else {
      cssBuilder.addExternalCSS(sbuf);
    }
    sbuf.append(LINE_SEP);
    sbuf.append("</head>");
    sbuf.append(LINE_SEP);
    sbuf.append("<body>");
    sbuf.append(LINE_SEP);

    sbuf.append("<hr size=\"1\" noshade=\"true\" />");
    sbuf.append(LINE_SEP);

    sbuf.append("Log session start time ");
    sbuf.append(new java.util.Date());
    sbuf.append("<br />");
    sbuf.append(LINE_SEP);
    sbuf.append("<br />");
    sbuf.append(LINE_SEP);
    sbuf.append("<table cellspacing=\"0\">");
    sbuf.append(LINE_SEP);

    createTableHeader(sbuf);

    return sbuf.toString();
  }

  private void createTableHeader(StringBuffer sbuf) {
    Converter c = head;
    String name;
    sbuf.append("<tr class=\"header\">");
    sbuf.append(LINE_SEP);
    while (c != null) {
      // if (c instanceof ThrowableHandlingConverter) {
      // c = c.getNext();
      // continue;
      // }
      name = computeConverterName(c);
      if (name == null) {
        c = c.getNext();
        continue;
      }
      sbuf.append("<td class=\"");
      sbuf.append(computeConverterName(c));
      sbuf.append("\">");
      sbuf.append(computeConverterName(c));
      sbuf.append("</td>");
      sbuf.append(LINE_SEP);
      c = c.getNext();
    }
    sbuf.append("</tr>");
    sbuf.append(LINE_SEP);
  }

  /**
   * Returns the appropriate HTML footers.
   */
  public String getFooter() {
    StringBuffer sbuf = new StringBuffer();
    sbuf.append("</table>");
    sbuf.append(LINE_SEP);
    sbuf.append("<br>");
    sbuf.append(LINE_SEP);
    sbuf.append("</body></html>");
    return sbuf.toString();
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

    if (throwableRenderer.newLineRequired(event)) {
      buf.append("<tr><td class=\"Exception\" colspan=\"6\">");
      throwableRenderer.render(buf, event);
      buf.append("</td></tr>");
    }
    return buf.toString();
  }

  private void handleTableClosing(StringBuffer sbuf) {
    if (this.counter >= ROW_LIMIT) {
      counter = 0;
      sbuf.append("</table>");
      sbuf.append(LINE_SEP);
      sbuf.append("<br />");
      sbuf.append("<table cellspacing=\"0\">");
      sbuf.append(LINE_SEP);
      createTableHeader(sbuf);
    }
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

  private String computeConverterName(Converter c) {
    String className = c.getClass().getSimpleName();
    int index = className.indexOf("Converter");
    if (index == -1) {
      return className;
    } else {
      return className.substring(0, index);
    }
  }

}
