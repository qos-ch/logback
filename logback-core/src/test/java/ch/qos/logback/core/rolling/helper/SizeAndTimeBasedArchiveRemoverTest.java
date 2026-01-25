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

package ch.qos.logback.core.rolling.helper;


import java.io.File;
import java.time.Instant;
import java.util.Date;

import org.junit.jupiter.api.Test;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.ContextBase;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

public class SizeAndTimeBasedArchiveRemoverTest {

    Context context = new ContextBase();

    @Test
    public void smoke() {
        FileNamePattern fileNamePattern = new FileNamePattern("smoke-%d-%i.gz", context);
        SizeAndTimeBasedArchiveRemover remover = new SizeAndTimeBasedArchiveRemover(fileNamePattern, null);
        File[] fileArray = new File[2];
        File[] expected = new File[2];

        fileArray[0] = expected[1] = new File("/tmp/smoke-1970-01-01-0.gz");
        fileArray[1] = expected[0] = new File("/tmp/smoke-1970-01-01-1.gz");

        remover.descendingSort(fileArray, Instant.ofEpochMilli(0));

        assertArrayEquals(expected, fileArray);
    }

    @Test
    public void badFilenames() {
        FileNamePattern fileNamePattern = new FileNamePattern("smoke-%d-%i.gz", context);
        SizeAndTimeBasedArchiveRemover remover = new SizeAndTimeBasedArchiveRemover(fileNamePattern, null);
        File[] fileArray = new File[2];
        File[] expected = new File[2];

        fileArray[0] = expected[0] = new File("/tmp/smoke-1970-01-01-b.gz");
        fileArray[1] = expected[1] = new File("/tmp/smoke-1970-01-01-c.gz");

        remover.descendingSort(fileArray, Instant.ofEpochMilli(0));

        assertArrayEquals(expected, fileArray);
    }
}
