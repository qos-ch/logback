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
package ch.qos.logback.core.joran.implicitAction;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.HashMap;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import ch.qos.logback.core.joran.SimpleConfigurator;
import ch.qos.logback.core.joran.action.Action;
import ch.qos.logback.core.joran.spi.ElementSelector;
import ch.qos.logback.core.util.CoreTestConstants;
import ch.qos.logback.core.util.StatusPrinter;

public class ImplicitActionTest {

    static final String IMPLCIT_DIR = CoreTestConstants.TEST_SRC_PREFIX + "input/joran/implicitAction/";

    FruitContext fruitContext = new FruitContext();
    SimpleConfigurator simpleConfigurator;

    @Before
    public void setUp() throws Exception {
        fruitContext.setName("fruits");
        HashMap<ElementSelector, Action> rulesMap = new HashMap<ElementSelector, Action>();
        rulesMap.put(new ElementSelector("/context/"), new FruitContextAction());
        simpleConfigurator = new SimpleConfigurator(rulesMap);
        simpleConfigurator.setContext(fruitContext);
    }

    void verifyFruit() {
        List<Fruit> fList = fruitContext.getFruitList();
        assertNotNull(fList);
        assertEquals(1, fList.size());

        Fruit f0 = fList.get(0);
        assertEquals("blue", f0.getName());
        assertEquals(2, f0.textList.size());
        assertEquals("hello", f0.textList.get(0));
        assertEquals("world", f0.textList.get(1));
    }

    @Test
    public void nestedComplex() throws Exception {
        try {
            simpleConfigurator.doConfigure(IMPLCIT_DIR + "nestedComplex.xml");
            verifyFruit();

        } catch (Exception je) {
            StatusPrinter.print(fruitContext);
            throw je;
        }
    }

    @Test
    public void nestedComplexWithoutClassAtrribute() throws Exception {
        try {
            simpleConfigurator.doConfigure(IMPLCIT_DIR + "nestedComplexWithoutClassAtrribute.xml");

            verifyFruit();

        } catch (Exception je) {
            StatusPrinter.print(fruitContext);
            throw je;
        }
    }

    void verifyFruitList() {
        List<Fruit> fList = fruitContext.getFruitList();
        assertNotNull(fList);
        assertEquals(1, fList.size());

        Fruit f0 = fList.get(0);
        assertEquals(2, f0.cakeList.size());

        Cake cakeA = f0.cakeList.get(0);
        assertEquals("A", cakeA.getType());

        Cake cakeB = f0.cakeList.get(1);
        assertEquals("B", cakeB.getType());
    }

    @Test
    public void nestedComplexCollection() throws Exception {
        try {
            simpleConfigurator.doConfigure(IMPLCIT_DIR + "nestedComplexCollection.xml");
            verifyFruitList();
        } catch (Exception je) {
            StatusPrinter.print(fruitContext);
            throw je;
        }
    }

    @Test
    public void nestedComplexCollectionWithoutClassAtrribute() throws Exception {
        try {
            simpleConfigurator.doConfigure(IMPLCIT_DIR + "nestedComplexCollectionWithoutClassAtrribute.xml");
            verifyFruitList();
        } catch (Exception je) {
            StatusPrinter.print(fruitContext);
            throw je;
        }
    }

}
