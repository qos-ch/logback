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
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static ch.qos.logback.classic.kryo.KryoClassicConstants.STREAM_MAGIC;
import static ch.qos.logback.classic.kryo.KryoClassicConstants.STREAM_VERSION_5;
import static ch.qos.logback.core.kryo.KryoCoreConstants.KRYO_UNSAFE_PROPERTY_KEY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * V5 framing resilience: malformed or incomplete record sequences that the decoder
 * should still recover from when a later HEADER is present.
 */
public class ResilienceTest {

    LoggerContext loggerContext = new LoggerContext();
    int diff = RandomUtil.getPositiveInt();
    LogbackMDCAdapter logbackMDCAdapter = new LogbackMDCAdapter();

    @BeforeAll
    public static void beforeAll() {
        System.setProperty(KRYO_UNSAFE_PROPERTY_KEY, "false");
    }

    @AfterAll
    public static void afterAll() {
        System.clearProperty(KRYO_UNSAFE_PROPERTY_KEY);
    }

    @BeforeEach
    public void setUp() {
        loggerContext.setName("ResilienceTest");
        loggerContext.setMDCAdapter(logbackMDCAdapter);
    }

    /**
     * HEADER + valid EVENTs, then one truncated EVENT with a bad CRC, no CLOSE,
     * then HEADER + more EVENTs. The decoder must skip the broken record and
     * resume at the second HEADER.
     */
    @Test
    public void v5_incompleteEntry_missingClose_recoversOnNextHeader() throws IOException {
        Logger logger = loggerContext.getLogger(ResilienceTest.class);
        final int firstEpoch = 5;
        final int secondEpoch = 4;

        KryoEncoder first = newEncoder(STREAM_VERSION_5);
        KryoEncoder second = newEncoder(STREAM_VERSION_5);

        List<LoggingEventKryoVO> expected = new ArrayList<>();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.write(first.headerBytes());
        appendEvents(first, logger, 0, firstEpoch, expected, out);
        out.write(incompleteIncorrectEvent());
        // CLOSE omitted on purpose
        out.write(second.headerBytes());
        appendEvents(second, logger, firstEpoch, secondEpoch, expected, out);

        byte[] bytes = out.toByteArray();
        assertEquals(0, StreamFrameV5.countOccurrences(bytes, StreamFrameV5.closeCode()),
                "fixture must not contain CLOSE");
        assertEquals(2, StreamFrameV5.countOccurrences(bytes, STREAM_MAGIC),
                "file header plus mid-stream header");

        Path path = Path.of(KryoClassicTestConstants.TEST_OUTPUT_DIR_PREFIX + diff
                + "/v5-incomplete-missing-close.kryo");
        Files.createDirectories(path.getParent());
        Files.write(path, bytes);

        KryoDecoder decoder = new KryoDecoder(new File(path.toString()));
        assertEquals(STREAM_VERSION_5, decoder.getStreamVersion());
        List<LoggingEventKryoVO> actual = decoder.decodeAsList();
        assertEquals(expected.size(), actual.size());
        assertEquals(expected, actual);
        assertEquals(firstEpoch + secondEpoch, actual.size());
    }

    /** EVENT prefix claiming 80 payload bytes, wrong CRC, only 7 payload bytes. */
    private static byte[] incompleteIncorrectEvent() {
        int claimedLen = 80;
        byte[] payload = new byte[7];
        for (int i = 0; i < payload.length; i++) {
            payload[i] = (byte) (0xA0 + i);
        }
        byte[] lenBytes = StreamFrameV5.encodeUnsignedVarInt(claimedLen);
        int goodCrc = StreamFrameV5.computeCrc(StreamFrameV5.TYPE_EVENT, lenBytes, payload, 0, payload.length);
        int badCrc = (goodCrc + 1) & StreamFrameV5.CRC_MASK;

        byte[] rec = new byte[1 + lenBytes.length + payload.length];
        rec[0] = (byte) StreamFrameV5.pack(StreamFrameV5.TYPE_EVENT, badCrc);
        System.arraycopy(lenBytes, 0, rec, 1, lenBytes.length);
        System.arraycopy(payload, 0, rec, 1 + lenBytes.length, payload.length);
        return rec;
    }

