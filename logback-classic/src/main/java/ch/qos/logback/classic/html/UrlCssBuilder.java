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

import ch.qos.logback.core.html.CssBuilder;


/**
 * This class helps the HTMLLayout build the CSS link.
 * It either provides the HTMLLayout with a default css file,
 * or builds the link to an external, user-specified, file.
 *
 * @author S&eacute;bastien Pennec
 */
public class UrlCssBuilder implements CssBuilder {

  String url = "http://logback.qos.ch/css/classic.css";
  
  public String getUrl() {
    return url;
  }
  
  public void setUrl(String url) {
    this.url = url;
  }
  
  public void addCss(StringBuilder sbuf) {
    sbuf.append("<link REL=StyleSheet HREF=\"");
    sbuf.append(url);
    sbuf.append("\" TITLE=\"Basic\" />");
  }
}
