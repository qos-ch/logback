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
import ch.qos.logback.core.joran.spi.InterpretationContext;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.joran.spi.RuleStore;
import ch.qos.logback.core.joran.spi.SaxEventInterpreter;
import ch.qos.logback.core.joran.spi.SimpleRuleStore;
import ch.qos.logback.core.joran.util.ConfigurationWatchListUtil;
import ch.qos.logback.core.model.Model;
import ch.qos.logback.core.model.processor.DefaultProcessor;
import ch.qos.logback.core.spi.ContextAwareBase;
import ch.qos.logback.core.status.StatusUtil;

public abstract class GenericConfigurator extends ContextAwareBase {

    protected SaxEventInterpreter interpreter;

    public final void doConfigure(final URL url) throws JoranException {
        InputStream in = null;
        try {
            informContextOfURLUsedForConfiguration(getContext(), url);
            final URLConnection urlConnection = url.openConnection();
            // per http://jira.qos.ch/browse/LBCORE-105
            // per http://jira.qos.ch/browse/LBCORE-127
            urlConnection.setUseCaches(false);

            in = urlConnection.getInputStream();
            doConfigure(in, url.toExternalForm());
        } catch (final IOException ioe) {
            final String errMsg = "Could not open URL [" + url + "].";
            addError(errMsg, ioe);
            throw new JoranException(errMsg, ioe);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (final IOException ioe) {
                    final String errMsg = "Could not close input stream";
                    addError(errMsg, ioe);
                    throw new JoranException(errMsg, ioe);
                }
            }
        }
    }

    public final void doConfigure(final String filename) throws JoranException {
        doConfigure(new File(filename));
    }

    public final void doConfigure(final File file) throws JoranException {
        FileInputStream fis = null;
        try {
            final URL url = file.toURI().toURL();
            informContextOfURLUsedForConfiguration(getContext(), url);
            fis = new FileInputStream(file);
            doConfigure(fis, url.toExternalForm());
        } catch (final IOException ioe) {
            final String errMsg = "Could not open [" + file.getPath() + "].";
            addError(errMsg, ioe);
            throw new JoranException(errMsg, ioe);
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (final java.io.IOException ioe) {
                    final String errMsg = "Could not close [" + file.getName() + "].";
                    addError(errMsg, ioe);
                    throw new JoranException(errMsg, ioe);
                }
            }
        }
    }

    public static void informContextOfURLUsedForConfiguration(final Context context, final URL url) {
        ConfigurationWatchListUtil.setMainWatchURL(context, url);
    }

    public final void doConfigure(final InputStream inputStream) throws JoranException {
        doConfigure(new InputSource(inputStream));
    }

    public final void doConfigure(final InputStream inputStream, final String systemId) throws JoranException {
        final InputSource inputSource = new InputSource(inputStream);
        inputSource.setSystemId(systemId);
        doConfigure(inputSource);
    }

    protected abstract void addInstanceRules(RuleStore rs);

    protected abstract void addImplicitRules(SaxEventInterpreter interpreter);

    protected void addDefaultNestedComponentRegistryRules(final DefaultNestedComponentRegistry registry) {

    }

    protected ElementPath initialElementPath() {
        return new ElementPath();
    }

    protected void buildInterpreter() {
        final RuleStore rs = new SimpleRuleStore(context);
        addInstanceRules(rs);
        interpreter = new SaxEventInterpreter(context, rs, initialElementPath());
        final InterpretationContext interpretationContext = interpreter.getInterpretationContext();
        interpretationContext.setContext(context);
        addImplicitRules(interpreter);
        addDefaultNestedComponentRegistryRules(interpretationContext.getDefaultNestedComponentRegistry());
    }

    // this is the most inner form of doConfigure whereto other doConfigure
    // methods ultimately delegate
    public final void doConfigure(final InputSource inputSource) throws JoranException {

        final long threshold = System.currentTimeMillis();
        // if (!ConfigurationWatchListUtil.wasConfigurationWatchListReset(context)) {
        // informContextOfURLUsedForConfiguration(getContext(), null);
        // }
        final SaxEventRecorder recorder = new SaxEventRecorder(context);
        recorder.recordEvents(inputSource);

        playEventsAndProcessModel(recorder.saxEventList);

        // no exceptions a this level
        final StatusUtil statusUtil = new StatusUtil(context);
        if (statusUtil.noXMLParsingErrorsOccurred(threshold)) {
            addInfo("Registering current configuration as safe fallback point");
            registerSafeConfiguration(recorder.saxEventList);
        }
    }


    public void playEventsAndProcessModel(final List<SaxEvent> saxEvents) throws JoranException {
        buildInterpreter();
        playSaxEvents(saxEvents);
        final Model top = interpreter.getInterpretationContext().peekModel();
        //serializeModel(top);
        processModel(top);
    }

    //	public static String TTT = "c:/tmp/x.model";
    //	void serializeModel(Model top) {
    //		try {
    //			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(TTT));
    //			oos.writeObject(top);
    //			oos.close();
    //		} catch (IOException e) {
    //			e.printStackTrace();
    //		}
    //	}

    private void playSaxEvents(final List<SaxEvent> eventList) throws JoranException {
        // disallow simultaneous configurations of the same context
        synchronized (context.getConfigurationLock()) {
            interpreter.getEventPlayer().play(eventList);
        }

    }

    protected void processModel(final Model model) {
        final DefaultProcessor defaultProcessor = buildDefaultProcessor(context, interpreter.getInterpretationContext());
        defaultProcessor.process(model);
    }

    protected DefaultProcessor buildDefaultProcessor(final Context context, final InterpretationContext interpretationContext) {
        return new DefaultProcessor(context, interpreter.getInterpretationContext());
    }

    /**
     * Register the current event list in currently in the interpreter as a safe
     * configuration point.
     *
     * @since 0.9.30
     */
    public void registerSafeConfiguration(final List<SaxEvent> eventList) {
        context.putObject(SAFE_JORAN_CONFIGURATION, eventList);
    }

    /**
     * Recall the event list previously registered as a safe point.
     */
    @SuppressWarnings("unchecked")
    public List<SaxEvent> recallSafeConfiguration() {
        return (List<SaxEvent>) context.getObject(SAFE_JORAN_CONFIGURATION);
    }
}
