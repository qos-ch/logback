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
package ch.qos.logback.core.joran.conditional;

import java.util.List;

import ch.qos.logback.core.joran.event.SaxEvent;

public class ThenAction extends ThenOrElseActionBase {

    @Override
    void registerEventList(IfAction ifAction, List<SaxEvent> eventList) {
        ifAction.setThenSaxEventList(eventList);
    }

}
