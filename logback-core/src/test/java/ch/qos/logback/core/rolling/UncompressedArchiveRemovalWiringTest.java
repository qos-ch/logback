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
package ch.qos.logback.core.rolling;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.ContextBase;
import ch.qos.logback.core.util.FileSize;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Checks that rolling policies give the archive remover the pattern of uncompressed
 * files, so that files left uncompressed at rollover are eventually removed. See
 * https://github.com/qos-ch/logback/discussions/1032
 */
public class UncompressedArchiveRemovalWiringTest {

    static final String HOURLY = "yyyy-MM-dd-HH";
    static final Instant NOW = Instant.parse("2026-04-14T12:30:00Z");

    Context context = new ContextBase();
    RollingFileAppender<Object> rfa = new RollingFileAppender<>();
    TimeBasedRollingPolicy<Object> tbrp = new TimeBasedRollingPolicy<>();

    @TempDir
    Path tempDir;

    @BeforeEach
    public void setUp() {
        rfa.setContext(context);
        tbrp.setContext(context);
        tbrp.setParent(rfa);
        tbrp.setMaxHistory(2);
    }

    File create(String name) throws IOException {
        File f = tempDir.resolve(name).toFile();
        Files.createFile(f.toPath());
        return f;
    }

    void startAndClean(TimeBasedFileNamingAndTriggeringPolicy<Object> fnatp, String fileNamePattern) {
        fnatp.setCurrentTime(NOW.toEpochMilli());
        tbrp.setTimeBasedFileNamingAndTriggeringPolicy(fnatp);
        tbrp.setFileNamePattern(tempDir + "/" + fileNamePattern);
        tbrp.start();
        assertTrue(tbrp.isStarted());
        fnatp.getArchiveRemover().clean(NOW);
    }

    @Test
    public void timeBasedPolicy() throws IOException {
        File orphan = create("log.2026-04-14-09");
        File archive = create("log.2026-04-14-09.gz");
        File recentOrphan = create("log.2026-04-14-11");

        startAndClean(new DefaultTimeBasedFileNamingAndTriggeringPolicy<>(), "log.%d{" + HOURLY + ", UTC}.gz");

        assertFalse(orphan.exists());
        assertFalse(archive.exists());
        assertTrue(recentOrphan.exists());
    }

    @Test
    public void sizeAndTimeBasedPolicy() throws IOException {
        File orphan = create("log.2026-04-14-09.1");
        File archive = create("log.2026-04-14-09.0.gz");
        File recentOrphan = create("log.2026-04-14-11.0");

        SizeAndTimeBasedFileNamingAndTriggeringPolicy<Object> fnatp = new SizeAndTimeBasedFileNamingAndTriggeringPolicy<>();
        fnatp.setMaxFileSize(new FileSize(10000));
        startAndClean(fnatp, "log.%d{" + HOURLY + ", UTC}.%i.gz");

        assertFalse(orphan.exists());
        assertFalse(archive.exists());
        assertTrue(recentOrphan.exists());
    }
}
