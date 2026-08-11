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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.PrintStream;

import org.jline.jansi.AnsiConsole;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.classic.util.LogbackMDCAdapter;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.status.OnConsoleStatusListener;
import ch.qos.logback.core.testUtil.XTeeOutputStream;
import ch.qos.logback.core.util.StatusListenerConfigHelper;

/**
 * LOGBACK-1759 / <a href="https://github.com/qos-ch/logback/issues/1063">issue
 * #1063</a>: with {@code withJansi=true}, stopping a {@link ConsoleAppender}
 * must not close process-wide stdout, so that subsequent
 * {@link System#out}{@code .println} calls still succeed.
 */
public class Logback1759Test {

    /** Marker printed after {@code consoleAppender.stop()}; must remain effective. */
    static final String AFTER_STOP_LINE = "After   consoleAppender.stop()";

    static final String BEFORE_STOP_LINE = "Before   consoleAppender.stop()";

    LoggerContext context = new LoggerContext();
    Logger logger = context.getLogger("toto.foo");
    PatternLayoutEncoder patternLayoutEncoder = null;
    ConsoleAppender<ILoggingEvent> consoleAppender = null;
    LogbackMDCAdapter logbackMDCAdapter = new LogbackMDCAdapter();
    int count = 0;

    PrintStream originalOut;
    PrintStream originalErr;
    /** Captures bytes written through the PrintStream installed as {@link System#out} before Jansi replaces it. */
    XTeeOutputStream systemOutTee;

    @BeforeEach
    public void setup() {
        originalOut = System.out;
        originalErr = System.err;
        // Collect System.out while still writing through to the real console.
        // Note: withJansi + AnsiConsole.systemInstall() replaces System.out with an
        // AnsiPrintStream on FileDescriptor.out, so post-start println goes there, not
        // into this tee. Post-stop effectiveness is checked via checkError() on the
        // then-current System.out (see smoke()).
        systemOutTee = new XTeeOutputStream(originalOut);
        System.setOut(new PrintStream(systemOutTee, true));

        context.setMDCAdapter(logbackMDCAdapter);
        OnConsoleStatusListener onConsoleStatusListener = new OnConsoleStatusListener();
        StatusListenerConfigHelper.addOnConsoleListenerInstance(context, onConsoleStatusListener);
        init(count++);
    }

    @AfterEach
    public void tearDown() {
        if (consoleAppender != null && consoleAppender.isStarted()) {
            consoleAppender.stop();
        }
        try {
            AnsiConsole.systemUninstall();
        } catch (Throwable ignored) {
            // best-effort after a poisoned Jansi stream
        }
        System.setOut(originalOut);
        System.setErr(originalErr);
    }

    void init(int count) {
        System.out.println("Init called count=" + count);
        this.patternLayoutEncoder = new PatternLayoutEncoder();
        patternLayoutEncoder.setContext(context);
        patternLayoutEncoder.setPattern("%highlight(%level) %message%n");
        patternLayoutEncoder.start();

        this.consoleAppender = new ConsoleAppender<ILoggingEvent>();
        consoleAppender.setContext(context);
        consoleAppender.setEncoder(patternLayoutEncoder);
    }

    @Test
    public void smoke() {
        consoleAppender.setWithJansi(true);
        consoleAppender.start();

        LoggingEvent le = new LoggingEvent("x", logger, Level.INFO, "hello world", null, null);

        consoleAppender.doAppend(le);
        System.out.println(BEFORE_STOP_LINE);
        assertFalse(System.out.checkError(), "System.out should be healthy before stop");

        consoleAppender.stop();

        // Critical LOGBACK-1759 check: this println must still work on the process-wide
        // stream (with Jansi, that is AnsiConsole.out() / the installed System.out).
        // PrintStream does not throw on write-after-close; it sets the error flag.
        System.out.println(AFTER_STOP_LINE);
        assertFalse(System.out.checkError(),
                "System.out.println(\"" + AFTER_STOP_LINE + "\") must be effective after "
                        + "consoleAppender.stop() with withJansi=true (LOGBACK-1759 / issue #1063)");

        init(count++);

        consoleAppender.setWithJansi(true);
        consoleAppender.start();
        System.out.println("Second append");
        consoleAppender.doAppend(le);
        assertFalse(System.out.checkError(), "System.out should remain healthy after restart and second append");
    }

    /**
     * Without Jansi, all {@link System#out} traffic stays on the teed stream, so
     * the after-stop line is visible in the capture buffer as well.
     */
    @Test
    public void afterStopLineCapturedWhenNotUsingJansi() {
        consoleAppender.setWithJansi(false);
        consoleAppender.start();

        LoggingEvent le = new LoggingEvent("x", logger, Level.INFO, "hello world", null, null);
        consoleAppender.doAppend(le);
        System.out.println(BEFORE_STOP_LINE);
        consoleAppender.stop();
        System.out.println(AFTER_STOP_LINE);

        assertFalse(System.out.checkError());
        String captured = systemOutTee.toString();
        assertTrue(captured.contains(AFTER_STOP_LINE),
                "expected captured System.out to contain \"" + AFTER_STOP_LINE + "\", was: " + captured);
    }
}
