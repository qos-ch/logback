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

package ch.qos.logback.core.property;

import ch.qos.logback.core.PropertyDefinerBase;
import static ch.qos.logback.core.CoreConstants.NULL_STR;

import java.io.Console;
import java.nio.charset.Charset;

/**
 * Compute the console's charset.
 *
 * @since 1.5.7
 */
public class ConsoleCharsetPropertyDefiner extends PropertyDefinerBase {
    @Override
    public String getPropertyValue() {
        // System.console().charset() requires Java 17
        Console console = System.console();
        if (console != null) {
            Charset charset = console.charset();
            if (charset != null) {
                String charsetName = charset.name();
                addInfo("Found value '" + charsetName + "' as returned by System.console().");
                return charsetName;
            } else {
                addInfo("System.console() returned null charset. Returning \"NULL\" string as defined value.");
                return NULL_STR;
            }
        } else {
            addWarn("System.console() returned null. Cannot compute console's charset, returning the \"NULL\" string, i.e. the default JVM charset");
            return NULL_STR;
        }
    }

}
