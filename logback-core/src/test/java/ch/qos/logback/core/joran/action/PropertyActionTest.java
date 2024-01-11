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

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.ContextBase;
import ch.qos.logback.core.joran.spi.ActionException;
import ch.qos.logback.core.joran.spi.SaxEventInterpretationContext;
import ch.qos.logback.core.joran.spi.SaxEventInterpreter;
import ch.qos.logback.core.model.ImplicitModel;
import ch.qos.logback.core.model.PropertyModel;
import ch.qos.logback.core.model.TopModel;
import ch.qos.logback.core.model.processor.DefaultProcessor;
import ch.qos.logback.core.model.processor.ImplicitModelHandler;
import ch.qos.logback.core.model.processor.ModelInterpretationContext;
import ch.qos.logback.core.model.processor.NOPModelHandler;
import ch.qos.logback.core.model.processor.PropertyModelHandler;
import ch.qos.logback.core.testUtil.CoreTestConstants;
import ch.qos.logback.core.util.StatusPrinter;

/**
 * Test {@link PropertyAction}.
 * 
 * @author Ceki G&uuml;lc&uuml;
 */
public class PropertyActionTest {

    Context context;
    SaxEventInterpretationContext interpretationContext;
    ModelInterpretationContext mic;
    SaxEventInterpreter x;

    PropertyAction propertyAction;
    DummyAttributes atts = new DummyAttributes();
    DefaultProcessor defaultProcessor;
    TopModel topModel = new TopModel();
    String tagName = "property";

    @BeforeEach
    public void setUp() throws Exception {
        context = new ContextBase();
        interpretationContext = new SaxEventInterpretationContext(context, null);
        mic = new ModelInterpretationContext(context);
        topModel.setTag("top");
        interpretationContext.pushModel(topModel);
        mic.pushModel(topModel);
        propertyAction = new PropertyAction();
        propertyAction.setContext(context);
        defaultProcessor = new DefaultProcessor(context, mic);
        defaultProcessor.addHandler(TopModel.class, NOPModelHandler::makeInstance);
        defaultProcessor.addHandler(PropertyModel.class, PropertyModelHandler::makeInstance);
        defaultProcessor.addHandler(ImplicitModel.class, ImplicitModelHandler::makeInstance);
    }

    @AfterEach
    public void tearDown() throws Exception {
        StatusPrinter.print(context);
        context = null;
        propertyAction = null;
        atts = null;
    }

    @Test
    public void nameValuePair() throws ActionException {
        atts.setValue("name", "v1");
        atts.setValue("value", "work");
        propertyAction.begin(interpretationContext, tagName, atts);
        propertyAction.end(interpretationContext, tagName);
        defaultProcessor.process(topModel);
        Assertions.assertEquals("work", mic.getProperty("v1"));
    }

    @Test
    public void nameValuePairWithPrerequisiteSubsitution() throws ActionException {
        context.putProperty("w", "wor");
        atts.setValue("name", "v1");
        atts.setValue("value", "${w}k");
        propertyAction.begin(interpretationContext, tagName, atts);
        propertyAction.end(interpretationContext, tagName);
        defaultProcessor.process(topModel);
        Assertions.assertEquals("work", mic.getProperty("v1"));
    }

    @Test
    public void noValue() throws ActionException {
        atts.setValue("name", "v1");
        propertyAction.begin(interpretationContext, tagName, atts);
        propertyAction.end(interpretationContext, tagName);
        defaultProcessor.process(topModel);
        Assertions.assertEquals(2, context.getStatusManager().getCount());
        Assertions.assertTrue(checkError());
    }

    @Test
    public void noName() throws ActionException {
        atts.setValue("value", "v1");
        propertyAction.begin(interpretationContext, tagName, atts);
        propertyAction.end(interpretationContext, tagName);
        defaultProcessor.process(topModel);
        Assertions.assertEquals(2, context.getStatusManager().getCount());
        Assertions.assertTrue(checkError());
    }

    @Test
    public void noAttributes() throws ActionException {
        propertyAction.begin(interpretationContext, "noAttributes", atts);
        propertyAction.end(interpretationContext, "noAttributes");
        defaultProcessor.process(topModel);
        Assertions.assertEquals(2, context.getStatusManager().getCount());
        Assertions.assertTrue(checkError());
        StatusPrinter.print(context);
    }

