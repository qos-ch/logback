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

import java.util.Arrays;
import java.util.HashMap;
import java.util.Stack;

import ch.qos.logback.core.joran.action.PropertyAction;
import ch.qos.logback.core.joran.spi.ElementSelector;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.ContextBase;
import ch.qos.logback.core.joran.TrivialConfigurator;
import ch.qos.logback.core.joran.action.Action;
import ch.qos.logback.core.joran.action.NOPAction;
import ch.qos.logback.core.joran.action.ext.StackAction;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.status.StatusChecker;
import ch.qos.logback.core.testUtil.RandomUtil;
import ch.qos.logback.core.util.CoreTestConstants;
import ch.qos.logback.core.util.StatusPrinter;

import static org.junit.Assert.*;

public class IfThenElseTest {

    Context context = new ContextBase();
    StatusChecker checker = new StatusChecker(context);
    TrivialConfigurator tc;
    int diff = RandomUtil.getPositiveInt();
    static final String CONDITIONAL_DIR_PREFIX = CoreTestConstants.JORAN_INPUT_PREFIX + "conditional/";

    String ki1 = "ki1";
    String val1 = "val1";
    String sysKey = "sysKey";
    String dynaKey = "dynaKey";

    StackAction stackAction = new StackAction();

    @Before
    public void setUp() throws Exception {
        HashMap<ElementSelector, Action> rulesMap = new HashMap<ElementSelector, Action>();
        rulesMap.put(new ElementSelector("x"), new NOPAction());
        rulesMap.put(new ElementSelector("x/stack"), stackAction);
        rulesMap.put(new ElementSelector("x/property"), new PropertyAction());
        rulesMap.put(new ElementSelector("*/if"), new IfAction());
        rulesMap.put(new ElementSelector("*/if/then"), new ThenAction());
        rulesMap.put(new ElementSelector("*/if/then/*"), new NOPAction());
        rulesMap.put(new ElementSelector("*/if/else"), new ElseAction());
        rulesMap.put(new ElementSelector("*/if/else/*"), new NOPAction());

        tc = new TrivialConfigurator(rulesMap);
        tc.setContext(context);
    }

    @After
    public void tearDown() throws Exception {
        StatusPrinter.printIfErrorsOccured(context);
        System.clearProperty(sysKey);
    }

    @Test
    public void whenContextPropertyIsSet_IfThenBranchIsEvaluated() throws JoranException {
        context.putProperty(ki1, val1);
        tc.doConfigure(CONDITIONAL_DIR_PREFIX + "if0.xml");
        verifyConfig(new String[] { "BEGIN", "a", "END" });
    }

    @Test
    public void whenLocalPropertyIsSet_IfThenBranchIsEvaluated() throws JoranException {
        tc.doConfigure(CONDITIONAL_DIR_PREFIX + "if_localProperty.xml");
        verifyConfig(new String[] { "BEGIN", "a", "END" });
    }

    @Test
    public void whenNoPropertyIsDefined_ElseBranchIsEvaluated() throws JoranException {
        tc.doConfigure(CONDITIONAL_DIR_PREFIX + "if0.xml");
        verifyConfig(new String[] { "BEGIN", "b", "END" });
    }

    @Test
    public void whenContextPropertyIsSet_IfThenBranchIsEvaluated_NO_ELSE_DEFINED() throws JoranException {
        context.putProperty(ki1, val1);
        tc.doConfigure(CONDITIONAL_DIR_PREFIX + "ifWithoutElse.xml");
        verifyConfig(new String[] { "BEGIN", "a", "END" });
    }

    @Test
    public void whenNoPropertyIsDefined_IfThenBranchIsNotEvaluated_NO_ELSE_DEFINED() throws JoranException {
        tc.doConfigure(CONDITIONAL_DIR_PREFIX + "ifWithoutElse.xml");
        verifyConfig(new String[] { "BEGIN", "END" });
        assertTrue(checker.isErrorFree(0));
    }

    @Test
    public void nestedIf() throws JoranException {
        tc.doConfigure(CONDITIONAL_DIR_PREFIX + "nestedIf.xml");
        verifyConfig(new String[] { "BEGIN", "a", "c", "END" });
        assertTrue(checker.isErrorFree(0));
    }

    @Test
    public void useNonExistenceOfSystemPropertyToDefineAContextProperty() throws JoranException {
        assertNull(System.getProperty(sysKey));
        assertNull(context.getProperty(dynaKey));
        tc.doConfigure(CONDITIONAL_DIR_PREFIX + "ifSystem.xml");
        System.out.println(dynaKey + "=" + context.getProperty(dynaKey));
        assertNotNull(context.getProperty(dynaKey));
    }

    @Test
    public void noContextPropertyShouldBeDefinedIfSystemPropertyExists() throws JoranException {
        System.setProperty(sysKey, "a");
        assertNull(context.getProperty(dynaKey));
        System.out.println("before " + dynaKey + "=" + context.getProperty(dynaKey));
        tc.doConfigure(CONDITIONAL_DIR_PREFIX + "ifSystem.xml");
        System.out.println(dynaKey + "=" + context.getProperty(dynaKey));
        assertNull(context.getProperty(dynaKey));
    }

    private void verifyConfig(String[] expected) {
        Stack<String> witness = new Stack<String>();
        witness.addAll(Arrays.asList(expected));
        assertEquals(witness, stackAction.getStack());
    }

}
