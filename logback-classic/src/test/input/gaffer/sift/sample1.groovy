/**
 * @author Ceki G&uuml;c&uuml;
 */

import ch.qos.logback.classic.encoder.PatternLayoutEncoder

import static ch.qos.logback.classic.Level.DEBUG
import ch.qos.logback.classic.sift.GSiftingAppender
import ch.qos.logback.classic.sift.MDCBasedDiscriminator
import ch.qos.logback.core.FileAppender
import ch.qos.logback.core.read.ListAppender


appender("SIFT", GSiftingAppender) {
  discriminator(MDCBasedDiscriminator) {
    key = "userid"
    defaultValue = "unknown"
  }
  sift {
    appender("FILE-${userid}", FileAppender) {
      file = "test-${userid}.log"
      append = false
      encoder(PatternLayoutEncoder) {
        println "in encoder userid=${userid}"
        pattern = "${userid} - %msg%n"
      }
    }
  }
}

root(DEBUG, ["SIFT"])
