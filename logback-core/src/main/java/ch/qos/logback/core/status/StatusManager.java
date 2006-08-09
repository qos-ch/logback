/**
 * LOGBack: the reliable, fast and flexible logging library for Java.
 *
 * Copyright (C) 1999-2006, QOS.ch
 *
 * This library is free software, you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation.
 */
package ch.qos.logback.core.status;

import java.util.Iterator;


public interface StatusManager {
  public void add(Status status);
  public Iterator iterator();
  public int getLevel();
  /**
   * Return the number of entries.
   * @return
   */
  public int getCount();
}
