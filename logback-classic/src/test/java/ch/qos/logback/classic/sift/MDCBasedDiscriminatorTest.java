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
package ch.qos.logback.classic.sift;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.classic.util.LogbackMDCAdapter;
import ch.qos.logback.core.status.Status;
import ch.qos.logback.core.status.testUtil.StatusChecker;
import ch.qos.logback.core.testUtil.RandomUtil;
import ch.qos.logback.core.util.Duration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Ceki G&uuml;lc&uuml;
 */
public class MDCBasedDiscriminatorTest {

    static String DEFAULT_VAL = "DEFAULT_VAL";

    MDCBasedDiscriminator discriminator = new MDCBasedDiscriminator();
    LoggerContext loggerContext = new LoggerContext();
    LogbackMDCAdapter logbackMDCAdapter = new LogbackMDCAdapter();
    Logger logger = loggerContext.getLogger(this.getClass());

    int diff = RandomUtil.getPositiveInt();
    String key = "MDCBasedDiscriminatorTest_key" + diff;
    String value = "MDCBasedDiscriminatorTest_val" + diff;
    LoggingEvent event;

    @BeforeEach
    public void setUp() {
        loggerContext.setMDCAdapter(logbackMDCAdapter);
        discriminator.setContext(loggerContext);
        discriminator.setKey(key);
        discriminator.setDefaultValue(DEFAULT_VAL);
        discriminator.start();
        assertTrue(discriminator.isStarted());
    }

    @AfterEach
    public void tearDown() {
    }

    @Test
    public void smoke() {
        logbackMDCAdapter.put(key, value);
        event = new LoggingEvent("a", logger, Level.DEBUG, "", null, null);

        String discriminatorValue = discriminator.getDiscriminatingValue(event);
        // Clean MDC value should not be touched
        assertEquals(value, discriminatorValue);
    }

    @Test
    public void nullMDC() {
        event = new LoggingEvent("a", logger, Level.DEBUG, "", null, null);
        assertEquals(new HashMap<String, String>(), event.getMDCPropertyMap());
        String discriminatorValue = discriminator.getDiscriminatingValue(event);
        assertEquals(DEFAULT_VAL, discriminatorValue);
    }

    /**
     * Path characters in the MDC value cause the value to be rejected so that the
     * discriminating value is safe to use in file names (e.g. SiftingAppender
     * nested FileAppenders).
     */
    @Test
    public void pathCharactersYieldDefaultValue() {
        logbackMDCAdapter.put(key, "a/b\\c" + value);
        event = new LoggingEvent("a", logger, Level.DEBUG, "", null, null);
        assertEquals(DEFAULT_VAL, discriminator.getDiscriminatingValue(event));

        logbackMDCAdapter.put(key, "///foo\\\\bar//baz\\");
        event = new LoggingEvent("a", logger, Level.DEBUG, "", null, null);
        assertEquals(DEFAULT_VAL, discriminator.getDiscriminatingValue(event));
    }

    /**
     * When a value is rejected, a WARN is emitted, but only through the
     * {@link ch.qos.logback.core.util.BatchedFixedIntervalInvocationGate}: up to
     * four warnings are allowed, then a 10-minute lull suppresses further ones.
     */
    @Test
    public void rejectionWarnIsGatedByInvocationGate() {
        final String mdcWithPathChars = "a/b\\" + value;
        final String warnSnippet = "MDC value \\[.*\\] contains forbidden characters";
        // Matches the gate configured on MDCBasedDiscriminator (batch of 4, 10-minute lull).
        final int batchSize = 4;
        final long lullMillis = Duration.buildByMinutes(10).getMilliseconds();
        final long t0 = 1_000_000L;

        logbackMDCAdapter.put(key, mdcWithPathChars);
        StatusChecker statusChecker = new StatusChecker(loggerContext);

        for (int i = 0; i < batchSize; i++) {
            assertEquals(DEFAULT_VAL, discriminatingValueAt(t0));
        }
        statusChecker.assertMatchCount(warnSnippet, batchSize);
        statusChecker.assertContainsMatch(Status.WARN, warnSnippet);

        // Still within the lull: further rejections must not emit more warnings.
        assertEquals(DEFAULT_VAL, discriminatingValueAt(t0));
        assertEquals(DEFAULT_VAL, discriminatingValueAt(t0 + lullMillis - 1));
        statusChecker.assertMatchCount(warnSnippet, batchSize);

        // After the lull, warnings are allowed again (new batch).
        assertEquals(DEFAULT_VAL, discriminatingValueAt(t0 + lullMillis));
        statusChecker.assertMatchCount(warnSnippet, batchSize + 1);
    }