    @Test
    public void testFileNotLoaded() throws ActionException {
        atts.setValue("file", "toto");
        atts.setValue("value", "work");
        propertyAction.begin(interpretationContext, tagName, atts);
        propertyAction.end(interpretationContext, tagName);
        defaultProcessor.process(topModel);
        Assertions.assertEquals(2, context.getStatusManager().getCount());
        Assertions.assertTrue(checkError());
    }

    @Test
    public void testLoadFileWithPrerequisiteSubsitution() throws ActionException {
        context.putProperty("STEM", CoreTestConstants.TEST_SRC_PREFIX + "input/joran");
        atts.setValue("file", "${STEM}/propertyActionTest.properties");
        propertyAction.begin(interpretationContext, tagName, atts);
        propertyAction.end(interpretationContext, tagName);
        defaultProcessor.process(topModel);
        Assertions.assertEquals("tata", mic.getProperty("v1"));
        Assertions.assertEquals("toto", mic.getProperty("v2"));
    }

    @Test
    public void testLoadFile() throws ActionException {
        atts.setValue("file", CoreTestConstants.TEST_SRC_PREFIX + "input/joran/propertyActionTest.properties");
        propertyAction.begin(interpretationContext, tagName, atts);
        propertyAction.end(interpretationContext, tagName);
        defaultProcessor.process(topModel);
        Assertions.assertEquals("tata", mic.getProperty("v1"));
        Assertions.assertEquals("toto", mic.getProperty("v2"));
    }

    @Test
    public void testLoadResource() throws ActionException {
        atts.setValue("resource", "asResource/joran/propertyActionTest.properties");
        propertyAction.begin(interpretationContext, tagName, atts);
        propertyAction.end(interpretationContext, tagName);
        defaultProcessor.process(topModel);
        Assertions.assertEquals("tata", mic.getProperty("r1"));
        Assertions.assertEquals("toto", mic.getProperty("r2"));
    }

    @Test
    public void testLoadResourceWithPrerequisiteSubsitution() throws ActionException {
        context.putProperty("STEM", "asResource/joran");
        atts.setValue("resource", "${STEM}/propertyActionTest.properties");
        propertyAction.begin(interpretationContext, tagName, atts);
        propertyAction.end(interpretationContext, tagName);
        defaultProcessor.process(topModel);
        Assertions.assertEquals("tata", mic.getProperty("r1"));
        Assertions.assertEquals("toto", mic.getProperty("r2"));
    }

    @Test
    public void testLoadNotPossible() throws ActionException {
        atts.setValue("file", "toto");
        propertyAction.begin(interpretationContext, tagName, atts);
        propertyAction.end(interpretationContext, tagName);
        defaultProcessor.process(topModel);
        Assertions.assertEquals(2, context.getStatusManager().getCount());
        Assertions.assertEquals("Could not find properties file [toto].", getFirstStatusMessage());
    }

    @Test
    public void testLoadMissingOptionalFile() throws ActionException {
        atts.setValue("file", "toto");
        atts.setValue("optional", "true");
        propertyAction.begin(interpretationContext, tagName, atts);
        propertyAction.end(interpretationContext, tagName);
        defaultProcessor.process(topModel);
        Assertions.assertEquals(1, context.getStatusManager().getCount());
        Assertions.assertEquals("End of configuration.", getFirstStatusMessage());
    }

    @Test
    public void testLoadResourceNotPossible() throws ActionException {
        atts.setValue("resource", "toto");
        propertyAction.begin(interpretationContext, tagName, atts);
        propertyAction.end(interpretationContext, tagName);
        defaultProcessor.process(topModel);
        Assertions.assertEquals(2, context.getStatusManager().getCount());
        Assertions.assertEquals("Could not find resource [toto].", getFirstStatusMessage());
    }

    @Test
    public void testLoadMissingOptionalResource() throws ActionException {
        atts.setValue("resource", "toto");
        atts.setValue("optional", "true");
        propertyAction.begin(interpretationContext, tagName, atts);
        propertyAction.end(interpretationContext, tagName);
        defaultProcessor.process(topModel);
        Assertions.assertEquals(1, context.getStatusManager().getCount());
        Assertions.assertEquals("End of configuration.", getFirstStatusMessage());
    }

    private boolean checkError() {
        return PropertyModelHandler.INVALID_ATTRIBUTES.equals(getFirstStatusMessage());
    }

    private String getFirstStatusMessage() {
        return context.getStatusManager().getCopyOfStatusList().get(0).getMessage();
    }
}
