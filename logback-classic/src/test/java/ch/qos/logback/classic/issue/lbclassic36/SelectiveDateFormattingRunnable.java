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
//import org.joda.time.format.DateTimeFormat;
//import org.joda.time.format.DateTimeFormatter;

import ch.qos.logback.core.contention.RunnableWithCounterAndDone;

/**
 * A runnable which behaves differently depending on the desired locking model.
 * 
 * @author Ralph Goers
 * @author Ceki Gulcu
 */
public class SelectiveDateFormattingRunnable extends RunnableWithCounterAndDone {

    public static final String ISO8601_PATTERN = "yyyy-MM-dd HH:mm:ss,SSS";

    enum FormattingModel {
        SDF, JODA;
    }

    FormattingModel model;
    static long CACHE = 0;

    static SimpleDateFormat SDF = new SimpleDateFormat(ISO8601_PATTERN);

    // static final DateTimeFormatter JODA = DateTimeFormat
    // .forPattern(ISO8601_PATTERN);

    SelectiveDateFormattingRunnable(FormattingModel model) {
        this.model = model;
    }

    public void run() {
        switch (model) {
        case SDF:
            sdfRun();
            break;
        case JODA:
            jodaRun();
            break;
        }
    }

    void sdfRun() {

        for (;;) {
            synchronized (SDF) {
                long now = System.currentTimeMillis();
                if (CACHE != now) {
                    CACHE = now;
                    SDF.format(now);
                }
            }
            counter++;
            if (done) {
                return;
            }
        }
    }

    void jodaRun() {
        for (;;) {
            long now = System.currentTimeMillis();
            if (isCacheStale(now)) {
                // JODA.print(now);
            }
            counter++;
            if (done) {
                return;
            }
        }
    }

    private static boolean isCacheStale(long now) {
        // synchronized (JODA) {
        // if (CACHE != now) {
        // CACHE = now;
        // return true;
        // }
        // }
        return false;
    }

}
