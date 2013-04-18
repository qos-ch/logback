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
package ch.qos.logback.classic.boolex;

import ch.qos.logback.classic.spi.ILoggingEvent;

/**
 * An <b>internal</b> interface used by the GEventEvaluator.
 * 
 * @author Ceki G&uuml;c&uuml;
 */
public interface IEvaluator {
  boolean doEvaluate(ILoggingEvent event);
}
 