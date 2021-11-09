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
package ch.qos.logback.classic.html;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.xml.sax.EntityResolver;

import ch.qos.logback.classic.ClassicTestConstants;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.classic.spi.DummyThrowableProxy;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.classic.spi.StackTraceElementProxy;
import ch.qos.logback.classic.spi.ThrowableProxy;
import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.testUtil.StringListAppender;
import ch.qos.logback.core.util.StatusPrinter;

public class HTMLLayoutTest {

	LoggerContext lc;
	Logger root;
	HTMLLayout layout;

	@Before
	public void setUp() throws Exception {
		lc = new LoggerContext();
		lc.setName("default");

		layout = new HTMLLayout();
		layout.setThrowableRenderer(new DefaultThrowableRenderer());
		layout.setContext(lc);
		layout.setPattern("%level%thread%msg");
		layout.start();

		root = lc.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);

	}

	@After
	public void tearDown() throws Exception {
		lc = null;
		layout = null;
	}

	@Test
	public void testHeader() throws Exception {
		final String header = layout.getFileHeader();
		// System.out.println(header);

		final Document doc = parseOutput(header + "</body></html>");
		final Element rootElement = doc.getRootElement();
		assertNotNull(rootElement.element("body"));
	}

	@Test
	public void testPresentationHeader() throws Exception {
		StringBuilder header = new StringBuilder().append(layout.getFileHeader());
		final String presentationHeader = layout.getPresentationHeader();
		header.append(presentationHeader);
		// System.out.println(header);

		final Document doc = parseOutput(header.append("</table></body></html>").toString());
		final Element rootElement = doc.getRootElement();
		final Element bodyElement = rootElement.element("body");
		final Element tableElement = bodyElement.element("table");
		final Element trElement = tableElement.element("tr");
		final List<Element> elementList = trElement.elements();
		assertEquals("Level", elementList.get(0).getText());
		assertEquals("Thread", elementList.get(1).getText());
		assertEquals("Message", elementList.get(2).getText());
	}

	@Test
	public void testAppendThrowable() throws Exception {
		final StringBuilder buf = new StringBuilder();
		final DummyThrowableProxy tp = new DummyThrowableProxy();
		tp.setClassName("test1");
		tp.setMessage("msg1");

		final StackTraceElement ste1 = new StackTraceElement("c1", "m1", "f1", 1);
		final StackTraceElement ste2 = new StackTraceElement("c2", "m2", "f2", 2);

		final StackTraceElementProxy[] stepArray = { new StackTraceElementProxy(ste1), new StackTraceElementProxy(ste2) };
		tp.setStackTraceElementProxyArray(stepArray);
		final DefaultThrowableRenderer renderer = (DefaultThrowableRenderer) layout.getThrowableRenderer();

		renderer.render(buf, tp);
		System.out.println(buf.toString());
		final String[] result = buf.toString().split(CoreConstants.LINE_SEPARATOR);
		System.out.println(result[0]);
		assertEquals("test1: msg1", result[0]);
		assertEquals(DefaultThrowableRenderer.TRACE_PREFIX + "at c1.m1(f1:1)", result[1]);
	}

	@Test
	public void testDoLayout() throws Exception {
		final ILoggingEvent le = createLoggingEvent();

		StringBuilder result = new StringBuilder().append(layout.getFileHeader());
		result.append(layout.getPresentationHeader());
		result.append(layout.doLayout(le));
		result.append(layout.getPresentationFooter());
		result.append(layout.getFileFooter());

		final Document doc = parseOutput(result.toString());
		final Element rootElement = doc.getRootElement();
		rootElement.toString();

		// the rest of this test is very dependent of the output generated
		// by HTMLLayout. Given that the XML parser already verifies
		// that the result conforms to xhtml-strict, we may want to
		// skip the assertions below. However, the assertions below are another
		// *independent* way to check the output format.

		// head, body
		assertEquals(2, rootElement.elements().size());
		final Element bodyElement = rootElement.elements().get(1);
		final Element tableElement = bodyElement.elements().get(3);
		assertEquals("table", tableElement.getName());
		final Element trElement = tableElement.elements().get(1);
		{
			final Element tdElement = trElement.elements().get(0);
			assertEquals("DEBUG", tdElement.getText());
		}
		{
			final Element tdElement = trElement.elements().get(1);
			final String regex = ClassicTestConstants.NAKED_MAIN_REGEX;
			System.out.println(tdElement.getText());
			assertTrue(tdElement.getText().matches(regex));
		}
		{
			final Element tdElement = trElement.elements().get(2);
			assertEquals("test message", tdElement.getText());
		}
	}

	@Test
	public void layoutWithException() throws Exception {
		layout.setPattern("%level %thread %msg %ex");
		final LoggingEvent le = createLoggingEvent();
		le.setThrowableProxy(new ThrowableProxy(new Exception("test Exception")));
		final String result = layout.doLayout(le);

		StringBuilder stringToParse = new StringBuilder().append(layout.getFileHeader());
		stringToParse.append(layout.getPresentationHeader());
		stringToParse.append(result);
		stringToParse.append("</table></body></html>");

		// System.out.println(stringToParse);

		final Document doc = parseOutput(stringToParse.toString());
		final Element rootElement = doc.getRootElement();
		final Element bodyElement = rootElement.element("body");
		final Element tableElement = bodyElement.element("table");
		final List<Element> trElementList = tableElement.elements();
		final Element exceptionRowElement = trElementList.get(2);
		final Element exceptionElement = exceptionRowElement.element("td");

		assertEquals(3, tableElement.elements().size());
		assertTrue(exceptionElement.getText().contains("java.lang.Exception: test Exception"));
	}

	@Test
	@Ignore
	public void rawLimit() throws Exception {
		final StringBuilder sb = new StringBuilder();
		final String header = layout.getFileHeader();
		assertTrue(header.startsWith("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">"));
		sb.append(header);
		sb.append(layout.getPresentationHeader());
		for (int i = 0; i < CoreConstants.TABLE_ROW_LIMIT * 3; i++) {
			sb.append(layout.doLayout(new LoggingEvent(this.getClass().getName(), root, Level.DEBUG, "test message" + i, null, null)));
		}
		sb.append(layout.getPresentationFooter());
		sb.append(layout.getFileFooter());
		// check that the output adheres to xhtml-strict.dtd
		parseOutput(sb.toString());
	}

	private LoggingEvent createLoggingEvent() {
		return new LoggingEvent(this.getClass().getName(), root, Level.DEBUG, "test message", null, null);
	}

	Document parseOutput(final String output) throws Exception {
		final EntityResolver resolver = new XHTMLEntityResolver();
		final SAXReader reader = new SAXReader();
		reader.setValidation(true);
		reader.setEntityResolver(resolver);
		return reader.read(new ByteArrayInputStream(output.getBytes()));
	}

	void configure(final String file) throws JoranException {
		final JoranConfigurator jc = new JoranConfigurator();
		jc.setContext(lc);
		jc.doConfigure(file);
	}

	@Test
	public void testConversionRuleSupportInHtmlLayout() throws JoranException {
		configure(ClassicTestConstants.JORAN_INPUT_PREFIX + "conversionRule/htmlLayout0.xml");

		root.getAppender("LIST");
		final String msg = "Simon says";
		root.debug(msg);
		final StringListAppender<ILoggingEvent> sla = (StringListAppender<ILoggingEvent>) root.getAppender("LIST");
		assertNotNull(sla);
		StatusPrinter.print(lc);
		assertEquals(1, sla.strList.size());
		assertFalse(sla.strList.get(0).contains("PARSER_ERROR"));
	}
}
