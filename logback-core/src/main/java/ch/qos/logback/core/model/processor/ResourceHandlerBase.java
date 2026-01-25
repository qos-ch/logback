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
import ch.qos.logback.core.model.ResourceModel;
import ch.qos.logback.core.spi.ContextAwarePropertyContainer;
import ch.qos.logback.core.util.Loader;
import ch.qos.logback.core.util.OptionHelper;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

abstract public class ResourceHandlerBase extends ModelHandlerBase {

    protected String attributeInUse;
    protected boolean optional;

    protected ResourceHandlerBase(Context context) {
        super(context);
    }

    protected InputStream openURL(URL url) {
        try {
            return url.openStream();
        } catch (IOException e) {
            warnIfRequired("Failed to open [" + url.toString() + "]");
            return null;
        }
    }

    protected boolean checkAttributes(ResourceModel resourceModel) {
        String fileAttribute = resourceModel.getFile();
        String urlAttribute = resourceModel.getUrl();
        String resourceAttribute = resourceModel.getResource();

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


    protected String getAttribureInUse() {
        return this.attributeInUse;
    }

    protected URL getInputURL(ContextAwarePropertyContainer contextAwarePropertyContainer, ResourceModel resourceModel) {
        String fileAttribute = resourceModel.getFile();
        String urlAttribute = resourceModel.getUrl();
        String resourceAttribute = resourceModel.getResource();

        if (!OptionHelper.isNullOrEmptyOrAllSpaces(fileAttribute)) {
            this.attributeInUse = contextAwarePropertyContainer.subst(fileAttribute);
            return filePathAsURL(attributeInUse);
        }

        if (!OptionHelper.isNullOrEmptyOrAllSpaces(urlAttribute)) {
            this.attributeInUse = contextAwarePropertyContainer.subst(urlAttribute);
            return attributeToURL(attributeInUse);
        }

        if (!OptionHelper.isNullOrEmptyOrAllSpaces(resourceAttribute)) {
            this.attributeInUse = contextAwarePropertyContainer.subst(resourceAttribute);
            return resourceAsURL(attributeInUse);
        }
        // given preceding checkAttributes() check we cannot reach this line
        throw new IllegalStateException("A URL stream should have been returned at this stage");

    }

    protected URL filePathAsURL(String path) {
        URI uri = new File(path).toURI();
        try {
            return uri.toURL();
        } catch (MalformedURLException e) {
            // impossible to get here
            e.printStackTrace();
            return null;
        }
    }

    protected URL attributeToURL(String urlAttribute) {
        try {
            return new URL(urlAttribute);
        } catch (MalformedURLException mue) {
            String errMsg = "URL [" + urlAttribute + "] is not well formed.";
            addError(errMsg, mue);
            return null;
        }
    }

    protected URL resourceAsURL(String resourceAttribute) {
        URL url = Loader.getResourceBySelfClassLoader(resourceAttribute);
        if (url == null) {
            warnIfRequired("Could not find resource corresponding to [" + resourceAttribute + "]");
            return null;
        } else
            return url;
    }

    protected void warnIfRequired(String msg) {
        if (!optional) {
            addWarn(msg);
        }
    }
}
