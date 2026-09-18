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

package ch.qos.logback.classic.misc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CallerApp {

    public static void main(String[] args) {
        System.setProperty("logback.statusListenerClass", "stdout");
        System.setProperty("logback.configurationFile", "logback-caller.xml");

        Logger logger = LoggerFactory.getLogger(CallerApp.class);

        logger.atInfo().withCallerData(4).log("Hello with caller data");
        logger.atInfo().log("Hello with no caller data");
    }
}
