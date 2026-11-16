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
import com.esotericsoftware.kryo.kryo5.KryoException;
import com.esotericsoftware.kryo.kryo5.minlog.Log;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static ch.qos.logback.classic.kryo.KryoClassicConstants.STREAM_HEADER_SIZE;
import static ch.qos.logback.classic.kryo.KryoClassicConstants.STREAM_MAGIC;
import static ch.qos.logback.classic.kryo.KryoClassicConstants.STREAM_VERSION_5;
import static ch.qos.logback.core.kryo.KryoCoreConstants.KRYO_UNSAFE_PROPERTY_KEY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Decoding a framed stream from the start of an epoch, as reported by
 * {@link KryoDecoder.EpochListener}.
 */
@Timeout(30)
public class EpochSeekTest {

    static final int EVENT_COUNT = 1_050;
    static final int EPOCH_SIZE = 100;
    static final int EPOCH_COUNT = 11;
    static final long BASE_EPOCH_SECOND = 1_763_000_000L;

    LoggerContext loggerContext = new LoggerContext();
    int diff = RandomUtil.getPositiveInt();

    int savedLevel;
    String savedUnsafe;

    /** One reported epoch. */
    static final class Epoch {
        final long offset;
        final long firstOrdinal;

        Epoch(long offset, long firstOrdinal) {
            this.offset = offset;
            this.firstOrdinal = firstOrdinal;
        }
    }

    /** The result of a full decode with a listener. */
    static final class Decoded {
        final List<LoggingEventKryoVO> events = new ArrayList<>();
        final List<Epoch> epochs = new ArrayList<>();
        KryoDecoder.DecodeStats stats;
    }

    @BeforeEach
    public void setUp() {
        loggerContext.setName("EpochSeekTest");
        loggerContext.setMDCAdapter(new LogbackMDCAdapter());
        savedUnsafe = System.getProperty(KRYO_UNSAFE_PROPERTY_KEY);
        System.setProperty(KRYO_UNSAFE_PROPERTY_KEY, "false");
        // the damage tests make the decoder log errors on purpose
        savedLevel = currentMinlogLevel();
        Log.set(Log.LEVEL_NONE);
    }

    @AfterEach
    public void tearDown() {
        Log.set(savedLevel);
        if (savedUnsafe == null) {
            System.clearProperty(KRYO_UNSAFE_PROPERTY_KEY);
        } else {
            System.setProperty(KRYO_UNSAFE_PROPERTY_KEY, savedUnsafe);
        }
    }

    @Test
    public void epochsAreReported() throws IOException {
        byte[] bytes = framedStream();
        Decoded full = decode(write(bytes, "epochs.kryo"), 0);

        assertEquals(EVENT_COUNT, full.events.size());
        assertEquals(EPOCH_COUNT, full.epochs.size());
        for (int i = 0; i < EPOCH_COUNT; i++) {
            assertEquals((long) i * EPOCH_SIZE, full.epochs.get(i).firstOrdinal);
            assertMagicAt(bytes, full.epochs.get(i).offset);
        }
        assertEquals(0L, full.epochs.get(0).offset);
        assertFalse(full.stats.truncated());
    }

    @Test
    public void eachEpochDecodesOnItsOwn() throws IOException {
        File file = write(framedStream(), "each-epoch.kryo");
        Decoded full = decode(file, 0);

        for (Epoch epoch : full.epochs) {
            int from = (int) epoch.firstOrdinal;

            // up to the end of the file
            Decoded rest = decode(file, epoch.offset);
            assertEquals(full.events.subList(from, EVENT_COUNT), rest.events, "epoch at " + from);
            assertFalse(rest.stats.truncated());

            // only the epoch's events, stopping early
            int to = Math.min(from + EPOCH_SIZE, EVENT_COUNT);
            List<LoggingEventKryoVO> some = new ArrayList<>();
            KryoDecoder.DecodeStats stats = new KryoDecoder(file, epoch.offset).decodeWhile(event -> {
                some.add(event);
                return some.size() < to - from;
            });
            assertEquals(full.events.subList(from, to), some, "epoch at " + from);
            assertEquals(to - from, stats.count());
            assertEquals(0, stats.resyncCount());
            assertFalse(stats.truncated());
        }
    }

