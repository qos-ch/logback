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
import com.esotericsoftware.kryo.kryo5.minlog.Log;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class SingleItemCompressingStringSerializer extends SerializerBase<String> {

    static int SIZE_THRESHOLD_FOR_COMPRESSION = 256;
    static byte UNMODIFIED = 'U';
    static byte COMPRESSED = 'C';

    int totalGain = 0;
    private final ReusableUtf8 utf8 = new ReusableUtf8();

    public SingleItemCompressingStringSerializer(int capacity) {
        super(capacity);
    }

    public SingleItemCompressingStringSerializer(SerializerBase.BiMapType biMapType, int capacity) {
        super(biMapType, capacity);
    }

    @Override
    protected void writeNewEntry(Kryo kryo, Output output, String value) {
        Log.trace("writeNewEntry for String: " + value);

        // assume nulls are not permitted
        if (value == null) {
            throw new IllegalArgumentException("Null values are not permitted");
        }

        int length = value.length();

        if (length < SIZE_THRESHOLD_FOR_COMPRESSION) {
            outputAsString(output, value);
        } else {
            int utf8Len = utf8.encode(value);
            byte[] compressedBytes = gzipCompress(utf8.buffer(), 0, utf8Len);
            if (compressedBytes.length >= utf8Len) {
                // compression not effective
                outputAsString(output, value);
                Log.trace("String compression not effective, writing unmodified string: " + value);
            } else {
                totalGain += utf8Len - compressedBytes.length;
                Log.debug("Writing compressed string: "+compressedBytes+" instead of  "+utf8Len+" totalGain="+totalGain+" bytes");
                output.writeByte(COMPRESSED);
                output.writeInt(compressedBytes.length);
                output.writeBytes(compressedBytes);
            }
        }

    }

    private static void outputAsString(Output output, String value) {
        output.writeByte(UNMODIFIED);
        output.writeString(value);
    }

    @Override
    protected String readNewEntry(Kryo kryo, Input input) {

        byte entryType = input.readByte();

        if (entryType == UNMODIFIED) {
            String value = input.readString();
            Log.trace("readNewEntry for String: " + value);
            return value;
        } else { // COMPRESSED
            int compressedLength = input.readInt();
            byte[] compressedBytes = input.readBytes(compressedLength);
            byte[] decompressedBytes = decompressGzip(compressedBytes);
            String value = utf8.decode(decompressedBytes, 0, decompressedBytes.length);
            Log.trace("readNewEntry for Compressed String: " + value);
            return value;

        }


    }

    private byte[] decompressGzip(byte[] compressedBytes) {

        ByteArrayInputStream bais = new ByteArrayInputStream(compressedBytes);
        try (GZIPInputStream gzip = new GZIPInputStream(bais)) {
            byte[] result = gzip.readAllBytes();
            return result;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static byte[] gzipCompress(byte[] data) {
        return gzipCompress(data, 0, data.length);
    }

    public static byte[] gzipCompress(byte[] data, int off, int len) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (GZIPOutputStream gzip = new GZIPOutputStream(baos)) {
            gzip.write(data, off, len);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return baos.toByteArray();
    }
}
