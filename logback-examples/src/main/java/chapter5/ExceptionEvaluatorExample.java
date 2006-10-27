package chapter5;

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.util.StatusPrinter;

public class ExceptionEvaluatorExample {

  public static void main(String[] args) {
    Logger logger = (Logger) LoggerFactory
        .getLogger(ExceptionEvaluatorExample.class);
    LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();

    try {
      JoranConfigurator configurator = new JoranConfigurator();
      configurator.setContext(lc);
      configurator.doConfigure(args[0]);
    } catch (JoranException je) {
      StatusPrinter.print(lc);
    }
    for (int i = 0; i < 5; i++) {
      if (i == 3) {
        logger.debug("logging statement " + i, new TestException(
            "do not display this"));
      } else {
        logger.debug("logging statement " + i, new Exception("display"));
      }
    }

    StatusPrinter.print(lc);
  }
}