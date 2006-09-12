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
import ch.qos.logback.classic.helpers.Transform;
import ch.qos.logback.classic.pattern.ThrowableHandlingConverter;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.classic.spi.ThrowableInformation;
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

  static final String TRACE_PREFIX = "<br />&nbsp;&nbsp;&nbsp;&nbsp;";
  protected final int BUF_SIZE = 256;
  protected final int MAX_CAPACITY = 1024;
  
  private String pattern;
  
  private Converter head;
  
  //private String timezone;
  private String title = "Logback Log Messages";

  private CssBuilder cssBuilder;
  private boolean internalCSS = false;
  private String url2ExternalCSS = "http://logging.apache.org/log4j/docs/css/eventTable-1.0.css";

  // Does our PatternConverter chain handle throwable on its own?
  private boolean chainHandlesThrowable;

  // counter keeping track of the rows output
  private long counter = 0;
  //max number of rows before we close the table and create a new one
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
      postCompileProcessing(head);
      DynamicConverter.startConverters(this.head);
    }  catch (ScanException ex) {
      addError("Incorrect pattern found", ex);
    }
    
    started = true;
  }
  
  private void postCompileProcessing(Converter c) {
    while (c != null) {
      if (c instanceof ThrowableHandlingConverter) {
        chainHandlesThrowable = true;
      }
      c = c.getNext();
    }
    chainHandlesThrowable = false;
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
  public String getContentType() {
    return "text/html";
  }

  void appendThrowableAsHTML(final String[] s, final StringBuffer sbuf) {
    if (s != null) {
      int len = s.length;
      if (len == 0) {
        return;
      }
      sbuf.append(Transform.escapeTags(s[0]));
      sbuf.append(LINE_SEP);
      for (int i = 1; i < len; i++) {
        sbuf.append(TRACE_PREFIX);
        sbuf.append(Transform.escapeTags(s[i]));
        sbuf.append(LINE_SEP);
      }
    }
  }

  /**
   * Returns appropriate HTML headers.
   */
  public String getHeader() {
    StringBuffer sbuf = new StringBuffer();
    sbuf.append("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\"");
    sbuf.append(" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">");
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

  /**
   * The HTML layout handles the throwable contained in logging events. Hence,
   * this method return <code>false</code>.
   */
  public boolean ignoresThrowable() {
    return false;
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
      if (c instanceof ThrowableHandlingConverter) {
        ThrowableHandlingConverter converter = (ThrowableHandlingConverter)c;
        if (converter.onNewLine(event)) {
          buf.append("</tr>");
          buf.append("<tr>");
          appendEventToBuffer(buf, c, event);
          if (c.getNext() != null) {
            //here we assume that when we exist the while loop,
            //a </tr> tag is added.
            buf.append("</tr>");
            buf.append("<tr>");
          }
        }
      } else {
        appendEventToBuffer(buf, c, event);
      }
      c = c.getNext();
    }
    buf.append("</tr>");
    buf.append(LINE_SEP);

    // if the pattern chain handles throwables then no need to do it again here.
    if (!chainHandlesThrowable) {
      ThrowableInformation ti = event.getThrowableInformation();
      if (ti != null) {
        String[] s = ti.getThrowableStrRep();
        if (s != null) {
          buf.append("<tr><td class=\"Exception\" colspan=\"6\">");
          appendThrowableAsHTML(s, buf);
          buf.append("</td></tr>");
          buf.append(LINE_SEP);
        }
      }
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
  
  private void appendEventToBuffer(StringBuffer buf, Converter c, LoggingEvent event) {
    buf.append("<td class=\"");
    buf.append(computeConverterName(c));
    buf.append("\">");
    buf.append(c.convert(event));
    buf.append("</td>");
    buf.append(LINE_SEP);    
  }

  /**
   * Generate an internal CSS file.
   * 
   * @param buf The StringBuffer where the CSS file will be placed.
   */
  void getInternalCSS(StringBuffer buf) {

    buf.append("<STYLE  type=\"text/css\">");
    buf.append(LINE_SEP);
    buf.append("table { margin-left: 2em; margin-right: 2em; border-left: 2px solid #AAA; }");
    buf.append(LINE_SEP);

    buf.append("TR.even { background: #FFFFFF; }");
    buf.append(LINE_SEP);

    buf.append("TR.odd { background: #DADADA; }");
    buf.append(LINE_SEP);

    buf.append("TR.warn TD.level, TR.error TD.level, TR.fatal TD.level {font-weight: bold; color: #FF4040 }");
    buf.append(LINE_SEP);

    buf.append("TD { padding-right: 1ex; padding-left: 1ex; border-right: 2px solid #AAA; }");
    buf.append(LINE_SEP);

    buf.append("TD.Time, TD.Date { text-align: right; font-family: courier, monospace; font-size: smaller; }");
    buf.append(LINE_SEP);

    buf.append("TD.Thread { text-align: left; }");
    buf.append(LINE_SEP);

    buf.append("TD.Level { text-align: right; }");
    buf.append(LINE_SEP);

    buf.append("TD.Logger { text-align: left; }");
    buf.append(LINE_SEP);

    buf.append("TR.header { background: #9090FF; color: #FFF; font-weight: bold; font-size: larger; }");
    buf.append(LINE_SEP);

    buf.append("TD.Exception { background: #C0C0F0; font-family: courier, monospace;}");
    buf.append(LINE_SEP);

    buf.append("</STYLE>");
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
