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

package ch.qos.logback.core.joran.action;

import ch.qos.logback.core.joran.spi.ActionException;
import ch.qos.logback.core.joran.spi.SaxEventInterpretationContext;
import ch.qos.logback.core.model.Model;
import ch.qos.logback.core.model.ResourceModel;
import org.xml.sax.Attributes;

/**
 * An action which builds subclass instances of {@link ResourceModel}.
 *
 * @since 1.5.8
 */
abstract public class ResourceAction extends Action {

    private static final String FILE_ATTR = "file";
    private static final String URL_ATTR = "url";
    private static final String RESOURCE_ATTR = "resource";
    private static final String OPTIONAL_ATTR = "optional";

    Model parentModel;
    ResourceModel resourceModel;
    boolean inError = false;

    abstract protected ResourceModel makeNewResourceModel();

    @Override
    public void begin(SaxEventInterpretationContext seic, String tagName, Attributes attributes) throws ActionException {
        String optionalStr = attributes.getValue(OPTIONAL_ATTR);

        this.resourceModel = makeNewResourceModel();
        this.resourceModel.setOptional(optionalStr);
        fillInIncludeModelAttributes(resourceModel, tagName, attributes);
        if (!seic.isModelStackEmpty()) {
            parentModel = seic.peekModel();
        }
        final int lineNumber = getLineNumber(seic);
        this.resourceModel.setLineNumber(lineNumber);
        seic.pushModel(this.resourceModel);
    }

    private void fillInIncludeModelAttributes(ResourceModel resourceModel, String tagName, Attributes attributes) {
        this.resourceModel.setTag(tagName);
        String fileAttribute = attributes.getValue(FILE_ATTR);
        String urlAttribute = attributes.getValue(URL_ATTR);
        String resourceAttribute = attributes.getValue(RESOURCE_ATTR);

        this.resourceModel.setFile(fileAttribute);
        this.resourceModel.setUrl(urlAttribute);
        this.resourceModel.setResource(resourceAttribute);
    }

    @Override
    public void end(SaxEventInterpretationContext seic, String name) throws ActionException {
        if(inError)
            return;

        Model m = seic.peekModel();

        if (m != resourceModel) {
            addWarn("The object at the of the stack is not the model [" + resourceModel.idString()
                            + "] pushed earlier.");
            addWarn("This is wholly unexpected.");
        }

        // do not pop nor add to parent if there is no parent
        if (parentModel != null) {
            parentModel.addSubModel(resourceModel);
            seic.popModel();
        }
    }
}
