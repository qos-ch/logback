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

import static ch.qos.logback.core.model.ModelConstants.DEBUG_SYSTEM_PROPERTY_KEY;
import static ch.qos.logback.core.model.ModelConstants.NULL_STR;
import static java.lang.Boolean.FALSE;

import java.net.URL;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.ReconfigureOnChangeTask;
import ch.qos.logback.classic.model.ConfigurationModel;
import ch.qos.logback.core.Context;
import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.joran.util.ConfigurationWatchListUtil;
import ch.qos.logback.core.model.Model;
import ch.qos.logback.core.model.processor.ModelHandlerBase;
import ch.qos.logback.core.model.processor.ModelInterpretationContext;
import ch.qos.logback.core.spi.ConfigurationEvent;
import ch.qos.logback.core.status.OnConsoleStatusListener;
import ch.qos.logback.core.util.ContextUtil;
import ch.qos.logback.core.util.Duration;
import ch.qos.logback.core.util.OptionHelper;
import ch.qos.logback.core.util.StatusListenerConfigHelper;

public class ConfigurationModelHandler extends ModelHandlerBase {

    static final Duration SCAN_PERIOD_DEFAULT = Duration.buildByMinutes(1);

    public ConfigurationModelHandler(Context context) {
        super(context);
    }

    static public ModelHandlerBase makeInstance(Context context, ModelInterpretationContext mic) {
        return new ConfigurationModelHandler(context);
    }

    protected Class<ConfigurationModel> getSupportedModelClass() {
        return ConfigurationModel.class;
    }

    @Override
    public void handle(ModelInterpretationContext mic, Model model) {

        ConfigurationModel configurationModel = (ConfigurationModel) model;

        // See LOGBACK-527 (the system property is looked up first). Thus, it overrides
        // the equivalent property in the config file. This reversal of scope priority
        // is justified
        // by the use case: the admin trying to chase rogue config file
        String debugAttrib = OptionHelper.getSystemProperty(DEBUG_SYSTEM_PROPERTY_KEY, null);
        if (debugAttrib == null) {
            debugAttrib = mic.subst(configurationModel.getDebugStr());
        }
        

        if (!(OptionHelper.isNullOrEmpty(debugAttrib) || debugAttrib.equalsIgnoreCase(FALSE.toString())
                || debugAttrib.equalsIgnoreCase(NULL_STR))) {
            StatusListenerConfigHelper.addOnConsoleListenerInstance(context, new OnConsoleStatusListener());
        }

        processScanAttrib(mic, configurationModel);

        LoggerContext lc = (LoggerContext) context;
        boolean packagingData = OptionHelper.toBoolean(mic.subst(configurationModel.getPackagingDataStr()),
                LoggerContext.DEFAULT_PACKAGING_DATA);
        lc.setPackagingDataEnabled(packagingData);

        ContextUtil contextUtil = new ContextUtil(context);
        contextUtil.addGroovyPackages(lc.getFrameworkPackages());
    }

    void processScanAttrib(ModelInterpretationContext mic, ConfigurationModel configurationModel) {
        String scanStr = mic.subst(configurationModel.getScanStr());
        if (!OptionHelper.isNullOrEmpty(scanStr) && !"false".equalsIgnoreCase(scanStr)) {

            ScheduledExecutorService scheduledExecutorService = context.getScheduledExecutorService();
            URL mainURL = ConfigurationWatchListUtil.getMainWatchURL(context);
            if (mainURL == null) {
                addWarn("Due to missing top level configuration file, reconfiguration on change (configuration file scanning) cannot be done.");
                return;
            }
            ReconfigureOnChangeTask rocTask = new ReconfigureOnChangeTask();
            rocTask.setContext(context);

            addInfo("Registering a new ReconfigureOnChangeTask "+ rocTask);

            context.fireConfigurationEvent(ConfigurationEvent.newConfigurationChangeDetectorRegisteredEvent(rocTask));

            String scanPeriodStr = mic.subst(configurationModel.getScanPeriodStr());
            Duration duration = getDurationOfScanPeriodAttribute(scanPeriodStr, SCAN_PERIOD_DEFAULT);

            addInfo("Will scan for changes in [" + mainURL + "] ");
            // Given that included files are encountered at a later phase, the complete list
            // of files
            // to scan can only be determined when the configuration is loaded in full.
            // However, scan can be active if mainURL is set. Otherwise, when changes are
            // detected
            // the top level config file cannot be accessed.
            addInfo("Setting ReconfigureOnChangeTask scanning period to " + duration);

            ScheduledFuture<?> scheduledFuture = scheduledExecutorService.scheduleAtFixedRate(rocTask,
                    duration.getMilliseconds(), duration.getMilliseconds(), TimeUnit.MILLISECONDS);
            rocTask.setScheduredFuture(scheduledFuture);
            context.addScheduledFuture(scheduledFuture);
        }
    }

    private Duration getDurationOfScanPeriodAttribute(String scanPeriodAttrib, Duration defaultDuration) {
        Duration duration = null;

        if (!OptionHelper.isNullOrEmpty(scanPeriodAttrib)) {
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
