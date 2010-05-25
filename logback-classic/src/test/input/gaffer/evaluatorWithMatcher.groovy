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
