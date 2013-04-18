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
package ch.qos.logback.core.sift;

import java.util.List;

import ch.qos.logback.core.Appender;
import ch.qos.logback.core.CoreConstants;

public interface AppenderTracker<E> {

  int THRESHOLD = 30 * 60 * CoreConstants.MILLIS_IN_ONE_SECOND; // 30 minutes

  void put(String key, Appender<E> value, long timestamp);
  Appender<E> get(String key, long timestamp);
  void stopStaleAppenders(long timestamp);
  List<String> keyList();
  List<Appender<E>> valueList();
  void stopAndRemoveNow(String key);
}