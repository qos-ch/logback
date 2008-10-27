/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * 
 * Copyright (C) 1999-2006, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
 */
package ch.qos.logback.core.layout;

import ch.qos.logback.core.Layout;
import ch.qos.logback.core.LayoutBase;

/**
 * Echos the incoming object adding a line separator character(s) at the end.
 * 
 * @author Ceki
 */
public class EchoLayout<E> extends LayoutBase<E> {

  public String doLayout(E event) {
    return event+Layout.LINE_SEP;
  }

}
