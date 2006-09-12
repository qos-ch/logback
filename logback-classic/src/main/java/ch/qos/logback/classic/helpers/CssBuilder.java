/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * 
 * Copyright (C) 1999-2006, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
 */
package ch.qos.logback.classic.helpers;

import static ch.qos.logback.core.Layout.LINE_SEP;

/**
 * This class helps the HTMLLayout build the CSS link.
 * It either provides the HTMLLayout with a default css file,
 * or builds the link to an external, user-specified, file.
 *
 * @author S&eacute;bastien Pennec
 */
public class CssBuilder {

  String url;
  
  public CssBuilder() {  
  }
  
  public String getUrl() {
    return url;
  }
  
  public void setUrl(String url) {
    this.url = url;
  }
  
  public void addExternalCSS(StringBuffer sbuf) {
    sbuf.append("<LINK REL=StyleSheet HREF=\"");
    sbuf.append(url);
    sbuf.append("\" TITLE=\"Basic\" />");
  }
  
  public static void addDefaultCSS(StringBuffer buf) {
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
}
