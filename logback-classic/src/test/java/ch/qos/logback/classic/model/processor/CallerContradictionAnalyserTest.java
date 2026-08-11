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
package ch.qos.logback.classic.model.processor;

import ch.qos.logback.classic.ClassicTestConstants;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.classic.util.LogbackMDCAdapter;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.status.Status;
import ch.qos.logback.core.status.testUtil.StatusChecker;
import org.junit.jupiter.api.Test;
import org.slf4j.spi.MDCAdapter;

import static ch.qos.logback.classic.model.processor.CallerContradictionAnalyser.INCLUDE_CALLER_DATA_NAME_SUFFIX;
import static ch.qos.logback.classic.model.processor.CallerContradictionAnalyser.LAYOUT_NAME_SUFFIX;
import static ch.qos.logback.classic.model.processor.CallerContradictionAnalyser.SKIP_CALLER_CONTRADICTION_ANALYSIS_PROPERTY;
import static ch.qos.logback.classic.model.processor.CallerInstructionLogic.CALLER_CONTRADICTION_URL;
import static ch.qos.logback.classic.model.processor.CallerInstructionLogic.LONE_PREPROCESS_WANT_MSG_TEMPLATE;
import static ch.qos.logback.classic.model.processor.CallerInstructionLogic.NO_CONTRADICTIONS_MSG;
import static ch.qos.logback.classic.model.processor.CallerInstructionLogic.WARNING_MSG_TEMPLATE;

/**
 * End-to-end coverage of {@link CallerContradictionAnalyser},
 * {@link CallerContradictionWarnAnalyser} and {@link CallerInstructionLogic}
 * via real configuration files.
 */
public class CallerContradictionAnalyserTest {

    private static final String INPUT_DIR =
            ClassicTestConstants.JORAN_INPUT_PREFIX + "callerContradiction/";

    LoggerContext loggerContext = new LoggerContext();
    MDCAdapter mdcAdapter = new LogbackMDCAdapter();
    StatusChecker checker = new StatusChecker(loggerContext);

    void configure(String file) throws JoranException {
        loggerContext.setMDCAdapter(mdcAdapter);
        JoranConfigurator jc = new JoranConfigurator();
        jc.setContext(loggerContext);
        jc.doConfigure(file);
    }

    // --- no contradiction ---

    @Test
    public void asyncIncludeCallerDataTrueWrapsCallerPatternNoWarn() throws JoranException {
        // PREPROCESS_WANT (ASYNC) + DIRECT_WANT (CONSOLE) is allowed
        configure(INPUT_DIR + "asyncWithCallerDataWrapsCallerPattern.xml");
        assertNoContradiction();
    }

    @Test
    public void asyncDefaultIncludeCallerDataWrapsNonCallerPatternNoWarn() throws JoranException {
        // DO_NOT_WANT (ASYNC) alone is allowed
        configure(INPUT_DIR + "asyncNoCallerDataWrapsNoCallerPattern.xml");
        assertNoContradiction();
    }

    @Test
    public void consoleWithCallerPatternOnlyNoWarn() throws JoranException {
        // DIRECT_WANT (CONSOLE) alone is allowed
        configure(INPUT_DIR + "consoleWithCallerPatternOnly.xml");
        assertNoContradiction();
    }

    // --- DO_NOT_WANT vs DIRECT_WANT ---

    @Test
    public void asyncDefaultIncludeCallerDataWrapsCallerPatternTriggersWarn() throws JoranException {
        // DO_NOT_WANT (ASYNC) + DIRECT_WANT (CONSOLE)
        configure(INPUT_DIR + "asyncNoCallerDataWrapsCallerPattern.xml");

        assertContradictionWarning(String.format(WARNING_MSG_TEMPLATE, "ASYNC", "CONSOLE"));
    }

    @Test
    public void skipPropertyInConfigDisablesContradictionAnalysis() throws JoranException {
        // Would warn without the skip property; with it, analysis is not run.
        configure(INPUT_DIR + "asyncNoCallerDataWrapsCallerPatternSkipAnalysis.xml");

        assertAnalysisSkipped();
    }

    @Test
    public void skipPropertyOnContextDisablesContradictionAnalysis() throws JoranException {
        loggerContext.putProperty(SKIP_CALLER_CONTRADICTION_ANALYSIS_PROPERTY, "true");
        configure(INPUT_DIR + "asyncNoCallerDataWrapsCallerPattern.xml");

        assertAnalysisSkipped();
    }

    @Test
    public void socketDefaultIncludeCallerDataWithCallerPatternTriggersWarn() throws JoranException {
        // DO_NOT_WANT (SOCKET) + DIRECT_WANT (CONSOLE)
        configure(INPUT_DIR + "socketDefaultIncludeCallerDataWithCallerPattern.xml");

        assertContradictionWarning(String.format(WARNING_MSG_TEMPLATE, "SOCKET", "CONSOLE"));
    }

