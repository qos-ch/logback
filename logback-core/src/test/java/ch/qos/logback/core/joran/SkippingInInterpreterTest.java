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
package ch.qos.logback.core.joran;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.List;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import ch.qos.logback.core.joran.spi.ElementSelector;
import org.junit.Test;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.ContextBase;
import ch.qos.logback.core.joran.action.Action;
import ch.qos.logback.core.joran.action.NOPAction;
import ch.qos.logback.core.joran.action.ext.BadBeginAction;
import ch.qos.logback.core.joran.action.ext.BadEndAction;
import ch.qos.logback.core.joran.action.ext.HelloAction;
import ch.qos.logback.core.joran.action.ext.TouchAction;
import ch.qos.logback.core.joran.spi.ActionException;
import ch.qos.logback.core.status.Status;
import ch.qos.logback.core.status.StatusManager;
import ch.qos.logback.core.util.CoreTestConstants;

/**
 * Test the way Interpreter skips child elements in case of exceptions thrown by
 * Actions. It also tests addition of status messages in case of exceptions.
 * 
 * @author Ceki Gulcu
 */
public class SkippingInInterpreterTest {

    HashMap<ElementSelector, Action> rulesMap = new HashMap<ElementSelector, Action>();
    Context context = new ContextBase();
    StatusManager sm = context.getStatusManager();

    SAXParser createParser() throws Exception {
        SAXParserFactory spf = SAXParserFactory.newInstance();
        return spf.newSAXParser();
    }

    void doTest(String filename, Integer expectedInt, Class<?> exceptionClass) throws Exception {

        rulesMap.put(new ElementSelector("test"), new NOPAction());
        rulesMap.put(new ElementSelector("test/badBegin"), new BadBeginAction());
        rulesMap.put(new ElementSelector("test/badBegin/touch"), new TouchAction());
        rulesMap.put(new ElementSelector("test/badEnd"), new BadEndAction());
        rulesMap.put(new ElementSelector("test/badEnd/touch"), new TouchAction());
        rulesMap.put(new ElementSelector("test/hello"), new HelloAction());

        rulesMap.put(new ElementSelector("test/isolate"), new NOPAction());
        rulesMap.put(new ElementSelector("test/isolate/badEnd"), new BadEndAction());
        rulesMap.put(new ElementSelector("test/isolate/badEnd/touch"), new TouchAction());
        rulesMap.put(new ElementSelector("test/isolate/touch"), new TouchAction());
        rulesMap.put(new ElementSelector("test/hello"), new HelloAction());

        TrivialConfigurator tc = new TrivialConfigurator(rulesMap);
        tc.setContext(context);
        tc.doConfigure(CoreTestConstants.TEST_SRC_PREFIX + "input/joran/skip/" + filename);

        String str = context.getProperty(HelloAction.PROPERTY_KEY);
        assertEquals("Hello John Doe.", str);

        Integer i = (Integer) context.getObject(TouchAction.KEY);
        if (expectedInt == null) {
            assertNull(i);
        } else {
            assertEquals(expectedInt, i);
        }

        // check the existence of an ERROR status
        List<Status> statusList = sm.getCopyOfStatusList();
        Status s0 = statusList.get(0);
        assertEquals(Status.ERROR, s0.getLevel());
        assertTrue(s0.getThrowable().getClass() == exceptionClass);
    }

    @Test
    public void testSkippingRuntimeExInBadBegin() throws Exception {
        doTest("badBegin1.xml", null, IllegalStateException.class);
    }

    @Test
    public void testSkippingActionExInBadBegin() throws Exception {
        doTest("badBegin2.xml", null, ActionException.class);
    }

    @Test
    public void testSkippingRuntimeExInBadEnd() throws Exception {
        doTest("badEnd1.xml", new Integer(2), IllegalStateException.class);
    }

    @Test
    public void testSkippingActionExInBadEnd() throws Exception {
        doTest("badEnd2.xml", new Integer(2), ActionException.class);
    }
}
