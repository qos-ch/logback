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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Iterator;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.ContextBase;
import ch.qos.logback.core.joran.spi.ActionException;
import ch.qos.logback.core.joran.spi.InterpretationContext;
import ch.qos.logback.core.joran.spi.SaxEventInterpreter;
import ch.qos.logback.core.model.ImplicitModel;
import ch.qos.logback.core.model.PropertyModel;
import ch.qos.logback.core.model.TopModel;
import ch.qos.logback.core.model.processor.DefaultProcessor;
import ch.qos.logback.core.model.processor.ImplicitModelHandler;
import ch.qos.logback.core.model.processor.NOPModelHandler;
import ch.qos.logback.core.model.processor.PropertyModelHandler;
import ch.qos.logback.core.status.ErrorStatus;
import ch.qos.logback.core.status.Status;
import ch.qos.logback.core.testUtil.CoreTestConstants;
import ch.qos.logback.core.util.StatusPrinter;

/**
 * Test {@link PropertyAction}.
 * @author Ceki G&uuml;lc&uuml;
 */
public class PropertyActionTest {

    Context context;
    InterpretationContext interpretationContext;
    SaxEventInterpreter x;
    
    PropertyAction propertyAction;
    DummyAttributes atts = new DummyAttributes();
    DefaultProcessor defaultProcessor;
    TopModel topModel = new TopModel();
    String tagName = "property";
    
    @Before
    public void setUp() throws Exception {
        context = new ContextBase();
        interpretationContext = new InterpretationContext(context, null);
        topModel.setTag("top");
        interpretationContext.pushModel(topModel);
        propertyAction = new PropertyAction();
        propertyAction.setContext(context);
        defaultProcessor = new DefaultProcessor(context, interpretationContext);
        defaultProcessor.addHandler(TopModel.class, NOPModelHandler::makeInstance);
        defaultProcessor.addHandler(PropertyModel.class, PropertyModelHandler::makeInstance);
        defaultProcessor.addHandler(ImplicitModel.class, ImplicitModelHandler::makeInstance);
    }

    @After
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
        assertEquals("work", interpretationContext.getProperty("v1"));
    }

    @Test
    public void nameValuePairWithPrerequisiteSubsitution() throws ActionException {
        context.putProperty("w", "wor");
        atts.setValue("name", "v1");
        atts.setValue("value", "${w}k");
        propertyAction.begin(interpretationContext, tagName, atts);
        propertyAction.end(interpretationContext, tagName);
        defaultProcessor.process(topModel);
        assertEquals("work", interpretationContext.getProperty("v1"));
    }

    @Test
    public void noValue() throws ActionException {
        atts.setValue("name", "v1");
        propertyAction.begin(interpretationContext, tagName, atts);
        propertyAction.end(interpretationContext, tagName);
        defaultProcessor.process(topModel);
        assertEquals(2, context.getStatusManager().getCount());
        assertTrue(checkError());
    }

    @Test
    public void noName() throws ActionException {
        atts.setValue("value", "v1");
        propertyAction.begin(interpretationContext, tagName, atts);
        propertyAction.end(interpretationContext, tagName);
        defaultProcessor.process(topModel);
        assertEquals(2, context.getStatusManager().getCount());
        assertTrue(checkError());
    }

    @Test
    public void noAttributes() throws ActionException {
        propertyAction.begin(interpretationContext, "noAttributes", atts);
        propertyAction.end(interpretationContext, "noAttributes");
        defaultProcessor.process(topModel);
        assertEquals(2, context.getStatusManager().getCount());
        assertTrue(checkError());
        StatusPrinter.print(context);
    }

    @Test
    public void testFileNotLoaded() throws ActionException {
        atts.setValue("file", "toto");
        atts.setValue("value", "work");
        propertyAction.begin(interpretationContext, tagName, atts);
        propertyAction.end(interpretationContext, tagName);
        defaultProcessor.process(topModel);
        assertEquals(2, context.getStatusManager().getCount());
        assertTrue(checkError());
    }

    @Test
    public void testLoadFileWithPrerequisiteSubsitution() throws ActionException {
        context.putProperty("STEM", CoreTestConstants.TEST_SRC_PREFIX + "input/joran");
        atts.setValue("file", "${STEM}/propertyActionTest.properties");
        propertyAction.begin(interpretationContext, tagName, atts);
        propertyAction.end(interpretationContext, tagName);
        defaultProcessor.process(topModel);
        assertEquals("tata", interpretationContext.getProperty("v1"));
        assertEquals("toto", interpretationContext.getProperty("v2"));
    }

    @Test
    public void testLoadFile() throws ActionException {
        atts.setValue("file", CoreTestConstants.TEST_SRC_PREFIX + "input/joran/propertyActionTest.properties");
        propertyAction.begin(interpretationContext, tagName, atts);
        propertyAction.end(interpretationContext, tagName);
        defaultProcessor.process(topModel);
        assertEquals("tata", interpretationContext.getProperty("v1"));
        assertEquals("toto", interpretationContext.getProperty("v2"));
    }

    @Test
    public void testLoadResource() throws ActionException {
        atts.setValue("resource", "asResource/joran/propertyActionTest.properties");
        propertyAction.begin(interpretationContext, tagName, atts);
        propertyAction.end(interpretationContext, tagName);
        defaultProcessor.process(topModel);
        assertEquals("tata", interpretationContext.getProperty("r1"));
        assertEquals("toto", interpretationContext.getProperty("r2"));
    }

    @Test
    public void testLoadResourceWithPrerequisiteSubsitution() throws ActionException {
        context.putProperty("STEM", "asResource/joran");
        atts.setValue("resource", "${STEM}/propertyActionTest.properties");
        propertyAction.begin(interpretationContext, tagName, atts);
        propertyAction.end(interpretationContext, tagName);
        defaultProcessor.process(topModel);
        assertEquals("tata", interpretationContext.getProperty("r1"));
        assertEquals("toto", interpretationContext.getProperty("r2"));
    }

    @Test
    public void testLoadNotPossible() throws ActionException {
        atts.setValue("file", "toto");
        propertyAction.begin(interpretationContext, tagName, atts);
        propertyAction.end(interpretationContext, tagName);
        defaultProcessor.process(topModel);
        assertEquals(2, context.getStatusManager().getCount());
        assertTrue(checkFileErrors());
    }

    private boolean checkError() {
        Iterator<Status> it = context.getStatusManager().getCopyOfStatusList().iterator();
        ErrorStatus es = (ErrorStatus) it.next();
        return PropertyModelHandler.INVALID_ATTRIBUTES.equals(es.getMessage());
    }

    private boolean checkFileErrors() {
        Iterator<Status> it = context.getStatusManager().getCopyOfStatusList().iterator();
        ErrorStatus es1 = (ErrorStatus) it.next();
        return "Could not find properties file [toto].".equals(es1.getMessage());
    }
}