    @Test
    public void listenerOfDecoderOpenedAtOffset() throws IOException {
        File file = write(framedStream(), "listener-at-offset.kryo");
        Decoded full = decode(file, 0);
        Epoch fifth = full.epochs.get(5);

        Decoded fromFifth = decode(file, fifth.offset);
        assertEquals(EPOCH_COUNT - 5, fromFifth.epochs.size());
        for (int i = 0; i < fromFifth.epochs.size(); i++) {
            Epoch expected = full.epochs.get(5 + i);
            Epoch actual = fromFifth.epochs.get(i);
            assertEquals(expected.offset, actual.offset);
            assertEquals(expected.firstOrdinal - fifth.firstOrdinal, actual.firstOrdinal);
        }
    }

    @Test
    public void inputStreamConstructor() throws IOException {
        byte[] bytes = framedStream();
        Decoded full = decode(write(bytes, "input-stream.kryo"), 0);
        Epoch third = full.epochs.get(3);
        byte[] tail = Arrays.copyOfRange(bytes, (int) third.offset, bytes.length);

        // available() returns 0, as it may on a socket before the data arrives
        ByteArrayInputStream in = new ByteArrayInputStream(tail) {
            @Override
            public synchronized int available() {
                return 0;
            }
        };
        KryoDecoder decoder = new KryoDecoder(in);
        List<Epoch> epochs = new ArrayList<>();
        decoder.setEpochListener((offset, firstOrdinal) -> epochs.add(new Epoch(offset, firstOrdinal)));
        List<LoggingEventKryoVO> events = new ArrayList<>();
        decoder.decodeWithConsumer(events::add);

        assertEquals(full.events.subList((int) third.firstOrdinal, EVENT_COUNT), events);
        // offsets are relative to the start of the stream handed to the constructor
        assertEquals(0L, epochs.get(0).offset);
        assertEquals(full.epochs.get(4).offset - third.offset, epochs.get(1).offset);
    }

    @Test
    public void wrongOffsetIsRejected() throws IOException {
        File file = write(framedStream(), "wrong-offset.kryo");
        Decoded full = decode(file, 0);
        long inside = full.epochs.get(2).offset + STREAM_HEADER_SIZE + 3;

        IOException e = assertThrows(IOException.class, () -> new KryoDecoder(file, inside));
        assertTrue(e.getMessage().contains("Invalid stream magic"), e.getMessage());
    }

    /**
     * A single-bit error in the first payload byte of an event in epoch 3 fails its CRC. The
     * rest of epoch 3 is lost, and the decoder finds epoch 4 by resync.
     */
    @Test
    public void damagedEpochIsSkippedAndLaterEpochsAreReportedAtTheirPositions() throws IOException {
        byte[] bytes = framedStream();
        Decoded clean = decode(write(bytes, "clean.kryo"), 0);

        byte[] damaged = bytes.clone();
        long firstEventOfEpoch3 = clean.epochs.get(3).offset + STREAM_HEADER_SIZE;
        damaged[payloadStart(damaged, (int) firstEventOfEpoch3)] ^= 0x01;
        File damagedFile = write(damaged, "damaged.kryo");
        Decoded full = decode(damagedFile, 0);

        assertTrue(full.stats.resyncCount() > 0);
        assertEquals(EVENT_COUNT - EPOCH_SIZE, full.events.size());
        // every header is reported at its true position, epoch 4 after a resync
        assertEquals(EPOCH_COUNT, full.epochs.size());
        for (int i = 0; i < EPOCH_COUNT; i++) {
            assertEquals(clean.epochs.get(i).offset, full.epochs.get(i).offset);
            assertMagicAt(damaged, full.epochs.get(i).offset);
        }
        assertEquals(3L * EPOCH_SIZE, full.epochs.get(4).firstOrdinal);

        Decoded fromFourth = decode(damagedFile, full.epochs.get(4).offset);
        assertEquals(clean.events.subList(4 * EPOCH_SIZE, EVENT_COUNT), fromFourth.events);
        assertFalse(fromFourth.stats.truncated());
    }

