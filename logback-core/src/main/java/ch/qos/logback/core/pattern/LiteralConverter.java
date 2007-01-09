/**
 * LOGBack: the reliable, fast and flexible logging library for Java.
 *
 * Copyright (C) 1999-2005, QOS.ch, LOGBack.com
 *
 * This library is free software, you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation.
 */
package ch.qos.logback.core.pattern;

public final class LiteralConverter<E> extends Converter<E> {

  String literal;

  public LiteralConverter(String literal) {
    this.literal = literal;
  }

  public String convert(E o) {
    return literal;
  }
  
  
}
