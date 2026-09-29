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
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Locale;
import java.util.TimeZone;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.ContextBase;
import ch.qos.logback.core.status.testUtil.StatusChecker;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Removal of archives left uncompressed, e.g. when the application was down at
 * rollover time. See https://github.com/qos-ch/logback/discussions/1032
 */
public class UncompressedArchiveRemovalTest {

    static final String HOURLY = "yyyy-MM-dd-HH";
    // 12:30 UTC, so with maxHistory=2 the first period to delete is hour 09
    static final Instant NOW = Instant.parse("2026-04-14T12:30:00Z");

    Context context = new ContextBase();
    StatusChecker checker = new StatusChecker(context);
    RollingCalendar rc = new RollingCalendar(HOURLY, TimeZone.getTimeZone("UTC"), Locale.US);

    @TempDir
    Path tempDir;

    String dir;

    @BeforeEach
    public void setUp() {
        dir = tempDir.toString();
    }

    FileNamePattern fnp(String pattern) {
        return new FileNamePattern(dir + "/" + pattern, context);
    }

    File create(String name) throws IOException {
        File f = new File(dir, name);
        f.getParentFile().mkdirs();
        Files.write(f.toPath(), "0123456789".getBytes(StandardCharsets.UTF_8));
        return f;
    }

    TimeBasedArchiveRemover timeBasedRemover(String compressedPattern, String uncompressedPattern) {
        TimeBasedArchiveRemover remover = new TimeBasedArchiveRemover(fnp(compressedPattern),
                fnp(uncompressedPattern), rc);
        remover.setContext(context);
        remover.setMaxHistory(2);
        return remover;
    }

    @Test
    public void uncompressedFileOlderThanMaxHistoryIsRemoved() throws IOException {
        File orphan = create("log.2026-04-14-09");
        File archive = create("log.2026-04-14-09.gz");
        File recentOrphan = create("log.2026-04-14-11");
        File active = create("log.2026-04-14-12");

        timeBasedRemover("log.%d{" + HOURLY + ", UTC}.gz", "log.%d{" + HOURLY + ", UTC}").clean(NOW);

        assertFalse(orphan.exists());
        assertFalse(archive.exists());
        assertTrue(recentOrphan.exists());
        assertTrue(active.exists());
        checker.assertContainsMatch("deleting historically stale uncompressed file .*log.2026-04-14-09$");
    }

    @Test
    public void zipAndXzSuffixes() throws IOException {
        for (String suffix : new String[] { ".zip", ".xz" }) {
            File orphan = create("log.2026-04-14-09");
            File archive = create("log.2026-04-14-09" + suffix);

            timeBasedRemover("log.%d{" + HOURLY + ", UTC}" + suffix, "log.%d{" + HOURLY + ", UTC}").clean(NOW);

            assertFalse(orphan.exists(), suffix);
            assertFalse(archive.exists(), suffix);
        }
    }

    @Test
    public void withoutCompressionFilesAreDeletedOnceAndWithoutWarnings() throws IOException {
        File archive = create("log.2026-04-14-09");

        String pattern = "log.%d{" + HOURLY + ", UTC}";
        timeBasedRemover(pattern, pattern).clean(NOW);

        assertFalse(archive.exists());
        checker.assertNoMatch("Cannot delete");
        checker.assertNoMatch(".*uncompressed");
    }

    @Test
    public void legacyConstructorIgnoresUncompressedFiles() throws IOException {
        File orphan = create("log.2026-04-14-09");
        File archive = create("log.2026-04-14-09.gz");

        TimeBasedArchiveRemover remover = new TimeBasedArchiveRemover(fnp("log.%d{" + HOURLY + ", UTC}.gz"), rc);
        remover.setContext(context);
        remover.setMaxHistory(2);
        remover.clean(NOW);

        assertFalse(archive.exists());
        assertTrue(orphan.exists());
    }

    @Test
    public void emptiedFolderOfUncompressedFileIsRemoved() throws IOException {
        File orphan = create("2026-04-14-09/log");

        timeBasedRemover("%d{" + HOURLY + ", UTC}/log.gz", "%d{" + HOURLY + ", UTC}/log").clean(NOW);

        assertFalse(orphan.exists());
        assertFalse(orphan.getParentFile().exists());
    }

    @Test
    public void totalSizeCapLeavesUncompressedFilesAlone() throws IOException {
        File active = create("log.2026-04-14-12");
        File beingCompressed = create("log.2026-04-14-11");
        File recentOrphan = create("log.2026-04-14-10");

        TimeBasedArchiveRemover remover = timeBasedRemover("log.%d{" + HOURLY + ", UTC}.gz",
                "log.%d{" + HOURLY + ", UTC}");
        remover.setMaxHistory(5);
        remover.setTotalSizeCap(1);
        remover.capTotalSize(NOW);

        assertTrue(active.exists());
        assertTrue(beingCompressed.exists());
        assertTrue(recentOrphan.exists());
    }

    @Test
    public void sizeAndTimeUncompressedFilesOlderThanMaxHistoryAreRemoved() throws IOException {
        File archive0 = create("log.2026-04-14-09.0.gz");
        File archive1 = create("log.2026-04-14-09.1.gz");
        File orphan2 = create("log.2026-04-14-09.2");
        File recentOrphan = create("log.2026-04-14-11.3");
        File active = create("log.2026-04-14-12.0");

        SizeAndTimeBasedArchiveRemover remover = new SizeAndTimeBasedArchiveRemover(
                fnp("log.%d{" + HOURLY + ", UTC}.%i.gz"), fnp("log.%d{" + HOURLY + ", UTC}.%i"), rc);
        remover.setContext(context);
        remover.setMaxHistory(2);
        remover.clean(NOW);

        assertFalse(archive0.exists());
        assertFalse(archive1.exists());
        assertFalse(orphan2.exists());
        assertTrue(recentOrphan.exists());
        assertTrue(active.exists());
        checker.assertNoMatch("Cannot delete");
        checker.assertMatchCount("deleting historically stale uncompressed file", 1);
    }

    @Test
    public void sizeAndTimeWithoutCompressionDeletesEachFileOnce() throws IOException {
        File archive0 = create("log.2026-04-14-09.0");
        File archive1 = create("log.2026-04-14-09.1");

        String pattern = "log.%d{" + HOURLY + ", UTC}.%i";
        SizeAndTimeBasedArchiveRemover remover = new SizeAndTimeBasedArchiveRemover(fnp(pattern), fnp(pattern), rc);
        remover.setContext(context);
        remover.setMaxHistory(2);
        remover.clean(NOW);

        assertFalse(archive0.exists());
        assertFalse(archive1.exists());
        checker.assertNoMatch("Cannot delete");
        checker.assertNoMatch(".*uncompressed");
    }
}
