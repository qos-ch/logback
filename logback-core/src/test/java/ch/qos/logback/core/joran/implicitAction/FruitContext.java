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

import java.util.ArrayList;
import java.util.List;

import ch.qos.logback.core.ContextBase;

public class FruitContext extends ContextBase {

    List<Fruit> fruitList = new ArrayList<Fruit>();

    public void addFruit(Fruit fs) {
        fruitList.add(fs);
    }

    public List<Fruit> getFruitList() {
        return fruitList;
    }

    public void setFruitShellList(List<Fruit> fruitList) {
        this.fruitList = fruitList;
    }
}
