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
import java.util.concurrent.atomic.AtomicLong;

/**
 * A converters based on a a locally incremented sequence number. The sequence number is
 * initialized to the number of milliseconds elapsed since 1970-01-01 until this instance
 * is initialized.
 *
 * <p>
 * <b>EXPERIMENTAL</b> This class is experimental and may be removed in the future.
 *
 */
public class LocalSequenceNumberConverter extends ClassicConverter {

  AtomicLong sequenceNumber = new AtomicLong(System.currentTimeMillis());

  @Override
  public String convert(ILoggingEvent event) {
    return Long.toString(sequenceNumber.getAndIncrement());
  }
}
