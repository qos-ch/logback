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

import ch.qos.logback.classic.LoggerContext;
import com.esotericsoftware.kryo.kryo5.Kryo;
import com.esotericsoftware.kryo.kryo5.KryoException;
import com.esotericsoftware.kryo.kryo5.Serializer;
import com.esotericsoftware.kryo.kryo5.io.Input;
import com.esotericsoftware.kryo.kryo5.minlog.Log;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import static ch.qos.logback.classic.kryo.KryoClassicConstants.*;

public class KryoDecoder {


    Kryo kryo;
    Input input;

    /** Bytes pushed back after a resync found a HEADER inside a previously consumed buffer. */
    byte[] unread;
    int unreadPos;

    KryoEncoder.SerializerType serializerType;
    byte streamVersion;

    /**
     * Open a Kryo stream and configure the decoder from its header.
     */
    public KryoDecoder(File file) throws IOException {
        this.input = new Input(new BufferedInputStream(new FileInputStream(file)));
        readAndApplyHeader();
    }

    /**
     * Open a Kryo stream. The header determines version and serializer type;
     * {@code serializerType} is retained for call-site compatibility and is
     * checked against the header when present.
     */
    public KryoDecoder(KryoEncoder.SerializerType serializerType, File file) throws IOException {
        this(file);
        if (this.serializerType != serializerType) {
            throw new IOException("Stream header serializer type is " + this.serializerType
                    + " but caller requested " + serializerType);
        }
    }

    void readAndApplyHeader() throws IOException {
        if (input.available() < STREAM_HEADER_SIZE) {
            throw new IOException("Stream too short for stream header (need "
                    + STREAM_HEADER_SIZE + " bytes)");
        }
        byte[] rec = input.readBytes(STREAM_HEADER_SIZE);
        if (!StreamFrame.isStreamHeader(rec)) {
            throw new IOException("Invalid stream magic: expected STWD, got "
                    + formatBytes(Arrays.copyOf(rec, STREAM_MAGIC.length)));
        }
        applyHeaderBytes(rec);
    }

    void applyHeaderBytes(byte[] rec) {
        streamVersion = rec[4];
        byte flags = rec[5];
        serializerType = ((flags & FLAG_CUSTOM_SERIALIZER) != 0)
                ? KryoEncoder.SerializerType.Custom
                : KryoEncoder.SerializerType.Vanilla;
        Log.debug("Stream version is " + streamVersion);
        rebuildKryo();
    }

    void rebuildKryo() {
        FieldByteAccumulator saved = currentFieldByteAccumulator();
        kryo = KryoBuilder.build(serializerType, streamVersion);
        FieldByteAccumulator next = currentFieldByteAccumulator();
        if (next != null) {
            next.merge(saved);
        }
    }

    private FieldByteAccumulator currentFieldByteAccumulator() {
        if (kryo == null) {
            return null;
        }
        Serializer<?> serializer = kryo.getSerializer(LoggingEventKryoVO.class);
        if (serializer instanceof FieldByteAccumulatorAware) {
            return ((FieldByteAccumulatorAware) serializer).getFieldByteAccumulator();
        }
        return null;
    }

    public KryoEncoder.SerializerType getSerializerType() {
        return serializerType;
    }

    public byte getStreamVersion() {
        return streamVersion;
    }

