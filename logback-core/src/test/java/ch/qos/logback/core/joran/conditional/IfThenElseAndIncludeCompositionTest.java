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
package ch.qos.logback.core.joran.conditional;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Stack;

import ch.qos.logback.core.joran.spi.ElementSelector;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.ContextBase;
import ch.qos.logback.core.joran.TrivialConfigurator;
import ch.qos.logback.core.joran.action.Action;
import ch.qos.logback.core.joran.action.IncludeAction;
import ch.qos.logback.core.joran.action.NOPAction;
import ch.qos.logback.core.joran.action.ext.StackAction;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.testUtil.RandomUtil;
import ch.qos.logback.core.util.CoreTestConstants;
import ch.qos.logback.core.util.StatusPrinter;

public class IfThenElseAndIncludeCompositionTest {

    Context context = new ContextBase();
    TrivialConfigurator tc;
    int diff = RandomUtil.getPositiveInt();
    static final String CONDITIONAL_DIR_PREFIX = CoreTestConstants.JORAN_INPUT_PREFIX + "conditional/";

    final static String THEN_FILE_TO_INCLUDE_KEY = "thenFileToInclude";
    final static String ELSE_FILE_TO_INCLUDE_KEY = "elseFileToInclude";

    static final String NESTED_INCLUDE_FILE = CONDITIONAL_DIR_PREFIX + "nestedInclude.xml";
    static final String THEN_FILE_TO_INCLUDE = CONDITIONAL_DIR_PREFIX + "includedA.xml";
    static final String ELSE_FILE_TO_INCLUDE = CONDITIONAL_DIR_PREFIX + "includedB.xml";

    StackAction stackAction = new StackAction();

    @Before
    public void setUp() throws Exception {
        HashMap<ElementSelector, Action> rulesMap = new HashMap<ElementSelector, Action>();
        rulesMap.put(new ElementSelector("x"), new NOPAction());
        rulesMap.put(new ElementSelector("x/stack"), stackAction);
        rulesMap.put(new ElementSelector("*/if"), new IfAction());
        rulesMap.put(new ElementSelector("*/if/then"), new ThenAction());
        rulesMap.put(new ElementSelector("*/if/then/*"), new NOPAction());
        rulesMap.put(new ElementSelector("*/if/else"), new ElseAction());
        rulesMap.put(new ElementSelector("*/if/else/*"), new NOPAction());
        rulesMap.put(new ElementSelector("x/include"), new IncludeAction());

        tc = new TrivialConfigurator(rulesMap);
        tc.setContext(context);
    }

    @After
    public void tearDown() throws Exception {
        StatusPrinter.printInCaseOfErrorsOrWarnings(context);
        context = null;
        // StackAction.reset();
    }

    @Test
    public void includeNestedWithinIf() throws JoranException {
        context.putProperty(THEN_FILE_TO_INCLUDE_KEY, THEN_FILE_TO_INCLUDE);
        context.putProperty(ELSE_FILE_TO_INCLUDE_KEY, ELSE_FILE_TO_INCLUDE);
        tc.doConfigure(NESTED_INCLUDE_FILE);
        verifyConfig(new String[] { "BEGIN", "e0", "IncludedB0", "e1", "END" });
    }

    void verifyConfig(String[] expected) {
        Stack<String> witness = new Stack<String>();
        witness.addAll(Arrays.asList(expected));
        assertEquals(witness, stackAction.getStack());
    }

}
