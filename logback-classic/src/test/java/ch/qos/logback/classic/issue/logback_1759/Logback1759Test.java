/*
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2024, QOS.ch. All rights reserved.
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

package ch.qos.logback.core.issue.logback_1759;

import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.Context;
import ch.qos.logback.core.ContextBase;
import ch.qos.logback.core.encoder.EchoEncoder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class Logback1759Test {

    Context context = new ContextBase();
    EchoEncoder echoEncoder = new EchoEncoder();
    ConsoleAppender consoleAppender = new ConsoleAppender();

    @BeforeEach
    public void setup() {
        consoleAppender.setContext(context);
        echoEncoder.setContext(context);
        echoEncoder.start();
        consoleAppender.setEncoder(echoEncoder);
    }

    @Test
    public void smoke() {
        consoleAppender.setWithJansi(true);
        consoleAppender.start();

        consoleAppender.doAppend("hello 1");

        consoleAppender.stop();
        consoleAppender.start();
        consoleAppender.doAppend("hello 2");
    }
}
