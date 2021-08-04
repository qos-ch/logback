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
package org.slf4j.implTest;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import ch.qos.logback.core.testUtil.RandomUtil;

public class RecursiveLBAppender extends AppenderBase<ILoggingEvent> {

    public List<ILoggingEvent> list = new ArrayList<ILoggingEvent>();
    public List<String> stringList = new ArrayList<String>();

    PatternLayout layout;

    public RecursiveLBAppender() {
        this(null);
    }

    public RecursiveLBAppender(PatternLayout layout) {
        this.layout = layout;
    }

    @Override
    public void start() {
        int diff = RandomUtil.getPositiveInt();
        Logger logger = LoggerFactory.getLogger("ResursiveLBAppender" + diff);
        logger.info("testing");
        super.start();
    }

    protected void append(ILoggingEvent e) {
        list.add(e);
        if (layout != null) {
            String s = layout.doLayout(e);
            stringList.add(s);
        }
    }
}
