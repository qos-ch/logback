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

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AsyncAppenderBase;

/**
 * In order to optimize performance this appender deems events of level TRACE, DEBUG and INFO as discardable. See the
 * <a href="http://logback.qos.ch/manual/appenders.html#AsyncAppender">chapter on appenders</a> in the manual for
 * further information.
 *
 *
 * @author Ceki G&uuml;lc&uuml;
 * @since 1.0.4
 */
public class AsyncAppender extends AsyncAppenderBase<ILoggingEvent> {

    boolean includeCallerData = false;

    /**
     * Events of level TRACE, DEBUG and INFO are deemed to be discardable.
     * @param event
     * @return true if the event is of level TRACE, DEBUG or INFO false otherwise.
     */
    protected boolean isDiscardable(ILoggingEvent event) {
        Level level = event.getLevel();
        return level.toInt() <= Level.INFO_INT;
    }

    protected void preprocess(ILoggingEvent eventObject) {
        eventObject.prepareForDeferredProcessing();
        if (includeCallerData)
            eventObject.getCallerData();
    }

    public boolean isIncludeCallerData() {
        return includeCallerData;
    }

    public void setIncludeCallerData(boolean includeCallerData) {
        this.includeCallerData = includeCallerData;
    }

}
