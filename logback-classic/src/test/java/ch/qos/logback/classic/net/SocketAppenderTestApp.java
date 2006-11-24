package ch.qos.logback.classic.net;

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.util.StatusPrinter;

public class SocketAppenderTestApp {

  public static void main(String[] args) {

    Logger logger = (Logger) LoggerFactory
        .getLogger(SocketAppenderTestApp.class);
    LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
    SocketAppender appender = new SocketAppender("localhost", 4560);
    appender.setContext(lc);
    appender.setName("socket");
    appender.start();

    logger.addAppender(appender);

    for (int i = 0; i <= 1000; i++) {
      logger.debug("** Hello world. n=" + i);
    }

    StatusPrinter.print(lc.getStatusManager());

  }
}
