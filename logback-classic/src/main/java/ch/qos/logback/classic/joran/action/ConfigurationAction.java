/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2013, QOS.ch. All rights reserved.
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
package ch.qos.logback.classic.joran.action;

import ch.qos.logback.classic.util.EnvUtil;
import ch.qos.logback.core.status.OnConsoleStatusListener;
import org.xml.sax.Attributes;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.turbo.ReconfigureOnChangeFilter;
import ch.qos.logback.core.joran.action.Action;
import ch.qos.logback.core.joran.spi.InterpretationContext;
import ch.qos.logback.core.util.ContextUtil;
import ch.qos.logback.core.util.Duration;
import ch.qos.logback.core.util.OptionHelper;

public class ConfigurationAction extends Action {
  static final String INTERNAL_DEBUG_ATTR = "debug";
  static final String SCAN_ATTR = "scan";
  static final String SCAN_PERIOD_ATTR = "scanPeriod";
  static final String DEBUG_SYSTEM_PROPERTY_KEY = "logback.debug";

  long threshold = 0;

  public void begin(InterpretationContext ic, String name, Attributes attributes) {
    threshold = System.currentTimeMillis();

    // See LOGBACK-527 (the system property is looked up first. Thus, it overrides
    // the equivalent property in the config file. This reversal of scope priority is justified
    // by the use case: the admin trying to chase rogue config file
    String debugAttrib = getSystemProperty(DEBUG_SYSTEM_PROPERTY_KEY);
    if (debugAttrib == null) {
      debugAttrib = ic.subst(attributes.getValue(INTERNAL_DEBUG_ATTR));
    }

    if (OptionHelper.isEmpty(debugAttrib) || debugAttrib.equalsIgnoreCase("false")
            || debugAttrib.equalsIgnoreCase("null")) {
      addInfo(INTERNAL_DEBUG_ATTR + " attribute not set");
    } else {
      OnConsoleStatusListener.addNewInstanceToContext(context);
    }

    processScanAttrib(ic, attributes);

    ContextUtil contextUtil = new ContextUtil(context);
    contextUtil.addHostNameAsProperty();

    if(EnvUtil.isGroovyAvailable()) {
      LoggerContext lc = (LoggerContext) context;
      contextUtil.addGroovyPackages(lc.getFrameworkPackages());
    }

    // the context is turbo filter attachable, so it is pushed on top of the
    // stack
    ic.pushObject(getContext());
  }

  String getSystemProperty(String name) {
    /*
     * LOGBACK-743: accessing a system property in the presence of a
     * SecurityManager (e.g. applet sandbox) can result in a SecurityException.
     */
    try {
      return System.getProperty(name);
    } catch (SecurityException ex) {
      return null;
    }
  }

  void processScanAttrib(InterpretationContext ic, Attributes attributes) {
    String scanAttrib = ic.subst(attributes.getValue(SCAN_ATTR));
    if (!OptionHelper.isEmpty(scanAttrib)
            && !"false".equalsIgnoreCase(scanAttrib)) {
      ReconfigureOnChangeFilter rocf = new ReconfigureOnChangeFilter();
      rocf.setContext(context);
      String scanPeriodAttrib = ic.subst(attributes.getValue(SCAN_PERIOD_ATTR));
      if (!OptionHelper.isEmpty(scanPeriodAttrib)) {
        try {
          Duration duration = Duration.valueOf(scanPeriodAttrib);
          rocf.setRefreshPeriod(duration.getMilliseconds());
          addInfo("Setting ReconfigureOnChangeFilter scanning period to "
                  + duration);
        } catch (NumberFormatException nfe) {
          addError("Error while converting [" + scanAttrib + "] to long", nfe);
        }
      }
      rocf.start();
      LoggerContext lc = (LoggerContext) context;
      addInfo("Adding ReconfigureOnChangeFilter as a turbo filter");
      lc.addTurboFilter(rocf);
    }
  }

  public void end(InterpretationContext ec, String name) {
    addInfo("End of configuration.");
    ec.popObject();
  }
}
