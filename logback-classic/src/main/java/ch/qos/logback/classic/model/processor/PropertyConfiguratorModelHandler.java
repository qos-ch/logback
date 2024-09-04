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

package ch.qos.logback.classic.model.processor;

import ch.qos.logback.classic.joran.PropertyConfigurator;
import ch.qos.logback.classic.model.PropertyConfiguratorModel;
import ch.qos.logback.core.Context;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.joran.util.ConfigurationWatchListUtil;
import ch.qos.logback.core.model.IncludeModel;
import ch.qos.logback.core.model.Model;
import ch.qos.logback.core.model.ResourceModel;
import ch.qos.logback.core.model.processor.ModelHandlerException;
import ch.qos.logback.core.model.processor.ModelInterpretationContext;
import ch.qos.logback.core.model.processor.ResourceHandlerBase;
import ch.qos.logback.core.util.OptionHelper;

import java.io.InputStream;
import java.net.URL;

public class PropertyConfiguratorModelHandler extends ResourceHandlerBase {
    boolean inError = false;

    public PropertyConfiguratorModelHandler(Context context) {
        super(context);
    }

    static public PropertyConfiguratorModelHandler makeInstance(Context context, ModelInterpretationContext mic) {
        return new PropertyConfiguratorModelHandler(context);
    }

    @Override
    public void handle(ModelInterpretationContext mic, Model model) throws ModelHandlerException {
        PropertyConfiguratorModel propertyConfiguratorModel = (PropertyConfiguratorModel) model;

        this.optional = OptionHelper.toBoolean(propertyConfiguratorModel.getOptional(), false);

        if (!checkAttributes(propertyConfiguratorModel)) {
            inError = true;
            return;
        }

        InputStream in = getInputStream(mic, propertyConfiguratorModel);
        if(in == null) {
            inError = true;
            return;
        }

        addInfo("Reading configuration from ["+getAttribureInUse()+"]");

        PropertyConfigurator propertyConfigurator = new PropertyConfigurator();
        propertyConfigurator.setContext(mic.getContext());
        try {
            propertyConfigurator.doConfigure(in);
        } catch (JoranException e) {
            addError("Could not configure from "+getAttribureInUse());
            throw new ModelHandlerException(e);
        }

    }

    protected InputStream getInputStream(ModelInterpretationContext mic, ResourceModel resourceModel) {
        URL inputURL = getInputURL(mic, resourceModel);
        if (inputURL == null)
            return null;

        ConfigurationWatchListUtil.addToWatchList(context, inputURL);
        return openURL(inputURL);
    }

}
