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

import java.util.List;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.ContextBase;
import ch.qos.logback.core.joran.event.SaxEvent;
import ch.qos.logback.core.joran.spi.JoranException;

public class FruitFactory {

    static int count = 0;

    private List<SaxEvent> eventList;
    Fruit fruit;

    public void setFruit(Fruit fruit) {
        this.fruit = fruit;
    }

    public Fruit buildFruit() {

        Context context = new ContextBase();
        this.fruit = null;
        context.putProperty("fruitKey", "orange-" + count);
        // for next round
        count++;
        FruitConfigurator fruitConfigurator = new FruitConfigurator(this);
        fruitConfigurator.setContext(context);
        try {
            fruitConfigurator.doConfigure(eventList);
        } catch (JoranException je) {
            je.printStackTrace();
        }
        return fruit;
    }

    public String toString() {
        final String TAB = " ";

        StringBuilder retValue = new StringBuilder();

        retValue.append("FruitFactory ( ");
        if (eventList != null && eventList.size() > 0) {
            retValue.append("event1 = ").append(eventList.get(0)).append(TAB);
        }
        retValue.append(" )");

        return retValue.toString();
    }

    public void setEventList(List<SaxEvent> eventList) {
        this.eventList = eventList;
    }

}
