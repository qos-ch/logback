import ch.qos.logback.core.ConsoleAppender
import ch.qos.logback.core.encoder.LayoutWrappingEncoder
import ch.qos.logback.classic.PatternLayout
import ch.qos.logback.classic.Level
import ch.qos.logback.core.status.OnConsoleStatusListener
import ch.qos.logback.classic.Logger

appender("C", ConsoleAppender) {
  encoder(LayoutWrappingEncoder) {
    layout(PatternLayout) {
      pattern = "%m%n"
    }
  }
}
root Level.WARN, ["C"]