    @Test
    public void v5_missingClose_recoversOnNextHeader() throws IOException {
        Logger logger = loggerContext.getLogger(ResilienceTest.class);
        final int firstEpoch = 5;
        final int secondEpoch = 4;

        KryoEncoder first = newEncoder(STREAM_VERSION_5);
        KryoEncoder second = newEncoder(STREAM_VERSION_5);

        List<LoggingEventKryoVO> expected = new ArrayList<>();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.write(first.headerBytes());
        appendEvents(first, logger, 0, firstEpoch, expected, out);
        out.write(second.headerBytes());
        appendEvents(second, logger, firstEpoch, secondEpoch, expected, out);

        byte[] bytes = out.toByteArray();
        assertEquals(2, StreamFrameV5.countOccurrences(bytes, STREAM_MAGIC));

        Path path = Path.of(KryoClassicTestConstants.TEST_OUTPUT_DIR_PREFIX + diff
                + "/v5-missing-close.kryo");
        Files.createDirectories(path.getParent());
        Files.write(path, bytes);

        KryoDecoder decoder = new KryoDecoder(new File(path.toString()));
        assertEquals(STREAM_VERSION_5, decoder.getStreamVersion());
        List<LoggingEventKryoVO> actual = decoder.decodeAsList();
        assertEquals(expected, actual);
    }

    @Test
    public void v5_tornEventThenAppendedHeader_recoversOnNextHeader() throws IOException {
        Logger logger = loggerContext.getLogger(ResilienceTest.class);
        final int firstEpoch = 3;
        final int secondEpoch = 4;

        KryoEncoder first = newEncoder(STREAM_VERSION_5);
        KryoEncoder second = newEncoder(STREAM_VERSION_5);

        List<LoggingEventKryoVO> expected = new ArrayList<>();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.write(first.headerBytes());
        appendEvents(first, logger, 0, firstEpoch, expected, out);

        LoggingEvent torn = new LoggingEvent("x", logger, Level.INFO,
                "torn-payload-".repeat(20) + "{}", null, new Object[]{99});
        byte[] framed = first.encode(torn);
        int keep = 2 + 10;
        assertTrue(framed.length > keep + 4, "need a payload long enough to tear");
        out.write(framed, 0, keep);

        out.write(second.headerBytes());
        appendEvents(second, logger, firstEpoch, secondEpoch, expected, out);

        Path path = Path.of(KryoClassicTestConstants.TEST_OUTPUT_DIR_PREFIX + diff
                + "/v5-torn-then-append.kryo");
        Files.createDirectories(path.getParent());
        Files.write(path, out.toByteArray());

        KryoDecoder decoder = new KryoDecoder(new File(path.toString()));
        List<LoggingEventKryoVO> actual = decoder.decodeAsList();
        assertEquals(expected, actual);
    }

    /** A clean file reports no damage and consumes every byte. */
    @Test
    public void decodeStats_cleanFile() throws IOException {
        for (byte version : new byte[]{STREAM_VERSION_5}) {
            byte[] bytes = cleanStream(version, 6);
            KryoDecoder.DecodeStats stats = decode(bytes, "clean-" + version + ".kryo");
            assertEquals(6, stats.count());
            assertEquals(0, stats.resyncCount());
            assertEquals(false, stats.endedMidEvent());
            assertEquals(false, stats.truncated());
            assertEquals(bytes.length, stats.bytesConsumed());
        }
    }

