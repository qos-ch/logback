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
package ch.qos.logback.core.joran.spi;


import java.util.ArrayList;
import java.util.List;

import ch.qos.logback.core.joran.event.BodyEvent;
import ch.qos.logback.core.joran.event.EndEvent;
import ch.qos.logback.core.joran.event.SaxEvent;
import ch.qos.logback.core.joran.event.StartEvent;

public class EventPlayer {

    final SaxEventInterpreter interpreter;
    final List<SaxEvent> saxEvents;
    int currentIndex;

    public EventPlayer(SaxEventInterpreter interpreter, List<SaxEvent> saxEvents) {
        this.interpreter = interpreter;
        this.saxEvents = saxEvents;
    }

    /**
     * Return a copy of the current event list in the player.
     * 
     * @return
     * @since 0.9.20
     */
    public List<SaxEvent> getCopyOfPlayerEventList() {
        return new ArrayList<SaxEvent>(saxEvents);
    }

    public void play() {
         
        for (currentIndex = 0; currentIndex < saxEvents.size(); currentIndex++) {
            SaxEvent se = saxEvents.get(currentIndex);

            if (se instanceof StartEvent) {
                interpreter.startElement((StartEvent) se);
            }
            if (se instanceof BodyEvent) {
                interpreter.characters((BodyEvent) se);
            }
            if (se instanceof EndEvent) {
                interpreter.endElement((EndEvent) se);
            }

        }
    }

    public void addEventsDynamically(List<SaxEvent> eventList, int offset) {
        this.saxEvents.addAll(currentIndex + offset, eventList);
    }
}
