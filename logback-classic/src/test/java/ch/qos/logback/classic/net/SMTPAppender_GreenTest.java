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
package ch.qos.logback.classic.net;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.MDC;

import com.icegreen.greenmail.util.DummySSLSocketFactory;
import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.GreenMailUtil;
import com.icegreen.greenmail.util.ServerSetup;

import ch.qos.logback.classic.ClassicTestConstants;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.html.HTMLLayout;
import ch.qos.logback.classic.html.XHTMLEntityResolver;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Layout;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.status.OnConsoleStatusListener;
import ch.qos.logback.core.testUtil.RandomUtil;
import ch.qos.logback.core.util.StatusListenerConfigHelper;
import ch.qos.logback.core.util.StatusPrinter;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;

public class SMTPAppender_GreenTest {

	static boolean NO_SSL = false;
	static boolean WITH_SSL = true;

	static final String HEADER = "HEADER\n";
	static final String FOOTER = "FOOTER\n";
	static final String DEFAULT_PATTERN = "%-4relative %mdc [%thread] %-5level %class - %msg%n";

	static final boolean SYNCHRONOUS = false;
	static final boolean ASYNCHRONOUS = true;

	int port = RandomUtil.getRandomServerPort();
	// GreenMail cannot be static. As a shared server induces race conditions
	GreenMail greenMailServer;

	SMTPAppender smtpAppender;
	LoggerContext loggerContext = new LoggerContext();
	Logger logger = loggerContext.getLogger(this.getClass());

	static String REQUIRED_USERNAME = "alice";
	static String REQUIRED_PASSWORD = "alicepass";

	@Before
	public void setUp() throws Exception {
		StatusListenerConfigHelper.addOnConsoleListenerInstance(loggerContext, new OnConsoleStatusListener());
		MDC.clear();
	}

	void startSMTPServer(boolean withSSL) {
		ServerSetup serverSetup;

		if (withSSL) {
			serverSetup = new ServerSetup(port, null, ServerSetup.PROTOCOL_SMTPS);
		} else {
			serverSetup = new ServerSetup(port, null, ServerSetup.PROTOCOL_SMTP);
		}
		greenMailServer = new GreenMail(serverSetup);
		// user password is checked for the specified user ONLY
		greenMailServer.setUser(REQUIRED_USERNAME, REQUIRED_PASSWORD);
		greenMailServer.start();
		// give the server a head start
		try {
			Thread.sleep(10);
		} catch (InterruptedException e) {
		}
	}

	@After
	public void tearDown() throws Exception {
		greenMailServer.stop();
	}

	void buildSMTPAppender(String subject, boolean synchronicity) throws Exception {
		smtpAppender = new SMTPAppender();
		smtpAppender.setContext(loggerContext);
		smtpAppender.setName("smtp");
		smtpAppender.setFrom("user@host.dom");
		smtpAppender.setSMTPHost("localhost");
		smtpAppender.setSMTPPort(port);
		smtpAppender.setSubject(subject);
		smtpAppender.addTo("nospam@qos.ch");
		smtpAppender.setAsynchronousSending(synchronicity);
	}

	private Layout<ILoggingEvent> buildPatternLayout(String pattern) {
		PatternLayout layout = new PatternLayout();
		layout.setContext(loggerContext);
		layout.setFileHeader(HEADER);
		layout.setOutputPatternAsHeader(false);
		layout.setPattern(pattern);
		layout.setFileFooter(FOOTER);
		layout.start();
		return layout;
	}

	private Layout<ILoggingEvent> buildHTMLLayout() {
		HTMLLayout layout = new HTMLLayout();
		layout.setContext(loggerContext);
		layout.setPattern("%level%class%msg");
		layout.start();
		return layout;
	}

	private void waitForServerToReceiveEmails(int emailCount) throws InterruptedException {
		greenMailServer.waitForIncomingEmail(5000, emailCount);
	}

	private MimeMultipart verifyAndExtractMimeMultipart(String subject)
			throws MessagingException, IOException, InterruptedException {
		int oldCount = 0;
		int expectedEmailCount = 1;
		// wait for the server to receive the messages
		waitForServerToReceiveEmails(expectedEmailCount);
		MimeMessage[] mma = greenMailServer.getReceivedMessages();
		assertNotNull(mma);
		assertEquals(expectedEmailCount, mma.length);
		MimeMessage mm = mma[oldCount];
		// http://jira.qos.ch/browse/LBCLASSIC-67
		assertEquals(subject, mm.getSubject());
		return (MimeMultipart) mm.getContent();
	}

	void waitUntilEmailIsSent() throws InterruptedException {
		loggerContext.getScheduledExecutorService().shutdown();
		loggerContext.getScheduledExecutorService().awaitTermination(1000, TimeUnit.MILLISECONDS);
	}

