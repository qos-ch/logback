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
package ch.qos.logback.classic.model.processor;

import ch.qos.logback.classic.joran.ReconfigureOnChangeTask;
import ch.qos.logback.classic.model.ConfigurationModel;
import ch.qos.logback.core.Context;
import ch.qos.logback.core.joran.util.ConfigurationWatchListUtil;
import ch.qos.logback.core.model.Model;
import ch.qos.logback.core.model.processor.ModelHandlerBase;
import ch.qos.logback.core.model.processor.ModelHandlerException;
import ch.qos.logback.core.model.processor.ModelInterpretationContext;
import ch.qos.logback.core.spi.ConfigurationEvent;
import ch.qos.logback.core.util.Duration;
import ch.qos.logback.core.util.OptionHelper;

import java.net.URL;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * This is a subclass of {@link ConfigurationModelHandler} offering configuration reloading support.
 *
 */
public class ConfigurationModelHandlerFull extends ConfigurationModelHandler {

    public static String FAILED_WATCH_PREDICATE_MESSAGE_1 = "Missing watchable .xml or .properties files.";
    public static String FAILED_WATCH_PREDICATE_MESSAGE_2 = "Watching .xml files requires that the main configuration file is reachable as a URL";

    public ConfigurationModelHandlerFull(Context context) {
        super(context);
    }

    static public ModelHandlerBase makeInstance2(Context context, ModelInterpretationContext mic) {
        return new ConfigurationModelHandlerFull(context);
    }

    @Override
    protected void processScanAttrib(ModelInterpretationContext mic, ConfigurationModel configurationModel) {

    }

    @Override
    public void postHandle(ModelInterpretationContext mic, Model model) throws ModelHandlerException {
        ConfigurationModel configurationModel = (ConfigurationModel) model;
        postProcessScanAttrib(mic, configurationModel);
    }

    protected void postProcessScanAttrib(ModelInterpretationContext mic, ConfigurationModel configurationModel) {
        String scanStr = mic.subst(configurationModel.getScanStr());
        String scanPeriodStr = mic.subst(configurationModel.getScanPeriodStr());
        detachedPostProcess(scanStr, scanPeriodStr);
    }

    /**
     * This method is called from this class but also from logback-tyler.
     *
     * This method assumes that the variables scanStr and scanPeriodStr have undergone variable substitution
     * as applicable to their current environment
     *
     * @param scanStr
     * @param scanPeriodStr
     * @since 1.5.0
     */
    public void detachedPostProcess(String scanStr, String scanPeriodStr) {
        if (!OptionHelper.isNullOrEmptyOrAllSpaces(scanStr) && !"false".equalsIgnoreCase(scanStr)) {
            ScheduledExecutorService scheduledExecutorService = context.getScheduledExecutorService();
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

            addInfo("Will scan for changes in [" + ConfigurationWatchListUtil.getConfigurationWatchList(context) + "] ");
            // Given that included files are encountered at a later phase, the complete list
            // of files to scan can only be determined when the configuration is loaded in full.
            // However, scan can be active if mainURL is set. Otherwise, when changes are
            // detected the top level config file cannot be accessed.
            addInfo("Setting ReconfigureOnChangeTask scanning period to " + duration);

            ScheduledFuture<?> scheduledFuture = scheduledExecutorService.scheduleAtFixedRate(rocTask, duration.getMilliseconds(), duration.getMilliseconds(),
                            TimeUnit.MILLISECONDS);
            rocTask.setScheduredFuture(scheduledFuture);
            context.addScheduledFuture(scheduledFuture);
        }

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