    /** A file cut inside its last event, as after a crash, is reported as truncated. */
    @Test
    public void decodeStats_fileEndingMidEvent() throws IOException {
        Logger logger = loggerContext.getLogger(ResilienceTest.class);
        for (byte version : new byte[]{STREAM_VERSION_5}) {
            // The offset of the sixth event is taken from the same stream. The size of an
            // event depends on its timestamp, so a second stream could differ by a byte.
            KryoEncoder encoder = newEncoder(version);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            out.write(encoder.headerBytes());
            appendEvents(encoder, logger, 0, 5, new ArrayList<>(), out);
            int fiveEvents = out.size();
            appendEvents(encoder, logger, 5, 1, new ArrayList<>(), out);
            byte[] clean = out.toByteArray();
            // keep the first five events and half of the sixth
            int cut = fiveEvents + (clean.length - fiveEvents) / 2;
            byte[] bytes = java.util.Arrays.copyOf(clean, cut);
            KryoDecoder.DecodeStats stats = decode(bytes, "cut-" + version + ".kryo");
            assertEquals(5, stats.count(), "version " + version);
            assertTrue(stats.endedMidEvent(), "version " + version);
            assertTrue(stats.truncated(), "version " + version);
            assertEquals(fiveEvents, stats.bytesConsumed(), "version " + version);
        }
    }

    /** Damage in the middle is reported even though decoding resumes at the next header. */
    @Test
    public void decodeStats_damageInTheMiddle() throws IOException {
        Logger logger = loggerContext.getLogger(ResilienceTest.class);
        KryoEncoder first = newEncoder(STREAM_VERSION_5);
        KryoEncoder second = newEncoder(STREAM_VERSION_5);
        List<LoggingEventKryoVO> expected = new ArrayList<>();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.write(first.headerBytes());
        appendEvents(first, logger, 0, 5, expected, out);
        out.write(incompleteIncorrectEvent());
        out.write(second.headerBytes());
        appendEvents(second, logger, 5, 4, expected, out);

        KryoDecoder.DecodeStats stats = decode(out.toByteArray(), "damaged-middle.kryo");
        assertEquals(9, stats.count());
        assertTrue(stats.resyncCount() > 0);
        assertTrue(stats.truncated());
        assertEquals(false, stats.endedMidEvent());
    }

    /** Streams of the removed versions 3 and 4 are rejected with a clear message. */
    @Test
    public void unsupportedStreamVersionIsRejected() throws IOException {
        for (byte version : new byte[]{3, 4}) {
            byte[] header = {STREAM_MAGIC[0], STREAM_MAGIC[1], STREAM_MAGIC[2], STREAM_MAGIC[3],
                    version, KryoClassicConstants.FLAG_CUSTOM_SERIALIZER};
            Path path = Path.of(KryoClassicTestConstants.TEST_OUTPUT_DIR_PREFIX + diff
                    + "/unsupported-v" + version + ".kryo");
            Files.createDirectories(path.getParent());
            Files.write(path, header);
            IOException e = assertThrows(IOException.class, () -> new KryoDecoder(new File(path.toString())));
            assertTrue(e.getMessage().contains("Unsupported stream version"), e.getMessage());
        }
    }

    private byte[] cleanStream(byte version, int events) throws IOException {
        Logger logger = loggerContext.getLogger(ResilienceTest.class);
        KryoEncoder encoder = newEncoder(version);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.write(encoder.headerBytes());
        appendEvents(encoder, logger, 0, events, new ArrayList<>(), out);
        return out.toByteArray();
    }

    private KryoDecoder.DecodeStats decode(byte[] bytes, String name) throws IOException {
        Path path = Path.of(KryoClassicTestConstants.TEST_OUTPUT_DIR_PREFIX + diff + "/" + name);
        Files.createDirectories(path.getParent());
        Files.write(path, bytes);
        return new KryoDecoder(new File(path.toString())).decodeWithConsumer(event -> {
        });
    }

    private KryoEncoder newEncoder(byte streamVersion) {
        KryoEncoder encoder = new KryoEncoder();
        encoder.setContext(loggerContext);
        encoder.setSerializerType(KryoEncoder.SerializerType.Custom);
        encoder.setStreamVersion(streamVersion);
        encoder.start();
        return encoder;
    }

    private static void appendEvents(KryoEncoder encoder, Logger logger, int startIndex, int count,
                                     List<LoggingEventKryoVO> expected, ByteArrayOutputStream out)
            throws IOException {
        for (int i = 0; i < count; i++) {
            int n = startIndex + i;
            LoggingEvent event = new LoggingEvent("x", logger, Level.INFO,
                    "resilience {}", null, new Object[]{n});
            expected.add(new LoggingEventKryoVO(event));
            out.write(encoder.encode(event));
        }
    }
}
