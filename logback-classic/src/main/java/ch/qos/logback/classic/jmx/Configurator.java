package ch.qos.logback.classic.jmx;

import java.net.URL;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.classic.util.ContextInitializer;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.spi.ContextAwareBase;

public class Configurator extends ContextAwareBase implements
    ConfiguratorMBean {

  public Configurator(LoggerContext loggerContext) {
    this.context = loggerContext;
  }

  public void reload() {
    LoggerContext lc = (LoggerContext) context;
    addInfo("Shutting down context: " + lc.getName());
    lc.shutdownAndReset();
    ContextInitializer.autoConfig(lc, lc.getClass().getClassLoader());
    addInfo("Context: " + lc.getName() + " reloaded.");
  }

  public void reload(String fileName) throws JoranException {
    LoggerContext lc = (LoggerContext) context;
    JoranConfigurator configurator = new JoranConfigurator();
    configurator.setContext(lc);
    configurator.doConfigure(fileName);
  }

  public void reload(URL url) throws JoranException {
    LoggerContext lc = (LoggerContext) context;
    addInfo("Shutting down context: " + lc.getName());
    lc.shutdownAndReset();
    ContextInitializer.configureByResource(lc, url);
    addInfo("Context: " + lc.getName() + " reloaded.");
  }

  public void setLoggerLevel(String loggerName, String levelStr) {
    LoggerContext lc = (LoggerContext) context;
    Logger logger = lc.getLogger(loggerName);
    if ("null".equalsIgnoreCase(levelStr)) {
      logger.setLevel(null);
    } else {
      Level level = Level.toLevel(levelStr, null);
      if (level != null) {
        logger.setLevel(level);
      }
    }
  }

}
