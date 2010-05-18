import ch.qos.logback.classic.PatternLayout
import ch.qos.logback.classic.testUtil.SampleConverter
import ch.qos.logback.core.testUtil.StringListAppender

import static ch.qos.logback.classic.Level.DEBUG

conversionRule("sample", SampleConverter)
appender("LIST", StringListAppender) {
  layout(PatternLayout) {
    Pattern = "%sample - %msg"
  }
}
root(DEBUG, ["LIST"])
