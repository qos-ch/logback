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

package ch.qos.logback.core.joran.action;

import ch.qos.logback.core.joran.spi.SaxEventInterpretationContext;
import ch.qos.logback.core.model.Model;
import ch.qos.logback.core.model.ResourceModel;
import org.xml.sax.Attributes;

/**
 * An action which builds subclass instances of {@link ResourceModel}.
 *
 * @since 1.5.8
 */
abstract public class ResourceAction extends BaseModelAction {

    private static final String FILE_ATTR = "file";
    private static final String URL_ATTR = "url";
    private static final String RESOURCE_ATTR = "resource";
    private static final String OPTIONAL_ATTR = "optional";


    abstract protected ResourceModel makeNewResourceModel();

    @Override
    protected boolean validPreconditions(SaxEventInterpretationContext intercon, String name, Attributes attributes) {
        PreconditionValidator pv = new PreconditionValidator(this, intercon, name, attributes);
        pv.validateOneAndOnlyOneAttributeProvided(FILE_ATTR, URL_ATTR, RESOURCE_ATTR);
        return pv.isValid();
    }

    @Override
    protected Model buildCurrentModel(SaxEventInterpretationContext interpretationContext, String localName,
                                      Attributes attributes) {
        ResourceModel resourceModel = makeNewResourceModel();
        fillInIncludeModelAttributes(resourceModel, localName, attributes);
        return resourceModel;
    }


    private void fillInIncludeModelAttributes(ResourceModel resourceModel, String tagName, Attributes attributes) {
        resourceModel.setTag(tagName);
        String fileAttribute = attributes.getValue(FILE_ATTR);
        String urlAttribute = attributes.getValue(URL_ATTR);
        String resourceAttribute = attributes.getValue(RESOURCE_ATTR);
        String optionalAttribute = attributes.getValue(OPTIONAL_ATTR);
        resourceModel.setFile(fileAttribute);
        resourceModel.setUrl(urlAttribute);
        resourceModel.setResource(resourceAttribute);
        resourceModel.setOptional(optionalAttribute);
    }

}
