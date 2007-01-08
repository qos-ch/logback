/**
 * LOGBack: the generic, reliable, fast and flexible logging framework.
 *
 * Copyright (C) 1999-2006, QOS.ch
 *
 * This library is free software, you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation.
 */
package ch.qos.logback.core.spi;

import java.util.Iterator;

import ch.qos.logback.core.Appender;

/**
 * Interface for attaching appenders to objects.
 * 
 * @author Ceki G&uuml;lc&uuml;
 */
public interface AppenderAttachable<E> {
  /**
   * Add an appender.
   */
  public void addAppender(Appender<E> newAppender);

  /**
   * Get an iterator for appenders contained in the parent object.
   */
  public Iterator iteratorForAppenders();

  /**
   * Get an appender by name.
   */
  public Appender<E> getAppender(String name);

  /**
   * Returns <code>true</code> if the specified appender is in list of
   * attached attached, <code>false</code> otherwise.
   */
  public boolean isAttached(Appender<E> appender);

  /**
   * Detach all previously added appenders.
   */
  void detachAndStopAllAppenders();

  /**
   * Detach the appender passed as parameter from the list of appenders.
   */
  boolean detachAppender(Appender<E> appender);

  /**
   * Detach the appender with the name passed as parameter from the list of
   * appenders.
   */
  Appender detachAppender(String name);
}
