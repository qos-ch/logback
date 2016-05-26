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
package ch.qos.logback.core.joran.event;

import org.xml.sax.Locator;

public class BodyEvent extends SaxEvent {

    private String text;

    BodyEvent(String text, Locator locator) {
        super(null, null, null, locator);
        this.text = text;
    }

    /**
     * Always trim trailing spaces from the body text.
     * 
     * @return
     */
    public String getText() {
        if (text != null) {
            return text.trim();
        }
        return text;
    }

    @Override
    public String toString() {
        return "BodyEvent(" + getText() + ")" + locator.getLineNumber() + "," + locator.getColumnNumber();
    }

    public void append(String str) {
        text += str;
    }

}
