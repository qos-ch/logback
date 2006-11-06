/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * 
 * Copyright (C) 1999-2006, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
 */

package chapter4.mail;

import java.io.File;
import ch.qos.logback.core.rolling.TriggeringPolicyBase;


/**
 *  A simple TriggeringPolicy implementation that triggers
 *  email transmission after 1024 events regardless of event level.
 * */
public class CounterBasedTP extends TriggeringPolicyBase {
  boolean started;
  static int LIMIT = 1024;
  int counter = 0;

  public boolean isTriggeringEvent(File file, Object event) {
    counter++;

    if (counter == LIMIT) {
      counter = 0;

      return true;
    } else {
      return false;
    }
  }
}
