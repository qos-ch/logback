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

import static org.junit.Assert.*;

import java.util.Random;

import org.junit.Test;

public class ByteArrayUtilTest {

    int BA_SIZE = 16;
    byte[] byteArray = new byte[BA_SIZE];

    Random random = new Random(18532235);

    @Test
    public void smoke() {
        verifyLoop(byteArray, 0, 0);
        verifyLoop(byteArray, 0, 10);
        verifyLoop(byteArray, 0, Integer.MAX_VALUE);
        verifyLoop(byteArray, 0, Integer.MIN_VALUE);
    }

    @Test
    public void random() {
        for (int i = 0; i < 100000; i++) {
            int rOffset = random.nextInt(BA_SIZE - 4);
            int rInt = random.nextInt();
            verifyLoop(byteArray, rOffset, rInt);
        }
    }

    void verifyLoop(byte[] ba, int offset, int expected) {
        ByteArrayUtil.writeInt(byteArray, offset, expected);
        int back = ByteArrayUtil.readInt(byteArray, offset);
        assertEquals(expected, back);

    }

}
