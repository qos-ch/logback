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

import java.util.ArrayList;
import java.util.List;

import ch.qos.logback.core.ContextBase;

public class FruitContext extends ContextBase {

    List<FruitShell> fruitShellList = new ArrayList<FruitShell>();

    public void addFruitShell(FruitShell fs) {
        fruitShellList.add(fs);
    }

    public List<FruitShell> getFruitShellList() {
        return fruitShellList;
    }

    public void setFruitShellList(List<FruitShell> fruitShellList) {
        this.fruitShellList = fruitShellList;
    }
}