    /**
     * A torn event directly followed by the next epoch's header: the event's declared length
     * swallows the header, which the decoder finds again in the bytes it already read.
     */
    @Test
    public void headerSwallowedByTornEvent() throws IOException {
        byte[] bytes = framedStream();
        Decoded clean = decode(write(bytes, "before-tear.kryo"), 0);
        int header3 = (int) clean.epochs.get(3).offset;
        int cut = tornLastEventOfEpoch(bytes, (int) clean.epochs.get(2).offset);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.write(bytes, 0, cut);
        out.write(bytes, header3, bytes.length - header3);
        byte[] torn = out.toByteArray();
        Decoded full = decode(write(torn, "torn.kryo"), 0);

        assertTrue(full.stats.resyncCount() > 0);
        Epoch reported = full.epochs.get(3);
        assertEquals(cut, reported.offset);
        assertMagicAt(torn, reported.offset);
        assertEquals(3L * EPOCH_SIZE - 1, reported.firstOrdinal);
        for (Epoch epoch : full.epochs) {
            assertMagicAt(torn, epoch.offset);
        }
        assertEquals(clean.events.subList(3 * EPOCH_SIZE, EVENT_COUNT),
                full.events.subList(3 * EPOCH_SIZE - 1, full.events.size()));
    }

    /**
     * As above, but an epoch with no events follows the torn event: the second header's bytes
     * are then served from the bytes the resync pushed back.
     */
    @Test
    public void headerServedFromPushedBackBytes() throws IOException {
        byte[] bytes = framedStream();
        Decoded clean = decode(write(bytes, "before-tear2.kryo"), 0);
        int header3 = (int) clean.epochs.get(3).offset;
        int cut = tornLastEventOfEpoch(bytes, (int) clean.epochs.get(2).offset);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.write(bytes, 0, cut);
        out.write(bytes, header3, STREAM_HEADER_SIZE); // an empty epoch
        out.write(bytes, header3, bytes.length - header3);
        byte[] torn = out.toByteArray();
        Decoded full = decode(write(torn, "torn-empty-epoch.kryo"), 0);

        assertEquals(EPOCH_COUNT + 1, full.epochs.size());
        assertEquals(cut, full.epochs.get(3).offset);
        assertEquals(cut + STREAM_HEADER_SIZE, full.epochs.get(4).offset);
        assertEquals(full.epochs.get(3).firstOrdinal, full.epochs.get(4).firstOrdinal);
        for (Epoch epoch : full.epochs) {
            assertMagicAt(torn, epoch.offset);
        }
        assertEquals(clean.events.subList(3 * EPOCH_SIZE, EVENT_COUNT),
                full.events.subList(3 * EPOCH_SIZE - 1, full.events.size()));
    }

    @Test
    public void unframedStream() throws IOException {
        KryoEncoder encoder = new KryoEncoder();
        encoder.setContext(loggerContext);
        encoder.setSerializerType(KryoEncoder.SerializerType.Vanilla);
        encoder.start();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.write(encoder.headerBytes());
        for (int i = 0; i < 20; i++) {
            out.write(encoder.encode(makeEvent(i)));
        }
        out.write(encoder.footerBytes());
        byte[] bytes = out.toByteArray();

        Decoded atZero = decode(write(bytes, "vanilla.kryo"), 0);
        assertEquals(20, atZero.events.size());
        assertEquals(1, atZero.epochs.size());
        assertEquals(0L, atZero.epochs.get(0).offset);
        assertEquals(0L, atZero.epochs.get(0).firstOrdinal);

        // a valid header, but at an offset other than 0
        byte[] prefixed = new byte[7 + bytes.length];
        System.arraycopy(bytes, 0, prefixed, 7, bytes.length);
        File prefixedFile = write(prefixed, "vanilla-prefixed.kryo");
        IOException e = assertThrows(IOException.class, () -> new KryoDecoder(prefixedFile, 7));
        assertTrue(e.getMessage().contains("not framed"), e.getMessage());
    }

    @Test
    public void exceptionFromPredicatePropagates() throws IOException {
        File file = write(framedStream(), "throwing.kryo");
        KryoException thrown = new KryoException("from the consumer");
        KryoException caught = assertThrows(KryoException.class,
                () -> new KryoDecoder(file).decodeWithConsumer(event -> {
                    throw thrown;
                }));
        assertSame(thrown, caught);
    }