    private static String formatBytes(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            if (i > 0) {
                sb.append(' ');
            }
            sb.append(String.format("%02X", bytes[i]));
        }
        return sb.toString();
    }


    /**
     * Outcome of a decode pass: event count and nanoseconds spent reading objects
     * versus invoking the consumer, plus what the decoder met on the way.
     *
     * @param resyncCount   how many times the decoder met damage (a torn or corrupt frame,
     *                      an undecodable event) and had to resynchronise or give up
     * @param endedMidEvent whether the stream ended inside a frame or event
     * @param bytesConsumed bytes of the headers and frames which were decoded successfully
     */
    public static final class DecodeStats {

        private final int count;
        private final long decodeNanos;
        private final long consumeNanos;
        private final int resyncCount;
        private final boolean endedMidEvent;
        private final long bytesConsumed;

        public DecodeStats(int count, long decodeNanos, long consumeNanos,
                           int resyncCount, boolean endedMidEvent, long bytesConsumed) {
            this.count = count;
            this.decodeNanos = decodeNanos;
            this.consumeNanos = consumeNanos;
            this.resyncCount = resyncCount;
            this.endedMidEvent = endedMidEvent;
            this.bytesConsumed = bytesConsumed;
        }

        public int count() {
            return count;
        }

        public long decodeNanos() {
            return decodeNanos;
        }

        public long consumeNanos() {
            return consumeNanos;
        }

        public int resyncCount() {
            return resyncCount;
        }

        public boolean endedMidEvent() {
            return endedMidEvent;
        }

        public long bytesConsumed() {
            return bytesConsumed;
        }

        /** True if some events may be missing: the file was damaged or cut short. */
        public boolean truncated() {
            return resyncCount > 0 || endedMidEvent;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (!(o instanceof DecodeStats))
                return false;
            DecodeStats that = (DecodeStats) o;
            return count == that.count && decodeNanos == that.decodeNanos && consumeNanos == that.consumeNanos
                    && resyncCount == that.resyncCount && endedMidEvent == that.endedMidEvent
                    && bytesConsumed == that.bytesConsumed;
        }

        @Override
        public int hashCode() {
            return java.util.Objects.hash(count, decodeNanos, consumeNanos, resyncCount, endedMidEvent, bytesConsumed);
        }

        @Override
        public String toString() {
            return "DecodeStats[count=" + count + ", decodeNanos=" + decodeNanos + ", consumeNanos=" + consumeNanos
                    + ", resyncCount=" + resyncCount + ", endedMidEvent=" + endedMidEvent
                    + ", bytesConsumed=" + bytesConsumed + "]";
        }
    }

    private int resyncCount;
    private boolean endedMidEvent;
    private long bytesConsumed;

    private DecodeStats stats(int count, long decodeNanos, long consumeNanos) {
        return new DecodeStats(count, decodeNanos, consumeNanos, resyncCount, endedMidEvent, bytesConsumed);
    }

    public DecodeStats decodeWithConsumer(Consumer<LoggingEventKryoVO> consumer) throws IOException {
        Log.trace("Decoding Kryo called with consumer");
        resyncCount = 0;
        endedMidEvent = false;
        bytesConsumed = STREAM_HEADER_SIZE;
        try {
            if (isFramed(streamVersion)) {
                return decodeFramed(consumer);
            }
            return decodeUnframed(consumer);
        } finally {
            input.close();
        }
    }

    private DecodeStats decodeUnframed(Consumer<LoggingEventKryoVO> consumer) throws IOException {
        int count = 0;
        long decodeNanos = 0L;
        long consumeNanos = 0L;
        try {
            while (input.available() > 0) {
                long decodeStart = System.nanoTime();
                LoggingEventKryoVO event = kryo.readObject(input, LoggingEventKryoVO.class);
                long consumeStart = System.nanoTime();
                consumer.accept(event);
                long consumeEnd = System.nanoTime();
                decodeNanos += consumeStart - decodeStart;
                consumeNanos += consumeEnd - consumeStart;
                count++;
                bytesConsumed = input.total();
            }
            return stats(count, decodeNanos, consumeNanos);
        } catch (EOFException | KryoException e) {
            Log.error("KryoDecoder#decodeUnframed: Failed to decode kryo stream count="+count, e);
            // an unframed stream cannot be resynchronised: the rest of it is lost
            resyncCount++;
            endedMidEvent = true;
            return stats(count, decodeNanos, consumeNanos);
        }
    }

    private DecodeStats decodeFramed(Consumer<LoggingEventKryoVO> consumer) throws IOException {
        int count = 0;
        long decodeNanos = 0L;
        long consumeNanos = 0L;
        statsHolder.count = 0;
        try {
            while (true) {
                int before = count;
                boolean more = streamVersion == STREAM_VERSION_5
                        ? decodeOneV5(consumer, statsHolder)
                        : decodeOneV4(consumer, statsHolder);
                if (!more) {
                    break;
                }
                if (statsHolder.count > before) {
                    decodeNanos += statsHolder.decodeNanos;
                    consumeNanos += statsHolder.consumeNanos;
                    count = statsHolder.count;
                }
            }
            return stats(count, decodeNanos, consumeNanos);
        } catch (KryoException e) {
            Log.error("KryoDecoder#decodeFramed: Failed to decode kryo stream count="+count, e);
            resyncCount++;
            return stats(count, decodeNanos, consumeNanos);
        }
    }

    private final FrameLoopStats statsHolder = new FrameLoopStats();

    private static final class FrameLoopStats {
        int count;
        long decodeNanos;
        long consumeNanos;
    }

    private boolean decodeOneV4(Consumer<LoggingEventKryoVO> consumer, FrameLoopStats stats) {
        byte[] rec = readFrameHeader();
        if (rec == null) {
            return false;
        }
        if (StreamFrame.isStreamHeader(rec)) {
            applyHeaderBytes(rec);
            bytesConsumed += rec.length;
            return true;
        }
        if (StreamFrame.isClose(rec)) {
            rebuildKryo();
            bytesConsumed += rec.length;
            return true;
        }
        if (!StreamFrame.isEvent(rec)) {
            Log.error("KryoDecoder: unknown frame type 0x"
                    + Integer.toHexString(rec[0] & 0xFF));
            return resyncToHeader(rec);
        }
        int len = StreamFrame.lengthOf(rec);
        return finishEvent(consumer, stats, rec, len, rec, StreamFrame::crcMatches);
    }

    private boolean decodeOneV5(Consumer<LoggingEventKryoVO> consumer, FrameLoopStats stats) {
        int b0 = readRawByte();
        if (b0 < 0) {
            return false;
        }
        if (b0 == (STREAM_MAGIC[0] & 0xFF)) {
            byte[] rec = new byte[STREAM_HEADER_SIZE];
            rec[0] = (byte) b0;
            int got = readRaw(rec, 1, STREAM_HEADER_SIZE - 1);
            if (got < STREAM_HEADER_SIZE - 1) {
                endedMidEvent = true;
                return false;
            }
            if (StreamFrame.isStreamHeader(rec)) {
                applyHeaderBytes(rec);
                bytesConsumed += rec.length;
                return true;
            }
            return resyncToHeader(rec);
        }
        int type = StreamFrameV5.typeOf(b0);
        ByteArrayOutputStream seed = new ByteArrayOutputStream(8);
        seed.write(b0);
        int len = readUnsignedVarInt(seed);
        if (len < 0) {
            return resyncToHeader(seed.toByteArray());
        }
        byte[] prefix = seed.toByteArray();
        byte[] lenBytes = java.util.Arrays.copyOfRange(prefix, 1, prefix.length);
        if (type == StreamFrameV5.TYPE_CLOSE && len == 0) {
            if (!StreamFrameV5.crcMatches(b0, lenBytes, new byte[0], 0, 0)) {
                Log.error("KryoDecoder: V5 CLOSE CRC mismatch");
                return resyncToHeader(prefix);
            }
            rebuildKryo();
            bytesConsumed += prefix.length;
            return true;
        }
        if (type != StreamFrameV5.TYPE_EVENT) {
            Log.error("KryoDecoder: unknown V5 frame type " + type);
            return resyncToHeader(prefix);
        }
        return finishEventV5(consumer, stats, prefix, b0, lenBytes, len);
    }

    private boolean finishEventV5(Consumer<LoggingEventKryoVO> consumer, FrameLoopStats stats,
                                  byte[] prefix, int packedByte, byte[] lenBytes, int len) {
        if (len > KryoEncoder.MAX_BUFFER_SIZE) {
            Log.error("KryoDecoder: EVENT length " + len + " exceeds max");
            return resyncToHeader(prefix);
        }
        byte[] payload = new byte[len];
        int got = readRaw(payload, 0, len);
        if (got < len) {
            // the input ran out inside the payload
            endedMidEvent = true;
            Log.error("KryoDecoder: truncated EVENT payload count=" + stats.count
                    + " got=" + got + " expected=" + len);
            return resyncToHeader(got <= 0 ? prefix : concat(prefix, payload, got));
        }
        if (StreamFrame.indexOfMagic(payload, 0, payload.length) >= 0) {
            Log.error("KryoDecoder: STWD inside V5 EVENT payload at count=" + stats.count
                    + ", treating as torn write");
            return resyncToHeader(concat(prefix, payload, payload.length));
        }
        if (!StreamFrameV5.crcMatches(packedByte, lenBytes, payload, 0, len)) {
            Log.error("KryoDecoder: EVENT CRC mismatch at count=" + stats.count);
            if (!resyncToHeader(concat(prefix, payload, payload.length))) {
                rebuildKryo();
            }
            return true;
        }
        return acceptEvent(consumer, stats, payload, concat(prefix, payload, payload.length));
    }

    @FunctionalInterface
    private interface CrcCheck {
        boolean test(byte[] rec, byte[] payload);
    }

    private boolean finishEvent(Consumer<LoggingEventKryoVO> consumer, FrameLoopStats stats,
                                byte[] rec, int len, byte[] seed, CrcCheck crc) {
        if (len > KryoEncoder.MAX_BUFFER_SIZE) {
            Log.error("KryoDecoder: EVENT length " + len + " exceeds max");
            return resyncToHeader(seed);
        }
        byte[] payload = new byte[len];
        int got = readRaw(payload, 0, len);
        if (got < len) {
            // the input ran out inside the payload
            endedMidEvent = true;
            Log.error("KryoDecoder: truncated EVENT payload count=" + stats.count
                    + " got=" + got + " expected=" + len);
            return resyncToHeader(got <= 0 ? seed : concat(seed, payload, got));
        }
        if (StreamFrame.indexOfMagic(payload, 0, payload.length) >= 0) {
            Log.error("KryoDecoder: STWD inside EVENT payload at count=" + stats.count
                    + ", treating as torn write");
            return resyncToHeader(concat(seed, payload, payload.length));
        }
        if (!crc.test(rec, payload)) {
            Log.error("KryoDecoder: EVENT CRC mismatch at count=" + stats.count);
            if (!resyncToHeader(concat(seed, payload, payload.length))) {
                rebuildKryo();
            }
            return true;
        }
        return acceptEvent(consumer, stats, payload, concat(seed, payload, payload.length));
    }

    private boolean acceptEvent(Consumer<LoggingEventKryoVO> consumer, FrameLoopStats stats,
                                byte[] payload, byte[] seedOnFail) {
        LoggingEventKryoVO event;
        long decodeStart = System.nanoTime();
        try {
            event = kryo.readObject(new Input(payload), LoggingEventKryoVO.class);
        } catch (RuntimeException e) {
            Log.error("KryoDecoder: EVENT payload decode failed count=" + stats.count, e);
            if (!resyncToHeader(seedOnFail)) {
                rebuildKryo();
            }
            return true;
        }
        long consumeStart = System.nanoTime();
        consumer.accept(event);
        long consumeEnd = System.nanoTime();
        stats.decodeNanos = consumeStart - decodeStart;
        stats.consumeNanos = consumeEnd - consumeStart;
        stats.count++;
        bytesConsumed += seedOnFail.length;
        return true;
    }

    /** @return length, or -1 on EOF/overflow */
    private int readUnsignedVarInt(ByteArrayOutputStream seed) {
        int result = 0;
        int shift = 0;
        while (shift <= 28) {
            int b = readRawByte();
            if (b < 0) {
                endedMidEvent = true;
                return -1;
            }
            seed.write(b);
            result |= (b & 0x7F) << shift;
            if ((b & 0x80) == 0) {
                return result;
            }
            shift += 7;
        }
        return -1;
    }

    private byte[] readFrameHeader() {
        byte[] rec = new byte[STREAM_HEADER_SIZE];
        int got = readRaw(rec, 0, STREAM_HEADER_SIZE);
        if (got == STREAM_HEADER_SIZE) {
            return rec;
        }
        if (got > 0) {
            endedMidEvent = true;
        }
        return null;
    }

    /**
     * Scan {@code seed} then the remaining input for the next {@code STWD} header.
     * Bytes after that header in {@code seed} are pushed back so the next frame
     * read sees them.
     *
     * @return true if a header was found and applied
     */
    boolean resyncToHeader(byte[] seed) {
        resyncCount++;
        byte[] window = seed != null ? seed : new byte[0];
        int idx = StreamFrame.indexOfMagic(window, 0, window.length);
        try {
            while (idx < 0) {
                int b = readRawByte();
                if (b < 0) {
                    return false;
                }
                window = appendByte(window, (byte) b);
                int keep = STREAM_MAGIC.length - 1;
                if (window.length > keep) {
                    window = Arrays.copyOfRange(window, window.length - keep, window.length);
                }
                idx = StreamFrame.indexOfMagic(window, 0, window.length);
            }
            int have = window.length - idx;
            byte[] header = new byte[STREAM_HEADER_SIZE];
            int copy = Math.min(have, STREAM_HEADER_SIZE);
            System.arraycopy(window, idx, header, 0, copy);
            if (have < STREAM_HEADER_SIZE) {
                int need = STREAM_HEADER_SIZE - have;
                if (readRaw(header, have, need) != need) {
                    return false;
                }
            } else if (have > STREAM_HEADER_SIZE) {
                prependUnread(Arrays.copyOfRange(window, idx + STREAM_HEADER_SIZE, window.length));
            }
            if (!StreamFrame.isStreamHeader(header)) {
                return false;
            }
            applyHeaderBytes(header);
            bytesConsumed += header.length;
            return true;
        } catch (KryoException e) {
            return false;
        }
    }

    /**
     * Fills {@code dest[off..off+len)} from the unread pushback buffer, then from
     * {@link Input#read(byte[], int, int)}. Returns the number of bytes actually copied
     * (less than {@code len} on EOF). Used for EVENT payloads and 6-byte frame headers.
     * Resync scanning still uses {@link #readRawByte()}.
     */
    private int readRaw(byte[] dest, int off, int len) {
        int n = 0;
        if (unread != null && unreadPos < unread.length) {
            int take = Math.min(len, unread.length - unreadPos);
            System.arraycopy(unread, unreadPos, dest, off, take);
            unreadPos += take;
            n += take;
            if (unreadPos >= unread.length) {
                unread = null;
                unreadPos = 0;
            }
            if (n == len) {
                return n;
            }
        }
        try {
            while (n < len) {
                int r = input.read(dest, off + n, len - n);
                if (r <= 0) {
                    break;
                }
                n += r;
            }
        } catch (KryoException e) {
            // return bytes already copied
        }
        return n;
    }

    private int readRawByte() {
        if (unread != null && unreadPos < unread.length) {
            int b = unread[unreadPos++] & 0xFF;
            if (unreadPos >= unread.length) {
                unread = null;
                unreadPos = 0;
            }
            return b;
        }
        try {
            return input.readByte() & 0xFF;
        } catch (KryoException e) {
            return -1;
        }
    }

    private void prependUnread(byte[] extra) {
        if (extra == null || extra.length == 0) {
            return;
        }
        if (unread == null || unreadPos >= unread.length) {
            unread = extra;
            unreadPos = 0;
            return;
        }
        byte[] left = Arrays.copyOfRange(unread, unreadPos, unread.length);
        unread = StreamFrame.concat(extra, left);
        unreadPos = 0;
    }

    private static byte[] concat(byte[] header, byte[] payload, int payloadLen) {
        byte[] out = new byte[header.length + payloadLen];
        System.arraycopy(header, 0, out, 0, header.length);
        System.arraycopy(payload, 0, out, header.length, payloadLen);
        return out;
    }

    private static byte[] appendByte(byte[] src, byte b) {
        byte[] out = Arrays.copyOf(src, src.length + 1);
        out[src.length] = b;
        return out;
    }

    public List<LoggingEventKryoVO> decodeAsList() throws IOException {
        List<LoggingEventKryoVO> loggerEventKryoVOList = new ArrayList<>();
        Log.trace("input.available()=" + input.available());
        ListConsumer listConsumer = new ListConsumer(loggerEventKryoVOList);

        DecodeStats stats = decodeWithConsumer(listConsumer);
        Log.trace("decodeAsList "+stats.count()+" logging events from kryo stream");
        return loggerEventKryoVOList;
    }


    public DecodeStats decodeAndAppend(LoggerContext loggerContext) throws IOException {
        LoggingConsumer loggingConsumer = new LoggingConsumer(loggerContext);
        return decodeWithConsumer(loggingConsumer);
    }

}
