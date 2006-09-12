/**
 * LOGBack: the reliable, fast and flexible logging library for Java.
 *
 * Copyright (C) 1999-2006, QOS.ch
 *
 * This library is free software, you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation.
 */
package ch.qos.logback.core;

import ch.qos.logback.core.spi.ContextAwareBase;

abstract public class LayoutBase extends ContextAwareBase implements Layout  {

  Context context;
  protected boolean started;
  
  String header;
  String footer;
  
  public void setContext(Context context) {
    this.context = context;
  }

  public Context getContext() {
    return this.context;
  }

  public void start() {
    started = true;
  }

  public void stop() {
    started = false;
  }
  
  public boolean isStarted() {
    return started;
  }
  
  public String getHeader() {
    return header;
  }
  
  public String getFooter() {
    return footer;
  }

  public String getContentType() {
    return "text/plain";
  }
  
  public void setHeader(String header) {
    this.header = header;
  }

  public void setFooter(String footer) {
    this.footer = footer;
  }
}
