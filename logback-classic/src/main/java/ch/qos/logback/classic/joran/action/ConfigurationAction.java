/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2015, QOS.ch. All rights reserved.
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

import java.net.URL;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.xml.sax.Attributes;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.ReconfigureOnChangeTask;
import ch.qos.logback.classic.util.EnvUtil;
import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.joran.action.Action;
import ch.qos.logback.core.joran.spi.InterpretationContext;
import ch.qos.logback.core.joran.util.ConfigurationWatchListUtil;
import ch.qos.logback.core.status.OnConsoleStatusListener;
import ch.qos.logback.core.util.ContextUtil;
import ch.qos.logback.core.util.Duration;
import ch.qos.logback.core.util.OptionHelper;
import ch.qos.logback.core.util.StatusListenerConfigHelper;

public class ConfigurationAction extends Action {
    static final String INTERNAL_DEBUG_ATTR = "debug";
    static final String PACKAGING_DATA_ATTR = "packagingData";
    static final String SCAN_ATTR = "scan";
    static final String SCAN_PERIOD_ATTR = "scanPeriod";
    static final String DEBUG_SYSTEM_PROPERTY_KEY = "logback.debug";
    static final Duration SCAN_PERIOD_DEFAULT = Duration.buildByMinutes(1);
    
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

        if (OptionHelper.isEmpty(debugAttrib) || debugAttrib.equalsIgnoreCase("false") || debugAttrib.equalsIgnoreCase("null")) {
            addInfo(INTERNAL_DEBUG_ATTR + " attribute not set");
        } else {
            StatusListenerConfigHelper.addOnConsoleListenerInstance(context, new OnConsoleStatusListener());
        }

        processScanAttrib(ic, attributes);

        LoggerContext lc = (LoggerContext) context;
        boolean packagingData = OptionHelper.toBoolean(ic.subst(attributes.getValue(PACKAGING_DATA_ATTR)), LoggerContext.DEFAULT_PACKAGING_DATA);
        lc.setPackagingDataEnabled(packagingData);

        if (EnvUtil.isGroovyAvailable()) {
            ContextUtil contextUtil = new ContextUtil(context);
            contextUtil.addGroovyPackages(lc.getFrameworkPackages());
        }

        // the context is turbo filter attachable, so it is pushed on top of the
        // stack
        ic.pushObject(getContext());
    }

    String getSystemProperty(String name) {
        /*
         * LOGBACK-743: accessing a system property in the presence of a SecurityManager (e.g. applet sandbox) can
         * result in a SecurityException.
         */
        try {
            return System.getProperty(name);
        } catch (SecurityException ex) {
            return null;
        }
    }

    void processScanAttrib(InterpretationContext ic, Attributes attributes) {
        String scanAttrib = ic.subst(attributes.getValue(SCAN_ATTR));
        if (!OptionHelper.isEmpty(scanAttrib) && !"false".equalsIgnoreCase(scanAttrib)) {

            ScheduledExecutorService scheduledExecutorService = context.getScheduledExecutorService();
            URL mainURL = ConfigurationWatchListUtil.getMainWatchURL(context);
            if (mainURL == null) {
                addWarn("Due to missing top level configuration file, reconfiguration on change (configuration file scanning) cannot be done.");
                return;
            }
            ReconfigureOnChangeTask rocTask = new ReconfigureOnChangeTask();
            rocTask.setContext(context);

            context.putObject(CoreConstants.RECONFIGURE_ON_CHANGE_TASK, rocTask);

            String scanPeriodAttrib = ic.subst(attributes.getValue(SCAN_PERIOD_ATTR));
            Duration duration = getDurationOfScanPeriodAttribute(scanPeriodAttrib, SCAN_PERIOD_DEFAULT);

            addInfo("Will scan for changes in [" + mainURL + "] ");
            // Given that included files are encountered at a later phase, the complete list of files 
            // to scan can only be determined when the configuration is loaded in full.
            // However, scan can be active if mainURL is set. Otherwise, when changes are detected
            // the top level config file cannot be accessed.
            addInfo("Setting ReconfigureOnChangeTask scanning period to " + duration);
 
            ScheduledFuture<?> scheduledFuture = scheduledExecutorService.scheduleAtFixedRate(rocTask, duration.getMilliseconds(), duration.getMilliseconds(),
                            TimeUnit.MILLISECONDS);
            context.addScheduledFuture(scheduledFuture);
        }
    }

    private Duration getDurationOfScanPeriodAttribute(String scanPeriodAttrib, Duration defaultDuration) {
        Duration duration = null;

        if (!OptionHelper.isEmpty(scanPeriodAttrib)) {
            try {
                duration = Duration.valueOf(scanPeriodAttrib);
            } catch(IllegalStateException|IllegalArgumentException e) {
               addWarn("Failed to parse 'scanPeriod' attribute ["+scanPeriodAttrib+"]", e);
                // default duration will be set below
            }
        }
        
        if(duration == null) {
            addInfo("No 'scanPeriod' specified. Defaulting to " + defaultDuration.toString());
            duration = defaultDuration;
        }
        return duration;
    }

    public void end(InterpretationContext ec, String name) {
        addInfo("End of configuration.");
        ec.popObject();
    }
}
