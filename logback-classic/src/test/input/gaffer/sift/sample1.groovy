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
/**
 * @author Ceki G&uuml;c&uuml;
 */

import ch.qos.logback.classic.encoder.PatternLayoutEncoder

import static ch.qos.logback.classic.Level.DEBUG
import ch.qos.logback.classic.sift.GSiftingAppender
import ch.qos.logback.classic.sift.MDCBasedDiscriminator
import ch.qos.logback.core.FileAppender
import static ch.qos.logback.classic.ClassicTestConstants.OUTPUT_DIR_PREFIX;

appender("SIFT", GSiftingAppender) {
  discriminator(MDCBasedDiscriminator) {
    key = "userid"
    defaultValue = "unknown"
  }
  sift {
    appender("FILE-${userid}", FileAppender) {
      file = OUTPUT_DIR_PREFIX+"test-${userid}.log"
      append = false
      encoder(PatternLayoutEncoder) {
        println "in encoder userid=${userid}"
        pattern = "${userid} - %msg%n"
      }
    }
  }
}

root(DEBUG, ["SIFT"])
