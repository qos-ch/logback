package ch.qos.logback.classic.net;

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.MDC;
import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.core.Layout;
import ch.qos.logback.core.util.StatusPrinter;

public class SMTPAppenderTestApp {

  public static void main(String[] args) {

    Logger logger = (Logger) LoggerFactory
        .getLogger(SocketAppenderTestApp.class);
    LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
    MDC.put("key", "testValue");
    SMTPAppender appender = new SMTPAppender();
    appender.setContext(lc);
    appender.setName("smtp");
    appender.setFrom("user@host.dom");
    appender.setLayout(buildLayout(lc));
    appender.setSMTPHost("mail.qos.ch");
    appender.setSubject("logging report");
    appender.setTo("sebastien.nospam@qos.ch");

    appender.start();

    logger.addAppender(appender);

    for (int i = 0; i <= 10; i++) {
      logger.debug("** Hello world. n=" + i);
    }
    logger.error("Triggering request");

    StatusPrinter.print(lc.getStatusManager());
  }

  private static Layout buildLayout(LoggerContext lc) {
    PatternLayout layout = new PatternLayout();
    layout.setContext(lc);
    layout.setHeader("Some header\n");
    layout.setPattern("%-4relative [%thread] %-5level %class - %msg %X{test}%n");
    layout.setFooter("Some footer");
    layout.start();
    return layout;
  }

}
