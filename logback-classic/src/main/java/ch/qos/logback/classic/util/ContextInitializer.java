package ch.qos.logback.classic.util;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import ch.qos.logback.classic.BasicConfigurator;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.status.InfoStatus;
import ch.qos.logback.core.status.StatusManager;
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
  final public static String STATUS_LISTENER_CLASS = "logback.statusListenerClass";
  final public static String SYSOUT = "SYSOUT";

  final LoggerContext loggerContext;

  public ContextInitializer(LoggerContext loggerContext) {
    this.loggerContext = loggerContext;
  }

  public void configureByResource(URL url)
      throws JoranException {
    if (url == null) {
      throw new IllegalArgumentException("URL argument cannot be null");
    }
    JoranConfigurator configurator = new JoranConfigurator();
    configurator.setContext(loggerContext);
    configurator.doConfigure(url);
  }

  private URL findConfigFileURLFromSystemProperties(ClassLoader classLoader) {
    String logbackConfigFile = System.getProperty(CONFIG_FILE_PROPERTY, null);
    if (logbackConfigFile != null) {
      URL result = null;
      try {
        result = new URL(logbackConfigFile);
        return result;
      } catch (MalformedURLException e) {
        // so, resource is not a URL:
        // attempt to get the resource from the class path
        result = Loader.getResource(logbackConfigFile, classLoader);
        if (result != null) {
          return result;
        }
        File f = new File(logbackConfigFile);
        if (f.exists() && f.isFile()) {
          try {
            result = f.toURL();
            return result;
          } catch (MalformedURLException e1) {
          }
        }
      } finally {
        statusOnResourceSearch(logbackConfigFile, result);
      }
    }
    return null;
  }

  public void autoConfig(ClassLoader classLoader) throws JoranException {
    StatusListenerConfigHelper.installIfAsked(loggerContext);

    URL url = findConfigFileURLFromSystemProperties(classLoader);
    if (url == null) {
      url = Loader.getResource(TEST_AUTOCONFIG_FILE, classLoader);
      statusOnResourceSearch(TEST_AUTOCONFIG_FILE, url);
    }
    if (url == null) {
      url = Loader.getResource(AUTOCONFIG_FILE, classLoader);
      statusOnResourceSearch(AUTOCONFIG_FILE, url);
    }
    if (url != null) {
      configureByResource(url);
    } else {
      BasicConfigurator.configure(loggerContext);
    }
  }

  public void autoConfig() throws JoranException {
    ClassLoader tccl = Loader.getTCL();
    autoConfig(tccl);
  }

  private void statusOnResourceSearch(String resourceName, URL url) {
    StatusManager sm = loggerContext.getStatusManager();
    if (url == null) {
      sm.add(new InfoStatus("Could not find resource [" + resourceName + "]",
          loggerContext));
    } else {
      sm.add(new InfoStatus("Found resource [" + resourceName + "]",
          loggerContext));
    }
  }

}
