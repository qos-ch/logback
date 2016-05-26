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

import java.io.IOException;

import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;

public class TrivialLogbackAppender extends AppenderBase<ILoggingEvent> {

    PatternLayoutEncoder encoder;

    public PatternLayoutEncoder getEncoder() {
        return encoder;
    }

    public void setEncoder(PatternLayoutEncoder encoder) {
        this.encoder = encoder;
    }

    @Override
    public void start() {
        if (this.encoder == null) {
            addError("No encoder set for the appender named [" + name + "].");
            return;
        }
        try {
            encoder.init(System.out);
        } catch (IOException e) {
        }
        super.start();
    }

    @Override
    protected void append(ILoggingEvent loggingevent) {
        // note that AppenderBase.doAppend will invoke this method only if
        // this appender was successfully started.
        try {
            this.encoder.doEncode(loggingevent);
        } catch (IOException e) {
            // we can't do much with the exception except halting
            super.stop();
            addError("Failed to write to the console");
        }
    }

}
