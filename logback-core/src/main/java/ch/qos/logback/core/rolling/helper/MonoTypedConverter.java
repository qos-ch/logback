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

/**
 * Converters which can deal only with one type should implement this interface.
 * 
 * @author Ceki G&ulcu;lc&uuml;
 * 
 */
public interface MonoTypedConverter {
  boolean isApplicable(Object o);
}
