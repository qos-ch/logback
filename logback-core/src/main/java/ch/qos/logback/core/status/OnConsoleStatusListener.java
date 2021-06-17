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
package ch.qos.logback.core.status;

import java.io.PrintStream;

import ch.qos.logback.core.util.StatusPrinter;

/**
 * Print new incoming status messages on the console (System.out).
 *
 * Optionally a custom LogLevel can be set:
 *
 * <pre>
 * {@code
 * <statusListener class="ch.qos.logback.core.status.OnConsoleStatusListener">
 *     <level>WARN</level>
 * </statusListener>
 * }
 * </pre>
 *
 * @author Ceki G&uuml;lc&uuml;, Tillmann Heigel
 */
public class OnConsoleStatusListener extends OnPrintStreamStatusListenerBase {

    private int statusLevel = Status.INFO;

    private void print(Status status) {
        StringBuilder sb = new StringBuilder();

        if (prefix != null) {
            sb.append(prefix);
        }

        if (status.getLevel() >= statusLevel) {
            StatusPrinter.buildStr(sb, "", status);
            getPrintStream().print(sb);
        }
    }

    protected PrintStream getPrintStream() {
        return System.out;
    }

    public void addStatusEvent(Status status) {
        if (!isStarted) {
            return;
        }
        print(status);
    }

    public void setLevel(String level) {
        switch (level) {
            case "INFO":
                statusLevel = Status.INFO;
                break;
            case "WARN":
                statusLevel = Status.WARN;
                break;
            case "ERROR":
                statusLevel = Status.ERROR;
                break;
            default:
                statusLevel = Status.INFO;
        }
    }
}
