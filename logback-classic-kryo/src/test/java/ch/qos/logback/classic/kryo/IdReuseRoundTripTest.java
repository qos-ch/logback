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

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.classic.util.LogbackMDCAdapter;
import ch.qos.logback.core.testUtil.RandomUtil;
import com.esotericsoftware.kryo.kryo5.minlog.Log;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static ch.qos.logback.classic.kryo.KryoClassicConstants.STREAM_VERSION_5;
import static ch.qos.logback.core.kryo.KryoCoreConstants.KRYO_UNSAFE_PROPERTY_KEY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Round trip of a stream with far more distinct values than a serializer's value-to-id map
 * holds (1,024 entries for the strings shared by messages and MDC values, 1,024 for MDC maps),
 * so that the writer evicts values and reuses their ids. The reader must follow the same eviction order:
 * a drift would make a cached lookup return null or a wrong value.
 */
public class IdReuseRoundTripTest {

    static final int EVENT_COUNT = 20_000;
    static final long BASE_EPOCH_SECOND = 1_763_000_000L;

    LoggerContext loggerContext = new LoggerContext();
    int diff = RandomUtil.getPositiveInt();

    List<String> warnings = new ArrayList<>();
    int savedLevel;
    String savedUnsafe;

    @BeforeEach
    public void setUp() {
        loggerContext.setName("IdReuseRoundTripTest");
        loggerContext.setMDCAdapter(new LogbackMDCAdapter());

        savedUnsafe = System.getProperty(KRYO_UNSAFE_PROPERTY_KEY);
        System.setProperty(KRYO_UNSAFE_PROPERTY_KEY, "false");

        // minlog has no getter for its logger or level: remember the level from its flags
        savedLevel = currentMinlogLevel();
        Log.setLogger(new Log.Logger() {
            @Override
            public void log(int level, String category, String message, Throwable ex) {
                if (level >= Log.LEVEL_WARN) {
                    warnings.add(message + (ex == null ? "" : " " + ex));
                }
            }
        });
        Log.set(Log.LEVEL_WARN);
    }

    @AfterEach
    public void tearDown() {
        Log.setLogger(new Log.Logger());
        Log.set(savedLevel);
        if (savedUnsafe == null) {
            System.clearProperty(KRYO_UNSAFE_PROPERTY_KEY);
        } else {
            System.setProperty(KRYO_UNSAFE_PROPERTY_KEY, savedUnsafe);
        }
    }

    @Test
    public void roundTripPastCapacity() throws IOException {
        roundTrip(KryoClassicConstants.DEFAULT_MAX_EVENTS_BEFORE_RESET, "id-reuse.kryo");
    }

    /** Maps are also reset every 100 events, in the middle of the stream. */
    @Test
    public void roundTripPastCapacityWithFrequentResets() throws IOException {
        roundTrip(100, "id-reuse-reset100.kryo");
    }

    private void roundTrip(int maxEventsBeforeReset, String fileName) throws IOException {
        KryoEncoder encoder = new KryoEncoder();
        encoder.setContext(loggerContext);
        encoder.setSerializerType(KryoEncoder.SerializerType.Custom);
        encoder.setStreamVersion(STREAM_VERSION_5);
        encoder.setMaxEventsBeforeReset(maxEventsBeforeReset);
        encoder.start();

        List<LoggingEventKryoVO> expected = new ArrayList<>(EVENT_COUNT);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.write(encoder.headerBytes());
        for (int i = 0; i < EVENT_COUNT; i++) {
            LoggingEvent event = makeEvent(i);
            expected.add(new LoggingEventKryoVO(event));
            out.write(encoder.encode(event));
        }
        out.write(encoder.footerBytes());

        Path path = Path.of(KryoClassicTestConstants.TEST_OUTPUT_DIR_PREFIX + diff + "/" + fileName);
        Files.createDirectories(path.getParent());
        Files.write(path, out.toByteArray());

        List<LoggingEventKryoVO> actual = new ArrayList<>(EVENT_COUNT);
        KryoDecoder.DecodeStats stats = new KryoDecoder(new File(path.toString())).decodeWithConsumer(actual::add);

        assertEquals(EVENT_COUNT, actual.size());
        for (int i = 0; i < EVENT_COUNT; i++) {
            assertEquals(expected.get(i), actual.get(i), "at index " + i);
        }
        assertEquals(0, stats.resyncCount());
        assertTrue(warnings.isEmpty(), () -> "minlog warnings or errors: " + warnings);
    }

    /**
     * Mostly distinct values. Each event adds about two new strings to the shared string map.
     * From event 10,000 on, every tenth event repeats an earlier message and MDC value: 200
     * events back, still cached, or 10,000 events back, long evicted.
     */
    private LoggingEvent makeEvent(int i) {
        Logger logger = loggerContext.getLogger("id.reuse.L" + (i % 7));
        LoggingEvent event = new LoggingEvent("x", logger, Level.INFO, messageFor(i), null, new Object[]{i});
        event.setThreadName("thread-" + (i % 3));
        // the nanoseconds match the milliseconds, as the V5 format rebuilds the latter from them
        int millis = i % 1000;
        int extraNanos = (i * 7919) % 1_000_000;
        event.setInstant(Instant.ofEpochSecond(BASE_EPOCH_SECOND + i / 1000, millis * 1_000_000L + extraNanos));
        event.setSequenceNumber(i);

        Map<String, String> mdc = new LinkedHashMap<>();
        mdc.put("requestId", "req-" + earlierIndex(i));
        mdc.put("user", "user-" + (i % 50));
        event.setMDCPropertyMap(mdc);
        return event;
    }

    private static String messageFor(int i) {
        return "message " + earlierIndex(i) + " {}";
    }

    private static int earlierIndex(int i) {
        if (i % 10 == 0 && i >= 10_000) {
            return (i % 20 == 0) ? i - 200 : i - 10_000;
        }
        return i;
    }

    private static int currentMinlogLevel() {
        if (Log.TRACE) {
            return Log.LEVEL_TRACE;
        }
        if (Log.DEBUG) {
            return Log.LEVEL_DEBUG;
        }
        if (Log.INFO) {
            return Log.LEVEL_INFO;
        }
        if (Log.WARN) {
            return Log.LEVEL_WARN;
        }
        if (Log.ERROR) {
            return Log.LEVEL_ERROR;
        }
        return Log.LEVEL_NONE;
    }
}
