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

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.model.ConfigurationModel;
import ch.qos.logback.core.Context;
import ch.qos.logback.core.model.Model;
import ch.qos.logback.core.model.processor.ModelHandlerBase;
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
 * to remove java.xml reachability. See also LOGBACK-1717.
 *
 * <p>
 * See {@link ConfigurationModelHandlerFull} subclass offering configuration
 * reloading support.
 * </p>
 */
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
        // is justified by the use case: the admin trying to chase rogue config file
        String debugAttrib = OptionHelper.getSystemProperty(DEBUG_SYSTEM_PROPERTY_KEY, null);
        if (debugAttrib == null) {
            debugAttrib = mic.subst(configurationModel.getDebugStr());
        }
        

        if (!(OptionHelper.isNullOrEmptyOrAllSpaces(debugAttrib) || debugAttrib.equalsIgnoreCase(FALSE.toString())
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

    protected void processScanAttrib(ModelInterpretationContext mic, ConfigurationModel configurationModel) {
        String scanStr = mic.subst(configurationModel.getScanStr());
        if (!OptionHelper.isNullOrEmptyOrAllSpaces(scanStr) && !"false".equalsIgnoreCase(scanStr)) {
            addInfo("Skipping ReconfigureOnChangeTask registration");
        }
    }


}
