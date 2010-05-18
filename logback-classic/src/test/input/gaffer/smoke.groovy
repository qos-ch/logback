import ch.qos.logback.core.ConsoleAppender
import ch.qos.logback.core.encoder.LayoutWrappingEncoder
import ch.qos.logback.classic.PatternLayout
import ch.qos.logback.classic.Level
import ch.qos.logback.core.status.OnConsoleStatusListener
import ch.qos.logback.classic.Logger


context.name = "a"

Logger xLogger = context.getLogger("x")
xLogger.setLevel(Level.INFO)

statusListener OnConsoleStatusListener

println "hostname ${hostname}"

addInfo("xxx")

appender("C", ConsoleAppender) {
  encoder(LayoutWrappingEncoder) {
    layout(PatternLayout) {
      pattern = "%m%n"
    }
  }
}

root Level.WARN, ["C"]
