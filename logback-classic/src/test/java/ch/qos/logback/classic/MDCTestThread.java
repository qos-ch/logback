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
package ch.qos.logback.classic;

import org.slf4j.MDC;

public class MDCTestThread extends Thread {

    String val;

    public MDCTestThread(String val) {
        super();
        this.val = val;
    }

    String x0;
    String x1;
    String x2;

    public void run() {
        x0 = MDC.get("x");
        MDC.put("x", val);
        x1 = MDC.get("x");
        MDC.clear();
        x2 = MDC.get("x");
        // System.out.println("Exiting "+val);
    }
}