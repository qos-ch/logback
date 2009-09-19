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
