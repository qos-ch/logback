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

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class MD5Util {


    static String MD5_ALGORITHM_KEY = "MD5";
    final MessageDigest md5;

    public MD5Util() throws NoSuchAlgorithmException {
        md5 = MessageDigest.getInstance(MD5_ALGORITHM_KEY);

    }


    public boolean equalsHash(byte[] b0, byte[] b1) {
        return Arrays.equals(b0, b1);
    }

    /**
     * Compute hash for input string. The hash is computed on the input alone
     * with no previous or subsequent data.
     *
     * @param input
     * @return
     */
    public byte[] md5Hash(String input) {
        byte[] messageDigest = md5.digest(input.getBytes());
        md5.reset();
        return messageDigest;
    }

    public String asHexString(byte[] messageDigest) {
        BigInteger number = new BigInteger(1, messageDigest);
        StringBuilder hexString = new StringBuilder(number.toString(16));
        // Pad with zeros if necessary
        while (hexString.length() < 32) {
            hexString.insert(0, '0');
        }
        return hexString.toString();
    }

}
