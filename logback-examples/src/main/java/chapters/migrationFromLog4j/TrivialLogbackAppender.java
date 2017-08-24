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

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import ch.qos.logback.core.Layout;

public class TrivialLogbackAppender extends AppenderBase<ILoggingEvent> {

    Layout<ILoggingEvent> layout;

    @Override
    public void start() {
        if (this.layout == null) {
            addError("No layout set for the appender named [" + name + "].");
            return;
        }
        String header = layout.getFileHeader();
        System.out.println(header);
        super.start();
    }

    @Override
    protected void append(ILoggingEvent loggingEvent) {
        // note that AppenderBase.doAppend will invoke this method only if
        // this appender was successfully started.
        String eventAsStr = this.layout.doLayout(loggingEvent);
        System.out.println(eventAsStr);
    }


    public Layout<ILoggingEvent> getLayout() {
        return layout;
    }

    public void setLayout(Layout<ILoggingEvent> layout) {
        this.layout = layout;
    }

}
