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
import java.util.Arrays;
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
		logger = loggerContext.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
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
		final ILoggingEvent event = createLoggingEvent();
		final ILoggingEvent remoteEvent = writeAndRead(event);
		checkForEquality(event, remoteEvent);
	}

	@Test
	public void context() throws Exception {
		loggerContext.putProperty("testKey", "testValue");
		final ILoggingEvent event = createLoggingEvent();
		final ILoggingEvent remoteEvent = writeAndRead(event);
		checkForEquality(event, remoteEvent);

		assertNotNull(remoteEvent.getLoggerName());
		assertEquals(org.slf4j.Logger.ROOT_LOGGER_NAME, remoteEvent.getLoggerName());

		final LoggerContextVO loggerContextRemoteView = remoteEvent.getLoggerContextVO();
		assertNotNull(loggerContextRemoteView);
		assertEquals("testContext", loggerContextRemoteView.getName());
		final Map<String, String> props = loggerContextRemoteView.getPropertyMap();
		assertNotNull(props);
		assertEquals("testValue", props.get("testKey"));
	}

	@Test
	public void MDC() throws Exception {
		MDC.put("key", "testValue");
		final ILoggingEvent event = createLoggingEvent();
		final ILoggingEvent remoteEvent = writeAndRead(event);
		checkForEquality(event, remoteEvent);
		final Map<String, String> MDCPropertyMap = remoteEvent.getMDCPropertyMap();
		assertEquals("testValue", MDCPropertyMap.get("key"));
	}

	@Test
	public void updatedMDC() throws Exception {
		MDC.put("key", "testValue");
		final ILoggingEvent event1 = createLoggingEvent();
		final Serializable s1 = pst.transform(event1);
		oos.writeObject(s1);

		MDC.put("key", "updatedTestValue");
		final ILoggingEvent event2 = createLoggingEvent();
		final Serializable s2 = pst.transform(event2);
		oos.writeObject(s2);

		// create the input stream based on the ouput stream
		final ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
		inputStream = new ObjectInputStream(bis);

		// skip over one object
		inputStream.readObject();
		final ILoggingEvent remoteEvent2 = (ILoggingEvent) inputStream.readObject();

		// We observe the second logging event. It should provide us with
		// the updated MDC property.
		final Map<String, String> MDCPropertyMap = remoteEvent2.getMDCPropertyMap();
		assertEquals("updatedTestValue", MDCPropertyMap.get("key"));
	}

	@Test
	public void nonSerializableParameters() throws Exception {
		final LoggingEvent event = createLoggingEvent();
		final LuckyCharms lucky0 = new LuckyCharms(0);
		event.setArgumentArray(new Object[] { lucky0, null });
		final ILoggingEvent remoteEvent = writeAndRead(event);
		checkForEquality(event, remoteEvent);

		final Object[] aa = remoteEvent.getArgumentArray();
		assertNotNull(aa);
		assertEquals(2, aa.length);
		assertEquals("LC(0)", aa[0]);
		assertNull(aa[1]);
	}

	@Test
	public void testWithThrowable() throws Exception {
		final Throwable throwable = new Throwable("just testing");
		final LoggingEvent event = createLoggingEventWithThrowable(throwable);
		final ILoggingEvent remoteEvent = writeAndRead(event);
		checkForEquality(event, remoteEvent);
	}


	@Test
	public void testWithMarker() throws Exception {
		final Marker marker = MarkerFactory.getMarker("A_MARKER");
		final LoggingEvent event = createLoggingEvent();


		event.addMarker(marker);
		assertNotNull(event.getMarkerList());

		final ILoggingEvent remoteEvent = writeAndRead(event);
		checkForEquality(event, remoteEvent);

		assertNotNull(remoteEvent.getMarkerList());
		assertEquals(Arrays.asList(marker), remoteEvent.getMarkerList());
	}

	@Test
	public void testWithTwoMarkers() throws Exception {
		final Marker marker = MarkerFactory.getMarker("A_MARKER");
		final Marker marker2 = MarkerFactory.getMarker("B_MARKER");
		marker.add(marker2);
		final LoggingEvent event = createLoggingEvent();

		event.addMarker(marker);
		assertNotNull(event.getMarkerList());

		final ILoggingEvent remoteEvent = writeAndRead(event);
		checkForEquality(event, remoteEvent);

		assertNotNull(remoteEvent.getMarkerList());
		assertEquals(Arrays.asList(marker), remoteEvent.getMarkerList());
	}

	@Test
	public void testWithCallerData() throws Exception {
		final LoggingEvent event = createLoggingEvent();
		event.getCallerData();
		final ILoggingEvent remoteEvent = writeAndRead(event);
		checkForEquality(event, remoteEvent);
	}

	@Test
	public void extendendeThrowable() throws Exception {
		final LoggingEvent event = createLoggingEvent();
		final Throwable throwable = new Throwable("just testing");
		final ThrowableProxy tp = new ThrowableProxy(throwable);
		event.setThrowableProxy(tp);
		tp.calculatePackagingData();
		final ILoggingEvent remoteEvent = writeAndRead(event);
		checkForEquality(event, remoteEvent);
	}

	@Test
	public void serializeLargeArgs() throws Exception {

		final StringBuilder buffer = new StringBuilder();
		for (int i = 0; i < 100000; i++) {
			buffer.append("X");
		}
		final String largeString = buffer.toString();
		final Object[] argArray = { new LuckyCharms(2), largeString };

		final LoggingEvent event = createLoggingEvent();
		event.setArgumentArray(argArray);

		final ILoggingEvent remoteEvent = writeAndRead(event);
		checkForEquality(event, remoteEvent);
		final Object[] aa = remoteEvent.getArgumentArray();
		assertNotNull(aa);
		assertEquals(2, aa.length);
		final String stringBack = (String) aa[1];
		assertEquals(largeString, stringBack);
	}

	private LoggingEvent createLoggingEvent() {
		return new LoggingEvent(this.getClass().getName(), logger, Level.DEBUG, "test message", null, null);
	}

	private LoggingEvent createLoggingEventWithThrowable(final Throwable t) {
		return new LoggingEvent(this.getClass().getName(), logger, Level.DEBUG, "test message", t, null);
	}

	private void checkForEquality(final ILoggingEvent original, final ILoggingEvent afterSerialization) {
		assertEquals(original.getLevel(), afterSerialization.getLevel());
		assertEquals(original.getFormattedMessage(), afterSerialization.getFormattedMessage());
		assertEquals(original.getMessage(), afterSerialization.getMessage());

		System.out.println();

		final ThrowableProxyVO witness = ThrowableProxyVO.build(original.getThrowableProxy());
		assertEquals(witness, afterSerialization.getThrowableProxy());

	}

	private ILoggingEvent writeAndRead(final ILoggingEvent event) throws IOException, ClassNotFoundException {
		final Serializable ser = pst.transform(event);
		oos.writeObject(ser);
		final ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
		inputStream = new HardenedLoggingEventInputStream(bis);

		return (ILoggingEvent) inputStream.readObject();
	}

}
