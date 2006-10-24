/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * 
 * Copyright (C) 1999-2006, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
 */
package ch.qos.logback.core.html;

import static ch.qos.logback.core.Layout.LINE_SEP;

/**
 * This class helps the HTMLLayout build the CSS link. It either provides the
 * HTMLLayout with a default css file, or builds the link to an external,
 * user-specified, file.
 * 
 * @author S&eacute;bastien Pennec
 */
public class DefaultCssBuilder implements CssBuilder {

  public DefaultCssBuilder() {
  }

  public void addCss(StringBuffer sbuf) {
    sbuf.append("<STYLE  type=\"text/css\">");
    sbuf.append(LINE_SEP);
    sbuf
        .append("table { margin-left: 2em; margin-right: 2em; border-left: 2px solid #AAA; }");
    sbuf.append(LINE_SEP);

    sbuf.append("TR.even { background: #FFFFFF; }");
    sbuf.append(LINE_SEP);

    sbuf.append("TR.odd { background: #EAEAEA; }");
    sbuf.append(LINE_SEP);

    sbuf
        .append("TR.warn TD.Level, TR.error TD.Level, TR.fatal TD.Level {font-weight: bold; color: #FF4040 }");
    sbuf.append(LINE_SEP);

    sbuf
        .append("TD { padding-right: 1ex; padding-left: 1ex; border-right: 2px solid #AAA; }");
    sbuf.append(LINE_SEP);

    sbuf
        .append("TD.Time, TD.Date { text-align: right; font-family: courier, monospace; font-size: smaller; }");
    sbuf.append(LINE_SEP);

    sbuf.append("TD.Thread { text-align: left; }");
    sbuf.append(LINE_SEP);

    sbuf.append("TD.Level { text-align: right; }");
    sbuf.append(LINE_SEP);

    sbuf.append("TD.Logger { text-align: left; }");
    sbuf.append(LINE_SEP);

    sbuf
        .append("TR.header { background: #596ED5; color: #FFF; font-weight: bold; font-size: larger; }");
    sbuf.append(LINE_SEP);

    sbuf
        .append("TD.Exception { background: #A2AEE8; font-family: courier, monospace;}");
    sbuf.append(LINE_SEP);

    sbuf.append("</STYLE>");
    sbuf.append(LINE_SEP);
  }
}
