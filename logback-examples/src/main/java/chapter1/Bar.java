/**
 * Logback: the generic, reliable, fast and flexible logging framework.
 * 
 * Copyright (C) 1999-2006, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
 */
package chapter1;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class Bar {
  Logger logger = LoggerFactory.getLogger(Bar.class);

  public void doIt() {
    logger.debug("doing my job");
  }
}