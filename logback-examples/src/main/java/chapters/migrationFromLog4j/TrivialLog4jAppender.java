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
package chapters.migrationFromLog4j;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;

public class TrivialLog4jAppender extends AppenderSkeleton {

    protected void append(LoggingEvent loggingevent) {
        String s = this.layout.format(loggingevent);
        System.out.println(s);
    }

    public void close() {
        // nothing to do
    }

    public boolean requiresLayout() {
        return true;
    }

}
