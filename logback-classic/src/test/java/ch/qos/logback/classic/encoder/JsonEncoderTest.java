/*
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2023, QOS.ch. All rights reserved.
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

package ch.qos.logback.classic.encoder;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;

import static org.junit.jupiter.api.Assertions.assertEquals;



@Disabled
public class JsonEncoderTest {



    LoggerContext context = new LoggerContext();
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    Logger logger = context.getLogger(PatternLayoutEncoderTest.class);
    Charset utf8Charset = Charset.forName("UTF-8");

    JsonEncoder je = new JsonEncoder();

    @BeforeEach
    public void setUp() {
        je.setContext(context);
    }

    @Test
    public void smoke() throws IOException {
        String msg = "hello";
        ILoggingEvent event = makeLoggingEvent(msg);
        byte[] eventBytes = je.encode(event);
        baos.write(eventBytes);
        assertEquals(msg, baos.toString());
    }

    ILoggingEvent makeLoggingEvent(String message) {
        return new LoggingEvent("", logger, Level.DEBUG, message, null, null);
    }

}
