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
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.classic.model.ConfigurationModel;
import ch.qos.logback.classic.util.LevelUtil;
import ch.qos.logback.core.Context;
import ch.qos.logback.core.joran.GenericXMLConfigurator;
import ch.qos.logback.core.model.Model;
import ch.qos.logback.core.model.util.PropertyModelHandlerHelper;
import ch.qos.logback.core.model.util.VariableSubstitutionsHelper;
import ch.qos.logback.core.spi.ContextAwareBase;
import ch.qos.logback.core.spi.ContextAwarePropertyContainer;
import ch.qos.logback.core.status.OnConsoleStatusListener;
import ch.qos.logback.core.util.OptionHelper;
import ch.qos.logback.core.util.StatusListenerConfigHelper;
import ch.qos.logback.core.util.StringUtil;

import java.util.Map;
import java.util.function.Supplier;

public class TylerConfiguratorBase extends ContextAwareBase implements ContextAwarePropertyContainer {

    public static final String SET_CONTEXT_METHOD_NAME = "setContext";
    public static final String SET_CONTEXT_NAME_METHOD_NAME = "setContextName";
    public static final String SETUP_LOGGER_METHOD_NAME = "setupLogger";
    public static final String VARIABLE_SUBSTITUTIONS_HELPER_FIELD_NAME = "variableSubstitutionsHelper";
    public static final String PROPERTY_MODEL_HANDLER_HELPER_FIELD_NAME = "propertyModelHandlerHelper";

    // initialized via #setContext
    protected VariableSubstitutionsHelper variableSubstitutionsHelper;
    // context set in #setContext
    protected PropertyModelHandlerHelper propertyModelHandlerHelper = new PropertyModelHandlerHelper(this);

    protected Logger setupLogger(String loggerName, String levelString, Boolean additivity) {
        LoggerContext loggerContext = (LoggerContext) context;
        Logger logger = loggerContext.getLogger(loggerName);
        if (!OptionHelper.isNullOrEmptyOrAllSpaces(levelString)) {
            Level level = LevelUtil.levelStringToLevel(levelString);
            logger.setLevel(level);
        }
        if (additivity != null) {
            logger.setAdditive(additivity);
        }
        return logger;
    }

    @Override
    public void setContext(Context context) {
        super.setContext(context);
        variableSubstitutionsHelper = new VariableSubstitutionsHelper(context);
        propertyModelHandlerHelper.setContext(context);
    }

    protected void setContextName(String name) {
        if (StringUtil.isNullOrEmpty(name)) {
            addError("Cannot set context name to null or empty string");
            return;
        }
        try {
            String substName = subst(name);
            addInfo("Setting context name to [" + substName + "]");
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
    @Override
    public String subst(String ref) {
        return variableSubstitutionsHelper.subst(ref);
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

    public boolean isNull(String k) {
        String val = OptionHelper.propertyLookup(k, this, context);
        return (val == null);
    }

    /**
     * Method used in conditional evaluation
     *
     * @param k  a property name
     * @return true if the property is defined
     * @since 1.5.4
     */
    public boolean isDefined(String k) {
        String val = OptionHelper.propertyLookup(k, this, context);
        return (val != null);
    }

    /**
     * Shorthand for {@link #property(String)}.
     *
     * @param k a property name
     * @return value of property k
     * @since 1.5.4
     */
    public String p(String k) {
        return property(k);
    }

    /**
     * Return the value of the property named k. If the value is null, then the
     * empty string is returned to avoid null checks.
     *
     * @param k property name
     * @return the value of the property named k
     * @since 1.5.4
     */
    public String property(String k) {
        String val = OptionHelper.propertyLookup(k, this, context);
        if (val != null)
            return val;
        else
            return "";
    }

    private JoranConfigurator makeAnotherInstance() {
        JoranConfigurator jc = new JoranConfigurator();
        jc.setContext(context);
        return jc;
    }

    /**
     * Return a supplier which supplies an instance of {@link JoranConfigurator} set to
     * the same context the context of 'this'.
     * @since 1.5.11
     */
    @Override
    public Supplier<? extends GenericXMLConfigurator> getConfiguratorSupplier() {
        Supplier<? extends GenericXMLConfigurator> supplier = () -> this.makeAnotherInstance();
        return supplier;
    }

    protected void processModelFromIncludedFile(Model modelFromIncludedFile) {
        Supplier<? extends GenericXMLConfigurator > configuratorSupplier = this.getConfiguratorSupplier();
        GenericXMLConfigurator genericXMLConfigurator = configuratorSupplier.get();
        ConfigurationModel configururationModel = new ConfigurationModel();
        configururationModel.addSubModel(modelFromIncludedFile);
        genericXMLConfigurator.processModel(configururationModel);
    }
}
