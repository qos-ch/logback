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
package ch.qos.logback.classic.spi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.MDC;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.net.LoggingEventPreSerializationTransformer;
import ch.qos.logback.classic.net.server.HardenedLoggingEventInputStream;
import ch.qos.logback.core.spi.PreSerializationTransformer;

public class LoggingEventSerializationTest {

    LoggerContext loggerContext;
    Logger logger;

    ByteArrayOutputStream bos;
    ObjectOutputStream oos;
    ObjectInputStream inputStream;
    PreSerializationTransformer<ILoggingEvent> pst = new LoggingEventPreSerializationTransformer();

    @Before
    public void setUp() throws Exception {
        loggerContext = new LoggerContext();
        loggerContext.setName("testContext");
        logger = loggerContext.getLogger(Logger.ROOT_LOGGER_NAME);
        // create the byte output stream
        bos = new ByteArrayOutputStream();
        oos = new ObjectOutputStream(bos);
    }

    @After
    public void tearDown() throws Exception {
        loggerContext = null;
        logger = null;
        oos.close();
    }

    @Test
    public void smoke() throws Exception {
        ILoggingEvent event = createLoggingEvent();
        ILoggingEvent remoteEvent = writeAndRead(event);
        checkForEquality(event, remoteEvent);
    }

    @Test
    public void context() throws Exception {
        loggerContext.putProperty("testKey", "testValue");
        ILoggingEvent event = createLoggingEvent();
        ILoggingEvent remoteEvent = writeAndRead(event);
        checkForEquality(event, remoteEvent);

        assertNotNull(remoteEvent.getLoggerName());
        assertEquals(Logger.ROOT_LOGGER_NAME, remoteEvent.getLoggerName());

        LoggerContextVO loggerContextRemoteView = remoteEvent.getLoggerContextVO();
        assertNotNull(loggerContextRemoteView);
        assertEquals("testContext", loggerContextRemoteView.getName());
        Map<String, String> props = loggerContextRemoteView.getPropertyMap();
        assertNotNull(props);
        assertEquals("testValue", props.get("testKey"));
    }

    @Test
    public void MDC() throws Exception {
        MDC.put("key", "testValue");
        ILoggingEvent event = createLoggingEvent();
        ILoggingEvent remoteEvent = writeAndRead(event);
        checkForEquality(event, remoteEvent);
        Map<String, String> MDCPropertyMap = remoteEvent.getMDCPropertyMap();
        assertEquals("testValue", MDCPropertyMap.get("key"));
    }

    @Test
    public void updatedMDC() throws Exception {
        MDC.put("key", "testValue");
        ILoggingEvent event1 = createLoggingEvent();
        Serializable s1 = pst.transform(event1);
        oos.writeObject(s1);

        MDC.put("key", "updatedTestValue");
        ILoggingEvent event2 = createLoggingEvent();
        Serializable s2 = pst.transform(event2);
        oos.writeObject(s2);

        // create the input stream based on the ouput stream
        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
        inputStream = new ObjectInputStream(bis);

        // skip over one object
        inputStream.readObject();
        ILoggingEvent remoteEvent2 = (ILoggingEvent) inputStream.readObject();

        // We observe the second logging event. It should provide us with
        // the updated MDC property.
        Map<String, String> MDCPropertyMap = remoteEvent2.getMDCPropertyMap();
        assertEquals("updatedTestValue", MDCPropertyMap.get("key"));
    }

    @Test
    public void nonSerializableParameters() throws Exception {
        LoggingEvent event = createLoggingEvent();
        LuckyCharms lucky0 = new LuckyCharms(0);
        event.setArgumentArray(new Object[] { lucky0, null });
        ILoggingEvent remoteEvent = writeAndRead(event);
        checkForEquality(event, remoteEvent);

        Object[] aa = remoteEvent.getArgumentArray();
        assertNotNull(aa);
        assertEquals(2, aa.length);
        assertEquals("LC(0)", aa[0]);
        assertNull(aa[1]);
    }