	@Test
	public void synchronousSmoke() throws Exception {
		startSMTPServer(NO_SSL);
		String subject = "synchronousSmoke";
		buildSMTPAppender(subject, SYNCHRONOUS);

		smtpAppender.setLayout(buildPatternLayout(DEFAULT_PATTERN));

		smtpAppender.start();

		logger.addAppender(smtpAppender);
		logger.debug("hello");
		logger.error("en error", new Exception("an exception"));

		MimeMultipart mp = verifyAndExtractMimeMultipart(subject);
		String body = GreenMailUtil.getBody(mp.getBodyPart(0));
		assertTrue(body.startsWith(HEADER.trim()));
		assertTrue(body.endsWith(FOOTER.trim()));
	}

	@Test
	public void asynchronousSmoke() throws Exception {
		startSMTPServer(NO_SSL);

		String subject = "asynchronousSmoke";
		buildSMTPAppender(subject, ASYNCHRONOUS);
		smtpAppender.setLayout(buildPatternLayout(DEFAULT_PATTERN));
		smtpAppender.start();

		logger.addAppender(smtpAppender);
		logger.debug("hello");
		logger.error("en error", new Exception("an exception"));

		waitUntilEmailIsSent();
		MimeMultipart mp = verifyAndExtractMimeMultipart(subject);
		String body = GreenMailUtil.getBody(mp.getBodyPart(0));
		assertTrue(body.startsWith(HEADER.trim()));
		assertTrue(body.endsWith(FOOTER.trim()));
	}

	// See also http://jira.qos.ch/browse/LOGBACK-734
	@Test
	public void callerDataShouldBeCorrectlySetWithAsynchronousSending() throws Exception {
		startSMTPServer(NO_SSL);
		String subject = "LOGBACK-734";
		buildSMTPAppender("LOGBACK-734", ASYNCHRONOUS);
		smtpAppender.setLayout(buildPatternLayout(DEFAULT_PATTERN));
		smtpAppender.setIncludeCallerData(true);
		smtpAppender.start();
		logger.addAppender(smtpAppender);
		logger.debug("LOGBACK-734");
		logger.error("callerData", new Exception("ShouldBeCorrectlySetWithAsynchronousSending"));

		waitUntilEmailIsSent();
		MimeMultipart mp = verifyAndExtractMimeMultipart(subject);
		String body = GreenMailUtil.getBody(mp.getBodyPart(0));
		assertTrue("actual [" + body + "]", body.contains("DEBUG " + this.getClass().getName() + " - LOGBACK-734"));
	}

	// lost MDC
	@Test
	public void LOGBACK_352() throws Exception {
		startSMTPServer(NO_SSL);
		String subject = "LOGBACK_352";
		buildSMTPAppender(subject, SYNCHRONOUS);
		smtpAppender.setAsynchronousSending(false);
		smtpAppender.setLayout(buildPatternLayout(DEFAULT_PATTERN));
		smtpAppender.start();
		logger.addAppender(smtpAppender);
		MDC.put("key", "val");
		logger.debug("LBCLASSIC_104");
		MDC.clear();
		logger.error("en error", new Exception("test"));

		MimeMultipart mp = verifyAndExtractMimeMultipart(subject);
		String body = GreenMailUtil.getBody(mp.getBodyPart(0));
		assertTrue(body.startsWith(HEADER.trim()));
		System.out.println(body);
		assertTrue(body.contains("key=val"));
		assertTrue(body.endsWith(FOOTER.trim()));
	}

	@Test
	public void html() throws Exception {
		startSMTPServer(NO_SSL);
		String subject = "html";
		buildSMTPAppender(subject, SYNCHRONOUS);
		smtpAppender.setAsynchronousSending(false);
		smtpAppender.setLayout(buildHTMLLayout());
		smtpAppender.start();
		logger.addAppender(smtpAppender);
		logger.debug("html");
		logger.error("en error", new Exception("an exception"));

		MimeMultipart mp = verifyAndExtractMimeMultipart(subject);

		// verifyAndExtractMimeMultipart strict adherence to xhtml1-strict.dtd
		SAXReader reader = new SAXReader();
		reader.setValidation(true);
		reader.setEntityResolver(new XHTMLEntityResolver());
		byte[] messageBytes = getAsByteArray(mp.getBodyPart(0).getInputStream());
		ByteArrayInputStream bais = new ByteArrayInputStream(messageBytes);
		try {
			reader.read(bais);
		} catch (DocumentException de) {
			System.out.println("incoming message:");
			System.out.println(new String(messageBytes));
			throw de;
		}
		System.out.println("incoming message:");
		System.out.println(new String(messageBytes));
	}

