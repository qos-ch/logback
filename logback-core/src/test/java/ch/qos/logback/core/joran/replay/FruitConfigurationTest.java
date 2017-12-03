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
package ch.qos.logback.core.joran.replay;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.List;

import ch.qos.logback.core.joran.spi.ElementSelector;
import ch.qos.logback.core.testUtil.CoreTestConstants;

import org.junit.Test;

import ch.qos.logback.core.joran.SimpleConfigurator;
import ch.qos.logback.core.joran.action.Action;
import ch.qos.logback.core.joran.action.NOPAction;
import ch.qos.logback.core.util.StatusPrinter;

/** 
 * The Fruit* code is intended to test Joran's replay capability
 * */
public class FruitConfigurationTest {

    FruitContext fruitContext = new FruitContext();

    public List<FruitShell> doFirstPart(String filename) throws Exception {

        try {
            HashMap<ElementSelector, Action> rulesMap = new HashMap<ElementSelector, Action>();
            rulesMap.put(new ElementSelector("group/fruitShell"), new FruitShellAction());
            rulesMap.put(new ElementSelector("group/fruitShell/fruit"), new FruitFactoryAction());
            rulesMap.put(new ElementSelector("group/fruitShell/fruit/*"), new NOPAction());
            SimpleConfigurator simpleConfigurator = new SimpleConfigurator(rulesMap);

            simpleConfigurator.setContext(fruitContext);

            simpleConfigurator.doConfigure(CoreTestConstants.TEST_SRC_PREFIX + "input/joran/replay/" + filename);

            return fruitContext.getFruitShellList();
        } catch (Exception je) {
            StatusPrinter.print(fruitContext);
            throw je;
        }
    }

    @Test
    public void fruit1() throws Exception {
        List<FruitShell> fsList = doFirstPart("fruit1.xml");
        assertNotNull(fsList);
        assertEquals(1, fsList.size());

        FruitShell fs0 = fsList.get(0);
        assertNotNull(fs0);
        assertEquals("fs0", fs0.getName());
        Fruit fruit0 = fs0.fruitFactory.buildFruit();
        assertTrue(fruit0 instanceof Fruit);
        assertEquals("blue", fruit0.getName());
    }

    @Test
    public void fruit2() throws Exception {
        List<FruitShell> fsList = doFirstPart("fruit2.xml");
        assertNotNull(fsList);
        assertEquals(2, fsList.size());

        FruitShell fs0 = fsList.get(0);
        assertNotNull(fs0);
        assertEquals("fs0", fs0.getName());
        Fruit fruit0 = fs0.fruitFactory.buildFruit();
        assertTrue(fruit0 instanceof Fruit);
        assertEquals("blue", fruit0.getName());

        FruitShell fs1 = fsList.get(1);
        assertNotNull(fs1);
        assertEquals("fs1", fs1.getName());
        Fruit fruit1 = fs1.fruitFactory.buildFruit();
        assertTrue(fruit1 instanceof WeightytFruit);
        assertEquals("orange", fruit1.getName());
        assertEquals(1.2, ((WeightytFruit) fruit1).getWeight(), 0.01);
    }

    @Test
    public void withSubst() throws Exception {
        List<FruitShell> fsList = doFirstPart("fruitWithSubst.xml");
        assertNotNull(fsList);
        assertEquals(1, fsList.size());

        FruitShell fs0 = fsList.get(0);
        assertNotNull(fs0);
        assertEquals("fs0", fs0.getName());
        int oldCount = FruitFactory.count;
        Fruit fruit0 = fs0.fruitFactory.buildFruit();
        assertTrue(fruit0 instanceof WeightytFruit);
        assertEquals("orange-" + oldCount, fruit0.getName());
        assertEquals(1.2, ((WeightytFruit) fruit0).getWeight(), 0.01);
    }

}
