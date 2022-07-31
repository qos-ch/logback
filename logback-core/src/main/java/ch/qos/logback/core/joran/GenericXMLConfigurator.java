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
package ch.qos.logback.core.joran;

import static ch.qos.logback.core.CoreConstants.SAFE_JORAN_CONFIGURATION;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

import org.xml.sax.InputSource;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.joran.event.SaxEvent;
import ch.qos.logback.core.joran.event.SaxEventRecorder;
import ch.qos.logback.core.joran.spi.DefaultNestedComponentRegistry;
import ch.qos.logback.core.joran.spi.ElementPath;
import ch.qos.logback.core.joran.spi.SaxEventInterpretationContext;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.joran.spi.RuleStore;
import ch.qos.logback.core.joran.spi.SaxEventInterpreter;
import ch.qos.logback.core.joran.spi.SimpleRuleStore;
import ch.qos.logback.core.joran.util.ConfigurationWatchListUtil;
import ch.qos.logback.core.model.Model;
import ch.qos.logback.core.model.processor.DefaultProcessor;
import ch.qos.logback.core.model.processor.ModelInterpretationContext;
import ch.qos.logback.core.spi.ContextAwareBase;
import ch.qos.logback.core.spi.ErrorCodes;
import ch.qos.logback.core.status.StatusUtil;

public abstract class GenericXMLConfigurator extends ContextAwareBase {

    protected SaxEventInterpreter saxEventInterpreter;
    protected ModelInterpretationContext modelInterpretationContext;

    public ModelInterpretationContext getModelInterpretationContext() {
        return this.modelInterpretationContext;
    }

    public final void doConfigure(URL url) throws JoranException {
        InputStream in = null;
        try {
            informContextOfURLUsedForConfiguration(getContext(), url);
            URLConnection urlConnection = url.openConnection();
            // per http://jira.qos.ch/browse/LBCORE-105
            // per http://jira.qos.ch/browse/LBCORE-127
            urlConnection.setUseCaches(false);

            in = urlConnection.getInputStream();
            doConfigure(in, url.toExternalForm());
        } catch (IOException ioe) {
            String errMsg = "Could not open URL [" + url + "].";
            addError(errMsg, ioe);
            throw new JoranException(errMsg, ioe);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ioe) {
                    String errMsg = "Could not close input stream";
                    addError(errMsg, ioe);
                    throw new JoranException(errMsg, ioe);
                }
            }
        }
    }

    public final void doConfigure(String filename) throws JoranException {
        doConfigure(new File(filename));
    }

    public final void doConfigure(File file) throws JoranException {
        FileInputStream fis = null;
        try {
            URL url = file.toURI().toURL();
            informContextOfURLUsedForConfiguration(getContext(), url);
            fis = new FileInputStream(file);
            doConfigure(fis, url.toExternalForm());
        } catch (IOException ioe) {
            String errMsg = "Could not open [" + file.getPath() + "].";
            addError(errMsg, ioe);
            throw new JoranException(errMsg, ioe);
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (java.io.IOException ioe) {
                    String errMsg = "Could not close [" + file.getName() + "].";
                    addError(errMsg, ioe);
                    throw new JoranException(errMsg, ioe);
                }
            }
        }
    }

    public static void informContextOfURLUsedForConfiguration(Context context, URL url) {
        ConfigurationWatchListUtil.setMainWatchURL(context, url);
    }

    public final void doConfigure(InputStream inputStream) throws JoranException {
        doConfigure(new InputSource(inputStream));
    }

    public final void doConfigure(InputStream inputStream, String systemId) throws JoranException {
        InputSource inputSource = new InputSource(inputStream);
        inputSource.setSystemId(systemId);
        doConfigure(inputSource);
    }

    protected abstract void addElementSelectorAndActionAssociations(RuleStore rs);
 
    protected abstract void setImplicitRuleSupplier(SaxEventInterpreter interpreter);

    protected void addDefaultNestedComponentRegistryRules(DefaultNestedComponentRegistry registry) {
        // nothing by default
    }

    protected ElementPath initialElementPath() {
        return new ElementPath();
    }

    protected void buildSaxEventInterpreter(List<SaxEvent> saxEvents) {
        RuleStore rs = new SimpleRuleStore(context);
        addElementSelectorAndActionAssociations(rs);
        this.saxEventInterpreter = new SaxEventInterpreter(context, rs, initialElementPath(), saxEvents);
        SaxEventInterpretationContext interpretationContext = saxEventInterpreter.getSaxEventInterpretationContext();
        interpretationContext.setContext(context);
        setImplicitRuleSupplier(saxEventInterpreter);
    }


    protected void buildModelInterpretationContext() {
        this.modelInterpretationContext = new ModelInterpretationContext(context);
        addDefaultNestedComponentRegistryRules(modelInterpretationContext.getDefaultNestedComponentRegistry());
    }

    
    // this is the most inner form of doConfigure whereto other doConfigure
    // methods ultimately delegate
    public final void doConfigure(final InputSource inputSource) throws JoranException {

        long threshold = System.currentTimeMillis();

        SaxEventRecorder recorder = populateSaxEventRecorder(inputSource);
        Model top = buildModelFromSaxEventList(recorder.getSaxEventList());
        if(top == null) {
            addError(ErrorCodes.EMPTY_MODEL_STACK);
            return;
        }
        processModel(top);

        // no exceptions a this level
        StatusUtil statusUtil = new StatusUtil(context);
        if (statusUtil.noXMLParsingErrorsOccurred(threshold)) {
            addInfo("Registering current configuration as safe fallback point");
            registerSafeConfiguration(top);
        }
    }

    public SaxEventRecorder populateSaxEventRecorder(final InputSource inputSource) throws JoranException {
        SaxEventRecorder recorder = new SaxEventRecorder(context);
        recorder.recordEvents(inputSource);
        return recorder;
    }

    public Model buildModelFromSaxEventList(List<SaxEvent> saxEvents) throws JoranException {
        buildSaxEventInterpreter(saxEvents);
        playSaxEvents();
        Model top = saxEventInterpreter.getSaxEventInterpretationContext().peekModel();
        return top;
    }

    private void playSaxEvents() throws JoranException {
        saxEventInterpreter.getEventPlayer().play();
    }

    public void processModel(Model model) {
        buildModelInterpretationContext();
        DefaultProcessor defaultProcessor = new DefaultProcessor(context,  this.modelInterpretationContext);
        addModelHandlerAssociations(defaultProcessor);

        // disallow simultaneous configurations of the same context
        synchronized (context.getConfigurationLock()) {
            defaultProcessor.process(model);
        }
    }

    protected void addModelHandlerAssociations(DefaultProcessor defaultProcessor) {
    }
    
    /**
     * Register the current event list in currently in the interpreter as a safe
     * configuration point.
     *
     * @since 0.9.30
     */
    public void registerSafeConfiguration(Model top) {
        context.putObject(SAFE_JORAN_CONFIGURATION, top);
    }

    /**
     * Recall the event list previously registered as a safe point.
     */
    public Model recallSafeConfiguration() {
        return (Model) context.getObject(SAFE_JORAN_CONFIGURATION);
    }
}
