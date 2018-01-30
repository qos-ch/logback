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
package ch.qos.logback.classic.issue.lbclassic36;

import java.text.SimpleDateFormat;
import java.util.Date;

//import org.joda.time.format.DateTimeFormat;
//import org.joda.time.format.DateTimeFormatter;

public class DateFormatPerf_Tapp {
    public static final String ISO8601_PATTERN = "yyyy-MM-dd HH:mm:ss,SSS";
    static final long NANOS_IN_ONE_SEC = 1000 * 1000 * 1000L;

    static long RUN_LENGTH = 1000 * 1000;

    public static void main(String[] args) {
        for (int i = 0; i < 5; i++) {
            doRawJoda();
            doRawSDF();
        }

        print("Raw Joda:     ", doRawJoda());
        print("Raw SDF:      ", doRawSDF());
    }

    static void print(String msg, double avg) {
        System.out.println(msg + " average tick " + avg + " nanoseconds");
    }

    static double doRawJoda() {
        // DateTimeFormatter jodaFormat = DateTimeFormat.forPattern(ISO8601_PATTERN);
        @SuppressWarnings("unused")
        long timeInMillis = new Date().getTime();
        long start = System.nanoTime();
        for (int i = 0; i < RUN_LENGTH; ++i) {
            // jodaFormat.print(timeInMillis);
        }
        return (System.nanoTime() - start) * 1.0d / RUN_LENGTH;
    }

    static double doRawSDF() {
        SimpleDateFormat simpleFormat = new SimpleDateFormat(ISO8601_PATTERN);
        long timeInMillis = new Date().getTime();
        long start = System.nanoTime();
        for (int i = 0; i < RUN_LENGTH; ++i) {
            simpleFormat.format(timeInMillis);
        }
        return (System.nanoTime() - start) * 1.0d / RUN_LENGTH;
    }

}
