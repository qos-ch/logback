/**
 * Logback: the generic, reliable, fast and flexible logging framework.
 * 
 * Copyright (C) 2000-2008, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
 */
package ch.qos.logback.core.sift;

import java.util.List;

import ch.qos.logback.core.Appender;

public interface AppenderTracker<E, K> {

  static int MILLIS_IN_ONE_SECOND = 1000;
  static int THRESHOLD = 30 * 60 * MILLIS_IN_ONE_SECOND; // 30 minutes

  void put(K key, Appender<E> value, long timestamp);
  Appender<E> get(K key, long timestamp);
  void stopStaleAppenders(long timestamp);
  List<K> keyList();
  List<Appender<E>> valueList();


}