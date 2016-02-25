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
import ch.qos.logback.core.joran.spi.InterpretationContext;
import ch.qos.logback.core.status.ErrorStatus;
import ch.qos.logback.core.util.CoreTestConstants;
import ch.qos.logback.core.util.StatusPrinter;

/**
 * Test {@link PropertyAction}.
 * @author Ceki G&uuml;lc&uuml;
 */
public class PropertyActionTest {

    Context context;
    InterpretationContext ec;
    PropertyAction propertyAction;
    DummyAttributes atts = new DummyAttributes();

    @Before
    public void setUp() throws Exception {
        context = new ContextBase();
        ec = new InterpretationContext(context, null);
        propertyAction = new PropertyAction();
        propertyAction.setContext(context);
    }

    @After
    public void tearDown() throws Exception {
        context = null;
        propertyAction = null;
        atts = null;
    }

    @Test
    public void nameValuePair() {
        atts.setValue("name", "v1");
        atts.setValue("value", "work");
        propertyAction.begin(ec, null, atts);
        assertEquals("work", ec.getProperty("v1"));
    }

    @Test
    public void nameValuePairWithPrerequisiteSubsitution() {
        context.putProperty("w", "wor");
        atts.setValue("name", "v1");
        atts.setValue("value", "${w}k");
        propertyAction.begin(ec, null, atts);
        assertEquals("work", ec.getProperty("v1"));
    }

    @Test
    public void noValue() {
        atts.setValue("name", "v1");
        propertyAction.begin(ec, null, atts);
        assertEquals(1, context.getStatusManager().getCount());
        assertTrue(checkError());
    }

    @Test
    public void noName() {
        atts.setValue("value", "v1");
        propertyAction.begin(ec, null, atts);
        assertEquals(1, context.getStatusManager().getCount());
        assertTrue(checkError());
    }

    @Test
    public void noAttributes() {
        propertyAction.begin(ec, null, atts);
        assertEquals(1, context.getStatusManager().getCount());
        assertTrue(checkError());
        StatusPrinter.print(context);
    }

    @Test
    public void testFileNotLoaded() {
        atts.setValue("file", "toto");
        atts.setValue("value", "work");
        propertyAction.begin(ec, null, atts);
        assertEquals(1, context.getStatusManager().getCount());
        assertTrue(checkError());
    }

    @Test
    public void testLoadFileWithPrerequisiteSubsitution() {
        context.putProperty("STEM", CoreTestConstants.TEST_SRC_PREFIX + "input/joran");
        atts.setValue("file", "${STEM}/propertyActionTest.properties");
        propertyAction.begin(ec, null, atts);
        assertEquals("tata", ec.getProperty("v1"));
        assertEquals("toto", ec.getProperty("v2"));
    }

    @Test
    public void testLoadFile() {
        atts.setValue("file", CoreTestConstants.TEST_SRC_PREFIX + "input/joran/propertyActionTest.properties");
        propertyAction.begin(ec, null, atts);
        assertEquals("tata", ec.getProperty("v1"));
        assertEquals("toto", ec.getProperty("v2"));
    }

    @Test
    public void testLoadResource() {
        atts.setValue("resource", "asResource/joran/propertyActionTest.properties");
        propertyAction.begin(ec, null, atts);
        assertEquals("tata", ec.getProperty("r1"));
        assertEquals("toto", ec.getProperty("r2"));
    }

    @Test
    public void testLoadResourceWithPrerequisiteSubsitution() {
        context.putProperty("STEM", "asResource/joran");
        atts.setValue("resource", "${STEM}/propertyActionTest.properties");
        propertyAction.begin(ec, null, atts);
        assertEquals("tata", ec.getProperty("r1"));
        assertEquals("toto", ec.getProperty("r2"));
    }

    @Test
    public void testLoadNotPossible() {
        atts.setValue("file", "toto");
        propertyAction.begin(ec, null, atts);
        assertEquals(1, context.getStatusManager().getCount());
        assertTrue(checkFileErrors());
    }

    private boolean checkError() {
        Iterator it = context.getStatusManager().getCopyOfStatusList().iterator();
        ErrorStatus es = (ErrorStatus) it.next();
        return PropertyAction.INVALID_ATTRIBUTES.equals(es.getMessage());
    }

    private boolean checkFileErrors() {
        Iterator it = context.getStatusManager().getCopyOfStatusList().iterator();
        ErrorStatus es1 = (ErrorStatus) it.next();
        return "Could not find properties file [toto].".equals(es1.getMessage());
    }
}
