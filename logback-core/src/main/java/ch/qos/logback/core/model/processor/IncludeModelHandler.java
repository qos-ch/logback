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

package ch.qos.logback.core.model.processor;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.joran.GenericXMLConfigurator;
import ch.qos.logback.core.joran.event.SaxEvent;
import ch.qos.logback.core.joran.event.SaxEventRecorder;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.joran.util.ConfigurationWatchListUtil;
import ch.qos.logback.core.model.IncludeModel;
import ch.qos.logback.core.model.Model;
import ch.qos.logback.core.spi.ErrorCodes;
import ch.qos.logback.core.util.Loader;
import ch.qos.logback.core.util.OptionHelper;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.function.Supplier;

import static ch.qos.logback.core.joran.JoranConstants.CONFIGURATION_TAG;
import static ch.qos.logback.core.joran.JoranConstants.INCLUDED_TAG;

/**
 * @since 1.5.5
 */
public class IncludeModelHandler extends ResourceHandlerBase {
    boolean inError = false;

    public IncludeModelHandler(Context context) {
        super(context);
    }

    static public IncludeModelHandler makeInstance(Context context, ModelInterpretationContext mic) {
        return new IncludeModelHandler(context);
    }

    @Override
    protected Class<IncludeModel> getSupportedModelClass() {
        return IncludeModel.class;
    }

    @Override
    public void handle(ModelInterpretationContext mic, Model model) throws ModelHandlerException {
        IncludeModel includeModel = (IncludeModel) model;

        this.optional = OptionHelper.toBoolean(includeModel.getOptional(), false);

        if (!checkAttributes(includeModel)) {
            inError = true;
            return;
        }

        InputStream in = getInputStream(mic, includeModel);
        if(in == null) {
            inError = true;
            return;
        }

        SaxEventRecorder recorder = null;

        try {
            recorder = populateSaxEventRecorder(in);

            List<SaxEvent> saxEvents = recorder.getSaxEventList();
            if (saxEvents.isEmpty()) {
                addWarn("Empty sax event list");
                return;
            }

            Supplier<? extends GenericXMLConfigurator> jcSupplier = mic.getConfiguratorSupplier();
            if (jcSupplier == null) {
                addError("null configurator supplier. Abandoning inclusion of [" + attributeInUse + "]");
                inError = true;
                return;
            }

            GenericXMLConfigurator genericXMLConfigurator = jcSupplier.get();
            genericXMLConfigurator.getRuleStore().addPathPathMapping(INCLUDED_TAG, CONFIGURATION_TAG);

            Model modelFromIncludedFile = genericXMLConfigurator.buildModelFromSaxEventList(recorder.getSaxEventList());
            if (modelFromIncludedFile == null) {
                addError(ErrorCodes.EMPTY_MODEL_STACK);
                return;
            }

            includeModel.getSubModels().addAll(modelFromIncludedFile.getSubModels());

        } catch (JoranException e) {
            inError = true;
            addError("Error processing XML data in [" + attributeInUse + "]", e);
        }
    }

    public SaxEventRecorder populateSaxEventRecorder(final InputStream inputStream) throws JoranException {
        SaxEventRecorder recorder = new SaxEventRecorder(context);
        recorder.recordEvents(inputStream);
        return recorder;
    }

    private InputStream getInputStream(ModelInterpretationContext mic, IncludeModel includeModel) {
        URL inputURL = getInputURL(mic, includeModel);
        if (inputURL == null)
            return null;
        ConfigurationWatchListUtil.addToWatchList(context, inputURL);
        return openURL(inputURL);
    }

}