    /**
     * Clean MDC values, including ones with single dots, are returned unchanged
     * and no status warning should be produced.
     */
    @Test
    public void noWarnWhenMdcValueIsClean() {
        StatusChecker statusChecker = new StatusChecker(loggerContext);
        String[] cleanValues = { value, "a.b", "host.example.com", "192.168.0.1", ".hidden", "trailing." };
        for (String clean : cleanValues) {
            logbackMDCAdapter.put(key, clean);
            assertEquals(clean, discriminatingValueAt(1_000L));
        }
        statusChecker.assertIsWarningOrErrorFree();
    }

    /**
     * MDC values containing any of the characters in
     * {@link MDCBasedDiscriminator#FORBIDDEN_CHARACTERS}, at any position, or the
     * sequence '..' are rejected and the default value is returned instead.
     */
    @Test
    public void forbiddenCharactersYieldDefaultValue() {
        String[] forbiddenValues = { "..", "...", "a..b", "..a", "a..", "../" + value, "..${file.separator}" + value,
                "${file.separator}", "a/b.c", "a.\\.b" };
        for (String forbidden : forbiddenValues) {
            logbackMDCAdapter.put(key, forbidden);
            assertEquals(DEFAULT_VAL, discriminatingValueAt(1_000L), forbidden);
        }

        for (char c : MDCBasedDiscriminator.FORBIDDEN_CHARACTERS.toCharArray()) {
            for (String forbidden : new String[] { c + "a", "a" + c + "b", "a" + c }) {
                logbackMDCAdapter.put(key, forbidden);
                assertEquals(DEFAULT_VAL, discriminatingValueAt(1_000L), forbidden);
            }
        }
    }

    @Test
    public void emptyValueYieldsDefaultValue() {
        StatusChecker statusChecker = new StatusChecker(loggerContext);
        logbackMDCAdapter.put(key, "");
        assertEquals(DEFAULT_VAL, discriminatingValueAt(1_000L));
        statusChecker.assertContainsMatch(Status.WARN, "MDC value \\[\\] is empty");
    }

    /**
     * Values up to 64 characters are accepted, longer ones are rejected with a
     * warning.
     */
    @Test
    public void overlyLongValueYieldsDefaultValue() {
        StatusChecker statusChecker = new StatusChecker(loggerContext);
        String maxLengthValue = "a".repeat(64);
        logbackMDCAdapter.put(key, maxLengthValue);
        assertEquals(maxLengthValue, discriminatingValueAt(1_000L));
        statusChecker.assertIsWarningOrErrorFree();

        logbackMDCAdapter.put(key, maxLengthValue + "a");
        assertEquals(DEFAULT_VAL, discriminatingValueAt(1_000L));
        statusChecker.assertContainsMatch(Status.WARN, "MDC value of length 65 is longer than the maximum");
    }

    @Test
    public void sanitizePathCharactersReturnsNullOnForbiddenCharacters() {
        assertNull(discriminator.sanitizePathCharacters("..", 1_000L));
        assertNull(discriminator.sanitizePathCharacters("${file.separator}", 1_000L));
        assertNull(discriminator.sanitizePathCharacters("a/b", 1_000L));
        assertNull(discriminator.sanitizePathCharacters("a\\b", 1_000L));
        assertEquals("a.b", discriminator.sanitizePathCharacters("a.b", 1_000L));
    }

    @Test
    public void rejectedValueEmitsWarning() {
        StatusChecker statusChecker = new StatusChecker(loggerContext);
        logbackMDCAdapter.put(key, "..");
        assertEquals(DEFAULT_VAL, discriminatingValueAt(1_000L));
        statusChecker.assertContainsMatch(Status.WARN, "MDC value \\[\\.\\.\\] contains forbidden characters");
    }

    private String discriminatingValueAt(long timestamp) {
        event = new LoggingEvent("a", logger, Level.DEBUG, "", null, null);
        event.setTimeStamp(timestamp);
        return discriminator.getDiscriminatingValue(event);
    }
}
