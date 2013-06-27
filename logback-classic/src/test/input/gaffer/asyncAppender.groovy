import ch.qos.logback.classic.AsyncAppender
import ch.qos.logback.classic.PatternLayout
import ch.qos.logback.core.ConsoleAppender
import ch.qos.logback.core.encoder.LayoutWrappingEncoder

appender("STDOUT", ConsoleAppender) {
    encoder(LayoutWrappingEncoder) {
        layout(PatternLayout) {
            pattern = "${p} %m%n"
        }
    }
}
appender("STDOUT-ASYNC", AsyncAppender) {
    appenderRef('STDOUT')
}
root(DEBUG, ["STDOUT-ASYNC"])

