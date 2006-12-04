package chapter6;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.MDC;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;

public class FilterEvents {

  public static void main(String[] args) throws InterruptedException {
    Logger logger = (Logger) LoggerFactory.getLogger(FilterEvents.class);
    LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();

    try {
      JoranConfigurator configurator = new JoranConfigurator();
      configurator.setContext(lc);
      lc.shutdownAndReset();
      configurator.doConfigure(args[0]);
    } catch (JoranException je) {
      je.printStackTrace();
    }
    
    for (int i = 0; i < 10; i++) {
      if (i == 3)  {
        MDC.put("username", "sebastien");
        logger.debug("logging statement " + i);
        MDC.remove("username");
      } else if (i == 6) {
        Marker billing = MarkerFactory.getMarker("billing");
        logger.error(billing, "logging statement " + i);
      } else {
        logger.info("logging statement " + i);
      }
    }

    //StatusPrinter.print(lc.getStatusManager());
  }
}
