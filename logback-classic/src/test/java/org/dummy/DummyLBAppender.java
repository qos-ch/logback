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
package org.dummy;

import java.util.ArrayList;
import java.util.List;

import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;

public class DummyLBAppender extends AppenderBase<ILoggingEvent> {

    public List<ILoggingEvent> list = new ArrayList<ILoggingEvent>();
    public List<String> stringList = new ArrayList<String>();

    PatternLayout layout;

    DummyLBAppender() {
        this(null);
    }

    DummyLBAppender(PatternLayout layout) {
        this.layout = layout;
    }

    protected void append(ILoggingEvent e) {
        list.add(e);
        if (layout != null) {
            String s = layout.doLayout(e);
            stringList.add(s);
        }
    }
}
