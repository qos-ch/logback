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

import static ch.qos.logback.core.Layout.LINE_SEP;
import ch.qos.logback.core.html.CssBuilder;

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

  public void addCss(StringBuilder sbuf) {
    sbuf.append("<style  type=\"text/css\">");
    sbuf.append("table{ ");
    sbuf.append("margin-left: 2em; ");
    sbuf.append("margin-right: 2em; ");
    sbuf.append("border-left: 2px solid #AAA; ");
    sbuf.append("}");
    sbuf.append(LINE_SEP);
    sbuf.append("TR.even { ");
    sbuf.append("background: #FFFFFF; ");
    sbuf.append("}");
    sbuf.append(LINE_SEP);
    sbuf.append("TR.odd { ");
    sbuf.append("background: #EAEAEA; ");
    sbuf.append("}");
    sbuf.append(LINE_SEP);
    sbuf.append("TD {");
    sbuf.append("padding-right: 1ex; ");
    sbuf.append("padding-left: 1ex; ");
    sbuf.append("border-right: 2px solid #AAA;");
    sbuf.append("}");
    sbuf.append(LINE_SEP);
    sbuf.append("TD.Time, TD.Date { ");
    sbuf.append("text-align: right; ");
    sbuf.append("font-family: courier, monospace; ");
    sbuf.append("font-size: smaller; ");
    sbuf.append("}");
    sbuf.append(LINE_SEP);
    sbuf
        .append("TD.RemoteHost, TD.RequestProtocol, TD.RequestHeader, TD.RequestURL, TD.RemoteUser, TD.RequestURI, TD.ServerName {");
    sbuf.append("text-align: left; ");
    sbuf.append("}");
    sbuf.append(LINE_SEP);
    sbuf
        .append("TD.RequestAttribute, TD.RequestCookie, TD.ResponseHeader, TD.RequestParameter {");
    sbuf.append("text-align: left; ");
    sbuf.append("}");
    sbuf.append(LINE_SEP);
    sbuf
        .append("TD.RemoteIPAddress, TD.LocalIPAddress, TD.ContentLength, TD.StatusCode, TD.LocalPort {");
    sbuf.append("text-align: right; ");
    sbuf.append("}");
    sbuf.append(LINE_SEP);
    sbuf.append("TR.header { ");
    sbuf.append("background: #596ED5; ");
    sbuf.append("color: #FFF; ");
    sbuf.append("font-weight: bold; ");
    sbuf.append("font-size: larger; ");
    sbuf.append("}");
    sbuf.append(LINE_SEP);
    sbuf.append("  }");
    sbuf.append("}");
    sbuf.append("</style>");
  }
}