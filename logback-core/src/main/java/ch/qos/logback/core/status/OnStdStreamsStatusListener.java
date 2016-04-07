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

/**
 * Print all new incoming INFO status messages on System.out, WARN and ERROR on System.err.
 *
 * @author Ceki G&uuml;c&uuml;, Arnout Engelen
 */
public class OnStdStreamsStatusListener extends OnPrintStreamStatusListenerBase {

    @Override
    protected PrintStream getPrintStream(Status status) {
        if (status.getLevel() == Status.INFO) {
            return System.out;
        } else {
            return System.err;
        }
    }
}
