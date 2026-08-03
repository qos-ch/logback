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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.function.Supplier;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.ContextBase;
import ch.qos.logback.core.joran.SimpleConfigurator;
import ch.qos.logback.core.joran.action.Action;
import ch.qos.logback.core.joran.action.DefinePropertyAction;
import ch.qos.logback.core.joran.action.TopElementAction;
import ch.qos.logback.core.joran.spi.ElementSelector;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.model.DefineModel;
import ch.qos.logback.core.model.ImplicitModel;
import ch.qos.logback.core.model.Model;
import ch.qos.logback.core.model.TopModel;
import ch.qos.logback.core.testUtil.CoreTestConstants;

/**
 * Verify that {@link ModelHandlerBase#postHandle} is invoked for dependency
 * analysers, and that it runs after recursion into child models.
 */
public class AnalyserPostHandleTest {

    private static final String ANALYSER_INPUT_DIR = CoreTestConstants.JORAN_INPUT_PREFIX + "analyser/";

    Context context = new ContextBase();
    List<String> callLog = new ArrayList<>();
    SimpleConfigurator simpleConfigurator;

    @BeforeEach
    public void setUp() {
        callLog.clear();

        HashMap<ElementSelector, Supplier<Action>> rulesMap = new HashMap<>();
        rulesMap.put(new ElementSelector("top"), TopElementAction::new);
        rulesMap.put(new ElementSelector("top/define"), DefinePropertyAction::new);

        simpleConfigurator = new SimpleConfigurator(rulesMap) {
            @Override
            protected void addModelHandlerAssociations(DefaultProcessor defaultProcessor) {
                defaultProcessor.addHandler(TopModel.class, NOPModelHandler::makeInstance);
                defaultProcessor.addHandler(DefineModel.class, NOPModelHandler::makeInstance);
                defaultProcessor.addHandler(ImplicitModel.class, ImplicitModelHandler::makeInstance);

                defaultProcessor.addAnalyser(TopModel.class,
                        () -> new TrackingAnalyser(context, callLog, "top"));
                defaultProcessor.addAnalyser(DefineModel.class,
                        () -> new TrackingAnalyser(context, callLog, "define"));
            }
        };
        simpleConfigurator.setContext(context);
    }

    @Test
    public void postHandleIsCalledForAnalysers() throws JoranException {
        simpleConfigurator.doConfigure(ANALYSER_INPUT_DIR + "nested.xml");

        assertTrue(callLog.contains("top.postHandle"),
                "postHandle should be called for the top analyser, callLog=" + callLog);
        assertTrue(callLog.contains("define.postHandle"),
                "postHandle should be called for the define analyser, callLog=" + callLog);
    }

    @Test
    public void postHandleIsInvokedAfterChildAnalysers() throws JoranException {
        simpleConfigurator.doConfigure(ANALYSER_INPUT_DIR + "nested.xml");

        // handle parent, then children fully (handle + postHandle), then postHandle parent
        List<String> expected = Arrays.asList(
                "top.handle",
                "define.handle",
                "define.postHandle",
                "top.postHandle");
        assertEquals(expected, callLog);
    }

    /**
     * Minimal dependency analyser used only by this test. Records handle and
     * postHandle invocations into a shared call log.
     */
    static class TrackingAnalyser extends ModelHandlerBase {

        final List<String> callLog;
        final String label;

        TrackingAnalyser(Context context, List<String> callLog, String label) {
            super(context);
            this.callLog = callLog;
            this.label = label;
        }

        @Override
        public void handle(ModelInterpretationContext mic, Model model) {
            callLog.add(label + ".handle");
        }

        @Override
        public void postHandle(ModelInterpretationContext mic, Model model) {
            callLog.add(label + ".postHandle");
        }
    }
}
