/*
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2026, QOS.ch. All rights reserved.
 *
 * This program and the accompanying materials are dual-licensed under
 * either the terms of the Eclipse Public License v2.0 as published by
 * the Eclipse Foundation
 *
 *   or (per the licensee's choosing)
 *
 * under the terms of the GNU Lesser General Public License version 2.1
 * as published by the Free Software Foundation.
 */
package ch.qos.logback.classic.model.processor;

import ch.qos.logback.classic.joran.ReconfigureOnChangeTask;
import ch.qos.logback.classic.model.ConfigurationModel;
import ch.qos.logback.core.Context;
import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.joran.spi.ConfigurationWatchList;
import ch.qos.logback.core.joran.util.ConfigurationWatchListUtil;
import ch.qos.logback.core.model.Model;
import ch.qos.logback.core.model.processor.ModelHandlerBase;
import ch.qos.logback.core.model.processor.ModelHandlerException;
import ch.qos.logback.core.model.processor.ModelInterpretationContext;
import ch.qos.logback.core.spi.ConfigurationEvent;
import ch.qos.logback.core.util.Duration;
import ch.qos.logback.core.util.OptionHelper;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * This is a subclass of {@link ConfigurationModelHandler} offering configuration reloading support.
 *
 * <p>This class is also called by logback-tyler.</p>
 *
 */
public class ConfigurationModelHandlerFull extends ConfigurationModelHandler {

    public static final String FAILED_WATCH_PREDICATE_MESSAGE_1 = "Missing watchable .xml or .properties files.";
    public static final String FAILED_WATCH_PREDICATE_MESSAGE_2 = "Watching .xml files requires that the main configuration file is reachable as a URL";

    public ConfigurationModelHandlerFull(Context context) {
        super(context);
    }

    static public ModelHandlerBase makeInstance(Context context, ModelInterpretationContext mic) {
        return new ConfigurationModelHandlerFull(context);
    }

    @Override
    public void postHandle(ModelInterpretationContext mic, Model model) throws ModelHandlerException {
        ConfigurationModel configurationModel = (ConfigurationModel) model;

        // scanning is disabled
        if (scanning != Boolean.TRUE) {
            return;
        }

        String scanPeriodStr = mic.subst(configurationModel.getScanPeriodStr());
        scheduleReconfigureOnChangeTask(scanPeriodStr);

        ConfigurationWatchList cwl = ConfigurationWatchListUtil.getConfigurationWatchList(getContext());
        if (cwl != null) {
            try {
                addInfo("Main configuration file URL: " + cwl.getTopURL());
                addInfo("FileWatchList= {" + cwl.getFileWatchListAsStr() + "}");
                addInfo("URLWatchList= {" + cwl.getUrlWatchListAsStr() + "}");
            } catch (NoSuchMethodError e) {
                addWarn("It looks like the version of logback-classic is more recent than");
                addWarn("the version of logback-core. Please align the two versions.");
            }
        }
    }


    /**
     * This method is called from logback-tyler version 1.0.4 and earlier.
     * <p>
     * This method assumes that the variables scanStr and scanPeriodStr have undergone variable substitution
     * as applicable to their current environment
     *
     * @param scanPeriodStr
     * @since 1.5.0
     */
    public void detachedPostProcess(String scanStr, String scanPeriodStr) {
        if (OptionHelper.isNullOrEmptyOrAllSpaces(scanStr) || CoreConstants.FALSE_STR.equalsIgnoreCase(scanStr)) {
            return;
        }

        scheduleReconfigureOnChangeTask(scanPeriodStr);
    }

    /**
     * This method is called from logback-tyler version 1.0.x and later.
     * <p>
     * This method assumes that the variables scanPeriodStr has undergone variable substitution
     * as applicable to their current environment
     *
     * @param scanPeriodStr
     * @since 1.5.28
     */
    public void detachedPostProcess(String scanPeriodStr) {
        scheduleReconfigureOnChangeTask(scanPeriodStr);
    }


    /**
     * Schedules a task to monitor configuration changes and reconfigure the application when changes are detected.
     * The task periodically scans for changes based on the given scan period and reconfigures the system if needed.
     * This method ensures a valid configuration watch is in place before scheduling the task.
     *
     * @param scanPeriodStr the string value representing the scanning period, which will determine how often
     *                      the task checks for configuration changes. The value is expected to have undergone
     *                      necessary variable substitution for the current environment.
     */
    private void scheduleReconfigureOnChangeTask(String scanPeriodStr) {

        boolean watchPredicateFulfilled = ConfigurationWatchListUtil.watchPredicateFulfilled(context);
        if (!watchPredicateFulfilled) {
            addWarn(FAILED_WATCH_PREDICATE_MESSAGE_1);
            addWarn(FAILED_WATCH_PREDICATE_MESSAGE_2);
            return;
        }

        ReconfigureOnChangeTask rocTask = new ReconfigureOnChangeTask();
        rocTask.setContext(context);

        addInfo("Registering a new ReconfigureOnChangeTask " + rocTask);

        context.fireConfigurationEvent(ConfigurationEvent.newConfigurationChangeDetectorRegisteredEvent(rocTask));

        Duration duration = getDurationOfScanPeriodAttribute(scanPeriodStr, SCAN_PERIOD_DEFAULT);

        ConfigurationWatchList cwl = ConfigurationWatchListUtil.getConfigurationWatchList(context);

        String fileWatchListAsStr = (cwl != null) ? cwl.getFileWatchListAsStr() : "";

        addInfo("Will scan for changes in [" + fileWatchListAsStr + "] ");
        // Given that included files are encountered at a later phase, the complete list
        // of files to scan can only be determined when the configuration is loaded in full.
        // However, scan can be active if mainURL is set. Otherwise, when changes are
        // detected the top level config file cannot be accessed.
        addInfo("Setting ReconfigureOnChangeTask scanning period to " + duration);

        ScheduledExecutorService scheduledExecutorService = context.getScheduledExecutorService();
        ScheduledFuture<?> scheduledFuture = scheduledExecutorService.scheduleAtFixedRate(rocTask, duration.getMilliseconds(), duration.getMilliseconds(),
                TimeUnit.MILLISECONDS);
        rocTask.setScheduledFuture(scheduledFuture);
        context.addScheduledFuture(scheduledFuture);

    }

    private Duration getDurationOfScanPeriodAttribute(String scanPeriodAttrib, Duration defaultDuration) {
        Duration duration = null;

        if (!OptionHelper.isNullOrEmptyOrAllSpaces(scanPeriodAttrib)) {
            try {
                duration = Duration.valueOf(scanPeriodAttrib);
            } catch (IllegalStateException | IllegalArgumentException e) {
                addWarn("Failed to parse 'scanPeriod' attribute [" + scanPeriodAttrib + "]", e);
                // default duration will be set below
            }
        }

        if (duration == null) {
            addInfo("No 'scanPeriod' specified. Defaulting to " + defaultDuration.toString());
            duration = defaultDuration;
        }
        return duration;
    }
}
