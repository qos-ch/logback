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

package ch.qos.logback.classic.spi;

import org.slf4j.ILoggerFactory;
import org.slf4j.IMarkerFactory;
import org.slf4j.helpers.BasicMarkerFactory;
import org.slf4j.helpers.Util;
import org.slf4j.spi.MDCAdapter;
import org.slf4j.spi.SLF4JServiceProvider;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.util.ContextInitializer;
import ch.qos.logback.classic.util.LogbackMDCAdapter;
import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.status.StatusUtil;
import ch.qos.logback.core.util.StatusPrinter;

public class LogbackServiceProvider implements SLF4JServiceProvider {

    final static String NULL_CS_URL = CoreConstants.CODES_URL + "#null_CS";

    /**
     * Declare the version of the SLF4J API this implementation is compiled against.
     * The value of this field is modified with each major release.
     */
    // to avoid constant folding by the compiler, this field must *not* be final
    public static String REQUESTED_API_VERSION = "2.0.99"; // !final

    private LoggerContext defaultLoggerContext = new LoggerContext();


    // org.slf4j.LoggerFactory expects providers to initialize markerFactory as early as possible.
    private IMarkerFactory markerFactory = new BasicMarkerFactory();

    // org.slf4j.LoggerFactory expects providers to initialize their MDCAdapter field
    // as early as possible, preferably at construction time.
    private LogbackMDCAdapter mdcAdapter = new LogbackMDCAdapter();

    @Override
    public void initialize() {
        defaultLoggerContext.setName(CoreConstants.DEFAULT_CONTEXT_NAME);
        // set the MDCAdapter for the defaultLoggerContext immediately
        defaultLoggerContext.setMDCAdapter(mdcAdapter);
        initializeLoggerContext();
        defaultLoggerContext.start();
    }

    private void initializeLoggerContext() {
        try {
            try {
                new ContextInitializer(defaultLoggerContext).autoConfig();
            } catch (JoranException je) {
                Util.report("Failed to auto configure default logger context", je);
            }
            // LOGBACK-292
            if (!StatusUtil.contextHasStatusListener(defaultLoggerContext)) {
                StatusPrinter.printInCaseOfErrorsOrWarnings(defaultLoggerContext);
            }
            // contextSelectorBinder.init(defaultLoggerContext, KEY);

        } catch (Exception t) { // see LOGBACK-1159
            Util.report("Failed to instantiate [" + LoggerContext.class.getName() + "]", t);
        }
    }

    @Override

    public ILoggerFactory getLoggerFactory() {
        return defaultLoggerContext;
    }

    @Override
    public IMarkerFactory getMarkerFactory() {
        return markerFactory;
    }

    @Override
    public MDCAdapter getMDCAdapter() {
        return mdcAdapter;
    }

    @Override
    public String getRequestedApiVersion() {
        return REQUESTED_API_VERSION;
    }

}
