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
import ch.qos.logback.core.pattern.DynamicConverter;

/**
 * This class serves the super-class of all converters in logback. It extends
 * {@link DynamicConverter}.
 *
 * @author Ceki Gulcu
 */
abstract public class ClassicConverter extends DynamicConverter<ILoggingEvent>  {

}
