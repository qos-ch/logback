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
package ch.qos.logback.core;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Verifies that {@link ConsoleAppender} probes the JLine Jansi {@code AnsiConsole} class name before
 * the legacy FuseSource one, so that {@code <withJansi>} keeps working after the Jansi migration from
 * FuseSource to JLine. See LOGBACK issue 1043.
 */
public class ConsoleAppenderJansiClassResolutionTest {

    static final String JLINE = "org.jline.jansi.AnsiConsole";
    static final String FUSESOURCE = "org.fusesource.jansi.AnsiConsole";

    final ConsoleAppender<Object> ca = new ConsoleAppender<>();

    /**
     * ClassLoader that resolves only the AnsiConsole class names present in {@code known} (mapping each
     * to a distinct stand-in class) and reports the others as absent.
     */
    private ClassLoader loaderResolving(Map<String, Class<?>> known) {
        return new ClassLoader(getClass().getClassLoader()) {
            @Override
            public Class<?> loadClass(String name) throws ClassNotFoundException {
                Class<?> mapped = known.get(name);
                if (mapped != null) {
                    return mapped;
                }
                if (JLINE.equals(name) || FUSESOURCE.equals(name)) {
                    throw new ClassNotFoundException(name);
                }
                return super.loadClass(name);
            }
        };
    }

    @Test
    public void prefersJLineWhenBothArePresent() throws ClassNotFoundException {
        Map<String, Class<?>> known = new HashMap<>();
        known.put(JLINE, String.class);
        known.put(FUSESOURCE, Integer.class);
        assertEquals(String.class, ca.loadAnsiConsoleClass(loaderResolving(known)));
    }

    @Test
    public void fallsBackToFuseSourceWhenJLineIsAbsent() throws ClassNotFoundException {
        Map<String, Class<?>> known = new HashMap<>();
        known.put(FUSESOURCE, Integer.class);
        assertEquals(Integer.class, ca.loadAnsiConsoleClass(loaderResolving(known)));
    }

    @Test
    public void throwsWhenNoJansiIsAvailable() {
        Map<String, Class<?>> none = new HashMap<>();
        assertThrows(ClassNotFoundException.class, () -> ca.loadAnsiConsoleClass(loaderResolving(none)));
    }
}
