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
