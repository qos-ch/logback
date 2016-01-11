import ch.qos.logback.core.encoder.LayoutWrappingEncoder
import ch.qos.logback.classic.PatternLayout

def p = "HELLO"
appender("STDOUT", ConsoleAppender) {
  encoder(LayoutWrappingEncoder) {
    layout(PatternLayout) {
      pattern = "${p} %m%n"
    }
  }
}
root(DEBUG, ["STDOUT"])