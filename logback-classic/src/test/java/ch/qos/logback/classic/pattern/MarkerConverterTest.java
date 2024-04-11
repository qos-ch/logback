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
package ch.qos.logback.classic.pattern;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.IMarkerFactory;
import org.slf4j.Marker;
import org.slf4j.helpers.BasicMarkerFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MarkerConverterTest {

    LoggerContext lc;
    MarkerConverter converter;
    // use a different factory for each test so that they are independent
    IMarkerFactory markerFactory = new BasicMarkerFactory();

    @BeforeEach
    public void setUp() throws Exception {
        lc = new LoggerContext();
        converter = new MarkerConverter();
        converter.start();
    }

    @AfterEach
    public void tearDown() throws Exception {
        lc = null;
        converter.stop();
        converter = null;
    }

    @Test
    public void testWithNullMarker() {
        String result = converter.convert(createLoggingEvent(null));
        assertEquals("", result);
    }

    @Test
    public void testWithMarker() {
        String name = "test";
        Marker marker = markerFactory.getMarker(name);
        String result = converter.convert(createLoggingEvent(marker));
        assertEquals(name, result);
    }

    @Test
    public void testWithOneChildMarker() {
        Marker marker = markerFactory.getMarker("test");
        marker.add(markerFactory.getMarker("child"));

        String result = converter.convert(createLoggingEvent(marker));

        assertEquals("test [ child ]", result);
    }

    @Test
    public void testWithSeveralChildMarker() {
        Marker marker = markerFactory.getMarker("testParent");
        marker.add(markerFactory.getMarker("child1"));
        marker.add(markerFactory.getMarker("child2"));
        marker.add(markerFactory.getMarker("child3"));

        String result = converter.convert(createLoggingEvent(marker));

        assertEquals("testParent [ child1, child2, child3 ]", result);
    }

    private ILoggingEvent createLoggingEvent(Marker marker) {
        LoggingEvent le = new LoggingEvent(this.getClass().getName(), lc.getLogger(Logger.ROOT_LOGGER_NAME),
                Level.DEBUG, "test message", null, null);
        le.addMarker(marker);
        return le;
    }
}
