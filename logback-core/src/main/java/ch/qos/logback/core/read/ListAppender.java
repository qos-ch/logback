/**
 * LOGBack: the generic, reliable, fast and flexible logging framework.
 *
 * Copyright (C) 1999-2006, QOS.ch
 *
 * This library is free software, you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation.
 */
package ch.qos.logback.core.read;

import java.util.ArrayList;
import java.util.List;

import ch.qos.logback.core.AppenderBase;
import ch.qos.logback.core.Layout;

public class ListAppender extends AppenderBase {

  public List list = new ArrayList();
  
  @SuppressWarnings("unchecked")
  protected void append(Object o) {
    list.add(o);
  }

  public void setLayout(Layout layout) {
  }

  public Layout getLayout() {
    return null;
  }

}
