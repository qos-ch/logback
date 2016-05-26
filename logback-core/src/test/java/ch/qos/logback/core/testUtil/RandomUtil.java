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
package ch.qos.logback.core.testUtil;

import java.util.Random;

public class RandomUtil {

    private static Random random = new Random();

    public static int getRandomServerPort() {
        int r = random.nextInt(20000);
        // the first 1024 ports are usually reserved for the OS
        return r + 1024;
    }

    public static int getPositiveInt() {
        int r = random.nextInt();
        if (r < 0) {
            r = -r;
        }
        return r;
    }
}