    @Test
    public void smtpDefaultIncludeCallerDataLayoutWithCallerPatternTriggersWarn() throws JoranException {
        // DO_NOT_WANT (EMAIL.includeCallerData) + DIRECT_WANT (EMAIL.layout)
        configure(INPUT_DIR + "smtpDefaultIncludeCallerDataLayoutWithCallerPattern.xml");

        assertContradictionWarning(String.format(WARNING_MSG_TEMPLATE,
                "EMAIL" + INCLUDE_CALLER_DATA_NAME_SUFFIX, "EMAIL" + LAYOUT_NAME_SUFFIX));
    }

    @Test
    public void twoAsyncSuppressSameCallerPatternJoinsNamesInWarning() throws JoranException {
        // DO_NOT_WANT (ASYNC1, ASYNC2) + DIRECT_WANT (CONSOLE, FILE)
        configure(INPUT_DIR + "twoAsyncSuppressSameCallerPattern.xml");

        assertContradictionWarning(String.format(WARNING_MSG_TEMPLATE, "ASYNC1, ASYNC2", "CONSOLE, FILE"));
    }

    // --- lone PREPROCESS_WANT ---

    @Test
    public void asyncIncludeCallerDataTrueWrapsNonCallerPatternTriggersLonePreprocessWarn()
            throws JoranException {
        // PREPROCESS_WANT (ASYNC) without any DIRECT_WANT
        configure(INPUT_DIR + "asyncWithCallerDataWrapsNoCallerPattern.xml");

        assertContradictionWarning(String.format(LONE_PREPROCESS_WANT_MSG_TEMPLATE, "ASYNC"));
    }

    @Test
    public void smtpIncludeCallerDataTrueLayoutWithoutCallerPatternTriggersLonePreprocessWarn()
            throws JoranException {
        // PREPROCESS_WANT (EMAIL.includeCallerData) without any DIRECT_WANT
        // (Console and SMTP layout patterns do not use caller converters)
        configure(INPUT_DIR + "smtpIncludeCallerDataTrueLayoutWithoutCallerPattern.xml");

        assertContradictionWarning(String.format(LONE_PREPROCESS_WANT_MSG_TEMPLATE,
                "EMAIL" + INCLUDE_CALLER_DATA_NAME_SUFFIX));
    }

    // --- SMTP PREPROCESS_WANT + DIRECT_WANT (consistent) ---

    @Test
    public void smtpIncludeCallerDataTrueLayoutWithCallerPatternNoWarn() throws JoranException {
        // PREPROCESS_WANT (EMAIL.includeCallerData) + DIRECT_WANT (EMAIL.layout)
        configure(INPUT_DIR + "smtpIncludeCallerDataTrueLayoutWithCallerPattern.xml");
        assertNoContradiction();
    }

    // --- DO_NOT_WANT vs PREPROCESS_WANT ---

    @Test
    public void twoAsyncDifferentIncludeCallerDataNoCallerPatternTriggersWarn() throws JoranException {
        // DO_NOT_WANT (ASYNC_0) + PREPROCESS_WANT (ASYNC_1)
        configure(INPUT_DIR + "twoAsyncDifferentIncludeCallerDataNoCallerPattern.xml");

        assertContradictionWarning(String.format(WARNING_MSG_TEMPLATE, "ASYNC_0", "ASYNC_1"));
    }

    // --- DO_NOT_WANT vs PREPROCESS_WANT and vs DIRECT_WANT ---

    @Test
    public void twoAsyncDifferentIncludeCallerDataWithCallerPatternTriggersTwoWarns()
            throws JoranException {
        // DO_NOT_WANT (ASYNC_0) + PREPROCESS_WANT (ASYNC_1) + DIRECT_WANT (CONSOLE)
        configure(INPUT_DIR + "twoAsyncDifferentIncludeCallerDataWithCallerPattern.xml");

        checker.assertContainsMatch(Status.WARN,
                String.format(WARNING_MSG_TEMPLATE, "ASYNC_0", "ASYNC_1"));
        checker.assertContainsMatch(Status.WARN,
                String.format(WARNING_MSG_TEMPLATE, "ASYNC_0", "CONSOLE"));
        checker.assertContainsMatch(Status.WARN, "See " + CALLER_CONTRADICTION_URL + " for details");
        checker.assertMatchCount(NO_CONTRADICTIONS_MSG, 0);
    }

    private void assertNoContradiction() {
        checker.assertMatchCount(NO_CONTRADICTIONS_MSG, 1);
        checker.assertMatchCount(CALLER_CONTRADICTION_URL, 0);
    }

    private void assertContradictionWarning(String expectedWarnMsg) {
        checker.assertContainsMatch(Status.WARN, expectedWarnMsg);
        checker.assertContainsMatch(Status.WARN, "See " + CALLER_CONTRADICTION_URL + " for details");
        checker.assertMatchCount(NO_CONTRADICTIONS_MSG, 0);
    }

    private void assertAnalysisSkipped() {
        checker.assertMatchCount(NO_CONTRADICTIONS_MSG, 0);
        checker.assertMatchCount(CALLER_CONTRADICTION_URL, 0);
        checker.assertMatchCount(String.format(WARNING_MSG_TEMPLATE, "ASYNC", "CONSOLE"), 0);
    }
}
