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

abstract public class FormattingConverter extends Converter {

  static final int INITIAL_BUF_SIZE = 256;
  static final int MAX_CAPACITY = 1024;

  
  FormatInfo formattingInfo;

  final public FormatInfo getFormattingInfo() {
    return formattingInfo;
  }

  final public void setFormattingInfo(FormatInfo formattingInfo) {
    if (this.formattingInfo != null) {
      throw new IllegalStateException("FormattingInfo has been already set");
    }
    this.formattingInfo = formattingInfo;
  }

  final public void write(StringBuffer buf, Object event) {
    String s = convert(event);
    
    if(formattingInfo == null) {
      buf.append(s);
      return;
    }
    
    int min = formattingInfo.getMin();
    int max = formattingInfo.getMax();


    if (s == null) {
      if (0 < min)
        spacePad(buf, min);
      return;
    }

    int len = s.length();

    if (len > max) {
      if(formattingInfo.isLeftTruncate()) {
        buf.append(s.substring(len - max));
      } else {
        buf.append(s.substring(0, max));
      }
    } else if (len < min) {
      if (formattingInfo.isLeftPad()) {
        spacePad(buf, min - len);
        buf.append(s);
      } else {
        buf.append(s);
        spacePad(buf, min - len);
      }
    } else {
      buf.append(s);
    }
  }

  final static String[] SPACES = { " ", "  ", "    ", "        ", // 1,2,4,8 spaces
      "                ", // 16 spaces
      "                                " }; // 32 spaces

  /**
   * Fast space padding method.
   */
  static public void spacePad(StringBuffer sbuf, int length) {
    while (length >= 32) {
      sbuf.append(SPACES[5]);
      length -= 32;
    }

    for (int i = 4; i >= 0; i--) {
      if ((length & (1 << i)) != 0) {
        sbuf.append(SPACES[i]);
      }
    }
  }
}
