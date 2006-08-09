/**
 * LOGBack: the reliable, fast and flexible logging library for Java.
 * 
 * Copyright (C) 1999-2006, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
 */
package ch.qos.logback.core.pattern.parser;

import ch.qos.logback.core.pattern.FormatInfo;

public class FormattingNode extends Node {

  FormatInfo formatInfo;

  FormattingNode(int type) {
    super(type);
  }

  FormattingNode(int type, Object value) {
    super(type, value);
  }

  public FormatInfo getFormatInfo() {
    return formatInfo;
  }

  public void setFormatInfo(FormatInfo formatInfo) {
    this.formatInfo = formatInfo;
  }

  public boolean equals(Object o) {
    if (!super.equals(o)) {
      return false;
    }

    if(!(o instanceof FormattingNode)) {
        return false;
    }
    FormattingNode r = (FormattingNode) o;

    return (formatInfo != null ? formatInfo.equals(r.formatInfo)
        : r.formatInfo == null);
  }
}
