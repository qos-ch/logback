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

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Random;

import javax.mail.Part;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.dom4j.io.SAXReader;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.subethamail.smtp.AuthenticationHandler;
import org.subethamail.smtp.AuthenticationHandlerFactory;
import org.subethamail.smtp.auth.LoginAuthenticationHandler;
import org.subethamail.smtp.auth.LoginFailedException;
import org.subethamail.smtp.auth.PlainAuthenticationHandler;
import org.subethamail.smtp.auth.PluginAuthenticationHandler;
import org.subethamail.smtp.auth.UsernamePasswordValidator;
import org.subethamail.smtp.server.MessageListenerAdapter;
import org.subethamail.wiser.Wiser;
import org.subethamail.wiser.WiserMessage;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.html.HTMLLayout;
import ch.qos.logback.classic.html.XHTMLEntityResolver;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.Layout;
import ch.qos.logback.core.util.StatusPrinter;

public class SMTPAppender_SubethaSMTPTest {

  int diff = 1024 + new Random().nextInt(10000);
  Wiser wiser;

  SMTPAppender smtpAppender;
  LoggerContext lc = new LoggerContext();

  static final String TEST_SUBJECT = "test subject";
  static final String HEADER = "HEADER\n";
  static final String FOOTER = "FOOTER\n";

  @Before
  public void setUp() throws Exception { 
    wiser = new Wiser();
    wiser.setPort(diff); 
    wiser.getServer();
    wiser.start();
    //StartTLSCommand s;
    buildSMTPAppender();
  }

  void buildSMTPAppender() throws Exception {
    smtpAppender = new SMTPAppender();
    smtpAppender.setContext(lc);
    smtpAppender.setName("smtp");
    smtpAppender.setFrom("user@host.dom");
    smtpAppender.setSMTPHost("localhost");
    smtpAppender.setSMTPPort(diff);
    smtpAppender.setSubject(TEST_SUBJECT);
    smtpAppender.addTo("noreply@qos.ch");
  }

