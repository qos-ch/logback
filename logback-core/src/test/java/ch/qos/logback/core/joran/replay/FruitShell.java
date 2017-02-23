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

import ch.qos.logback.core.spi.ContextAwareBase;

public class FruitShell extends ContextAwareBase {

    FruitFactory fruitFactory;
    String name;

    public void setFruitFactory(FruitFactory fruitFactory) {
        this.fruitFactory = fruitFactory;
    }

    void testFruit() {

        Fruit fruit = fruitFactory.buildFruit();
        System.out.println(fruit);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * Constructs a <code>String</code> with all attributes
     * in name = value format.
     *
     * @return a <code>String</code> representation 
     * of this object.
     */
    public String toString() {
        final String TAB = " ";

        String retValue = "";

        retValue = "FruitShell ( " + "fruitFactory = " + this.fruitFactory + TAB + "name = " + this.name + TAB + " )";

        return retValue;
    }

}
