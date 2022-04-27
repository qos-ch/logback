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

    private String attributeInUse;
    private boolean optional;

    Model parentModel;
    IncludeModel includeModel;
    boolean inError = false;
    
    @Override
    public void begin(SaxEventInterpretationContext ec, String name, Attributes attributes) throws ActionException {

        parentModel = null;
        includeModel = null;
        
        SaxEventRecorder recorder = new SaxEventRecorder(context);
        
        String optionalStr = attributes.getValue(OPTIONAL_ATTR);
        
        createModelForAlternateUse(ec, name, attributes, optionalStr);
        
        
        this.attributeInUse = null;
        this.optional = OptionHelper.toBoolean(optionalStr, false);
        
        if (!checkAttributes(attributes)) {
            inError = true;
            return;
        }
         
        InputStream in = getInputStream(ec, attributes);

        try {
            if (in != null) {
                parseAndRecord(in, recorder);
                // remove the <included> tag from the beginning and </included> from the end
                trimHeadAndTail(recorder);

                // offset = 2, because we need to get past this element as well as the end
                // element
                ec.getSaxEventInterpreter().getEventPlayer().addEventsDynamically(recorder.getSaxEventList(), 2);
            }
        } catch (JoranException e) {
            addError("Error while parsing  " + attributeInUse, e);
        } finally {
            close(in);
        }

    }

    // model created for later use, not necessarily for configuration purposes.
    private void createModelForAlternateUse(SaxEventInterpretationContext seic, String name, Attributes attributes,
            String optionalStr) {
        this.includeModel = new IncludeModel();
        this.includeModel.setOptional(optionalStr);
        fillInIncludeModelAttributes(includeModel, name, attributes);
        if (!seic.isModelStackEmpty()) {
            parentModel = seic.peekModel();
        }
        final int lineNumber = getLineNumber(seic);
        this.includeModel.setLineNumber(lineNumber);
        seic.pushModel(includeModel);
    }

    private void fillInIncludeModelAttributes(IncludeModel includeModel, String name, Attributes attributes) {
        this.includeModel.setTag(name);
        String fileAttribute = attributes.getValue(FILE_ATTR);
        String urlAttribute = attributes.getValue(URL_ATTR);
        String resourceAttribute = attributes.getValue(RESOURCE_ATTR);
        
        this.includeModel.setFile(fileAttribute);
        this.includeModel.setUrl(urlAttribute);
        this.includeModel.setResource(resourceAttribute);
        
    }

    void close(InputStream in) {
        if (in != null) {
            try {
                in.close();
            } catch (IOException e) {
            }
        }
    }

    private boolean checkAttributes(Attributes attributes) {
        String fileAttribute = attributes.getValue(FILE_ATTR);
        String urlAttribute = attributes.getValue(URL_ATTR);
        String resourceAttribute = attributes.getValue(RESOURCE_ATTR);

        int count = 0;

        if (!OptionHelper.isNullOrEmpty(fileAttribute)) {
            count++;
        }
        if (!OptionHelper.isNullOrEmpty(urlAttribute)) {
            count++;
        }
        if (!OptionHelper.isNullOrEmpty(resourceAttribute)) {
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

    URL attributeToURL(String urlAttribute) {
        try {
            return new URL(urlAttribute);
        } catch (MalformedURLException mue) {
            String errMsg = "URL [" + urlAttribute + "] is not well formed.";
            addError(errMsg, mue);
            return null;
        }
    }

    InputStream openURL(URL url) {
        try {
            return url.openStream();
        } catch (IOException e) {
            optionalWarning("Failed to open [" + url.toString() + "]");
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

    URL getInputURL(SaxEventInterpretationContext ec, Attributes attributes) {
        String fileAttribute = attributes.getValue(FILE_ATTR);
        String urlAttribute = attributes.getValue(URL_ATTR);
        String resourceAttribute = attributes.getValue(RESOURCE_ATTR);

        if (!OptionHelper.isNullOrEmpty(fileAttribute)) {
            this.attributeInUse = ec.subst(fileAttribute);
            return filePathAsURL(attributeInUse);
        }

        if (!OptionHelper.isNullOrEmpty(urlAttribute)) {
            this.attributeInUse = ec.subst(urlAttribute);
            return attributeToURL(attributeInUse);
        }

        if (!OptionHelper.isNullOrEmpty(resourceAttribute)) {
            this.attributeInUse = ec.subst(resourceAttribute);
            return resourceAsURL(attributeInUse);
        }
        // given previous checkAttributes() check we cannot reach this line
        throw new IllegalStateException("A URL stream should have been returned");

    }

    InputStream getInputStream(SaxEventInterpretationContext ec, Attributes attributes) {
        URL inputURL = getInputURL(ec, attributes);
        if (inputURL == null)
            return null;

        ConfigurationWatchListUtil.addToWatchList(context, inputURL);
        return openURL(inputURL);
    }

    private void trimHeadAndTail(SaxEventRecorder recorder) {
        // Let's remove the two <included> events before
        // adding the events to the player.

        // note saxEventList.size() changes over time as events are removed 
        
        List<SaxEvent> saxEventList = recorder.getSaxEventList();

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

    private void parseAndRecord(InputStream inputSource, SaxEventRecorder recorder) throws JoranException {
        recorder.setContext(context);
        recorder.recordEvents(inputSource);
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
