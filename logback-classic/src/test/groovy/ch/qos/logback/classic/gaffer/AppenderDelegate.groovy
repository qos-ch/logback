package ch.qos.logback.classic.gaffer

import ch.qos.logback.core.Appender
import ch.qos.logback.core.spi.ContextAwareBase

/**
 * @author Ceki G&uuml;c&uuml;
 */
@Mixin(ContextAwareBase)
class AppenderDelegate {

  Appender appender;

  AppenderDelegate(Appender appender) {
    this.appender = appender;
  }

  void methodMissing(String name, args) {
    println "method $name accessed"
  }
  
  void propertyMissing(String name, def value) {
    println "-- propertyMissing"
    if(appender.hasProperty(name)) {
      //println "-- appender has property $name"
      appender."${name}" = value;
    } else {
      //println "-- appender does not have property [$name]"
      addError("Appender [${appender.name}] of type [${appender.getClass().canonicalName}] has no [${name}] property " )
    }
  }
}
