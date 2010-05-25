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
    appender("LIST-${userid}", ListAppender) {
      println "USERID=$userid"
    }
  }
}

root(DEBUG, ["SIFT"])
