/*
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2024, QOS.ch. All rights reserved.
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

package ch.qos.logback.classic.tyler;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.model.util.VariableSubstitutionsHelper;
import ch.qos.logback.core.spi.ContextAwareBase;
import ch.qos.logback.core.spi.PropertyContainer;
import ch.qos.logback.core.spi.ScanException;
import ch.qos.logback.core.status.OnConsoleStatusListener;
import ch.qos.logback.core.util.OptionHelper;
import ch.qos.logback.core.util.StatusListenerConfigHelper;
import ch.qos.logback.core.util.StringUtil;

import java.util.HashMap;
import java.util.Map;

public class TylerConfiguratorBase extends ContextAwareBase implements PropertyContainer {

    public static final String SET_CONTEXT_NAME = "setContextName";
    public static final String SETUP_LOGGER_METHOD_NAME = "setupLogger";

    VariableSubstitutionsHelper variableSubstitutionsHelper;

    private Logger setupLogger(String loggerName, Level level, String levelString, Boolean additivity) {
        LoggerContext loggerContext = (LoggerContext) context;
        Logger logger = loggerContext.getLogger(loggerName);
        if (!OptionHelper.isNullOrEmptyOrAllSpaces(levelString)) {
            logger.setLevel(level);
        }
        if (additivity != null) {
            logger.setAdditive(additivity);
        }
        return logger;
    }

    protected void setContextName(String name) {
        if(StringUtil.isNullOrEmpty(name)) {
            addError("Cannot set context name to null or empty string");
            return;
        }
        try {
            String substName = subst(name);
            addInfo("Setting context name to ["+substName+"]");
            context.setName(substName);
        } catch (IllegalStateException e) {
            addError("Failed to rename context as [" + name + "]");
        }
    }

    protected void addOnConsoleStatusListener() {
        StatusListenerConfigHelper.addOnConsoleListenerInstance(context, new OnConsoleStatusListener());
    }

    /**
     * Performs variable substitution.
     *
     * @param ref
     * @return
     */
    public String subst(String ref) {
        if (ref == null) {
            return null;
        }

        try {
            return OptionHelper.substVars(ref, this, context);
        } catch (ScanException | IllegalArgumentException e) {
            addError("Problem while parsing [" + ref + "]", e);
            return ref;
        }
    }

    @Override
    public void addSubstitutionProperty(String key, String value) {
        variableSubstitutionsHelper.addSubstitutionProperty(key, value);
    }

    /**
     * If a key is found in propertiesMap then return it.
     */
    @Override
    public String getProperty(String key) {
        return variableSubstitutionsHelper.getProperty(key);
    }

    @Override
    public Map<String, String> getCopyOfPropertyMap() {
        return variableSubstitutionsHelper.getCopyOfPropertyMap();
    }

}
