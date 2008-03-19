/**
 * LOGBack: the reliable, fast and flexible logging library for Java.
 * 
 * Copyright (C) 1999-2006, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
 */

package ch.qos.logback.core.rolling;

import java.io.File;

import ch.qos.logback.core.spi.LifeCycle;


/**
 * A <code>TriggeringPolicy</code> controls the conditions under which roll-over
 * occurs. Such conditions include time of day, file size, an 
 * external event, the log request or a combination thereof.
 *
 * @author Ceki G&uuml;lc&uuml;
 * */

public interface TriggeringPolicy<E> extends LifeCycle {
  
  /**
   * Should roll-over be triggered at this time?
   * 
   * @param activeFile A reference to the currently active log file. 
   * @param event A reference to the currently event. 
   * @return true if a roll-over should occur.
   */
  public boolean isTriggeringEvent(final File activeFile, final E event);
}
