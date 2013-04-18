/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2013, QOS.ch. All rights reserved.
 *
 * This program and the accompanying materials are dual-licensed under
 * either the terms of the Eclipse Public License v1.0 as published by
 * the Eclipse Foundation
 *
 *   or (per the licensee's choosing)
 *
 * under the terms of the GNU Lesser General Public License version 2.1
 * as published by the Free Software Foundation.
 */
package ch.qos.logback.classic.html;


import ch.qos.logback.core.CoreConstants;
import static ch.qos.logback.core.CoreConstants.LINE_SEPARATOR;
import ch.qos.logback.core.html.CssBuilder;

/**
 * This class helps the HTMLLayout build the CSS link. It either provides the
 * HTMLLayout with a default css file, or builds the link to an external,
 * user-specified, file.
 * 
 * @author S&eacute;bastien Pennec
 */
public class DefaultCssBuilder implements CssBuilder {

  public void addCss(StringBuilder sbuf) {
    sbuf.append("<style  type=\"text/css\">");
    sbuf.append(LINE_SEPARATOR);
    sbuf
        .append("table { margin-left: 2em; margin-right: 2em; border-left: 2px solid #AAA; }");
    sbuf.append(LINE_SEPARATOR);

    sbuf.append("TR.even { background: #FFFFFF; }");
    sbuf.append(LINE_SEPARATOR);

    sbuf.append("TR.odd { background: #EAEAEA; }");
    sbuf.append(LINE_SEPARATOR);

    sbuf
        .append("TR.warn TD.Level, TR.error TD.Level, TR.fatal TD.Level {font-weight: bold; color: #FF4040 }");
    sbuf.append(CoreConstants.LINE_SEPARATOR);

    sbuf
        .append("TD { padding-right: 1ex; padding-left: 1ex; border-right: 2px solid #AAA; }");
    sbuf.append(LINE_SEPARATOR);

    sbuf
        .append("TD.Time, TD.Date { text-align: right; font-family: courier, monospace; font-size: smaller; }");
    sbuf.append(LINE_SEPARATOR);

    sbuf.append("TD.Thread { text-align: left; }");
    sbuf.append(LINE_SEPARATOR);

    sbuf.append("TD.Level { text-align: right; }");
    sbuf.append(LINE_SEPARATOR);

    sbuf.append("TD.Logger { text-align: left; }");
    sbuf.append(LINE_SEPARATOR);

    sbuf
        .append("TR.header { background: #596ED5; color: #FFF; font-weight: bold; font-size: larger; }");
    sbuf.append(CoreConstants.LINE_SEPARATOR);

    sbuf
        .append("TD.Exception { background: #A2AEE8; font-family: courier, monospace;}");
    sbuf.append(LINE_SEPARATOR);

    sbuf.append("</style>");
    sbuf.append(LINE_SEPARATOR);
  }
}
