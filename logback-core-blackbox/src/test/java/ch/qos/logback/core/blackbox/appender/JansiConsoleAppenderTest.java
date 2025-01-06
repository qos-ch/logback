/*
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2023, QOS.ch. All rights reserved.
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
package ch.qos.logback.core.blackbox.appender;

import ch.qos.logback.core.Appender;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.Context;
import ch.qos.logback.core.ContextBase;

import ch.qos.logback.core.testUtil.DummyEncoder;
import ch.qos.logback.core.testUtil.XTeeOutputStream;
import org.fusesource.jansi.AnsiConsole;
import org.fusesource.jansi.AnsiPrintStream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.FilterOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;

/**
 * Redirecting System.out is quite messy. Disable this test in Maven but not in
 * Package.class
 */
public class JansiConsoleAppenderTest {
    Context context = new ContextBase();
    ConsoleAppender<Object> ca = new ConsoleAppender<Object>();

    XTeeOutputStream teeOut;
    XTeeOutputStream teeErr;
    PrintStream originalOut;
    PrintStream originalErr;

    @BeforeEach
    public void setUp() {
        originalOut = System.out;
        originalErr = System.err;
        // teeOut will output bytes on System out but it will also
        // collect them so that the output can be compared against
        // some expected output data
        // teeOut = new TeeOutputStream(originalOut);

        // keep the console quiet
        //teeOut = new XTeeOutputStream(null);
        //teeErr = new XTeeOutputStream(null);

        //System.setOut(new PrintStream(teeOut));
        //System.setErr(new PrintStream(teeErr));

        // redirect System.out to teeOut and System.err to teeErr
        //replace(originalOut, teeOut);
        //replace(originalErr, teeErr);
    }

    @AfterEach
    public void tearDown() {
        AnsiConsole.systemUninstall();
        System.setOut(originalOut);
        //replace(AnsiConsole.out(), originalOut);
        System.setErr(originalErr);
        //replace(AnsiConsole.err(), originalErr);

    }

    private void replace(AnsiPrintStream ansiPrintStream, OutputStream os) {
        try {
            Field field = FilterOutputStream.class.getDeclaredField("out");
            field.setAccessible(true);
            OutputStream oldOs = (OutputStream) field.get(ansiPrintStream);
            field.set(ansiPrintStream, os);
        } catch (Throwable t) {
            throw new IllegalStateException("Unable to initialize Jansi for testing", t);
        }
    }

    public Appender<Object> getAppender() {
        return new ConsoleAppender<>();
    }

    @Test
    public void jansiSystemOut() {

        DummyEncoder<Object> dummyEncoder = new DummyEncoder<>();
        ca.setEncoder(dummyEncoder);
        ca.setTarget("System.out");
        ca.setContext(context);
        ca.setWithJansi(true);
        ca.start();
        Assertions.assertTrue(ca.getOutputStream() instanceof AnsiPrintStream);
        ca.doAppend(new Object());
        // broken in Jansi 2.x as it uses java.io.FileDescriptor instead of System.out
        //Assertions.assertEquals("dummy", teeOut.toString().trim());
    }

    @Test
    public void jansiSystemErr() {
        DummyEncoder<Object> dummyEncoder = new DummyEncoder<>();
        ca.setEncoder(dummyEncoder);
        ca.setTarget("System.err");
        ca.setContext(context);
        ca.setWithJansi(true);
        ca.start();
        Assertions.assertTrue(ca.getOutputStream() instanceof AnsiPrintStream);
        ca.doAppend(new Object());
        // broken in Jansi 2.x as it uses java.io.FileDescriptor instead of System.err
        //Assertions.assertEquals("dummy", teeErr.toString().trim());
    }
}
