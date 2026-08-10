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
import ch.qos.logback.core.status.testUtil.StatusChecker;
import org.junit.jupiter.api.Test;
import org.slf4j.spi.MDCAdapter;

import static ch.qos.logback.classic.model.processor.CallerInstructionLogic.NO_CONTRADICTIONS_MSG;
import static ch.qos.logback.classic.model.processor.CallerInstructionLogic.WARNING_MSG_TEMPLATE;

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

    @Test
    public void asyncDefaultIncludeCallerDataWrapsCallerPatternTriggersWarn() throws JoranException {
        configure(INPUT_DIR + "asyncNoCallerDataWrapsCallerPattern.xml");

        String msg = String.format(
                WARNING_MSG_TEMPLATE, "ASYNC", "CONSOLE");
        checker.assertContainsMatch(msg);
        checker.assertContainsMatch("See https:\\/\\/logback.qos.ch/codes.html#callerContradiction for details");
    }

    @Test
    public void asyncIncludeCallerDataTrueWrapsCallerPatternNoWarn() throws JoranException {
        configure(INPUT_DIR + "asyncWithCallerDataWrapsCallerPattern.xml");
        checker.assertMatchCount(NO_CONTRADICTIONS_MSG,1
        );
    }

    @Test
    public void asyncDefaultIncludeCallerDataWrapsNonCallerPatternNoWarn() throws JoranException {
        configure(INPUT_DIR + "asyncNoCallerDataWrapsNoCallerPattern.xml");
        checker.assertMatchCount(NO_CONTRADICTIONS_MSG,1);
    }
}
