/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2010, QOS.ch. All rights reserved.
 *
 * This program and the accompanying materials are dual-licensed under
 * either the terms of the Eclipse Public License v1.0 as published by
 * the Eclipse Foundation
 *
 *   or (per the licensee's choosing)
 *
 * under the terms of the GNU Lesser General Public License version 2.1
 * as published by the Free Software Foundation.
 */
package ch.qos.logback.classic.gaffer;


import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.classic.turbo.ReconfigureOnChangeFilter
import ch.qos.logback.classic.turbo.TurboFilter
import ch.qos.logback.core.Appender
import ch.qos.logback.core.CoreConstants
import ch.qos.logback.core.spi.ContextAwareBase
import ch.qos.logback.core.status.StatusListener
import ch.qos.logback.core.util.CachingDateFormatter
import ch.qos.logback.core.util.Duration
import ch.qos.logback.core.spi.LifeCycle
import ch.qos.logback.core.spi.ContextAware

/**
 * @author Ceki G&uuml;c&uuml;
 */

public class ConfigurationDelegate extends ContextAwareBase {

  List<Appender> appenderList = [];

  Object getDeclaredOrigin() {
    return this;
  }

  void scan(String scanPeriodStr = null) {
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

  void statusListener(Class listenerClass) {
    StatusListener statusListener = listenerClass.newInstance()
    context.statusManager.add(statusListener)
    if(statusListener instanceof ContextAware) {
      ((ContextAware) statusListener).setContext(context);
    }
    if(statusListener instanceof LifeCycle) {
      ((LifeCycle) statusListener).start();
    }
    addInfo("Added status listener of type [${listenerClass.canonicalName}]");
  }

  void conversionRule(String conversionWord, Class converterClass) {
    String converterClassName = converterClass.getName();

    Map<String, String> ruleRegistry = (Map) context.getObject(CoreConstants.PATTERN_RULE_REGISTRY);
    if (ruleRegistry == null) {
      ruleRegistry = new HashMap<String, String>();
      context.putObject(CoreConstants.PATTERN_RULE_REGISTRY, ruleRegistry);
    }
    // put the new rule into the rule registry
    addInfo("registering conversion word " + conversionWord + " with class [" + converterClassName + "]");
    ruleRegistry.put(conversionWord, converterClassName);
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
          Appender appender = appenderList.find { it -> it.name == aName };
          if (appender != null) {
            logger.addAppender(appender);
          } else {
            addError("Failed to find appender named [${aName}]");
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
    addInfo("About to instantiate appender of type [" + clazz.name + "]");
    Appender appender = clazz.newInstance();
    addInfo("Naming appender as [" + name + "]");
    appender.name = name
    appender.context = context
    appenderList.add(appender)
    if (closure != null) {
      AppenderDelegate ad = new AppenderDelegate(appender);
      copyContributions(ad, appender)
      ad.context = context;
      closure.delegate = ad;
      closure.resolveStrategy = Closure.DELEGATE_FIRST
      closure();
    }
    try {
      appender.start()
    } catch (RuntimeException e) {
      addError("Failed to start apppender named [" + name + "]", e)
    }
  }

  private void copyContributions(AppenderDelegate appenderDelegate, Appender appender) {
    if (appender instanceof ConfigurationContributor) {
      ConfigurationContributor cc = (ConfigurationContributor) appender;
      cc.getMappings().each() { oldName, newName ->
        appenderDelegate.metaClass."${newName}" = appender.&"$oldName"
      }
    }
  }

  void turboFilter(Class clazz, Closure closure = null) {
    addInfo("About to instantiate turboFilter of type [" + clazz.name + "]");
    TurboFilter turboFilter = clazz.newInstance();
    turboFilter.context = context

    if (closure != null) {
      ComponentDelegate componentDelegate = new ComponentDelegate(turboFilter);
      componentDelegate.context = context;
      closure.delegate = componentDelegate;
      closure.resolveStrategy = Closure.DELEGATE_FIRST
      closure();
    }
    turboFilter.start();
    addInfo("Adding aforementioned turbo filter to context");
    context.addTurboFilter(turboFilter)
  }

  String timestamp(String datePattern, long timeReference = -1) {
    long now = -1;

    if (timeReference == -1) {
      addInfo("Using current interpretation time, i.e. now, as time reference.");
      now = System.currentTimeMillis()
    } else {
      now = timeReference
      addInfo("Using " + now + " as time reference.");
    }
    CachingDateFormatter sdf = new CachingDateFormatter(datePattern);
    sdf.format(now)
  }
}

