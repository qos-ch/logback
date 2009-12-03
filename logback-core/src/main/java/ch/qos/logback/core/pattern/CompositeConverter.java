/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2009, QOS.ch. All rights reserved.
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
package ch.qos.logback.core.pattern;

public class CompositeConverter<E> extends FormattingConverter<E> {

  StringBuffer buf = new StringBuffer();
  Converter<E> childConverter;

  public String convert(E event) {
    if (buf.capacity() > MAX_CAPACITY) {
      buf = new StringBuffer(INITIAL_BUF_SIZE);
    } else {
      buf.setLength(0);
    }

    for (Converter<E> c = childConverter; c != null; c = c.next) {
      c.write(buf, event);
    }
    return buf.toString();
  }

  public void setChildConverter(Converter<E> child) {
    childConverter = child;
  }

  public String toString() {
    StringBuffer buf = new StringBuffer();
    buf.append("CompositeConverter<");
    
    if(formattingInfo != null)
    buf.append(formattingInfo);
    
    if (childConverter != null) {
      buf.append(", children: "+childConverter);
    }
    buf.append(">");
    return buf.toString();
  }
}
