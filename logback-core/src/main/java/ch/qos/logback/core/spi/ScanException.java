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
package ch.qos.logback.core.spi;

public class ScanException extends Exception {

    private static final long serialVersionUID = -3132040414328475658L;

    Throwable cause;

    public ScanException(final String msg) {
        super(msg);
    }

    public ScanException(final String msg, final Throwable rootCause) {
        super(msg);
        cause = rootCause;
    }

    @Override
    public Throwable getCause() {
        return cause;
    }
}
