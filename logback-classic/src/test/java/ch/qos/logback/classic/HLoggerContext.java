/**
 * LOGBack: the reliable, fast and flexible logging library for Java.
 *
 * Copyright (C) 1999-2005, QOS.ch, LOGBack.com
 *
 * This library is free software, you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation.
 */

package ch.qos.logback.classic;

import ch.qos.logback.classic.Level;



/**
 * @author ceki
 */
public class HLoggerContext {

  private HLogger root;
  private int size;

  public HLoggerContext() {
    this.root = new HLogger("root", null);
    this.root.setLevel(Level.DEBUG);
    size = 1;
  }

  /**
   * Return this contexts root logger
   *
   * @return
   */
  public HLogger getRootLogger() {
    return root;
  }

  public HLogger getLogger(final String name) {

    int i = 0;
    HLogger HLogger = root;
    HLogger childHLogger = null;
    String childName;

    while (true) {
      int h = name.indexOf('.', i);
      if (h == -1) {
        childName = name.substring(i);
      } else {
        childName = name.substring(i, h);
      }
      // move i left of the last point
      i = h + 1;

      synchronized (HLogger) {
        childHLogger = HLogger.getChildBySuffix(childName);
        if (childHLogger == null) {
          childHLogger = HLogger.createChildByLastNamePart(childName);
          incSize();
        }
      }
      HLogger = childHLogger;
      if (h == -1) {
        return childHLogger;
      }
    }
  }

  private synchronized  void incSize() {
    size++;
  }

  int size() {
    return size;
  }
  /**
   * Check if the named logger exists in the hierarchy. If so return
   * its reference, otherwise returns <code>null</code>.
   *
   * @param name the name of the logger to search for.
   */
  HLogger exists(String name) {
    int i = 0;
    HLogger HLogger = root;
    HLogger childHLogger = null;
    String childName;
    while (true) {
      int h = name.indexOf('.', i);
      if (h == -1) {
        childName = name.substring(i);
      } else {
        childName = name.substring(i, h);
      }
      // move i left of the last point
      i = h + 1;

      synchronized (HLogger) {
        childHLogger = HLogger.getChildBySuffix(childName);
        if (childHLogger == null) {
          return null;
        }
      }
      HLogger = childHLogger;
      if (h == -1) {
        if (childHLogger.getName().equals(name)) {
          return childHLogger;
        } else {
          return null;
        }
      }
    }
  }
}
