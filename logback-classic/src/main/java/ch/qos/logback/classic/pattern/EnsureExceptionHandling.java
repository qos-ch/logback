/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2013, QOS.ch. All rights reserved.
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
package ch.qos.logback.classic.pattern;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.pattern.Converter;
import ch.qos.logback.core.pattern.ConverterUtil;
import ch.qos.logback.core.pattern.PostCompileProcessor;

public class EnsureExceptionHandling implements
    PostCompileProcessor<ILoggingEvent> {

  /**
   * This implementation checks if any of the converters in the chain handles
   * exceptions. If not, then this method adds a
   * {@link ExtendedThrowableProxyConverter} instance to the end of the chain.
   * <p>
   * This allows appenders using this layout to output exception information
   * event if the user forgets to add %ex to the pattern. Note that the
   * appenders defined in the Core package are not aware of exceptions nor
   * LoggingEvents.
   * <p>
   * If for some reason the user wishes to NOT print exceptions, then she can
   * add %nopex to the pattern.
   * 
   * 
   */
  public void process(Converter<ILoggingEvent> head) {
    if(head == null) {
      // this should never happen
      throw new IllegalArgumentException("cannot process empty chain");
    }
    if (!chainHandlesThrowable(head)) {
      Converter<ILoggingEvent> tail = ConverterUtil.findTail(head);
      Converter<ILoggingEvent> exConverter = new ExtendedThrowableProxyConverter();
      tail.setNext(exConverter);
    }
  }

  /**
   * This method computes whether a chain of converters handles exceptions or
   * not.
   * 
   * @param head
   *                The first element of the chain
   * @return true if can handle throwables contained in logging events
   */
  public boolean chainHandlesThrowable(Converter head) {
    Converter c = head;
    while (c != null) {
      if (c instanceof ThrowableHandlingConverter) {
        return true;
      }
      c = c.getNext();
    }
    return false;
  }
}
