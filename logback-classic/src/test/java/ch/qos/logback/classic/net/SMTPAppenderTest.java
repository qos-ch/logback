package ch.qos.logback.classic.net;

import javax.mail.Address;
import javax.mail.MessagingException;

import junit.framework.TestCase;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.Layout;

public class SMTPAppenderTest extends TestCase {

  SMTPAppender appender;

  public void setUp() throws Exception {
    super.setUp();
    LoggerContext lc = new LoggerContext();
    appender = new SMTPAppender();
    appender.setContext(lc);
    appender.setName("smtp");
    appender.setFrom("user@host.dom");
    appender.setLayout(buildLayout(lc));
    appender.setSMTPHost("mail2.qos.ch");
    appender.setSubject("logging report");
    appender.setTo("sebastien.nospam@qos.ch");
    appender.start();
  }

  public void tearDown() throws Exception {
    super.tearDown();
    appender = null;
  }

  public void testStart() {
    try {
      Address[] addressArray = appender.getMessage().getFrom();
      Address address = addressArray[0];
      assertEquals("user@host.dom", address.toString());

      addressArray = null;
      address = null;

      addressArray = appender.getMessage().getAllRecipients();
      address = addressArray[0];
      assertEquals("sebastien.nospam@qos.ch", address.toString());

      assertEquals("logging report", appender.getSubject());

      assertTrue(appender.isStarted());

    } catch (MessagingException ex) {
      fail("Unexpected exception.");
    }
  }

  public void testAppendNonTriggeringEvent() {
    LoggingEvent event = new LoggingEvent();
    event.setThreadName("thead name");
    event.setLevel(Level.DEBUG);
    appender.subAppend(event);
    assertEquals(1, appender.cb.length());
  }

  public void testEntryConditionsCheck() {
    appender.checkEntryConditions();
    assertEquals(0, appender.getContext().getStatusManager().getCount());
  }

  public void testEntryConditionsCheckNoMessage() {
    appender.setMessage(null);
    appender.checkEntryConditions();
    assertEquals(1, appender.getContext().getStatusManager().getCount());
  }

  public void setTriggeringPolicy() {
    appender.setEvaluator(null);
    appender.checkEntryConditions();
    assertEquals(1, appender.getContext().getStatusManager().getCount());
  }

  public void testEntryConditionsCheckNoLayout() {
    appender.setLayout(null);
    appender.checkEntryConditions();
    assertEquals(1, appender.getContext().getStatusManager().getCount());
  }

  private static Layout buildLayout(LoggerContext lc) {
    PatternLayout layout = new PatternLayout();
    layout.setContext(lc);
    layout.setHeader("Some header\n");
    layout.setPattern("%-4relative [%thread] %-5level %class - %msg%n");
    layout.setFooter("Some footer");
    layout.start();
    return layout;
  }
}
