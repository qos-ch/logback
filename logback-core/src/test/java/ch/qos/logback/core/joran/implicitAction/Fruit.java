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

public class Fruit {

    String name;
    List<String> textList = new ArrayList<String>();
    List<Cake> cakeList = new ArrayList<Cake>();

    public void setName(String n) {
        this.name = n;
    }

    public String getName() {
        return name;
    }

    public void addText(String s) {
        textList.add(s);
    }

    public void addCake(Cake c) {
        cakeList.add(c);
    }
}
