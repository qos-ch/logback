/**
 * LOGBack: the reliable, fast and flexible logging library for Java.
 *
 * Copyright (C) 1999-2006, QOS.ch
 *
 * This library is free software, you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation.
 */
package ch.qos.logback.classic.pattern;

import ch.qos.logback.classic.spi.LoggingEvent;

public abstract class NamedConverter extends ClassicConverter {

  Abbreviator abbreviator = null;

  /**
   * Gets fully qualified name from event.
   * 
   * @param event
   *          The LoggingEvent to process, cannot not be null.
   * @return name, must not be null.
   */
  protected abstract String getFullyQualifiedName(final LoggingEvent event);

  public void start() {
    String optStr = getFirstOption();
    if (optStr != null) {
      try {
        int targetLen = Integer.parseInt(optStr);
        if (targetLen > 0) {
          abbreviator = new ClassNameAbbreviator(targetLen);
        }
      } catch (NumberFormatException nfe) {
        // FIXME: better error reporting
      }
    }
  }

  public String convert(Object event) {
    String fqn = getFullyQualifiedName((LoggingEvent) event);

    if (abbreviator == null) {
      return fqn;
    } else {
      return abbreviator.abbreviate(fqn);
    }
  }
}
