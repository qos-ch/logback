/**
 * Logback: the generic, reliable, fast and flexible logging framework.
 * 
 * Copyright (C) 2000-2008, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
 */

package org.slf4j.impl;

/**
 * This class grants public access to package protected methods of
 * StaticLoggerBinder. Used for testing purposes.
 * 
 * @author Ceki Gulcu
 * 
 */
public class StaticLoggerBinderFriend {
 
  static public void reset() {
    StaticLoggerBinder.reset();
  }

}
