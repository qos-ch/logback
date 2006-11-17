/**
 * Logback: the generic, reliable, fast and flexible logging framework.
 * 
 * Copyright (C) 1999-2006, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
 */
package ch.qos.logback.core.read;

import ch.qos.logback.core.AppenderBase;
import ch.qos.logback.core.Layout;
import ch.qos.logback.core.helpers.CyclicBuffer;

public class CyclicBufferAppender extends AppenderBase {

  CyclicBuffer cb;
  int maxSize = 512;

  public void start() {
    cb = new CyclicBuffer(maxSize);
    super.start();
  }

  public void stop() {
    cb = null;
    super.stop();
  }

  @Override
  protected void append(Object eventObject) {
    if (!isStarted()) {
      return;
    }
    cb.add(eventObject);
  }

  public int getLength() {
    if (cb != null) {
      return cb.length();
    } else {
      return 0;
    }
  }

  public Object get(int i) {
    if (cb != null) {
      return cb.get(i);
    } else {
      return null;
    }
  }

  public Layout getLayout() {
    return null;
  }

  public void setLayout(Layout layout) {
  }

  public int getMaxSize() {
    return maxSize;
  }

  public void setMaxSize(int maxSize) {
    this.maxSize = maxSize;
  }

}
