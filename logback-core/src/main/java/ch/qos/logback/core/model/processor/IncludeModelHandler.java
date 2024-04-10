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
public class IncludeModelHandler extends ModelHandlerBase {
    boolean inError = false;
    private String attributeInUse;
    private boolean optional;

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

            //trimHeadAndTail(saxEvents);

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

    private void trimHeadAndTail( List<SaxEvent> saxEventList) {
        // Let's remove the two <included> events before
        // adding the events to the player.

        // note saxEventList.size() changes over time as events are removed

        if (saxEventList.size() == 0) {
            return;
        }

        SaxEvent first = saxEventList.get(0);
        if (first != null && first.qName.equalsIgnoreCase(INCLUDED_TAG)) {
            saxEventList.remove(0);
        }

        SaxEvent last = saxEventList.get(saxEventList.size() - 1);
        if (last != null && last.qName.equalsIgnoreCase(INCLUDED_TAG)) {
            saxEventList.remove(saxEventList.size() - 1);
        }
    }

    InputStream getInputStream(ModelInterpretationContext mic, IncludeModel includeModel) {
        URL inputURL = getInputURL(mic, includeModel);
        if (inputURL == null)
            return null;

        ConfigurationWatchListUtil.addToWatchList(context, inputURL);
        return openURL(inputURL);
    }

    InputStream openURL(URL url) {
        try {
            return url.openStream();
        } catch (IOException e) {
            optionalWarning("Failed to open [" + url.toString() + "]");
            return null;
        }
    }

    private boolean checkAttributes(IncludeModel includeModel) {
        String fileAttribute = includeModel.getFile();
        String urlAttribute = includeModel.getUrl();
        String resourceAttribute = includeModel.getResource();

        int count = 0;

        if (!OptionHelper.isNullOrEmptyOrAllSpaces(fileAttribute)) {
            count++;
        }
        if (!OptionHelper.isNullOrEmptyOrAllSpaces(urlAttribute)) {
            count++;
        }
        if (!OptionHelper.isNullOrEmptyOrAllSpaces(resourceAttribute)) {
            count++;
        }

        if (count == 0) {
            addError("One of \"path\", \"resource\" or \"url\" attributes must be set.");
            return false;
        } else if (count > 1) {
            addError("Only one of \"file\", \"url\" or \"resource\" attributes should be set.");
            return false;
        } else if (count == 1) {
            return true;
        }
        throw new IllegalStateException("Count value [" + count + "] is not expected");
    }

    URL getInputURL(ModelInterpretationContext mic, IncludeModel includeModel) {
        String fileAttribute = includeModel.getFile();
        String urlAttribute = includeModel.getUrl();
        String resourceAttribute = includeModel.getResource();

        if (!OptionHelper.isNullOrEmptyOrAllSpaces(fileAttribute)) {
            this.attributeInUse = mic.subst(fileAttribute);
            return filePathAsURL(attributeInUse);
        }

        if (!OptionHelper.isNullOrEmptyOrAllSpaces(urlAttribute)) {
            this.attributeInUse = mic.subst(urlAttribute);
            return attributeToURL(attributeInUse);
        }

        if (!OptionHelper.isNullOrEmptyOrAllSpaces(resourceAttribute)) {
            this.attributeInUse = mic.subst(resourceAttribute);
            return resourceAsURL(attributeInUse);
        }
        // given preceding checkAttributes() check we cannot reach this line
        throw new IllegalStateException("A URL stream should have been returned at this stage");

    }

    URL filePathAsURL(String path) {
        URI uri = new File(path).toURI();
        try {
            return uri.toURL();
        } catch (MalformedURLException e) {
            // impossible to get here
            e.printStackTrace();
            return null;
        }
    }

    URL attributeToURL(String urlAttribute) {
        try {
            return new URL(urlAttribute);
        } catch (MalformedURLException mue) {
            String errMsg = "URL [" + urlAttribute + "] is not well formed.";
            addError(errMsg, mue);
            return null;
        }
    }

    URL resourceAsURL(String resourceAttribute) {
        URL url = Loader.getResourceBySelfClassLoader(resourceAttribute);
        if (url == null) {
            optionalWarning("Could not find resource corresponding to [" + resourceAttribute + "]");
            return null;
        } else
            return url;
    }

    private void optionalWarning(String msg) {
        if (!optional) {
            addWarn(msg);
        }
    }
}
