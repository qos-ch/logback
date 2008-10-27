package ch.qos.logback.classic.net;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Random;

import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.dom4j.io.SAXReader;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.html.HTMLLayout;
import ch.qos.logback.classic.html.XHTMLEntityResolver;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.CoreGlobal;
import ch.qos.logback.core.Layout;

import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.GreenMailUtil;
import com.icegreen.greenmail.util.ServerSetup;

public class SMTPAppender_GreenTest {

  int diff = 1024 + new Random().nextInt(10000);
  GreenMail greenMail;
  SMTPAppender smtpAppender;
  LoggerContext lc = new LoggerContext();

  static final String TEST_SUBJECT = "test subject";
  static final String HEADER = "HEADER\n";
  static final String FOOTER = "FOOTER\n";

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
    smtpAppender.setSMTPHost("localhost");
    smtpAppender.setSMTPPort(diff);
    smtpAppender.setSubject(TEST_SUBJECT);
    smtpAppender.addTo("nospam@qos.ch");
    // smtpAppender.start();
  }

  private Layout<LoggingEvent> buildPatternLayout(LoggerContext lc) {
    PatternLayout layout = new PatternLayout();
    layout.setContext(lc);
    layout.setFileHeader(HEADER);
    layout.setPattern("%-4relative [%thread] %-5level %class - %msg%n");
    layout.setFileFooter(FOOTER);
    layout.start();
    return layout;
  }

  private Layout<LoggingEvent> buildHTMLLayout(LoggerContext lc) {
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
  }

  @Test
  public void smoke() throws Exception {
    smtpAppender.setLayout(buildPatternLayout(lc));
    smtpAppender.start();
    Logger logger = lc.getLogger("test");
    logger.addAppender(smtpAppender);
    logger.debug("hello");
    logger.error("en error", new Exception("an exception"));
    MimeMessage[] mma = greenMail.getReceivedMessages();
    assertNotNull(mma);
    assertEquals(1, mma.length);
    MimeMessage mm = mma[0];
    // http://jira.qos.ch/browse/LBCLASSIC-67
    assertEquals(TEST_SUBJECT, mm.getSubject());

    MimeMultipart mp = (MimeMultipart) mm.getContent();
    String body = GreenMailUtil.getBody(mp.getBodyPart(0));
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
    MimeMessage[] mma = greenMail.getReceivedMessages();
    assertNotNull(mma);
    assertEquals(1, mma.length);
    MimeMessage mm = mma[0];
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
   * Note that SMTPAppender only keeps only 500 or so (=buffer size)
   * events. So the generated output will be rather short.
   */
  public void htmlLong() throws Exception {
    smtpAppender.setLayout(buildHTMLLayout(lc));
    smtpAppender.start();
    Logger logger = lc.getLogger("test");
    logger.addAppender(smtpAppender);
    for (int i = 0; i < CoreGlobal.TABLE_ROW_LIMIT * 3; i++) {
      logger.debug("hello " + i);
    }
    logger.error("en error", new Exception("an exception"));
    MimeMessage[] mma = greenMail.getReceivedMessages();
    assertNotNull(mma);
    assertEquals(1, mma.length);
    MimeMessage mm = mma[0];
    assertEquals(TEST_SUBJECT, mm.getSubject());

    MimeMultipart mp = (MimeMultipart) mm.getContent();

    // verify strict adherence to xhtml1-strict.dtd
    SAXReader reader = new SAXReader();
    reader.setValidation(true);
    reader.setEntityResolver(new XHTMLEntityResolver());
    reader.read(mp.getBodyPart(0).getInputStream());
  }

}
