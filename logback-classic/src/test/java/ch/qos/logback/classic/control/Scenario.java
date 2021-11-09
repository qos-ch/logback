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
package ch.qos.logback.classic.control;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class Scenario {

    private final List<ScenarioAction> actionList = new Vector<>();

    public void add(final ScenarioAction action) {
        actionList.add(action);
    }

    public List<ScenarioAction> getActionList() {
        return new ArrayList<>(actionList);
    }

    public int size() {
        return actionList.size();
    }

    public ScenarioAction get(final int i) {
        return actionList.get(i);
    }
}
