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
package ch.qos.logback.classic.control;

import java.util.Random;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.corpus.RandomUtil;

public class ScenarioRandomUtil {
    private final static long SEED = 74130;

    private final static Random random = new Random(SEED);
    private final static int AVERAGE_ID_LEN = 32;
    private final static int AVERAGE_ID_DEV = 16;

    private final static int AVERAGE_CHILDREN_COUNT = 30;
    private final static int CHILDREN_COUNT_VAR = 10;

    public static boolean oneInFreq(int freq) {
        return (random.nextInt(freq) % freq) == 0;
    }

    public static Level randomLevel() {
        int rl = random.nextInt(6);
        switch (rl) {
        case 0:
            return null;
        case 1:
            return Level.TRACE;
        case 2:
            return Level.DEBUG;
        case 3:
            return Level.INFO;
        case 4:
            return Level.WARN;
        case 5:
            return Level.ERROR;
        default:
            throw new IllegalStateException("rl should have been a value between 0 to 5, but it is " + rl);
        }
    }

    public static String randomLoggerName(int average, int stdDeviation) {
        int depth = RandomUtil.gaussianAsPositiveInt(random, average, stdDeviation);
        StringBuilder buf = new StringBuilder();
        for (int i = 0; i < depth; i++) {
            if (i != 0) {
                buf.append('.');
            }
            buf.append(randomId());
        }
        return buf.toString();
    }

    public static String randomId() {

        int len = RandomUtil.gaussianAsPositiveInt(random, AVERAGE_ID_LEN, AVERAGE_ID_DEV);
        StringBuilder buf = new StringBuilder();
        for (int i = 0; i < len; i++) {
            int offset = random.nextInt(26);
            char c = (char) ('a' + offset);
            buf.append(c);
        }
        return buf.toString();
    }

    /**
     * Returns 3 for root, 3 for children of root, 9 for offspring of generation 2
     * and 3, and for generations 4 and later, return 0 with probability 0.5 and a
     * gaussian (average=AVERAGE_CHILDREN_COUNT) with probability 0.5.
     * 
     * @param name
     * @return
     */
    public static int randomChildrenCount(String name) {
        int dots = dotCount(name);
        if (dots <= 1) {
            return 3;
        } else if (dots == 2 || dots == 3) {
            return 9;
        } else {
            if (shouldHaveChildrenWithProbabilitz(0.5)) {
                return RandomUtil.gaussianAsPositiveInt(random, AVERAGE_CHILDREN_COUNT, CHILDREN_COUNT_VAR);
            } else {
                return 0;
            }
        }

    }

    /**
     * Returns true with probability p.
     * 
     * @param p
     * @return
     */
    static boolean shouldHaveChildrenWithProbabilitz(double p) {
        if (p < 0 || p > 1.0) {
            throw new IllegalArgumentException("p must be a value between 0 and 1.0, it was " + p + " instead.");
        }
        double r = random.nextDouble();
        if (r < p) {
            return true;
        } else {
            return false;
        }
    }

    static int dotCount(String s) {
        int count = 0;
        int len = s.length();
        for (int i = 0; i < len; i++) {
            char c = s.charAt(i);
            if (c == '.') {
                count++;
            }
        }
        return count;
    }
}
