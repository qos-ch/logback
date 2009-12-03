/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2009, QOS.ch. All rights reserved.
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
package chapter3;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Foo {
  static final Logger logger = LoggerFactory.getLogger(Foo.class);

  public void doIt() {
    logger.debug("Did it again!");
  }
}
