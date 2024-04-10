/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2015, QOS.ch. All rights reserved.
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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.List;

import org.xml.sax.Attributes;

import ch.qos.logback.core.joran.event.SaxEvent;
import ch.qos.logback.core.joran.event.SaxEventRecorder;
import ch.qos.logback.core.joran.spi.ActionException;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.joran.spi.SaxEventInterpretationContext;
import ch.qos.logback.core.joran.util.ConfigurationWatchListUtil;
import ch.qos.logback.core.model.IncludeModel;
import ch.qos.logback.core.model.Model;
import ch.qos.logback.core.util.Loader;
import ch.qos.logback.core.util.OptionHelper;

import static ch.qos.logback.core.joran.JoranConstants.INCLUDED_TAG;

/**
 * 
 * @author ceki
 *
 */
public class IncludeAction extends Action {

    private static final String FILE_ATTR = "file";
    private static final String URL_ATTR = "url";
    private static final String RESOURCE_ATTR = "resource";
    private static final String OPTIONAL_ATTR = "optional";

    Model parentModel;
    IncludeModel includeModel;
    boolean inError = false;
    
    @Override
    public void begin(SaxEventInterpretationContext seic, String tagName, Attributes attributes) throws ActionException {

        String optionalStr = attributes.getValue(OPTIONAL_ATTR);

        this.includeModel = new IncludeModel();
        this.includeModel.setOptional(optionalStr);
        fillInIncludeModelAttributes(includeModel, tagName, attributes);
        if (!seic.isModelStackEmpty()) {
            parentModel = seic.peekModel();
        }
        final int lineNumber = getLineNumber(seic);
        this.includeModel.setLineNumber(lineNumber);
        seic.pushModel(this.includeModel);
    }

    private void fillInIncludeModelAttributes(IncludeModel includeModel, String tagName, Attributes attributes) {
        this.includeModel.setTag(tagName);
        String fileAttribute = attributes.getValue(FILE_ATTR);
        String urlAttribute = attributes.getValue(URL_ATTR);
        String resourceAttribute = attributes.getValue(RESOURCE_ATTR);

        this.includeModel.setFile(fileAttribute);
        this.includeModel.setUrl(urlAttribute);
        this.includeModel.setResource(resourceAttribute);
    }


    @Override
    public void end(SaxEventInterpretationContext seic, String name) throws ActionException {
        
        if(inError)
            return;
        
        Model m = seic.peekModel();

        if (m != includeModel) {
            addWarn("The object at the of the stack is not the model [" + includeModel.idString()
                    + "] pushed earlier.");
            addWarn("This is wholly unexpected.");
        }

        // do not pop nor add to parent if there is no parent
        if (parentModel != null) {
            parentModel.addSubModel(includeModel);
            seic.popModel();
        }
    }
}
