/*
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2022, QOS.ch. All rights reserved.
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
package ch.qos.logback.core.blackbox.joran.conditional;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.ContextBase;
import ch.qos.logback.core.blackbox.BlackboxCoreTestConstants;
import ch.qos.logback.core.blackbox.joran.BlackboxSimpleConfigurator;
import ch.qos.logback.core.blackbox.joran.action.BlackboxTopElementAction;
import ch.qos.logback.core.blackbox.joran.action.ext.BlackboxStackAction;
import ch.qos.logback.core.blackbox.model.BlackboxStackModel;
import ch.qos.logback.core.blackbox.model.BlackboxTopModel;
import ch.qos.logback.core.blackbox.model.processor.BlackboxStackModelHandler;
import ch.qos.logback.core.joran.action.Action;
import ch.qos.logback.core.joran.action.PropertyAction;
import ch.qos.logback.core.joran.conditional.ElseAction;
import ch.qos.logback.core.joran.conditional.IfAction;
import ch.qos.logback.core.joran.conditional.ThenAction;
import ch.qos.logback.core.joran.spi.ElementSelector;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.joran.spi.RuleStore;
import ch.qos.logback.core.model.ImplicitModel;
import ch.qos.logback.core.model.PropertyModel;
import ch.qos.logback.core.model.conditional.ElseModel;
import ch.qos.logback.core.model.conditional.IfModel;
import ch.qos.logback.core.model.conditional.ThenModel;
import ch.qos.logback.core.model.processor.DefaultProcessor;
import ch.qos.logback.core.model.processor.ImplicitModelHandler;
import ch.qos.logback.core.model.processor.NOPModelHandler;
import ch.qos.logback.core.model.processor.PropertyModelHandler;
import ch.qos.logback.core.model.processor.conditional.ElseModelHandler;
import ch.qos.logback.core.model.processor.conditional.IfModelHandler;
import ch.qos.logback.core.model.processor.conditional.ThenModelHandler;
import ch.qos.logback.core.status.StatusUtil;
import ch.qos.logback.core.testUtil.CoreTestConstants;
import ch.qos.logback.core.testUtil.RandomUtil;
import ch.qos.logback.core.util.StatusPrinter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Stack;
import java.util.function.Supplier;

public class IfThenElseTest {

    Context context = new ContextBase();
    StatusUtil checker = new StatusUtil(context);
    BlackboxSimpleConfigurator simpleConfigurator;
    int diff = RandomUtil.getPositiveInt();
    static final String CONDITIONAL_DIR_PREFIX = BlackboxCoreTestConstants.JORAN_INPUT_PREFIX + "conditional/";

    String ki1 = "ki1";
    String val1 = "val1";
    String sysKey = "sysKey";
    String dynaKey = "dynaKey";

    @BeforeEach
    public void setUp() throws Exception {
        HashMap<ElementSelector, Supplier<Action>> rulesMap = new HashMap<>();
        rulesMap.put(new ElementSelector("x"), BlackboxTopElementAction::new);
        rulesMap.put(new ElementSelector("x/stack"), BlackboxStackAction::new);
        rulesMap.put(new ElementSelector("x/property"), PropertyAction::new);
        rulesMap.put(new ElementSelector("*/if"), IfAction::new);
        rulesMap.put(new ElementSelector("*/if/then"), ThenAction::new);
        rulesMap.put(new ElementSelector("*/if/else"), ElseAction::new);

        simpleConfigurator = new BlackboxSimpleConfigurator(rulesMap) {
            
            @Override
            protected void addElementSelectorAndActionAssociations(RuleStore rs) {
                super.addElementSelectorAndActionAssociations(rs);
                
                rs.addTransparentPathPart("if");
                rs.addTransparentPathPart("then");
                rs.addTransparentPathPart("else");

            }
            
            @Override
            protected void addModelHandlerAssociations(DefaultProcessor defaultProcessor) {
                defaultProcessor.addHandler(BlackboxTopModel.class, NOPModelHandler::makeInstance);
                
                defaultProcessor.addHandler(BlackboxStackModel.class, BlackboxStackModelHandler::makeInstance);
                defaultProcessor.addHandler(PropertyModel.class, PropertyModelHandler::makeInstance);
                defaultProcessor.addHandler(ImplicitModel.class, ImplicitModelHandler::makeInstance);
                defaultProcessor.addHandler(IfModel.class, IfModelHandler::makeInstance);
                defaultProcessor.addHandler(ThenModel.class, ThenModelHandler::makeInstance);
                defaultProcessor.addHandler(ElseModel.class, ElseModelHandler::makeInstance);
            }
        };
        
        simpleConfigurator.setContext(context);
    }

    @AfterEach
    public void tearDown() throws Exception {
        StatusPrinter.printIfErrorsOccured(context);
        System.clearProperty(sysKey);
    }

    @Test
    public void whenContextPropertyIsSet_IfThenBranchIsEvaluated() throws JoranException {
        context.putProperty(ki1, val1);
        simpleConfigurator.doConfigure(CONDITIONAL_DIR_PREFIX + "if0.xml");
        verifyConfig(new String[] { "BEGIN", "a", "END" });
    }

    @Test
    public void whenLocalPropertyIsSet_IfThenBranchIsEvaluated() throws JoranException {
        simpleConfigurator.doConfigure(CONDITIONAL_DIR_PREFIX + "if_localProperty.xml");
        verifyConfig(new String[] { "BEGIN", "a", "END" });
    }

    @Test
    public void whenNoPropertyIsDefined_ElseBranchIsEvaluated() throws JoranException {
        simpleConfigurator.doConfigure(CONDITIONAL_DIR_PREFIX + "if0.xml");
        verifyConfig(new String[] { "BEGIN", "b", "END" });
    }

    @Test
    public void whenContextPropertyIsSet_IfThenBranchIsEvaluated_NO_ELSE_DEFINED() throws JoranException {
        context.putProperty(ki1, val1);
        simpleConfigurator.doConfigure(CONDITIONAL_DIR_PREFIX + "ifWithoutElse.xml");
        verifyConfig(new String[] { "BEGIN", "a", "END" });
    }

    @Test
    public void whenNoPropertyIsDefined_IfThenBranchIsNotEvaluated_NO_ELSE_DEFINED() throws JoranException {
        simpleConfigurator.doConfigure(CONDITIONAL_DIR_PREFIX + "ifWithoutElse.xml");
        verifyConfig(new String[] { "BEGIN", "END" });
        Assertions.assertTrue(checker.isErrorFree(0));
    }

    @Test
    public void nestedIf() throws JoranException {
        simpleConfigurator.doConfigure(CONDITIONAL_DIR_PREFIX + "nestedIf.xml");
        //StatusPrinter.print(context);
        verifyConfig(new String[] { "BEGIN", "a", "c", "END" });
        Assertions.assertTrue(checker.isErrorFree(0));
    }

    @Test
    public void useNonExistenceOfSystemPropertyToDefineAContextProperty() throws JoranException {
        Assertions.assertNull(System.getProperty(sysKey));
        Assertions.assertNull(context.getProperty(dynaKey));
        simpleConfigurator.doConfigure(CONDITIONAL_DIR_PREFIX + "ifSystem.xml");
        System.out.println(dynaKey + "=" + context.getProperty(dynaKey));
        Assertions.assertNotNull(context.getProperty(dynaKey));
    }

    @Test
    public void noContextPropertyShouldBeDefinedIfSystemPropertyExists() throws JoranException {
        System.setProperty(sysKey, "a");
        Assertions.assertNull(context.getProperty(dynaKey));
        System.out.println("before " + dynaKey + "=" + context.getProperty(dynaKey));
        simpleConfigurator.doConfigure(CONDITIONAL_DIR_PREFIX + "ifSystem.xml");
        System.out.println(dynaKey + "=" + context.getProperty(dynaKey));
        Assertions.assertNull(context.getProperty(dynaKey));
    }

    private void verifyConfig(String[] expected) {
        Stack<String> witness = new Stack<>();
        witness.addAll(Arrays.asList(expected));
        
        @SuppressWarnings({ "unchecked", "rawtypes" })
        Stack<String> aStack = (Stack) context.getObject(BlackboxStackModelHandler.STACK_TEST);
        Assertions.assertEquals(witness, aStack);
    }

}
