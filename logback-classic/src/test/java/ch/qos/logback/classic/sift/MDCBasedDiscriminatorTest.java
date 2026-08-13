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
     * Path characters in the MDC value must be removed so the discriminating value
     * is safe to use in file names (e.g. SiftingAppender nested FileAppenders).
     * Multiple and consecutive {@code /} and {@code \} occurrences are all stripped.
     */
    @Test
    public void pathCharactersAreSanitizedFromMdcValue() {
        logbackMDCAdapter.put(key, "a/b\\c" + value);
        event = new LoggingEvent("a", logger, Level.DEBUG, "", null, null);

        String discriminatorValue = discriminator.getDiscriminatingValue(event);
        assertEquals("abc" + value, discriminatorValue);
    }

    @Test
    public void multiplePathCharactersAreAllRemoved() {
        logbackMDCAdapter.put(key, "///foo\\\\bar//baz\\");
        event = new LoggingEvent("a", logger, Level.DEBUG, "", null, null);

        String discriminatorValue = discriminator.getDiscriminatingValue(event);
        assertEquals("foobarbaz", discriminatorValue);
    }

    /**
     * When path characters are stripped, a WARN is emitted, but only through the
     * {@link ch.qos.logback.core.util.BatchedFixedIntervalInvocationGate}: up to
     * four warnings are allowed, then a 10-minute lull suppresses further ones.
     */
    @Test
    public void sanitizationWarnIsGatedByInvocationGate() {
        final String mdcWithPathChars = "a/b\\" + value;
        final String sanitized = "ab" + value;
        final String warnSnippet = "Required sanitizing of path characters from MDC value";
        // Matches the gate configured on MDCBasedDiscriminator (batch of 4, 10-minute lull).
        final int batchSize = 4;
        final long lullMillis = Duration.buildByMinutes(10).getMilliseconds();
        final long t0 = 1_000_000L;

        logbackMDCAdapter.put(key, mdcWithPathChars);
        StatusChecker statusChecker = new StatusChecker(loggerContext);

        for (int i = 0; i < batchSize; i++) {
            assertEquals(sanitized, discriminatingValueAt(t0));
        }
        statusChecker.assertMatchCount(warnSnippet, batchSize);
        statusChecker.assertContainsMatch(Status.WARN, warnSnippet);

        // Still within the lull: further sanitization must not emit more warnings.
        assertEquals(sanitized, discriminatingValueAt(t0));
        assertEquals(sanitized, discriminatingValueAt(t0 + lullMillis - 1));
        statusChecker.assertMatchCount(warnSnippet, batchSize);

        // After the lull, warnings are allowed again (new batch).
        assertEquals(sanitized, discriminatingValueAt(t0 + lullMillis));
        statusChecker.assertMatchCount(warnSnippet, batchSize + 1);
    }

    /**
     * Sanitization is a no-op when the MDC value has no path characters, so no
     * status warning should be produced.
     */
    @Test
    public void noSanitizationWarnWhenMdcValueHasNoPathCharacters() {
        logbackMDCAdapter.put(key, value);
        StatusChecker statusChecker = new StatusChecker(loggerContext);

        assertEquals(value, discriminatingValueAt(1_000L));
        statusChecker.assertMatchCount("Required sanitizing of path characters from MDC value \\[.*\\]", 0);
    }

    private String discriminatingValueAt(long timestamp) {
        event = new LoggingEvent("a", logger, Level.DEBUG, "", null, null);
        event.setTimeStamp(timestamp);
        return discriminator.getDiscriminatingValue(event);
    }
}
