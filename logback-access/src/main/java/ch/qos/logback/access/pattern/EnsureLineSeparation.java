/**
 * Logback: the generic, reliable, fast and flexible logging framework.
 * 
 * Copyright (C) 2000-2008, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
 */
package ch.qos.logback.access.pattern;

import ch.qos.logback.access.spi.AccessEvent;
import ch.qos.logback.core.pattern.Converter;
import ch.qos.logback.core.pattern.ConverterUtil;
import ch.qos.logback.core.pattern.PostCompileProcessor;

public class EnsureLineSeparation implements PostCompileProcessor<AccessEvent> {

  /**
   * Add a line separator converter so that access event appears on a separate
   * line.
   */
  public void process(Converter<AccessEvent> head) {
    Converter<AccessEvent> tail = ConverterUtil.findTail(head);
    Converter<AccessEvent> newLineConverter = new LineSeparatorConverter();
    if (tail == null) {
      head = newLineConverter;
    } else {
      if (!(tail instanceof LineSeparatorConverter)) {
        tail.setNext(newLineConverter);
      }
    }
  }
}
