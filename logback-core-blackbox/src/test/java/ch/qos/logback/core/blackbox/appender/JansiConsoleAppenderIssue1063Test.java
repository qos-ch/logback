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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.FileDescriptor;
import java.io.PrintStream;

import org.jline.jansi.AnsiConsole;
import org.jline.jansi.AnsiPrintStream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.Context;
import ch.qos.logback.core.ContextBase;
import ch.qos.logback.core.JansiConsoleAppender;
import ch.qos.logback.core.testUtil.DummyEncoder;

/**
 * Reproduces
 * <a href="https://github.com/qos-ch/logback/issues/1063">logback issue
 * #1063</a> (also
 * <a href="https://jira.qos.ch/browse/LOGBACK-1759">LOGBACK-1759</a>) for
 * {@link JansiConsoleAppender}.
 *
 * <p>
 * With Jansi, {@code start()} uses the process-wide {@link AnsiConsole#out()}
 * (also installed as {@link System#out} via {@link AnsiConsole#systemInstall()}).
 * {@link ch.qos.logback.core.OutputStreamAppender#stop()} then calls
 * {@code outputStream.close()} on that shared stream. A subsequent write marks
 * the stream's error flag; on some platforms (notably Windows) the underlying
 * {@link FileDescriptor#out} is closed at the OS level and cannot be recovered
 * within the same JVM.
 * </p>
 *
 * <p>
 * Expected correct behaviour: stopping the appender must not close the
 * process-wide stdout stream (flush only). {@link JansiConsoleAppender#stop()}
 * also balances {@link AnsiConsole#systemInstall()} with
 * {@link AnsiConsole#systemUninstall()} when this appender installed.
 * </p>
 */
public class JansiConsoleAppenderIssue1063Test {

    Context context = new ContextBase();
    PrintStream originalOut;
    PrintStream originalErr;

    @BeforeEach
    public void setUp() {
        originalOut = System.out;
        originalErr = System.err;
    }

    @AfterEach
    public void tearDown() {
        // Appenders under test should have stopped and uninstalled; drain any
        // leftover install count only if something failed mid-test.
        try {
            while (AnsiConsole.isInstalled()) {
                AnsiConsole.systemUninstall();
            }
        } catch (Throwable ignored) {
            // best-effort cleanup
        }
        System.setOut(originalOut);
        System.setErr(originalErr);
    }

    /**
     * Control: plain {@link ConsoleAppender} uses a non-closing
     * {@link ch.qos.logback.core.joran.spi.ConsoleTarget} stream, so stop must
     * leave {@link System#out} usable.
     */
    @Test
    public void plainConsoleAppenderStopDoesNotPoisonSystemOut() {
        ConsoleAppender<Object> ca = new ConsoleAppender<>();
        ca.setContext(context);
        ca.setEncoder(new DummyEncoder<>());
        ca.start();
        ca.doAppend(new Object());

        ca.stop();

        System.out.println("plain-after-stop");
        assertFalse(System.out.checkError(),
                "plain ConsoleAppender.stop() must not put System.out into error state");
    }

    /**
     * Issue #1063 adapted to {@link JansiConsoleAppender}: after stop(), a write
     * to the stream that was in use must not set the PrintStream error flag.
     */
    @Test
    public void jansiConsoleAppenderStopMustNotCloseSharedStdout() {
        JansiConsoleAppender<Object> ca = new JansiConsoleAppender<>();
        ca.setContext(context);
        ca.setEncoder(new DummyEncoder<>());
        ca.start();

        assertTrue(ca.getOutputStream() instanceof AnsiPrintStream);
        // systemInstall() makes AnsiConsole.out() the process-wide System.out
        assertSame(System.out, ca.getOutputStream(),
                "expected AnsiConsole.systemInstall() to install appender stream as System.out");

        PrintStream jansiOut = (PrintStream) ca.getOutputStream();
        ca.doAppend(new Object());
        assertFalse(jansiOut.checkError(), "stream should be healthy after append");

        ca.stop();
        assertFalse(AnsiConsole.isInstalled(),
                "stop() must systemUninstall when this appender called systemInstall");

        // After uninstall, System.out is restored; write to the former Jansi stream
        // must not have been closed by stop() (flush-only). PrintStream does not
        // throw on write-after-close; it sets the error flag.
        jansiOut.println("write-after-jansi-console-appender-stop");
        assertFalse(jansiOut.checkError(),
                "JansiConsoleAppender.stop() must not close the shared AnsiConsole stream "
                        + "(https://github.com/qos-ch/logback/issues/1063)");
    }

    /**
     * Reconfiguration path from the issue: stop (as during
     * {@code LoggerContext.reset()}) then start a new JansiConsoleAppender. The
     * second instance must get a usable stream.
     */
    @Test
    public void jansiConsoleAppenderUsableAfterStopAndRestart() {
        JansiConsoleAppender<Object> first = new JansiConsoleAppender<>();
        first.setContext(context);
        first.setEncoder(new DummyEncoder<>());
        first.start();
        first.doAppend(new Object());
        first.stop();

        JansiConsoleAppender<Object> second = new JansiConsoleAppender<>();
        second.setContext(context);
        second.setEncoder(new DummyEncoder<>());
        second.start();
        second.doAppend(new Object());

        PrintStream out = (PrintStream) second.getOutputStream();
        out.println("after-reconfigure");
        assertFalse(out.checkError(),
                "after stop+restart, Jansi-backed stream must still accept writes "
                        + "(https://github.com/qos-ch/logback/issues/1063)");
        second.stop();
    }

    /**
     * Documents the Windows-oriented observation from #1063 when it applies.
     * On Linux, {@link FileDescriptor#out} often remains {@code valid()} even
     * after the Jansi stream is closed; the PrintStream error flag is the
     * portable signal. This assertion only fires if the FD is actually closed.
     */
    @Test
    public void jansiConsoleAppenderStopShouldKeepFileDescriptorOutValid() {
        assertTrue(FileDescriptor.out.valid(), "precondition: stdout FD must be valid");

        JansiConsoleAppender<Object> ca = new JansiConsoleAppender<>();
        ca.setContext(context);
        ca.setEncoder(new DummyEncoder<>());
        ca.start();
        ca.doAppend(new Object());
        ca.stop();

        // Intentionally exercise the closed stream (sets checkError on some JDKs).
        System.out.println("fd-check-after-stop");

        assertTrue(FileDescriptor.out.valid(),
                "JansiConsoleAppender.stop() must not close FileDescriptor.out "
                        + "(https://github.com/qos-ch/logback/issues/1063)");
    }
}
