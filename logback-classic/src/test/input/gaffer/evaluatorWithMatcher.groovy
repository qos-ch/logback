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
//
// Built on Wed May 19 20:51:44 CEST 2010 by logback-translator
// For more information on configuration files in Groovy
// please see http://logback.qos.ch/manual/groovy.html
//

import ch.qos.logback.classic.boolex.JaninoEventEvaluator
import ch.qos.logback.classic.encoder.PatternLayoutEncoder
import ch.qos.logback.core.ConsoleAppender
import ch.qos.logback.core.filter.EvaluatorFilter

import static ch.qos.logback.classic.Level.DEBUG
import static ch.qos.logback.core.spi.FilterReply.DENY
import static ch.qos.logback.core.spi.FilterReply.NEUTRAL
import ch.qos.logback.core.boolex.Matcher
import ch.qos.logback.core.spi.LifeCycle

appender("STDOUT", ConsoleAppender) {
  filter(EvaluatorFilter) {
    evaluator(JaninoEventEvaluator) {
      Matcher aMatcher = new Matcher()
      aMatcher.name = "odd"
      aMatcher.regex = "statement [13579]"
      if(aMatcher instanceof LifeCycle)
        aMatcher.start();
      aMatcher.start();
      matcher = aMatcher
      expression = "odd.matches(formattedMessage)"
    }
    OnMismatch = NEUTRAL
    OnMatch = DENY
  }
  encoder(PatternLayoutEncoder) {
    pattern = "%-4relative [%thread] %-5level %logger - %msg%n"
  }
}
root(DEBUG, ["STDOUT"])
