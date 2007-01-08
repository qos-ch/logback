/**
 * LOGBack: the reliable, fast and flexible logging library for Java.
 *
 * Copyright (C) 1999-2006, QOS.ch
 *
 * This library is free software, you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation.
 */
package ch.qos.logback.core.layout;

import ch.qos.logback.core.LayoutBase;

public class DummyLayout<E> extends LayoutBase<E> {

  public static final String DUMMY = "dummy"+LINE_SEP;
  
  public String doLayout(E event) {
    return DUMMY;
  }

}
