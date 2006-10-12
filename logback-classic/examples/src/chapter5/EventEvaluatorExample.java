package chapter5;

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.util.StatusPrinter;

public class EventEvaluatorExample {

  public static void main(String[] args) throws InterruptedException {
    Logger logger = (Logger) LoggerFactory.getLogger(EventEvaluatorExample.class);
    LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();

    JoranConfigurator configurator = new JoranConfigurator();
    configurator.setContext(lc);
    configurator.doConfigure(args[0]);

    StatusPrinter.print(lc);
    
    for (int i = 0; i < 5; i++) {
      if (i == 3) {
        logger.debug("stacktrace logging statement" + i);
      } else {
        logger.debug("logging statement" + i);
      }
    }
    
    StatusPrinter.print(lc);
  }
}