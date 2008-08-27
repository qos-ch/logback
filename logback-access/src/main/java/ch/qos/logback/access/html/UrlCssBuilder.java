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

import ch.qos.logback.core.html.CssBuilder;


/**
 * This class helps the HTMLLayout build the CSS link.
 * It either provides the HTMLLayout with a default css file,
 * or builds the link to an external, user-specified, file.
 *
 * @author S&eacute;bastien Pennec
 */
public class UrlCssBuilder implements CssBuilder {

  String url = "http://logback.qos.ch/css/access.css";
  
  public UrlCssBuilder() {  
  }
  
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
