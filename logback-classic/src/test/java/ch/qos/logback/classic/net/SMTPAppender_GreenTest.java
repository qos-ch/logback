/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2009, QOS.ch. All rights reserved.
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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.dom4j.io.SAXReader;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.MDC;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.html.HTMLLayout;
import ch.qos.logback.classic.html.XHTMLEntityResolver;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.util.TeztConstants;
import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.Layout;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.testUtil.RandomUtil;
import ch.qos.logback.core.util.StatusPrinter;

import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.GreenMailUtil;
import com.icegreen.greenmail.util.ServerSetup;

public class SMTPAppender_GreenTest {

  int port = RandomUtil.getRandomServerPort();
  GreenMail greenMail;
  SMTPAppender smtpAppender;
  LoggerContext lc = new LoggerContext();
  Logger logger = lc.getLogger(this.getClass());
  
  static final String TEST_SUBJECT = "test subject";
  static final String HEADER = "HEADER\n";
  static final String FOOTER = "FOOTER\n";

  @Before
  public void setUp() throws Exception {
    MDC.clear();
    ServerSetup serverSetup = new ServerSetup(port, "localhost",
        ServerSetup.PROTOCOL_SMTP);
    greenMail = new GreenMail(serverSetup);
    greenMail.start();
    // let the grean mail server get a head start
    Thread.sleep(100);
  }
  
  @After
  public void tearDown() throws Exception {
    greenMail.stop();
  }
  

  void buildSMTPAppender() throws Exception {
    smtpAppender = new SMTPAppender();
    smtpAppender.setContext(lc);
    smtpAppender.setName("smtp");
    smtpAppender.setFrom("user@host.dom");
    smtpAppender.setSMTPHost("localhost");
    smtpAppender.setSMTPPort(port);
    smtpAppender.setSubject(TEST_SUBJECT);
    smtpAppender.addTo("nospam@qos.ch");
    // smtpAppender.start();
  }

  private Layout<ILoggingEvent> buildPatternLayout(LoggerContext lc) {
    PatternLayout layout = new PatternLayout();
    layout.setContext(lc);
    layout.setFileHeader(HEADER);
    layout.setPattern("%-4relative %mdc [%thread] %-5level %class - %msg%n");
    layout.setFileFooter(FOOTER);
    layout.start();
    return layout;
  }

  private Layout<ILoggingEvent> buildHTMLLayout(LoggerContext lc) {
    HTMLLayout layout = new HTMLLayout();
    layout.setContext(lc);
    // layout.setFileHeader(HEADER);
    layout.setPattern("%level%class%msg");
    // layout.setFileFooter(FOOTER);
    layout.start();
    return layout;
  }



  private MimeMultipart verify(String subject) throws MessagingException, IOException {
    MimeMessage[] mma = greenMail.getReceivedMessages();
    assertNotNull(mma);
    assertEquals(1, mma.length);
    MimeMessage mm = mma[0];
    // http://jira.qos.ch/browse/LBCLASSIC-67
    assertEquals(subject, mm.getSubject());
    return (MimeMultipart) mm.getContent();
  }

  @Test
  public void smoke() throws Exception {
    buildSMTPAppender();
    smtpAppender.setLayout(buildPatternLayout(lc));
    smtpAppender.start();
    logger.addAppender(smtpAppender);
    logger.debug("hello");
    logger.error("en error", new Exception("an exception"));
    
    StatusPrinter.print(lc);
    MimeMultipart mp =  verify(TEST_SUBJECT);
    String body = GreenMailUtil.getBody(mp.getBodyPart(0));
    assertTrue(body.startsWith(HEADER.trim()));
    assertTrue(body.endsWith(FOOTER.trim()));
  }

  @Test
  public void LBCLASSIC_104() throws Exception {
    buildSMTPAppender();
    smtpAppender.setLayout(buildPatternLayout(lc));
    smtpAppender.start();
    logger.addAppender(smtpAppender);
    MDC.put("key", "val");
    logger.debug("hello");
    MDC.clear();
    logger.error("en error", new Exception("an exception"));
    
    MimeMultipart mp =  verify(TEST_SUBJECT);
    String body = GreenMailUtil.getBody(mp.getBodyPart(0));
    assertTrue(body.startsWith(HEADER.trim()));
    assertTrue(body.contains("key=val"));
    assertTrue(body.endsWith(FOOTER.trim()));
  }

  
  @Test
  public void html() throws Exception {
    buildSMTPAppender();
    smtpAppender.setLayout(buildHTMLLayout(lc));
    smtpAppender.start();
    logger.addAppender(smtpAppender);
    logger.debug("hello");
    logger.error("en error", new Exception("an exception"));
    MimeMultipart mp =  verify(TEST_SUBJECT);
    
    // verify strict adherence to xhtml1-strict.dtd
    SAXReader reader = new SAXReader();
    reader.setValidation(true);
    reader.setEntityResolver(new XHTMLEntityResolver());
    reader.read(mp.getBodyPart(0).getInputStream());

  }

  @Test
  /**
   * Checks that even when many events are processed, the output is still
   * conforms to xhtml-strict.dtd.
   * 
   * Note that SMTPAppender only keeps only 500 or so (=buffer size) events. So
   * the generated output will be rather short.
   */
  public void htmlLong() throws Exception {
    buildSMTPAppender();
    smtpAppender.setLayout(buildHTMLLayout(lc));
    smtpAppender.start();
    logger.addAppender(smtpAppender);
    for (int i = 0; i < CoreConstants.TABLE_ROW_LIMIT * 3; i++) {
      logger.debug("hello " + i);
    }
    logger.error("en error", new Exception("an exception"));
    
    MimeMultipart mp =  verify(TEST_SUBJECT);
    
    // verify strict adherence to xhtml1-strict.dtd
    SAXReader reader = new SAXReader();
    reader.setValidation(true);
    reader.setEntityResolver(new XHTMLEntityResolver());
    reader.read(mp.getBodyPart(0).getInputStream());
  }

  private void configure(String file) throws JoranException {
    JoranConfigurator jc = new JoranConfigurator();
    jc.setContext(lc);
    System.out.println("port="+port);
    lc.putProperty("port", "" + port);
    jc.doConfigure(file);
  }

  @Test
  public void testCustomEvaluator() throws  Exception {
    configure(TeztConstants.TEST_DIR_PREFIX
        + "input/joran/smtp/customEvaluator.xml");
    
    logger.debug("hello");
    String msg2 = "world";
    logger.debug(msg2);
    logger.debug("invisible");
    MimeMultipart mp =  verify(this.getClass().getName()+ " - "+msg2);
    String body = GreenMailUtil.getBody(mp.getBodyPart(0));
    assertEquals("helloworld", body);
  }
}
