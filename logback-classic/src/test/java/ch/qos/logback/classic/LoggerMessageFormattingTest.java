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

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;

public class LoggerMessageFormattingTest {

	LoggerContext lc;
	ListAppender<ILoggingEvent> listAppender;

	@Before
	public void setUp() {
		lc = new LoggerContext();
		final Logger logger = lc.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
		listAppender = new ListAppender<>();
		listAppender.setContext(lc);
		listAppender.start();
		logger.addAppender(listAppender);
	}

	@Test
	public void testFormattingOneArg() {
		final Logger logger = lc.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
		logger.debug("{}", Integer.valueOf(12));
		final ILoggingEvent event = listAppender.list.get(0);
		assertEquals("12", event.getFormattedMessage());
	}

	@Test
	public void testFormattingTwoArg() {
		final Logger logger = lc.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
		logger.debug("{}-{}", Integer.valueOf(12), Integer.valueOf(13));
		final ILoggingEvent event = listAppender.list.get(0);
		assertEquals("12-13", event.getFormattedMessage());
	}

	@Test
	public void testNoFormatting() {
		final Logger logger = lc.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
		logger.debug("test", Integer.valueOf(12), Integer.valueOf(13));
		final ILoggingEvent event = listAppender.list.get(0);
		assertEquals("test", event.getFormattedMessage());
	}

	@Test
	public void testNoFormatting2() {
		final Logger logger = lc.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
		logger.debug("test");
		final ILoggingEvent event = listAppender.list.get(0);
		assertEquals("test", event.getFormattedMessage());
	}

	@Test
	public void testMessageConverter() {
		final Logger logger = lc.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
		logger.debug("{}", 12);
		final ILoggingEvent event = listAppender.list.get(0);
		final PatternLayout layout = new PatternLayout();
		layout.setContext(lc);
		layout.setPattern("%m");
		layout.start();
		final String formattedMessage = layout.doLayout(event);
		assertEquals("12", formattedMessage);
	}

}
