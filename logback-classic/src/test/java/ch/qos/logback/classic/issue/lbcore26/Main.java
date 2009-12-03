package ch.qos.logback.classic.issue.lbcore26;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.ClassicTestConstants;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.util.StatusPrinter;

public class Main {

  public static void main(String[] args) throws JoranException {

    LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
    JoranConfigurator configurator = new JoranConfigurator();
    configurator.setContext(lc);
    configurator.doConfigure(ClassicTestConstants.INPUT_PREFIX
        + "issue/lbcore26.xml");

    StatusPrinter.printInCaseOfErrorsOrWarnings(lc);
    Logger logger = LoggerFactory.getLogger(Main.class);
    for (int i = 0; i < 16; i++) {
      logger.info("hello " + new Date());
    }

  }

}