  private Layout<ILoggingEvent> buildPatternLayout(LoggerContext lc) {
    PatternLayout layout = new PatternLayout();
    layout.setContext(lc);
    layout.setFileHeader(HEADER);
    layout.setPattern("%-4relative [%thread] %-5level %logger %class - %msg%n");
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

  @After
  public void tearDown() throws Exception {
    wiser.stop();
  }

  private static String getWholeMessage(Part msg) {
    try {
      ByteArrayOutputStream bodyOut = new ByteArrayOutputStream();
      msg.writeTo(bodyOut);
      return bodyOut.toString("US-ASCII").trim();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
  
  private static String getBody(Part msg) {
    String all = getWholeMessage(msg);
    int i = all.indexOf("\r\n\r\n");
    return all.substring(i + 4, all.length());
}

  @Test
  public void smoke() throws Exception {
    smtpAppender.setLayout(buildPatternLayout(lc));
    smtpAppender.start();
    Logger logger = lc.getLogger("test");
    logger.addAppender(smtpAppender);
    logger.debug("hello");
    logger.error("en error", new Exception("an exception"));
    List<WiserMessage> wiserMsgList = wiser.getMessages();
    
    assertNotNull(wiserMsgList);
    assertEquals(1, wiserMsgList.size());
    WiserMessage wm = wiserMsgList.get(0);
    // http://jira.qos.ch/browse/LBCLASSIC-67
    MimeMessage mm = wm.getMimeMessage();
    assertEquals(TEST_SUBJECT, mm.getSubject());

    MimeMultipart mp = (MimeMultipart) mm.getContent();
    String body = getBody(mp.getBodyPart(0));
    System.out.println("["+body);
    assertTrue(body.startsWith(HEADER.trim()));
    assertTrue(body.endsWith(FOOTER.trim()));
  }

  @Test
  public void html() throws Exception {
    smtpAppender.setLayout(buildHTMLLayout(lc));
    smtpAppender.start();
    Logger logger = lc.getLogger("test");
    logger.addAppender(smtpAppender);
    logger.debug("hello");
    logger.error("en error", new Exception("an exception"));
    
    List<WiserMessage> wiserMsgList = wiser.getMessages();
    
    assertNotNull(wiserMsgList);
    assertEquals(1, wiserMsgList.size());
    WiserMessage wm = wiserMsgList.get(0);
    MimeMessage mm = wm.getMimeMessage();
    assertEquals(TEST_SUBJECT, mm.getSubject());

    MimeMultipart mp = (MimeMultipart) mm.getContent();

    // verify strict adherence to xhtml1-strict.dtd
    SAXReader reader = new SAXReader();
    reader.setValidation(true);
    reader.setEntityResolver(new XHTMLEntityResolver());
    reader.read(mp.getBodyPart(0).getInputStream());
    // System.out.println(GreenMailUtil.getBody(mp.getBodyPart(0)));
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
    smtpAppender.setLayout(buildHTMLLayout(lc));
    smtpAppender.start();
    Logger logger = lc.getLogger("test");
    logger.addAppender(smtpAppender);
    for (int i = 0; i < CoreConstants.TABLE_ROW_LIMIT * 3; i++) {
      logger.debug("hello " + i);
    }
    logger.error("en error", new Exception("an exception"));
    List<WiserMessage> wiserMsgList = wiser.getMessages();
    
    assertNotNull(wiserMsgList);
    assertEquals(1, wiserMsgList.size());
    WiserMessage wm = wiserMsgList.get(0);
    MimeMessage mm = wm.getMimeMessage();
    assertEquals(TEST_SUBJECT, mm.getSubject());

    MimeMultipart mp = (MimeMultipart) mm.getContent();

    // verify strict adherence to xhtml1-strict.dtd
    SAXReader reader = new SAXReader();
    reader.setValidation(true);
    reader.setEntityResolver(new XHTMLEntityResolver());
    reader.read(mp.getBodyPart(0).getInputStream());
  }
  
  @Test
  public void authenticated() throws Exception {
    MessageListenerAdapter mla = (MessageListenerAdapter)wiser.getServer().getMessageHandlerFactory();
    mla.setAuthenticationHandlerFactory(new TrivialAuthHandlerFactory());

    smtpAppender.setUsername("x");
    smtpAppender.setPassword("x");
    
    smtpAppender.setLayout(buildPatternLayout(lc));
    smtpAppender.start();
    Logger logger = lc.getLogger("test");
    logger.addAppender(smtpAppender);
    logger.debug("hello");
    logger.error("en error", new Exception("an exception"));

    List<WiserMessage> wiserMsgList = wiser.getMessages();

    assertNotNull(wiserMsgList);
    assertEquals(1, wiserMsgList.size());
    WiserMessage wm = wiserMsgList.get(0);
    // http://jira.qos.ch/browse/LBCLASSIC-67
    MimeMessage mm = wm.getMimeMessage();
    assertEquals(TEST_SUBJECT, mm.getSubject());

    MimeMultipart mp = (MimeMultipart) mm.getContent();
    String body = getBody(mp.getBodyPart(0));
    assertTrue(body.startsWith(HEADER.trim()));
    assertTrue(body.endsWith(FOOTER.trim()));
  }
  
  @Test
  @Ignore 
  // Unfortunately, there seems to be a problem with SubethaSMTP's implementation
  // of startTLS. The same SMTPAppender code works fine when tested with gmail.
  public void authenticatedSSL() throws Exception {
    MessageListenerAdapter mla = (MessageListenerAdapter)wiser.getServer().getMessageHandlerFactory();
    mla.setAuthenticationHandlerFactory(new TrivialAuthHandlerFactory());
    
    smtpAppender.setSTARTTLS(true);
    smtpAppender.setUsername("xx");
    smtpAppender.setPassword("xx");    
  
    smtpAppender.setLayout(buildPatternLayout(lc));
    smtpAppender.start();
    Logger logger = lc.getLogger("test");
    logger.addAppender(smtpAppender);
    logger.debug("hello");
    logger.error("en error", new Exception("an exception"));

    StatusPrinter.print(lc);
    List<WiserMessage> wiserMsgList = wiser.getMessages();

    assertNotNull(wiserMsgList);
    assertEquals(1, wiserMsgList.size());
  }
  
  @Test
  @Ignore
  public void authenticatedGmailStartTLS() throws Exception {
    smtpAppender.setSMTPHost("smtp.gmail.com");
    smtpAppender.setSMTPPort(587);
    
    smtpAppender.addTo("XXX@gmail.com");
    smtpAppender.setSTARTTLS(true);
    smtpAppender.setUsername("XXX@gmail.com");
    smtpAppender.setPassword("XXX");    
  
    smtpAppender.setLayout(buildPatternLayout(lc));
    smtpAppender.start();
    Logger logger = lc.getLogger("authenticatedGmailSTARTTLS");
    logger.addAppender(smtpAppender);
    logger.debug("hello");
    logger.error("en error", new Exception("an exception"));

    StatusPrinter.print(lc);
  }
  
  @Test
  @Ignore
  public void authenticatedGmail_SSL() throws Exception {
    smtpAppender.setSMTPHost("smtp.gmail.com");
    smtpAppender.setSMTPPort(465);
    
    smtpAppender.addTo("XXX@gmail.com");
    smtpAppender.setSSL(true);
    smtpAppender.setUsername("XXX@gmail.com");
    smtpAppender.setPassword("XXX");    
  
    smtpAppender.setLayout(buildPatternLayout(lc));
    smtpAppender.start();
    Logger logger = lc.getLogger("authenticatedGmail_SSL");
    logger.addAppender(smtpAppender);
    logger.debug("hello");
    logger.error("en error", new Exception("an exception"));

    StatusPrinter.print(lc);
  }
  
  public class TrivialAuthHandlerFactory implements AuthenticationHandlerFactory {
    public AuthenticationHandler create() {
      PluginAuthenticationHandler ret = new PluginAuthenticationHandler();
      UsernamePasswordValidator validator = new UsernamePasswordValidator() {
        public void login(String username, String password)
            throws LoginFailedException {
          if(!username.equals(password)) {
            throw new LoginFailedException("username="+username+", password="+password);
          }
        }
      };
      ret.addPlugin(new PlainAuthenticationHandler(validator));
      ret.addPlugin(new LoginAuthenticationHandler(validator));
      return ret;
    }
  }

}
