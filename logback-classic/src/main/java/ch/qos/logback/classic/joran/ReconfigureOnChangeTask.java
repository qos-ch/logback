/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2022, QOS.ch. All rights reserved.
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
package ch.qos.logback.classic.joran;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.concurrent.ScheduledFuture;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.joran.spi.ConfigurationWatchList;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.joran.util.ConfigurationWatchListUtil;
import ch.qos.logback.core.model.Model;
import ch.qos.logback.core.model.ModelUtil;
import ch.qos.logback.core.spi.ConfigurationEvent;
import ch.qos.logback.core.spi.ContextAwareBase;
import ch.qos.logback.core.status.StatusUtil;

import static ch.qos.logback.core.spi.ConfigurationEvent.newConfigurationChangeDetectorRunningEvent;
import static ch.qos.logback.core.spi.ConfigurationEvent.newConfigurationEndedSuccessfullyEvent;

public class ReconfigureOnChangeTask extends ContextAwareBase implements Runnable {

    public static final String DETECTED_CHANGE_IN_CONFIGURATION_FILES = "Detected change in configuration files.";
    static final String RE_REGISTERING_PREVIOUS_SAFE_CONFIGURATION = "Re-registering previous fallback configuration once more as a fallback configuration point";
    static final String FALLING_BACK_TO_SAFE_CONFIGURATION = "Given previous errors, falling back to previously registered safe configuration.";

    long birthdate = System.currentTimeMillis();
    List<ReconfigureOnChangeTaskListener> listeners = null;

    ScheduledFuture<?> scheduledFuture;

    @Override
    public void run() {
        context.fireConfigurationEvent(newConfigurationChangeDetectorRunningEvent(this));

        ConfigurationWatchList configurationWatchList = ConfigurationWatchListUtil.getConfigurationWatchList(context);
        if (configurationWatchList == null) {
            addWarn("Empty ConfigurationWatchList in context");
            return;
        }

        List<File> filesToWatch = configurationWatchList.getCopyOfFileWatchList();
        if (filesToWatch == null || filesToWatch.isEmpty()) {
            addInfo("Empty watch file list. Disabling ");
            return;
        }

        if (!configurationWatchList.changeDetected()) {
            return;
        }
        context.fireConfigurationEvent(ConfigurationEvent.newConfigurationChangeDetectedEvent(this));
        cancelFutureInvocationsOfThisTaskInstance();

        URL mainConfigurationURL = configurationWatchList.getMainURL();

        addInfo(DETECTED_CHANGE_IN_CONFIGURATION_FILES);
        addInfo(CoreConstants.RESET_MSG_PREFIX + "named [" + context.getName() + "]");

        LoggerContext lc = (LoggerContext) context;
        if (mainConfigurationURL.toString().endsWith("xml")) {
            performXMLConfiguration(lc, mainConfigurationURL);
        } else if (mainConfigurationURL.toString().endsWith("groovy")) {
            addError("Groovy configuration disabled due to Java 9 compilation issues.");
        }
        //fireDoneReconfiguring();
    }

    private void cancelFutureInvocationsOfThisTaskInstance() {
        boolean result = scheduledFuture.cancel(false);
        if(!result) {
            addWarn("could not cancel "+ this.toString());
        }
    }

    private void performXMLConfiguration(LoggerContext lc, URL mainConfigurationURL) {
        JoranConfigurator jc = new JoranConfigurator();
        jc.setContext(context);
        StatusUtil statusUtil = new StatusUtil(context);
        Model failsafeTop = jc.recallSafeConfiguration();
        URL mainURL = ConfigurationWatchListUtil.getMainWatchURL(context);
        addInfo("Resetting loggerContext ["+lc.getName()+"]");
        lc.reset();
        long threshold = System.currentTimeMillis();
        try {
            jc.doConfigure(mainConfigurationURL);
            // e.g. IncludeAction will add a status regarding XML parsing errors but no exception will reach here
            if (statusUtil.hasXMLParsingErrors(threshold)) {
                fallbackConfiguration(lc, failsafeTop, mainURL);
            }
        } catch (JoranException e) {
            addWarn("Exception occurred during reconfiguration", e);
            fallbackConfiguration(lc, failsafeTop, mainURL);
        }
    }

    private void fallbackConfiguration(LoggerContext lc, Model failsafeTop, URL mainURL) {
        // failsafe events are used only in case of errors. Therefore, we must *not*
        // invoke file inclusion since the included files may be the cause of the error.

        // List<SaxEvent> failsafeEvents = removeIncludeEvents(eventList);
        JoranConfigurator joranConfigurator = new JoranConfigurator();
        joranConfigurator.setContext(context);
        ConfigurationWatchList oldCWL = ConfigurationWatchListUtil.getConfigurationWatchList(context);
        ConfigurationWatchList newCWL = oldCWL.buildClone();

        if (failsafeTop == null) {
            addWarn("No previous configuration to fall back on.");
            return;
        } else {
            addWarn(FALLING_BACK_TO_SAFE_CONFIGURATION);
            addInfo("Safe model "+failsafeTop);
            try {
                lc.reset();
                ConfigurationWatchListUtil.registerConfigurationWatchList(context, newCWL);
                ModelUtil.resetForReuse(failsafeTop);
                joranConfigurator.processModel(failsafeTop);
                addInfo(RE_REGISTERING_PREVIOUS_SAFE_CONFIGURATION);
                joranConfigurator.registerSafeConfiguration(failsafeTop);
                context.fireConfigurationEvent(newConfigurationEndedSuccessfullyEvent(this));
            } catch (Exception e) {
                addError("Unexpected exception thrown by a configuration considered safe.", e);
            }
        }
    }

    @Override
    public String toString() {
        return "ReconfigureOnChangeTask(born:" + birthdate + ")";
    }


    public void setScheduredFuture(ScheduledFuture<?> aScheduledFuture) {
        this.scheduledFuture = aScheduledFuture;
    }
}
