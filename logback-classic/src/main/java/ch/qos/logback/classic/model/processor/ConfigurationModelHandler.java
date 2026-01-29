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

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.model.ConfigurationModel;
import ch.qos.logback.core.Context;
import ch.qos.logback.core.joran.util.ConfigurationWatchListUtil;
import ch.qos.logback.core.model.Model;
import ch.qos.logback.core.model.processor.ModelHandlerBase;
import ch.qos.logback.core.model.processor.ModelHandlerException;
import ch.qos.logback.core.model.processor.ModelInterpretationContext;
import ch.qos.logback.core.status.OnConsoleStatusListener;
import ch.qos.logback.core.util.ContextUtil;
import ch.qos.logback.core.util.Duration;
import ch.qos.logback.core.util.OptionHelper;
import ch.qos.logback.core.util.StatusListenerConfigHelper;

import static ch.qos.logback.core.model.ModelConstants.DEBUG_SYSTEM_PROPERTY_KEY;
import static ch.qos.logback.core.model.ModelConstants.NULL_STR;
import static java.lang.Boolean.FALSE;

/**
 * In 1.3.9/1.49, ConfigurationModelHandler has been reduced in functionality and no
 * longer initiates a reconfiguration task. This change was justified by the need
 * to remove java.xml reachability. See also https://jira.qos.ch/browse/LOGBACK-1717
 *
 * <p>
 * See {@link ConfigurationModelHandlerFull} subclass offering configuration
 * reloading support.
 * </p>
 */
public class ConfigurationModelHandler extends ModelHandlerBase {

    static final Duration SCAN_PERIOD_DEFAULT = Duration.buildByMinutes(1);

    protected Boolean scanning = null;

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
        // is justified by the use case: the admin trying to chase rogue config file
        String debugAttrib = OptionHelper.getSystemProperty(DEBUG_SYSTEM_PROPERTY_KEY, null);
        if (debugAttrib == null) {
            debugAttrib = mic.subst(configurationModel.getDebugStr());
        }
        

        if (!(OptionHelper.isNullOrEmptyOrAllSpaces(debugAttrib) || debugAttrib.equalsIgnoreCase(FALSE.toString())
                || debugAttrib.equalsIgnoreCase(NULL_STR))) {
            StatusListenerConfigHelper.addOnConsoleListenerInstance(context, new OnConsoleStatusListener());
        }

        // It is hard to gauge at this stage which URL ares watchable
        // However, we know for sure if the user wants scanning or not
        this.scanning = scanAttrToBoolean(configurationModel);

        mic.setTopScanBoolean(scanning);

        printScanMessage(scanning);

        if (scanning == Boolean.TRUE) {
            ConfigurationWatchListUtil.registerNewConfigurationWatchListWithContext(getContext());
            ConfigurationWatchListUtil.setMainWatchURL(context, mic.getTopURL());
        }

        LoggerContext lc = (LoggerContext) context;
        boolean packagingData = OptionHelper.toBoolean(mic.subst(configurationModel.getPackagingDataStr()),
                LoggerContext.DEFAULT_PACKAGING_DATA);
        lc.setPackagingDataEnabled(packagingData);

        ContextUtil contextUtil = new ContextUtil(context);
        contextUtil.addGroovyPackages(lc.getFrameworkPackages());


    }

    void printScanMessage(Boolean scanning) {
        if (scanning == null) {
            addInfo("Scan attribute not set or set to unrecognized value.");
            return;
        }
        if (scanning) {
            addInfo("Scan attribute set to true. Will scan for configuration file changes.");
        } else  {
            addInfo("Scan attribute set to false.");
        }
    }


    @Override
    public void postHandle(ModelInterpretationContext mic, Model model) throws ModelHandlerException {
        //ConfigurationModel configurationModel = (ConfigurationModel) model;

    }

    /**
     * Converts the scan string attribute of the given model to a Boolean value.
     *
     * <p>If the provided model is an instance of {@code ConfigurationModel}, the scan string is retrieved
     * and converted to a {@code Boolean}. If the provided model is not a {@code ConfigurationModel},
     * the method returns {@code null}.
     * </p>
     *
     * @param model the model object, which may be an instance of {@code ConfigurationModel}
     * @return a {@code Boolean} corresponding to the scan string attribute if the model is
     *         an instance of {@code ConfigurationModel}, or {@code null} otherwise
     *
     * @since 1.5.27
     */
    private Boolean scanAttrToBoolean(Model model) {
        if(model instanceof ConfigurationModel) {
            ConfigurationModel configurationModel = (ConfigurationModel) model;
            String scanStr = configurationModel.getScanStr();
            return OptionHelper.toBooleanObject(scanStr);
        } else {
            return null;
        }

    }
}
