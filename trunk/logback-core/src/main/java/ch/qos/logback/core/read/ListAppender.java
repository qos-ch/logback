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

public class ListAppender<E> extends AppenderBase<E> {

  public List<E> list = new ArrayList<E>();
  
  protected void append(E e) {
    list.add(e);
  }
}
