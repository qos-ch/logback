package chapter5;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.MDC;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.util.StatusPrinter;

public class GoMDC {

  public static void main(String[] args)  {
    Logger logger = LoggerFactory
        .getLogger(GoMDC.class);
    LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();

    try {
      JoranConfigurator configurator = new JoranConfigurator();
      configurator.setContext(lc);
      configurator.doConfigure("mdcFilter.xml");
      
    } catch (JoranException je) {
      StatusPrinter.print(lc);
    }

    logger.debug("I know me " + 0);
    MDC.put("key", "val");
    logger.debug("I know me " + 1);
    
    StatusPrinter.print(lc);
  }
}