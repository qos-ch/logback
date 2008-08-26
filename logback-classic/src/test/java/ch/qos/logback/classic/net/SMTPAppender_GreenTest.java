package ch.qos.logback.classic.net;

import static org.junit.Assert.*;

import java.util.Random;

import javax.mail.internet.MimeMessage;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.Layout;

import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetup;

public class SMTPAppender_GreenTest {

  int diff = 1024 + new Random().nextInt(10000);
  GreenMail greenMail;
  SMTPAppender smtpAppender;
  LoggerContext lc = new LoggerContext();

  static final String TEST_SUBJECT = "test subject";
  
  
  @Before
  public void setUp() throws Exception {
    ServerSetup serverSetup = new ServerSetup(diff, "localhost",
        ServerSetup.PROTOCOL_SMTP);
    greenMail = new GreenMail(serverSetup);
    greenMail.start();
    buildSMTPAppender();
  }

  void buildSMTPAppender() throws Exception {
    smtpAppender = new SMTPAppender();
    smtpAppender.setContext(lc);
    smtpAppender.setName("smtp");
    smtpAppender.setFrom("user@host.dom");

    smtpAppender.setLayout(buildLayout(lc));
    smtpAppender.setSMTPHost("localhost");
    smtpAppender.setSMTPPort(diff);
    smtpAppender.setSubject(TEST_SUBJECT);
    smtpAppender.addTo("nospam@qos.ch");
//    smtpAppender.start();
  }

  private Layout<LoggingEvent> buildLayout(LoggerContext lc) {
    PatternLayout layout = new PatternLayout();
    layout.setContext(lc);
    layout.setFileHeader("Some header\n");
    layout.setPattern("%-4relative [%thread] %-5level %class - %msg%n");
    layout.setFileFooter("Some footer");
    layout.start();
    return layout;
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void smoke() throws Exception  {
    smtpAppender.start();
    Logger logger = lc.getLogger("test");
    logger.addAppender(smtpAppender);
    logger.debug("hello");
    logger.error("en error", new Exception("an exception"));
    MimeMessage[] mma = greenMail.getReceivedMessages();
    assertNotNull(mma);
    assertEquals(1, mma.length);
    MimeMessage mm = mma[0];
    
    assertEquals(TEST_SUBJECT, mm.getSubject());
    //System.out.println(mm.getContent().toString());
    
  }

}
