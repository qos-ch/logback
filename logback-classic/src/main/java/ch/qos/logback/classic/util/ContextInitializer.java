package ch.qos.logback.classic.util;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import ch.qos.logback.classic.BasicConfigurator;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.util.Loader;

// contributors
// Ted Graham, Matt Fowles, see also http://jira.qos.ch/browse/LBCORE-32
/**
 * This class contains logback's logic for automatic configuration
 * 
 * @author Ceki Gulcu
 */
public class ContextInitializer {

  final public static String AUTOCONFIG_FILE = "logback.xml";
  final public static String TEST_AUTOCONFIG_FILE = "logback-test.xml";
  final public static String CONFIG_FILE_PROPERTY = "logback.configurationFile";

  public static void configureByResource(LoggerContext loggerContext, URL url)
      throws JoranException {
    if (url == null) {
      throw new IllegalArgumentException("URL argument cannot be null");
    }
    JoranConfigurator configurator = new JoranConfigurator();
    configurator.setContext(loggerContext);
    configurator.doConfigure(url);
  }

  static URL findConfigFileURLFromSystemProperties(ClassLoader classLoader) {
    String logbackConfigFile = System.getProperty(CONFIG_FILE_PROPERTY, null);
    if (logbackConfigFile != null) {
      try {
        return new URL(logbackConfigFile);
      } catch (MalformedURLException e) {
        // so, resource is not a URL:
        // attempt to get the resource from the class path
        URL urlAsResource = Loader.getResource(logbackConfigFile, classLoader);
        if (urlAsResource != null) {
          return urlAsResource;
        }
        File f = new File(logbackConfigFile);
        if (f.exists() && f.isFile()) {
          try {
            return f.toURL();
          } catch (MalformedURLException e1) {
          }
        }
      }
    }
    return null;
  }

  public static void autoConfig(LoggerContext loggerContext,
      ClassLoader classLoader) throws JoranException {
    StatusListenerConfigHelper.installIfAsked(loggerContext);
    
    URL url = findConfigFileURLFromSystemProperties(classLoader);
    if (url == null) {
      url = Loader.getResource(TEST_AUTOCONFIG_FILE, classLoader);
    }
    if (url == null) {
      url = Loader.getResource(AUTOCONFIG_FILE, classLoader);
    }
    if (url != null) {
      configureByResource(loggerContext, url);
    } else {
      BasicConfigurator.configure(loggerContext);
    }
  }

  public static void autoConfig(LoggerContext loggerContext)
      throws JoranException {
    ClassLoader tccl = Loader.getTCL();
    autoConfig(loggerContext, tccl);
  }
  

}
