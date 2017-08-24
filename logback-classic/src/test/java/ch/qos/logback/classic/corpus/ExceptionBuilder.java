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
package ch.qos.logback.classic.corpus;

import java.util.Random;

import javax.management.remote.JMXProviderException;

public class ExceptionBuilder {

    static Throwable build(Random r, double nestingProbability) {
        double rn = r.nextDouble();
        boolean nested = false;
        if (rn < nestingProbability) {
            nested = true;
        }

        Throwable cause = null;
        if (nested) {
            cause = makeThrowable(r, null);
        }
        return makeThrowable(r, cause);
    }

    private static Throwable makeThrowable(Random r, Throwable cause) {
        int exType = r.nextInt(4);
        switch (exType) {
        case 0:
            return new IllegalArgumentException("an illegal argument was passed", cause);
        case 1:
            return new Exception("this is a test", cause);
        case 2:
            return new JMXProviderException("jmx provider exception error occured", cause);
        case 3:
            return new OutOfMemoryError("ran out of memory");
        }
        return null;
    }

}
