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

import ch.qos.logback.classic.joran.PropertiesConfigurator;
import ch.qos.logback.classic.model.ConfigurationModel;
import ch.qos.logback.classic.model.PropertiesConfiguratorModel;
import ch.qos.logback.core.Context;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.joran.util.ConfigurationWatchListUtil;
import ch.qos.logback.core.model.Model;
import ch.qos.logback.core.model.ResourceModel;
import ch.qos.logback.core.model.processor.ModelHandlerException;
import ch.qos.logback.core.model.processor.ModelInterpretationContext;
import ch.qos.logback.core.model.processor.ResourceHandlerBase;
import ch.qos.logback.core.spi.ContextAwarePropertyContainer;
import ch.qos.logback.core.util.OptionHelper;

import java.io.InputStream;
import java.net.URL;

public class PropertiesConfiguratorModelHandler extends ResourceHandlerBase {
    boolean inError = false;

    static final boolean CREATE_CWL_IF_NOT_ALREADY_CREATED = true;

    public PropertiesConfiguratorModelHandler(Context context) {
        super(context);
    }

    static public PropertiesConfiguratorModelHandler makeInstance(Context context, ModelInterpretationContext mic) {
        return new PropertiesConfiguratorModelHandler(context);
    }

    @Override
    public void handle(ModelInterpretationContext mic, Model model) throws ModelHandlerException {

        Boolean topScanBoolean = mic.getTopScanBoolean();
        detachedHandle(mic, model, topScanBoolean);
    }

    /**
     *
     * Used by {@link #handle(ModelInterpretationContext, Model)} as well as logback-tyler. Note the widening of the
     * base from {@link ModelInterpretationContext} to {@link ContextAwarePropertyContainer}.
     *
     * @param capc
     * @param model
     * @throws ModelHandlerException
     * @since 1.5.10
     */
    public void detachedHandle(ContextAwarePropertyContainer capc, Model model, Boolean topScanBoolean) throws ModelHandlerException {

        PropertiesConfiguratorModel propertyConfiguratorModel = (PropertiesConfiguratorModel) model;

        this.optional = OptionHelper.toBoolean(propertyConfiguratorModel.getOptional(), false);

        if (!checkAttributes(propertyConfiguratorModel)) {
            inError = true;
            return;
        }

        URL inputURL = getInputURL(capc, propertyConfiguratorModel);
        if (inputURL == null) {
            inError = true;
            return;
        }


        Boolean localScan = OptionHelper.toBooleanObject(propertyConfiguratorModel.getScanStr());

        InputStream in = openURL(inputURL);
        if (in == null) {
            inError = true;
            return;
        }

        if(localScan == Boolean.TRUE || topScanBoolean == Boolean.TRUE) {
            if(topScanBoolean != Boolean.TRUE) {
                // if topScanBoolean ia not TRUE, then a ConfigurationWatchList has not been created and registered, yet
                // we need to do so now
                ConfigurationWatchListUtil.registerNewConfigurationWatchListWithContext(context);
            }
            ConfigurationWatchListUtil.addToWatchList(context, inputURL, CREATE_CWL_IF_NOT_ALREADY_CREATED);
        }



        addInfo("Reading configuration from [" + getAttribureInUse() + "]");

        PropertiesConfigurator propertiesConfigurator = new PropertiesConfigurator();
        propertiesConfigurator.setContext(capc.getContext());
        try {
            propertiesConfigurator.doConfigure(in);
        } catch (JoranException e) {
            addError("Could not configure from " + getAttribureInUse());
            throw new ModelHandlerException(e);
        }

    }

    protected InputStream getInputStream(ContextAwarePropertyContainer capc, ResourceModel resourceModel) {
        URL inputURL = getInputURL(capc, resourceModel);
        if (inputURL == null)
            return null;



        ConfigurationWatchListUtil.addToWatchList(context, inputURL, CREATE_CWL_IF_NOT_ALREADY_CREATED);
        return openURL(inputURL);
    }

}
