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
package ch.qos.logback.core.blackbox.appender;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.ContextBase;
import ch.qos.logback.core.JansiConsoleAppender;
import ch.qos.logback.core.testUtil.DummyEncoder;
import org.jline.jansi.AnsiConsole;
import org.jline.jansi.AnsiPrintStream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.PrintStream;

/**
 * Blackbox tests for {@link JansiConsoleAppender}, which uses
 * {@link AnsiConsole} without reflection.
 */
public class JansiConsoleAppenderTest {

    Context context = new ContextBase();
    JansiConsoleAppender<Object> ca = new JansiConsoleAppender<>();

    PrintStream originalOut;
    PrintStream originalErr;

    @BeforeEach
    public void setUp() {
        originalOut = System.out;
        originalErr = System.err;
    }

    @AfterEach
    public void tearDown() {
        // stop() undoes systemInstall() when this appender performed it
        ca.stop();
        System.setOut(originalOut);
        System.setErr(originalErr);
    }

    @Test
    public void jansiSystemOutWithoutReflection() {
        DummyEncoder<Object> dummyEncoder = new DummyEncoder<>();
        ca.setEncoder(dummyEncoder);
        ca.setTarget("System.out");
        ca.setContext(context);
        // JansiConsoleAppender overrides wrapTarget(); does not use withJansi
        ca.start();

        Assertions.assertTrue(ca.isStarted());
        Assertions.assertTrue(AnsiConsole.isInstalled());
        Assertions.assertTrue(ca.getOutputStream() instanceof AnsiPrintStream);
        ca.doAppend(new Object());

        ca.stop();
        Assertions.assertFalse(AnsiConsole.isInstalled(),
                "stop() must systemUninstall when this appender called systemInstall");
    }

    @Test
    public void jansiSystemErrWithoutReflection() {
        DummyEncoder<Object> dummyEncoder = new DummyEncoder<>();
        ca.setEncoder(dummyEncoder);
        ca.setTarget("System.err");
        ca.setContext(context);
        ca.start();

        Assertions.assertTrue(ca.isStarted());
        Assertions.assertTrue(AnsiConsole.isInstalled());
        Assertions.assertTrue(ca.getOutputStream() instanceof AnsiPrintStream);
        ca.doAppend(new Object());

        ca.stop();
        Assertions.assertFalse(AnsiConsole.isInstalled());
    }

}