    @Test
    public void testWithThrowable() throws Exception {
        Throwable throwable = new Throwable("just testing");
        LoggingEvent event = createLoggingEventWithThrowable(throwable);
        ILoggingEvent remoteEvent = writeAndRead(event);
        checkForEquality(event, remoteEvent);
    }


    @Test
    public void testWithMarker() throws Exception {
        Marker marker = MarkerFactory.getMarker("A_MARKER");
        LoggingEvent event = createLoggingEvent();
        
        
        event.setMarker(marker);
        assertNotNull(event.getMarker());
        
        ILoggingEvent remoteEvent = writeAndRead(event);
        checkForEquality(event, remoteEvent);

        assertNotNull(remoteEvent.getMarker());
        assertEquals(marker, remoteEvent.getMarker());
    }
    
    @Test
    public void testWithTwoMarkers() throws Exception {
        Marker marker = MarkerFactory.getMarker("A_MARKER");
        Marker marker2 = MarkerFactory.getMarker("B_MARKER");
        marker.add(marker2);
        LoggingEvent event = createLoggingEvent();
        
        event.setMarker(marker);
        assertNotNull(event.getMarker());
        
        ILoggingEvent remoteEvent = writeAndRead(event);
        checkForEquality(event, remoteEvent);

        assertNotNull(remoteEvent.getMarker());
        assertEquals(marker, remoteEvent.getMarker());
    }
    
    @Test
    public void testWithCallerData() throws Exception {
        LoggingEvent event = createLoggingEvent();
        event.getCallerData();
        ILoggingEvent remoteEvent = writeAndRead(event);
        checkForEquality(event, remoteEvent);
    }

    @Test
    public void extendendeThrowable() throws Exception {
        LoggingEvent event = createLoggingEvent();
        Throwable throwable = new Throwable("just testing");
        ThrowableProxy tp = new ThrowableProxy(throwable);
        event.setThrowableProxy(tp);
        tp.calculatePackagingData();
        ILoggingEvent remoteEvent = writeAndRead(event);
        checkForEquality(event, remoteEvent);
    }

    @Test
    public void serializeLargeArgs() throws Exception {

        StringBuilder buffer = new StringBuilder();
        for (int i = 0; i < 100000; i++) {
            buffer.append("X");
        }
        String largeString = buffer.toString();
        Object[] argArray = new Object[] { new LuckyCharms(2), largeString };

        LoggingEvent event = createLoggingEvent();
        event.setArgumentArray(argArray);

        ILoggingEvent remoteEvent = writeAndRead(event);
        checkForEquality(event, remoteEvent);
        Object[] aa = remoteEvent.getArgumentArray();
        assertNotNull(aa);
        assertEquals(2, aa.length);
        String stringBack = (String) aa[1];
        assertEquals(largeString, stringBack);
    }

    private LoggingEvent createLoggingEvent() {
        return new LoggingEvent(this.getClass().getName(), logger, Level.DEBUG, "test message", null, null);
    }

    private LoggingEvent createLoggingEventWithThrowable(Throwable t) {
        return new LoggingEvent(this.getClass().getName(), logger, Level.DEBUG, "test message", t, null);
    }

    private void checkForEquality(ILoggingEvent original, ILoggingEvent afterSerialization) {
        assertEquals(original.getLevel(), afterSerialization.getLevel());
        assertEquals(original.getFormattedMessage(), afterSerialization.getFormattedMessage());
        assertEquals(original.getMessage(), afterSerialization.getMessage());

        System.out.println();

        ThrowableProxyVO witness = ThrowableProxyVO.build(original.getThrowableProxy());
        assertEquals(witness, afterSerialization.getThrowableProxy());

    }

    private ILoggingEvent writeAndRead(ILoggingEvent event) throws IOException, ClassNotFoundException {
        Serializable ser = pst.transform(event);
        oos.writeObject(ser);
        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
        inputStream = new HardenedLoggingEventInputStream(bis);
        
        return (ILoggingEvent) inputStream.readObject();
    }

}
