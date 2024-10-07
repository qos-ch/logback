/*
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2024, QOS.ch. All rights reserved.
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

package ch.qos.logback.core.util;

import org.junit.jupiter.api.Test;

import java.security.NoSuchAlgorithmException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MD5Test {

    @Test
    void smoke() throws NoSuchAlgorithmException {
        MD5Util md5Util = new MD5Util();
        byte[] hash = md5Util.md5Hash("toto");
        String asHexStr = md5Util.asHexString(hash);
        assertEquals("f71dbe52628a3f83a77ab494817525c6", asHexStr);
        System.out.println(asHexStr);

    }
}
