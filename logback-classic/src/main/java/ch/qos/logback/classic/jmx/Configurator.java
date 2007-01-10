package ch.qos.logback.classic.jmx;

import java.net.URL;
import java.util.List;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.classic.util.ContextInitializer;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.spi.ContextAwareBase;

/**
 * A class that provides access to logback components via
 * JMX.
 * 
 *
 * @author Ceki G&uuml;lc&uuml;
 * @author S&eacute;bastien Pennec
 * 
 * Contributor:
 *   Sebastian Davids
 */
public class Configurator extends ContextAwareBase implements
    ConfiguratorMBean {

  private static String EMPTY = "";
  
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
    addInfo("Shutting down context: " + lc.getName());
    lc.shutdownAndReset();
    JoranConfigurator configurator = new JoranConfigurator();
    configurator.setContext(lc);
    configurator.doConfigure(fileName);
    addInfo("Context: " + lc.getName() + " reloaded.");
  }

  public void reload(URL url) throws JoranException {
    LoggerContext lc = (LoggerContext) context;
    addInfo("Shutting down context: " + lc.getName());
    lc.shutdownAndReset();
    ContextInitializer.configureByResource(lc, url);
    addInfo("Context: " + lc.getName() + " reloaded.");
  }

  public void setLoggerLevel(String loggerName, String levelStr) {
    if (loggerName == null) {
      return;
    }
    if (levelStr == null) {
      return;
    }
    loggerName = loggerName.trim();
    levelStr = levelStr.trim();
    
    addInfo("Trying to set level " + levelStr + " to logger " + loggerName);
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

  public String getLoggerLevel(String loggerName) {
    if (loggerName == null) {
      return EMPTY;
    }
    
    loggerName = loggerName.trim();
    
    LoggerContext lc = (LoggerContext) context;
    Logger logger = lc.exists(loggerName);
    if (logger != null) {
      return logger.getLevel().toString();
    } else {
      return EMPTY;
    }
  }

  public String getLoggerEffectiveLevel(String loggerName) {
    if (loggerName == null) {
      return EMPTY;
    }
    
    loggerName = loggerName.trim();
    
    LoggerContext lc = (LoggerContext) context;
    Logger logger = lc.exists(loggerName);
    if (logger != null) {
      return logger.getEffectiveLevel().toString();
    } else {
      return EMPTY;
    }
  }

  public List<Logger> getLoggerList() {
    LoggerContext lc = (LoggerContext)context;
    return lc.getLoggerList();
  }
  
  

}