    // ------------------------------------------------------------------

    private byte[] framedStream() throws IOException {
        KryoEncoder encoder = new KryoEncoder();
        encoder.setContext(loggerContext);
        encoder.setSerializerType(KryoEncoder.SerializerType.Custom);
        encoder.setStreamVersion(STREAM_VERSION_5);
        encoder.setMaxEventsBeforeReset(EPOCH_SIZE);
        encoder.start();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.write(encoder.headerBytes());
        for (int i = 0; i < EVENT_COUNT; i++) {
            out.write(encoder.encode(makeEvent(i)));
        }
        out.write(encoder.footerBytes());
        return out.toByteArray();
    }

    private LoggingEvent makeEvent(int i) {
        Logger logger = loggerContext.getLogger("epoch.seek.L" + (i % 5));
        LoggingEvent event = new LoggingEvent("x", logger, Level.INFO, "message " + i + " {}", null,
                new Object[]{i});
        event.setThreadName("thread-" + (i % 3));
        // the nanoseconds match the milliseconds, as the V5 format rebuilds the latter from them
        int millis = i % 1000;
        int extraNanos = (i * 7919) % 1_000_000;
        event.setInstant(Instant.ofEpochSecond(BASE_EPOCH_SECOND + i / 1000, millis * 1_000_000L + extraNanos));
        event.setSequenceNumber(i);
        // HashMap: the Vanilla format registers it, not LinkedHashMap
        Map<String, String> mdc = new HashMap<>();
        mdc.put("requestId", "req-" + i);
        event.setMDCPropertyMap(mdc);
        return event;
    }

    private File write(byte[] bytes, String name) throws IOException {
        Path path = Path.of(KryoClassicTestConstants.TEST_OUTPUT_DIR_PREFIX + diff + "/" + name);
        Files.createDirectories(path.getParent());
        Files.write(path, bytes);
        return path.toFile();
    }

    private static Decoded decode(File file, long offset) throws IOException {
        Decoded decoded = new Decoded();
        KryoDecoder decoder = new KryoDecoder(file, offset);
        decoder.setEpochListener((o, firstOrdinal) -> decoded.epochs.add(new Epoch(o, firstOrdinal)));
        decoded.stats = decoder.decodeWithConsumer(decoded.events::add);
        return decoded;
    }

    private static void assertMagicAt(byte[] bytes, long offset) {
        byte[] found = Arrays.copyOfRange(bytes, (int) offset, (int) offset + STREAM_MAGIC.length);
        assertTrue(Arrays.equals(STREAM_MAGIC, found), "no STWD at offset " + offset);
    }

    /** Offset of the payload of the frame starting at {@code frame}: type byte, then varint length. */
    private static int payloadStart(byte[] bytes, int frame) {
        int p = frame + 1;
        while ((bytes[p] & 0x80) != 0) {
            p++;
        }
        return p + 1;
    }

    private static int payloadLength(byte[] bytes, int frame) {
        int result = 0;
        int shift = 0;
        int p = frame + 1;
        while (true) {
            int b = bytes[p++] & 0xFF;
            result |= (b & 0x7F) << shift;
            if ((b & 0x80) == 0) {
                return result;
            }
            shift += 7;
        }
    }

    /**
     * Walks the frames of the epoch whose header is at {@code header} up to its CLOSE frame and
     * returns where to cut the epoch's last event: one payload byte after its prefix.
     */
    private static int tornLastEventOfEpoch(byte[] bytes, int header) {
        int p = header + STREAM_HEADER_SIZE;
        int lastEvent = -1;
        while (StreamFrameV5.typeOf(bytes[p] & 0xFF) == StreamFrameV5.TYPE_EVENT) {
            lastEvent = p;
            p = payloadStart(bytes, p) + payloadLength(bytes, p);
        }
        assertEquals(StreamFrameV5.TYPE_CLOSE, StreamFrameV5.typeOf(bytes[p] & 0xFF));
        assertTrue(payloadLength(bytes, lastEvent) > 2 * STREAM_HEADER_SIZE + 2,
                "the torn event must be long enough to swallow two headers");
        return payloadStart(bytes, lastEvent) + 1;
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
