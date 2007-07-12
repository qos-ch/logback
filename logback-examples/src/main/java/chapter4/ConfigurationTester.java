package chapter4;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.util.StatusPrinter;
import chapter4.sub.sample.Bar;

/**
 * 
 * This class can be used to check the result of a configuration file.
 * <p>
 * When all the logback-core, logback-classic, logback-examples and their dependencies have been
 * added to the ClassPath, one can launch this class using the following
 * command:
 * <p>
 * java chapter4.ConfigurationTester
 * chapter4/conf/name_of_the_configuration_file.xml
 * 
 * @author S&eacute;bastien Pennec
 */
public class ConfigurationTester {

  public static void main(String[] args) throws InterruptedException {
    Logger logger = (Logger) LoggerFactory.getLogger(ConfigurationTester.class);
    LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();

    try {
      JoranConfigurator configurator = new JoranConfigurator();
      configurator.setContext(lc);
      lc.shutdownAndReset();
      configurator.doConfigure(args[0]);
    } catch (JoranException je) {
      je.printStackTrace();
    }
    logger.debug("**Hello {}", new Bar());
    MDC.put("testKey", "testValueFromMDC");
    MDC.put("testKey2", "value2");
    for (int i = 0; i < 10; i++) {
      logger.debug("logging statement " + i);
    }
    Bar bar = new Bar();
    bar.createLoggingRequest();

    StatusPrinter.print(lc.getStatusManager());
  }
}
