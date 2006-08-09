/**
 * LOGBack: the reliable, fast and flexible logging library for Java.
 * 
 * Copyright (C) 1999-2006, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
 */
package ch.qos.logback.core.pattern;

public class CompositeConverter extends FormattingConverter {

  StringBuffer buf = new StringBuffer();
  Converter childConverter;

  public String convert(Object event) {
    if (buf.capacity() > MAX_CAPACITY) {
      buf = new StringBuffer(INITIAL_BUF_SIZE);
    } else {
      buf.setLength(0);
    }

    for (Converter c = childConverter; c != null; c = c.next) {
      c.write(buf, event);
    }
    return buf.toString();
  }

  public void setChildConverter(Converter child) {
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
