/**
 * Logback: the generic, reliable, fast and flexible logging framework.
 * 
 * Copyright (C) 2000-2009, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
 */
package ch.qos.logback.core.rolling.helper;

class SDFToken {
  final char c;
  int occurrences;

  public SDFToken(char c) {
    this.c = c;
    this.occurrences = 1;
  }

  void inc() {
    occurrences++;
  }
  
  String toRegex() {
    switch(c) {
    case 'G':
      return null;
    }
  }
}