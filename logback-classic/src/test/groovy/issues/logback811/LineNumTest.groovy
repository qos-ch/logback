/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2015, QOS.ch. All rights reserved.
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
package issues.logback811

import ch.qos.logback.core.util.StatusPrinter
import org.junit.Test
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class LineNumTest {

  // move logback.groovy to src/test/resources and runManually()

  @Test
  void runMannually() {
    Logger logger = LoggerFactory.getLogger(this.class)
    logger.debug("hello from logger on line 28")
  }
}
