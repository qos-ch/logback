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

package ch.qos.logback.classic.testUtil;

import java.util.Random;

public class Gaussian {

    Random random;

    double mean;
    double variance;

    public Gaussian(double mean, double variance) {
        this.random = new Random();
        this.mean = mean;
        this.variance = variance;
    }

    public Gaussian(long seed, double mean, double variance) {
        this.random = new Random(seed);
        this.mean = mean;
        this.variance = variance;
    }

    public double getGaussian() {
        return mean + random.nextGaussian() * variance;
    }

}
