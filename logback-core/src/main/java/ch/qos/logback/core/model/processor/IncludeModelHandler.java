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

package ch.qos.logback.core.model.processor;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.joran.GenericXMLConfigurator;
import ch.qos.logback.core.joran.event.SaxEvent;
import ch.qos.logback.core.joran.event.SaxEventRecorder;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.joran.util.ConfigurationWatchListUtil;
import ch.qos.logback.core.model.IncludeModel;
import ch.qos.logback.core.model.Model;
import ch.qos.logback.core.spi.ContextAwarePropertyContainer;
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
        Model modelFromIncludedFile = buildModelFromIncludedFile(mic, includeModel);
        if (modelFromIncludedFile == null) {
            warnIfRequired("Failed to build include model from included file");
            return;
        }
        processModelFromIncludedFile(includeModel, modelFromIncludedFile);
    }

    /**
     * This method is called by logback-tyler at TylerConfigurator run-time.
     *
     * @param capc
     * @param includeModel
     * @throws ModelHandlerException
     * @since 1.5.11
     */
    public Model buildModelFromIncludedFile(ContextAwarePropertyContainer capc, IncludeModel includeModel) throws ModelHandlerException {

        this.optional = OptionHelper.toBoolean(includeModel.getOptional(), false);

        if (!checkAttributes(includeModel)) {
            inError = true;
            return null;
        }

        InputStream in = getInputStream(capc, includeModel);
        if (in == null) {
            inError = true;
            return null;
        }

        SaxEventRecorder recorder = null;

        try {
            recorder = populateSaxEventRecorder(in);

            List<SaxEvent> saxEvents = recorder.getSaxEventList();
            if (saxEvents.isEmpty()) {
                addWarn("Empty sax event list");
                return null;
            }

            Supplier<? extends GenericXMLConfigurator> jcSupplier = capc.getConfiguratorSupplier();
            if (jcSupplier == null) {
                addError("null configurator supplier. Abandoning inclusion of [" + attributeInUse + "]");
                inError = true;
                return null;
            }

            GenericXMLConfigurator genericXMLConfigurator = jcSupplier.get();
            genericXMLConfigurator.getRuleStore().addPathPathMapping(INCLUDED_TAG, CONFIGURATION_TAG);

            Model modelFromIncludedFile = genericXMLConfigurator.buildModelFromSaxEventList(recorder.getSaxEventList());
            return modelFromIncludedFile;
        } catch (JoranException e) {
            inError = true;
            addError("Error processing XML data in [" + attributeInUse + "]", e);
            return null;
        }
    }

    private void processModelFromIncludedFile(IncludeModel includeModel, Model modelFromIncludedFile) {
        includeModel.getSubModels().addAll(modelFromIncludedFile.getSubModels());
    }

    public SaxEventRecorder populateSaxEventRecorder(final InputStream inputStream) throws JoranException {
        SaxEventRecorder recorder = new SaxEventRecorder(context);
        recorder.recordEvents(inputStream);
        return recorder;
    }

    private InputStream getInputStream(ContextAwarePropertyContainer capc, IncludeModel includeModel) {
        URL inputURL = getInputURL(capc, includeModel);
        if (inputURL == null)
            return null;
        ConfigurationWatchListUtil.addToWatchList(context, inputURL);
        return openURL(inputURL);
    }

}
