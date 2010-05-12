package ch.qos.logback.classic.gaffer;

import ch.qos.logback.core.util.Duration;
import groovy.lang.Closure;

import java.util.Map
import ch.qos.logback.core.Context
import ch.qos.logback.classic.turbo.ReconfigureOnChangeFilter
import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.core.spi.ContextAwareImpl
import ch.qos.logback.core.spi.ContextAwareBase
import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import ch.qos.logback.core.Appender;

/**
 * @author Ceki G&uuml;c&uuml;
 */

@Mixin(ContextAwareBase)
public class ConfigurationDelegate {

  List<Appender> appenderList = [];

  void configuration(Closure closure) {
    configuration([:], closure)
  }


  void configuration(Map map, Closure closure) {
    processScanAttributes(map.scan, map.scanPeriod);
  }

  private void processScanAttributes(boolean scan, String scanPeriodStr) {
    if (scan) {
      ReconfigureOnChangeFilter rocf = new ReconfigureOnChangeFilter();
      rocf.setContext(context);
      if (scanPeriodStr) {
        try {
          Duration duration = Duration.valueOf(scanPeriodStr);
          rocf.setRefreshPeriod(duration.getMilliseconds());
          addInfo("Setting ReconfigureOnChangeFilter scanning period to "
                  + duration);
        } catch (NumberFormatException nfe) {
          addError("Error while converting [" + scanAttrib + "] to long", nfe);
        }
      }
      rocf.start();
      addInfo("Adding ReconfigureOnChangeFilter as a turbo filter");
      context.addTurboFilter(rocf);
    }
  }

  void root(Level level, List<String> appenderNames = []) {
    if (level == null) {
      addError("Root logger cannot be set to level null");
    } else {
      logger(org.slf4j.Logger.ROOT_LOGGER_NAME, level, appenderNames);
    }
  }

  void logger(String name, Level level, List<String> appenderNames = [], Boolean additivity = null) {
    if (name) {
      Logger logger = ((LoggerContext) context).getLogger(name);
      logger.level = level;

      if (appenderNames) {
        appenderNames.each { aName ->
          Appender appender = appenderList.find { it.name == aName };
          if (appender != null) {
            logger.addAppender(appender);
          } else {
            addError("Failed to find appender named [${it.name}]");
          }
        }
      }

      if (additivity != null) {
        logger.additive = additivity;
      }
    } else {
      addInfo("No name attribute for logger");
    }
  }

  void appender(String name, Class clazz, Closure closure = null) {
    Appender appender = clazz.newInstance();
    appender.name = name
    appender.context = context
    appenderList.add(appender)
    if (closure != null) {
      AppenderDelegate ad = new AppenderDelegate(appender);
      ad.context = context;
      closure.delegate = ad;
      closure.resolveStrategy = Closure.DELEGATE_FIRST
      closure();
    }
    appender.start();
  }
}

