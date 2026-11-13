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

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;

/**
 * Recycled UTF-8 {@link CharsetEncoder}/{@link CharsetDecoder} for the string codecs.
 * Matches {@link String#getBytes} / {@code new String(byte[], UTF_8)} replacement policy.
 * Not thread-safe; one instance per serializer.
 */
final class ReusableUtf8 {

    /** UTF-8 is at most 3 bytes per Java {@code char} (surrogate pairs use 4 bytes for 2 chars). */
    static final int MAX_BYTES_PER_CHAR = 3;

    private final CharsetEncoder encoder = StandardCharsets.UTF_8.newEncoder()
            .onMalformedInput(CodingErrorAction.REPLACE)
            .onUnmappableCharacter(CodingErrorAction.REPLACE);
    private final CharsetDecoder decoder = StandardCharsets.UTF_8.newDecoder()
            .onMalformedInput(CodingErrorAction.REPLACE)
            .onUnmappableCharacter(CodingErrorAction.REPLACE);

    private byte[] encodeBuf = new byte[512];
    private char[] decodeBuf = new char[512];

    byte[] buffer() {
        return encodeBuf;
    }

    int maxEncodedLength(int charCount) {
        return encodedCapacity(charCount);
    }

    /**
     * Encodes {@code s} into {@link #buffer()}. Returns the UTF-8 byte count;
     * valid bytes are {@code buffer()[0 .. length)}.
     */
    int encode(String s) {
        int cap = encodedCapacity(s.length());
        if (encodeBuf.length < cap) {
            encodeBuf = new byte[cap];
        }
        return encode(s, encodeBuf, 0);
    }

    /**
     * Encodes {@code s} into {@code dest} starting at {@code off}.
     * {@code dest} must have room for {@link #maxEncodedLength(int) maxEncodedLength(s.length())}
     * bytes from {@code off}.
     */
    int encode(String s, byte[] dest, int off) {
        encoder.reset();
        CharBuffer in = CharBuffer.wrap(s);
        ByteBuffer out = ByteBuffer.wrap(dest, off, dest.length - off);
        CoderResult cr = encoder.encode(in, out, true);
        throwIfError(cr);
        if (cr.isOverflow()) {
            throw new IllegalArgumentException("UTF-8 dest too small for " + s.length() + " chars");
        }
        cr = encoder.flush(out);
        throwIfError(cr);
        if (cr.isOverflow()) {
            throw new IllegalArgumentException("UTF-8 dest too small for flush");
        }
        return out.position() - off;
    }

    String decode(byte[] src, int off, int len) {
        if (len == 0) {
            return "";
        }
        int cap = decodedCapacity(len);
        if (decodeBuf.length < cap) {
            decodeBuf = new char[cap];
        }
        for (;;) {
            decoder.reset();
            ByteBuffer in = ByteBuffer.wrap(src, off, len);
            CharBuffer out = CharBuffer.wrap(decodeBuf);
            CoderResult cr = decoder.decode(in, out, true);
            if (growDecodeBufIfOverflow(cr)) {
                continue;
            }
            throwIfError(cr);
            cr = decoder.flush(out);
            if (growDecodeBufIfOverflow(cr)) {
                continue;
            }
            throwIfError(cr);
            return new String(decodeBuf, 0, out.position());
        }
    }

    private boolean growDecodeBufIfOverflow(CoderResult cr) {
        if (!cr.isOverflow()) {
            return false;
        }
        decodeBuf = new char[decodeBuf.length * 2];
        return true;
    }

    private static void throwIfError(CoderResult cr) {
        if (cr.isError()) {
            throw new IllegalStateException("UTF-8 coding error: " + cr);
        }
    }

    private int encodedCapacity(int charCount) {
        int n = (int) Math.ceil(charCount * (double) encoder.maxBytesPerChar());
        return Math.max(n, 16);
    }

    private int decodedCapacity(int byteCount) {
        int n = (int) Math.ceil(byteCount * (double) decoder.maxCharsPerByte());
        return Math.max(n, 16);
    }
}
