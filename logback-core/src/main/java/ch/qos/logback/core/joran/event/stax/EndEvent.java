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

/**
 * Created with IntelliJ IDEA.
 * User: ceki
 * Date: 7/2/13
 * Time: 4:07 PM
 * To change this template use File | Settings | File Templates.
 */
public class EndEvent extends StaxEvent {

    public EndEvent(String name, Location location) {
        super(name, location);
    }

    @Override
    public String toString() {
        return "EndEvent(" + getName() + ")  [" + location.getLineNumber() + "," + location.getColumnNumber() + "]";
    }

}
