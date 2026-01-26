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

package ch.qos.logback.classic.issue.github879;


import ch.qos.logback.classic.ClassicConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {


    static {

        System.setProperty("outputPath", "logback-classic/target/test-output/issue879");
        String configFilePath = "logback-classic/src/test/java/ch/qos/logback/classic/issue/github879/";
        System.setProperty("logback.statusListenerClass", "stdout");
        System.setProperty(ClassicConstants.CONFIG_FILE_PROPERTY, configFilePath+"logback-879.xml");
    }


    public static void main(String[] args) {
        final Logger LOGGER = LoggerFactory.getLogger(Main.class);

        for (int i = 0; i < 20_000; i++) {
            LOGGER.info("X".repeat(45));
        }
    }
}