	private byte[] getAsByteArray(InputStream inputStream) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		byte[] buffer = new byte[1024];
		int n = -1;
		while ((n = inputStream.read(buffer)) != -1) {
			baos.write(buffer, 0, n);
		}
		return baos.toByteArray();
	}

	private void configure(String file) throws JoranException {
		JoranConfigurator jc = new JoranConfigurator();
		jc.setContext(loggerContext);
		loggerContext.putProperty("port", "" + port);
		jc.doConfigure(file);
	}

	@Test
	public void testCustomEvaluator() throws Exception {
		startSMTPServer(NO_SSL);
		configure(ClassicTestConstants.JORAN_INPUT_PREFIX + "smtp/customEvaluator.xml");

		logger.debug("test");
		String msg2 = "CustomEvaluator";
		logger.debug(msg2);
		logger.debug("invisible");
		waitUntilEmailIsSent();
		MimeMultipart mp = verifyAndExtractMimeMultipart(
				"testCustomEvaluator " + this.getClass().getName() + " - " + msg2);
		String body = GreenMailUtil.getBody(mp.getBodyPart(0));
		assertEquals("testCustomEvaluator", body);
	}

	@Test
	public void testCustomBufferSize() throws Exception {
		startSMTPServer(NO_SSL);
		configure(ClassicTestConstants.JORAN_INPUT_PREFIX + "smtp/customBufferSize.xml");

		logger.debug("invisible1");
		logger.debug("invisible2");
		String msg = "hello";
		logger.error(msg);
		waitUntilEmailIsSent();
		MimeMultipart mp = verifyAndExtractMimeMultipart(
				"testCustomBufferSize " + this.getClass().getName() + " - " + msg);
		String body = GreenMailUtil.getBody(mp.getBodyPart(0));
		assertEquals(msg, body);
	}

	// this test fails intermittently on Jenkins.
	@Test
	public void testMultipleTo() throws Exception {
		startSMTPServer(NO_SSL);
		buildSMTPAppender("testMultipleTo", SYNCHRONOUS);
		smtpAppender.setLayout(buildPatternLayout(DEFAULT_PATTERN));
		// buildSMTPAppender() already added one destination address
		smtpAppender.addTo("Test <test@example.com>, other-test@example.com");
		smtpAppender.start();
		logger.addAppender(smtpAppender);
		logger.debug("testMultipleTo hello");
		logger.error("testMultipleTo en error", new Exception("an exception"));
		Thread.yield();
		int expectedEmailCount = 3;
		waitForServerToReceiveEmails(expectedEmailCount);
		MimeMessage[] mma = greenMailServer.getReceivedMessages();
		assertNotNull(mma);
		assertEquals(expectedEmailCount, mma.length);
	}

	// http://jira.qos.ch/browse/LBCLASSIC-221
	@Test
	public void bufferShouldBeResetBetweenMessages() throws Exception {
		startSMTPServer(NO_SSL);
		buildSMTPAppender("bufferShouldBeResetBetweenMessages", SYNCHRONOUS);
		smtpAppender.setLayout(buildPatternLayout(DEFAULT_PATTERN));
		smtpAppender.start();
		logger.addAppender(smtpAppender);
		String msg0 = "hello zero";
		logger.debug(msg0);
		logger.error("error zero");

		String msg1 = "hello one";
		logger.debug(msg1);
		logger.error("error one");

		Thread.yield();
		int oldCount = 0;
		int expectedEmailCount = oldCount + 2;
		waitForServerToReceiveEmails(expectedEmailCount);

		MimeMessage[] mma = greenMailServer.getReceivedMessages();
		assertNotNull(mma);
		assertEquals(expectedEmailCount, mma.length);

		MimeMessage mm0 = mma[oldCount];
		MimeMultipart content0 = (MimeMultipart) mm0.getContent();
		@SuppressWarnings("unused")
		String body0 = GreenMailUtil.getBody(content0.getBodyPart(0));

		MimeMessage mm1 = mma[oldCount + 1];
		MimeMultipart content1 = (MimeMultipart) mm1.getContent();
		String body1 = GreenMailUtil.getBody(content1.getBodyPart(0));
		// second body should not contain content from first message
		assertFalse(body1.contains(msg0));
	}

	@Test
	public void multiLineSubjectTruncatedAtFirstNewLine() throws Exception {
		startSMTPServer(NO_SSL);
		String line1 = "line 1 of subject";
		String subject = line1 + "\nline 2 of subject\n";
		buildSMTPAppender(subject, ASYNCHRONOUS);

		smtpAppender.setLayout(buildPatternLayout(DEFAULT_PATTERN));
		smtpAppender.start();
		logger.addAppender(smtpAppender);
		logger.debug("hello");
		logger.error("en error", new Exception("an exception"));

		Thread.yield();
		waitUntilEmailIsSent();
		waitForServerToReceiveEmails(1);

		MimeMessage[] mma = greenMailServer.getReceivedMessages();
		assertEquals(1, mma.length);
		assertEquals(line1, mma[0].getSubject());
	}

	@Test
	public void authenticated() throws Exception {
		startSMTPServer(NO_SSL);
		buildSMTPAppender("testMultipleTo", SYNCHRONOUS);
		smtpAppender.setUsername(REQUIRED_USERNAME);
		smtpAppender.setPassword(REQUIRED_PASSWORD);

		smtpAppender.setLayout(buildPatternLayout(DEFAULT_PATTERN));
		smtpAppender.start();

		logger.addAppender(smtpAppender);
		logger.debug("authenticated");
		logger.error("authenticated en error", new Exception("an exception"));

		waitUntilEmailIsSent();
		waitForServerToReceiveEmails(1);

		MimeMessage[] mma = greenMailServer.getReceivedMessages();
		assertNotNull(mma);
		assertTrue("body should not be empty", mma.length == 1);
	}

	void setSystemPropertiesForStartTLS() {
		String PREFIX = "mail.smtp.";
		System.setProperty(PREFIX + "starttls.enable", "true");
		System.setProperty(PREFIX + "socketFactory.class", DummySSLSocketFactory.class.getName());
		System.setProperty(PREFIX + "socketFactory.fallback", "false");
	}

	void unsetSystemPropertiesForStartTLS() {
		String PREFIX = "mail.smtp.";
		System.clearProperty(PREFIX + "starttls.enable");
		System.clearProperty(PREFIX + "socketFactory.class");
		System.clearProperty(PREFIX + "socketFactory.fallback");
	}

	@Test
	public void authenticatedSSL() throws Exception {
		try {
			setSystemPropertiesForStartTLS();
		
		startSMTPServer(WITH_SSL);
		buildSMTPAppender("testMultipleTo", SYNCHRONOUS);
		smtpAppender.setUsername(REQUIRED_USERNAME);
		smtpAppender.setPassword(REQUIRED_PASSWORD);
		smtpAppender.setSTARTTLS(true);
		smtpAppender.setLayout(buildPatternLayout(DEFAULT_PATTERN));
		smtpAppender.start();

		logger.addAppender(smtpAppender);
		logger.debug("authenticated");
		logger.error("authenticated en error", new Exception("an exception"));

		waitUntilEmailIsSent();
		waitForServerToReceiveEmails(1);

		MimeMessage[] mma = greenMailServer.getReceivedMessages();
		assertNotNull(mma);
		assertTrue("body should not be empty", mma.length == 1);
		} finally {
			unsetSystemPropertiesForStartTLS();
		}
	}

	// ==============================================================================
	// IGNORED
	// ==============================================================================
	static String GMAIL_USER_NAME = "xx@gmail.com";
	static String GMAIL_PASSWORD = "xxx";

	@Ignore
	@Test
	public void authenticatedGmailStartTLS() throws Exception {
		smtpAppender.setSMTPHost("smtp.gmail.com");
		smtpAppender.setSMTPPort(587);
		smtpAppender.setAsynchronousSending(false);
		smtpAppender.addTo(GMAIL_USER_NAME);

		smtpAppender.setSTARTTLS(true);
		smtpAppender.setUsername(GMAIL_USER_NAME);
		smtpAppender.setPassword(GMAIL_PASSWORD);

		smtpAppender.setLayout(buildPatternLayout(DEFAULT_PATTERN));
		smtpAppender.setSubject("authenticatedGmailStartTLS - %level %logger{20} - %m");
		smtpAppender.start();
		Logger logger = loggerContext.getLogger("authenticatedGmailSTARTTLS");
		logger.addAppender(smtpAppender);
		logger.debug("authenticatedGmailStartTLS =- hello");
		logger.error("en error", new Exception("an exception"));

		StatusPrinter.print(loggerContext);
	}

	@Ignore
	@Test
	public void authenticatedGmail_SSL() throws Exception {
		smtpAppender.setSMTPHost("smtp.gmail.com");
		smtpAppender.setSMTPPort(465);
		smtpAppender.setSubject("authenticatedGmail_SSL - %level %logger{20} - %m");
		smtpAppender.addTo(GMAIL_USER_NAME);
		smtpAppender.setSSL(true);
		smtpAppender.setUsername(GMAIL_USER_NAME);
		smtpAppender.setPassword(GMAIL_PASSWORD);
		smtpAppender.setAsynchronousSending(false);
		smtpAppender.setLayout(buildPatternLayout(DEFAULT_PATTERN));
		smtpAppender.start();
		Logger logger = loggerContext.getLogger("authenticatedGmail_SSL");
		logger.addAppender(smtpAppender);
		logger.debug("hello" + new java.util.Date());
		logger.error("en error", new Exception("an exception"));

		StatusPrinter.print(loggerContext);

	}
}
