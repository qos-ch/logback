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

import ch.qos.logback.core.kryo.SerializerBase;
import com.esotericsoftware.kryo.kryo5.Kryo;
import com.esotericsoftware.kryo.kryo5.io.Input;
import com.esotericsoftware.kryo.kryo5.io.Output;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

/**
 * {@link String} serializer that DEFLATE-compresses <em>long new</em> string payloads as one
 * raw (nowrap) deflate stream for the life of this serializer instance.
 *
 * <p>New values shorter than {@link #SIZE_THRESHOLD_FOR_COMPRESSION} UTF-8 bytes are written
 * unmodified ({@code 'U'} + Kryo string). Longer new values are appended to the ongoing
 * deflate stream ({@code 'C'} + length-prefixed {@link Deflater#SYNC_FLUSH} chunk). Cached
 * hits do not touch the deflater. Short and long new entries may be interleaved; only
 * {@code 'C'} records advance compressor/decompressor state.
 *
 * <p>Uncompressed framing inside the deflate stream is {@code int utf8Length} (big-endian)
 * + UTF-8 bytes. There is no gzip/zlib wrapper; record boundaries are the outer
 * {@code chunkLength} plus {@code SYNC_FLUSH}.
 *
 * <p>Wire format per new entry:
 * <pre>
 *   'U'  Kryo string
 *   'C'  int chunkLength; byte[chunkLength]  // next SYNC_FLUSH deflate segment
 * </pre>
 */
public class MultiEventCompressedStringSerializer extends SerializerBase<String> {

    static int SIZE_THRESHOLD_FOR_COMPRESSION = 256;
    static byte UNMODIFIED = 'U';
    static byte COMPRESSED = 'C';

    private static final int DEFLATE_BUF = 512;

    /** Accumulated (uncompressed UTF-8 size − compressed chunk size) for logging/metrics. */
    private int totalGain = 0;

    private DrainableByteArrayOutputStream writeCapture;
    private Deflater deflater;
    private byte[] deflateBuf;
    private byte[] writeUncompressed;

    private Inflater inflater;
    private byte[] inflateBuf;
    private final ByteArrayOutputStream inflated = new ByteArrayOutputStream(DEFLATE_BUF);
    private final ReusableUtf8 utf8 = new ReusableUtf8();

    public MultiEventCompressedStringSerializer(int capacity) {
        super(capacity);
    }

    public MultiEventCompressedStringSerializer(BiMapType biMapType, int capacity) {
        super(biMapType, capacity);
    }

    @Override
    protected void writeNewEntry(Kryo kryo, Output output, String value) {
        if (value == null) {
            throw new IllegalArgumentException("Null values are not permitted");
        }
        // UTF-8 is at most 3 bytes/char: skip encoding when the upper bound is under the threshold.
        if ((long) value.length() * ReusableUtf8.MAX_BYTES_PER_CHAR < SIZE_THRESHOLD_FOR_COMPRESSION) {
            output.writeByte(UNMODIFIED);
            output.writeString(value);
            return;
        }
        ensureWriteUncompressed(4 + utf8.maxEncodedLength(value.length()));
        int utf8Len = utf8.encode(value, writeUncompressed, 4);
        if (utf8Len < SIZE_THRESHOLD_FOR_COMPRESSION) {
            output.writeByte(UNMODIFIED);
            output.writeString(value);
            return;
        }
        output.writeByte(COMPRESSED);
        writeCompressed(output, utf8Len);
    }

    @Override
    protected String readNewEntry(Kryo kryo, Input input) {
        byte flag = input.readByte();
        if (flag == UNMODIFIED) {
            return input.readString();
        }
        if (flag != COMPRESSED) {
            throw new RuntimeException("MultiEventStringSerializer: unknown entry flag " + flag);
        }
        try {
            return readCompressed(input);
        } catch (DataFormatException | IOException e) {
            throw new RuntimeException("MultiEventStringSerializer read failed", e);
        }
    }

    private void writeCompressed(Output output, int len) {
        ensureWritePipeline();
        writeUncompressed[0] = (byte) (len >>> 24);
        writeUncompressed[1] = (byte) (len >>> 16);
        writeUncompressed[2] = (byte) (len >>> 8);
        writeUncompressed[3] = (byte) len;
        deflater.setInput(writeUncompressed, 0, 4 + len);

        int n;
        do {
            n = deflater.deflate(deflateBuf, 0, deflateBuf.length, Deflater.SYNC_FLUSH);
            if (n > 0) {
                writeCapture.write(deflateBuf, 0, n);
            }
        } while (n == deflateBuf.length);

        totalGain += len - writeCapture.size();
        writeCapture.drainTo(output);
    }

    private String readCompressed(Input input) throws DataFormatException, IOException {
        int chunkLength = input.readInt();
        byte[] chunk = input.readBytes(chunkLength);
        ensureReadPipeline();
        inflater.setInput(chunk);
        while (!inflater.needsInput()) {
            int n = inflater.inflate(inflateBuf);
            if (n <= 0) {
                break;
            }
            inflated.write(inflateBuf, 0, n);
        }
        byte[] all = inflated.toByteArray();
        inflated.reset();
        if (all.length < 4) {
            throw new IOException("Truncated deflate length prefix: got " + all.length + " bytes");
        }
        int utf8Length = ((all[0] & 0xff) << 24)
                | ((all[1] & 0xff) << 16)
                | ((all[2] & 0xff) << 8)
                | (all[3] & 0xff);
        if (utf8Length < 0 || all.length < 4 + utf8Length) {
            throw new IOException("Truncated UTF-8 payload: expected " + utf8Length
                    + " bytes, got " + Math.max(0, all.length - 4));
        }
        if (all.length > 4 + utf8Length) {
            inflated.write(all, 4 + utf8Length, all.length - 4 - utf8Length);
        }
        return utf8.decode(all, 4, utf8Length);
    }

    private void ensureWriteUncompressed(int need) {
        if (writeUncompressed == null || writeUncompressed.length < need) {
            writeUncompressed = new byte[Math.max(need, DEFLATE_BUF)];
        }
    }

    private void ensureWritePipeline() {
        if (deflater != null) {
            return;
        }
        deflater = new Deflater(Deflater.BEST_SPEED, true);
        deflateBuf = new byte[DEFLATE_BUF];
        ensureWriteUncompressed(DEFLATE_BUF);
        writeCapture = new DrainableByteArrayOutputStream();
    }

    private void ensureReadPipeline() {
        if (inflater != null) {
            return;
        }
        inflater = new Inflater(true);
        inflateBuf = new byte[DEFLATE_BUF];
    }

    /**
     * Releases the deflater. Not required for normal record streaming with sync-flush.
     */
    public void finishWrite() {
        if (deflater != null) {
            deflater.end();
            deflater = null;
        }
    }

    public int getTotalGain() {
        return totalGain;
    }

    static final class DrainableByteArrayOutputStream extends ByteArrayOutputStream {
        DrainableByteArrayOutputStream() {
            super(DEFLATE_BUF);
        }

        synchronized void drainTo(Output output) {
            output.writeInt(count);
            output.writeBytes(buf, 0, count);
            reset();
        }
    }
}
