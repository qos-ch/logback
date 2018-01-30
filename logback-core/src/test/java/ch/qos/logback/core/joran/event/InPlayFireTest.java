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
package ch.qos.logback.core.joran.event;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;

import org.junit.Test;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.ContextBase;
import ch.qos.logback.core.joran.TrivialConfigurator;
import ch.qos.logback.core.joran.action.Action;
import ch.qos.logback.core.joran.spi.ElementSelector;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.testUtil.CoreTestConstants;

public class InPlayFireTest {

    Context context = new ContextBase();
    HashMap<ElementSelector, Action> rulesMap = new HashMap<ElementSelector, Action>();

    @Test
    public void testBasic() throws JoranException {
        ListenAction listenAction = new ListenAction();

        rulesMap.put(new ElementSelector("fire"), listenAction);
        TrivialConfigurator gc = new TrivialConfigurator(rulesMap);

        gc.setContext(context);
        gc.doConfigure(CoreTestConstants.TEST_SRC_PREFIX + "input/joran/fire1.xml");

        // for(SaxEvent se: listenAction.getSeList()) {
        // System.out.println(se);
        // }
        assertEquals(5, listenAction.getSeList().size());
        assertTrue(listenAction.getSeList().get(0) instanceof StartEvent);
        assertTrue(listenAction.getSeList().get(1) instanceof StartEvent);
        assertTrue(listenAction.getSeList().get(2) instanceof BodyEvent);
        assertTrue(listenAction.getSeList().get(3) instanceof EndEvent);
    }

    @Test
    public void testReplay() throws JoranException {
        ListenAction listenAction = new ListenAction();

        rulesMap.put(new ElementSelector("fire"), listenAction);
        TrivialConfigurator gc = new TrivialConfigurator(rulesMap);

        gc.setContext(context);
        gc.doConfigure(CoreTestConstants.TEST_SRC_PREFIX + "input/joran/fire1.xml");

        // for(SaxEvent se: listenAction.getSeList()) {
        // System.out.println(se);
        // }
        assertEquals(5, listenAction.getSeList().size());
        assertTrue(listenAction.getSeList().get(0) instanceof StartEvent);
        assertTrue(listenAction.getSeList().get(1) instanceof StartEvent);
        assertTrue(listenAction.getSeList().get(2) instanceof BodyEvent);
        assertTrue(listenAction.getSeList().get(3) instanceof EndEvent);
    }

}
