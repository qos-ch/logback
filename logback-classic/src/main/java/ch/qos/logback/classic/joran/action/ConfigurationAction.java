/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2009, QOS.ch. All rights reserved.
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

import org.xml.sax.Attributes;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.turbo.ReconfigureOnChangeFilter;
import ch.qos.logback.core.joran.action.Action;
import ch.qos.logback.core.joran.spi.InterpretationContext;
import ch.qos.logback.core.util.Duration;
import ch.qos.logback.core.util.OptionHelper;
import ch.qos.logback.core.util.StatusPrinter;

public class ConfigurationAction extends Action {
  static final String INTERNAL_DEBUG_ATTR = "debug";
  static final String SCAN_ATTR = "scan";
  static final String SCAN_PERIOD_ATTR = "scanPeriod";

  boolean debugMode = false;

  public void begin(InterpretationContext ec, String name, Attributes attributes) {
    String debugAttrib = attributes.getValue(INTERNAL_DEBUG_ATTR);

    if (OptionHelper.isEmpty(debugAttrib)
        || debugAttrib.equalsIgnoreCase("false")
        || debugAttrib.equalsIgnoreCase("null")) {
      addInfo(INTERNAL_DEBUG_ATTR + " attribute not set");
    } else {
      // LoggerContext loggerContext = (LoggerContext) context;
      // ConfiguratorBase.attachTemporaryConsoleAppender(context);

      debugMode = true;
    }

    processScanAttrib(attributes);

    // the context is turbo filter attachable, so it is pushed on top of the
    // stack
    ec.pushObject(getContext());
  }

  void processScanAttrib(Attributes attributes) {
    String scanAttrib = attributes.getValue(SCAN_ATTR);
    if (!OptionHelper.isEmpty(scanAttrib)
        && !"false".equalsIgnoreCase(scanAttrib)) {
      ReconfigureOnChangeFilter rocf = new ReconfigureOnChangeFilter();
      rocf.setContext(context);
      String scanPeriodAttrib = attributes.getValue(SCAN_PERIOD_ATTR);
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
    if (debugMode) {
      addInfo("End of configuration.");
      LoggerContext loggerContext = (LoggerContext) context;
      StatusPrinter.print(loggerContext);

      // LoggerContext loggerContext = (LoggerContext) context;
      // ConfiguratorBase.detachTemporaryConsoleAppender(repository, errorList);
    }
    ec.popObject();
  }
}
