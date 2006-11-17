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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import ch.qos.logback.core.Appender;

/**
 * A straightforward implementation of the {@link AppenderAttachable} interface.
 * 
 * @author Ceki G&uuml;lc&uuml;
 */
public class AppenderAttachableImpl implements AppenderAttachable {

  final private List<Appender> appenderList = new ArrayList<Appender>();

  /**
   * Attach an appender. If the appender is already in the list in won't be
   * added again.
   */
  public void addAppender(Appender newAppender) {
    // Null values for newAppender parameter are strictly forbidden.
    if (newAppender == null) {
      throw new IllegalArgumentException("Cannot null as an appener");
    }
    if (!appenderList.contains(newAppender)) {
      appenderList.add(newAppender);
    }
  }

  /**
   * Call the <code>doAppend</code> method on all attached appenders.
   */
  public int appendLoopOnAppenders(Object o) {
    int size = 0;
    Appender appender;

    size = appenderList.size();
    for (int i = 0; i < size; i++) {
      appender = (Appender) appenderList.get(i);
      appender.doAppend(o);
    }
    return size;
  }

  /**
   * Get all attached appenders as an Enumeration. If there are no attached
   * appenders <code>null</code> is returned.
   * 
   * @return Enumeration An enumeration of attached appenders.
   */
  public Iterator iteratorForAppenders() {
    return appenderList.iterator();
  }

  /**
   * Look for an attached appender named as <code>name</code>.
   * 
   * <p>
   * Return the appender with that name if in the list. Return null otherwise.
   * 
   */
  public Appender getAppender(String name) {
    if (name == null) {
      return null;
    }

    int size = appenderList.size();
    Appender appender;

    for (int i = 0; i < size; i++) {
      appender = (Appender) appenderList.get(i);

      if (name.equals(appender.getName())) {
        return appender;
      }
    }

    return null;
  }

  /**
   * Returns <code>true</code> if the specified appender is in the list of
   * attached appenders, <code>false</code> otherwise.
   * 
   * @since 1.2
   */
  public boolean isAttached(Appender appender) {
    if (appender == null) {
      return false;
    }

    int size = appenderList.size();
    Appender a;

    for (int i = 0; i < size; i++) {
      a = (Appender) appenderList.get(i);

      if (a == appender) {
        return true;
      }
    }

    return false;
  }

  /**
   * Remove and stop all previously attached appenders.
   */
  public void detachAndStopAllAppenders() {
    int len = appenderList.size();

    for (int i = 0; i < len; i++) {
      Appender a = (Appender) appenderList.get(i);
      a.stop();
    }

    appenderList.clear();
  }

  /**
   * Remove the appender passed as parameter form the list of attached
   * appenders.
   */
  public boolean detachAppender(Appender appender) {
    if (appender == null) {
      return false;
    }
    return appenderList.remove(appender);
  }

  /**
   * Remove the appender with the name passed as parameter form the list of
   * appenders.
   */
  public Appender detachAppender(String name) {
    if (name == null) {
      return null;
    }

    int size = appenderList.size();

    for (int i = 0; i < size; i++) {
      if (name.equals(((Appender) appenderList.get(i)).getName())) {
        return (Appender) appenderList.remove(i);
      }
    }
    return null;
  }
}
