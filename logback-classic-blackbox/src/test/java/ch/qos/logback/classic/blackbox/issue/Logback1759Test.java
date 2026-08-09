/*
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2026, QOS.ch. All rights reserved.
 *
 * This program and the accompanying materials are dual-licensed under
 * either the terms of the Eclipse Public License v2.0 as published by
 * the Eclipse Foundation
 *
 *   or (per the licensee's choosing)
 *
 * under the terms of the GNU Lesser General Public License version 2.1
 * as published by the Free Software Foundation.
 */

package ch.qos.logback.classic.blackbox.issue;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.classic.util.LogbackMDCAdapter;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.status.OnConsoleStatusListener;
import ch.qos.logback.core.util.StatusListenerConfigHelper;
import ch.qos.logback.core.util.StatusPrinter2;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class Logback1759Test {

    LoggerContext context = new LoggerContext();
    Logger logger = context.getLogger("toto.foo");
    StatusPrinter2 statusPrinter2 = new StatusPrinter2();
    PatternLayoutEncoder patternLayoutEncoder = null;
    ConsoleAppender consoleAppender = null;
    LogbackMDCAdapter logbackMDCAdapter = new LogbackMDCAdapter();
    int count = 0;

    @BeforeEach
    public void setup() {
        context.setMDCAdapter(logbackMDCAdapter);
        OnConsoleStatusListener onConsoleStatusListener = new OnConsoleStatusListener();
        StatusListenerConfigHelper.addOnConsoleListenerInstance(context, onConsoleStatusListener);
        init(count++);
    }

    void init(int count) {

        System.out.println("Init called count=" + count);
        this.patternLayoutEncoder = new PatternLayoutEncoder();
        patternLayoutEncoder.setContext(context);
        patternLayoutEncoder.setPattern("%highlight(%level) %message%n");
        patternLayoutEncoder.start();

        this.consoleAppender = new ConsoleAppender();
        consoleAppender.setContext(context);
        consoleAppender.setEncoder(patternLayoutEncoder);
    }

    @Test
    public void smoke() {
        consoleAppender.setWithJansi(true);
        consoleAppender.start();

        //String fqcn, Logger logger, Level level, String message, Throwable throwable,
        //        Object[] argArray

        LoggingEvent le = new LoggingEvent("x", logger, Level.INFO, "hello world", null, null);

        consoleAppender.doAppend(le);
        System.out.println("Before   consoleAppender.stop();");
        consoleAppender.stop();
        System.out.println("After   consoleAppender.stop();");
        init(count++);
        //consoleAppender.setWithJansi(true);

        consoleAppender.start();
        System.out.println("Second append");
        consoleAppender.doAppend(le);

    }
}
