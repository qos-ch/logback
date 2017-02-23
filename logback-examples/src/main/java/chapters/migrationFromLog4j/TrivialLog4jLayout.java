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

import org.apache.log4j.Layout;
import org.apache.log4j.spi.LoggingEvent;

/**
 * 
 * A very simple log4j layout which formats a logging event
 * by returning the message contained therein.
 * 
 * @author Ceki G&uuml;lc&uuml;
 *
 */
public class TrivialLog4jLayout extends Layout {

    public void activateOptions() {
        // there are no options to activate
    }

    public String format(LoggingEvent loggingEvent) {
        return loggingEvent.getRenderedMessage();
    }

    @Override
    public boolean ignoresThrowable() {
        return true;
    }

}
