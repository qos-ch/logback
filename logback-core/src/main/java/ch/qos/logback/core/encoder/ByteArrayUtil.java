/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2015, QOS.ch. All rights reserved.
 *
 * This program and the accompanying materials are dual-licensed under
 * either the terms of the Eclipse Public License v1.0 as published by
 * the Eclipse Foundation
 *
 *   or (per the licensee's choosing)
 *
 * under the terms of the GNU Lesser General Public License version 2.1
 * as published by the Free Software Foundation.
 */
package ch.qos.logback.core.encoder;

import java.io.ByteArrayOutputStream;

public class ByteArrayUtil {

    // big-endian
    static void writeInt(final byte[] byteArray, final int offset, final int i) {
        for (int j = 0; j < 4; j++) {
            final int shift = 24 - j * 8;
            byteArray[offset + j] = (byte) (i >>> shift);
        }
    }

    static void writeInt(final ByteArrayOutputStream baos, final int i) {
        for (int j = 0; j < 4; j++) {
            final int shift = 24 - j * 8;
            baos.write((byte) (i >>> shift));
        }
    }

    // big-endian
    static int readInt(final byte[] byteArray, final int offset) {
        int i = 0;
        for (int j = 0; j < 4; j++) {
            final int shift = 24 - j * 8;
            i += (byteArray[offset + j] & 0xFF) << shift;
        }
        return i;
    }

    static public String toHexString(final byte[] ba) {
        final StringBuilder sbuf = new StringBuilder();
        for (final byte b : ba) {
            final String s = Integer.toHexString(b & 0xff);
            if (s.length() == 1) {
                sbuf.append('0');
            }
            sbuf.append(s);
        }
        return sbuf.toString();
    }

    static public byte[] hexStringToByteArray(final String s) {
        final int len = s.length();
        final byte[] ba = new byte[len / 2];

        for (int i = 0; i < ba.length; i++) {
            final int j = i * 2;
            final int t = Integer.parseInt(s.substring(j, j + 2), 16);
            final byte b = (byte) (t & 0xFF);
            ba[i] = b;
        }
        return ba;
    }
}
