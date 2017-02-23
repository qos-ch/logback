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
package chapters.layouts;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.LayoutBase;

public class MySampleLayout2 extends LayoutBase<ILoggingEvent> {

    String prefix = null;
    boolean printThreadName = true;

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public void setPrintThreadName(boolean printThreadName) {
        this.printThreadName = printThreadName;
    }

    public String doLayout(ILoggingEvent event) {
        StringBuilder sbuf = new StringBuilder(128);
        if (prefix != null) {
            sbuf.append(prefix + ": ");
        }
        sbuf.append(event.getTimeStamp() - event.getLoggerContextVO().getBirthTime());
        sbuf.append(" ");
        sbuf.append(event.getLevel());
        if (printThreadName) {
            sbuf.append(" [");
            sbuf.append(event.getThreadName());
            sbuf.append("] ");
        } else {
            sbuf.append(" ");
        }
        sbuf.append(event.getLoggerName());
        sbuf.append(" - ");
        sbuf.append(event.getFormattedMessage());
        sbuf.append(CoreConstants.LINE_SEPARATOR);
        return sbuf.toString();
    }
}