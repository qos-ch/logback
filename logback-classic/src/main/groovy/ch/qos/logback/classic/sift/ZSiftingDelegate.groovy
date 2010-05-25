package ch.qos.logback.classic.sift

import ch.qos.logback.core.spi.ContextAwareBase
import ch.qos.logback.core.Appender
import ch.qos.logback.classic.gaffer.AppenderDelegate
import ch.qos.logback.core.util.StatusPrinter

/**
 * @author Ceki G&uuml;c&uuml;
 */
class ZSiftingDelegate extends ContextAwareBase {

  String key
  String value

  ZSiftingDelegate(String key, String value) {
    this.key = key
    this.value = value
  }

  Appender appender(String name, Class clazz, Closure closure = null) {
    addInfo("About to instantiate appender of type [" + clazz.name + "]");
    Appender appender = clazz.newInstance();
    addInfo("Naming appender as [" + name + "]");
    appender.name = name
    appender.context = context
    if (closure != null) {
      AppenderDelegate ad = new AppenderDelegate(appender);
      ad.metaClass."${key}" = value
      ad.fieldsToCaccade << "${key}"
      ad.context = context;
      closure.delegate = ad;
      closure.resolveStrategy = Closure.DELEGATE_FIRST
      closure();
    }
    appender.start();
    return appender;
  }
}
