package ch.qos.logback.classic.util;

import java.net.URL;

import ch.qos.logback.classic.BasicConfigurator;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.util.Loader;
import ch.qos.logback.core.util.StatusPrinter;

public class ContextInitializer {

  final public static String AUTOCONFIG_FILE = "logback.xml";
  final public static String TEST_AUTOCONFIG_FILE = "logback-test.xml";

  public static void configureByResource(LoggerContext loggerContext, URL url)
      throws JoranException {
    if (url == null) {
      throw new IllegalArgumentException("URL argument cannot be null");
    }
    JoranConfigurator configurator = new JoranConfigurator();
    configurator.setContext(loggerContext);
    configurator.doConfigure(url);
  }

  public static void autoConfig(LoggerContext loggerContext, ClassLoader classLoader) {
    URL url = Loader.getResource(TEST_AUTOCONFIG_FILE, classLoader);
    if (url == null) {
      url = Loader.getResource(AUTOCONFIG_FILE, classLoader);
    }

    if (url != null) {
      try {
        configureByResource(loggerContext, url);
      } catch (JoranException je) {
        StatusPrinter.print(loggerContext);
      }
    } else {
        BasicConfigurator.configure(loggerContext);
    }
  }
  
  public static void autoConfig(LoggerContext loggerContext) {
    ClassLoader tccl = Loader.getTCL();
    autoConfig(loggerContext, tccl);
   
  }
}
