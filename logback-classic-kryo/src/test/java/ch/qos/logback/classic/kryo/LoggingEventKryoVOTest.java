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
package ch.qos.logback.classic.kryo;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class LoggingEventKryoVOTest {

    @Test
    public void alreadySafeArgsAreNotCopied() {
        Object[] args = new Object[] { "user", 42, 99L, (short) 1, 1.5f, 2.5d, null };
        assertThat(LoggingEventKryoVO.safeObjectArray(args)).isSameAs(args);
    }

    @Test
    public void emptyArgsAreNotCopied() {
        Object[] args = new Object[0];
        assertThat(LoggingEventKryoVO.safeObjectArray(args)).isSameAs(args);
    }

    @Test
    public void nullArgsStayNull() {
        assertThat(LoggingEventKryoVO.safeObjectArray(null)).isNull();
    }

    @Test
    public void callerDataCopiedOnlyWhenPresent() {
        LoggingEventKryoVO without = new LoggingEventKryoVO.Builder()
                .withLoggerName("test")
                .withThreadName("main")
                .withLevelInt(20000)
                .withMessage("m")
                .build();
        assertThat(without.hasCallerData()).isFalse();
        assertThat(without.getCallerData()).isNull();

        StackTraceElement[] frames = {
                new StackTraceElement("com.example.App", "main", "App.java", 1)
        };
        LoggingEventKryoVO with = new LoggingEventKryoVO.Builder()
                .withLoggerName("test")
                .withThreadName("main")
                .withLevelInt(20000)
                .withMessage("m")
                .withCallerData(frames)
                .build();
        assertThat(with.hasCallerData()).isTrue();
        assertThat(with.getCallerData()).isEqualTo(frames);

        LoggingEventKryoVO copiedWithout = new LoggingEventKryoVO(without);
        assertThat(copiedWithout.hasCallerData()).isFalse();
        LoggingEventKryoVO copiedWith = new LoggingEventKryoVO(with);
        assertThat(copiedWith.hasCallerData()).isTrue();
        assertThat(copiedWith.getCallerData()).isEqualTo(frames);
    }

    @Test
    public void unsafeArgIsCopiedAndStringified() {
        Object custom = new Object() {
            @Override
            public String toString() {
                return "stringified";
            }
        };
        Object[] args = new Object[] { "ok", custom, 7 };
        Object[] safe = LoggingEventKryoVO.safeObjectArray(args);
        assertThat(safe).isNotSameAs(args);
        assertThat(safe[0]).isSameAs("ok");
        assertThat(safe[1]).isEqualTo("stringified").isInstanceOf(String.class);
        assertThat(safe[2]).isEqualTo(7);
        assertThat(args[1]).isSameAs(custom);
    }
}
