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
package ch.qos.logback.core.joran.event.stax;

import javax.xml.stream.Location;

public class BodyEvent extends StaxEvent {

    private String text;

    BodyEvent(String text, Location location) {
        super(null, location);
        this.text = text;
    }

    public String getText() {
        return text;
    }

    void append(String txt) {
        text += txt;
    }

    @Override
    public String toString() {
        return "BodyEvent(" + getText() + ")" + location.getLineNumber() + "," + location.getColumnNumber();
    }
}
