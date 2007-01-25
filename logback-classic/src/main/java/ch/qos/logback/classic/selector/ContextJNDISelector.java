package ch.qos.logback.classic.selector;

import static ch.qos.logback.classic.ClassicGlobal.JNDI_CONFIGURATION_RESOURCE;
import static ch.qos.logback.classic.ClassicGlobal.JNDI_CONTEXT_NAME;

import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.naming.Context;
import javax.naming.NamingException;

import org.slf4j.Logger;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.classic.util.ContextInitializer;
import ch.qos.logback.classic.util.JNDIUtil;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.util.Loader;
import ch.qos.logback.core.util.StatusPrinter;

/**
 * A class that allows the LoggerFactory to access an environment-based
 * LoggerContext.
 * 
 * To add in catalina.sh
 * 
 * JAVA_OPTS="$JAVA_OPTS "-Dlogback.ContextSelector=JNDI""
 * 
 * @author Ceki G&uuml;lc&uuml;
 * @author S&eacute;bastien Pennec
 */
public class ContextJNDISelector implements ContextSelector {

  private final Map<String, LoggerContext> contextMap;
  private final LoggerContext defaultContext;

  public ContextJNDISelector(LoggerContext context) {
    contextMap = Collections
        .synchronizedMap(new HashMap<String, LoggerContext>());
    defaultContext = context;
  }

  public LoggerContext getLoggerContext() {
    String contextName = null;
    Context ctx = null;

    try {
      // We first try to find the name of our
      // environment's LoggerContext
      ctx = JNDIUtil.getInitialContext();
      contextName = (String) JNDIUtil.lookup(ctx, JNDI_CONTEXT_NAME);
    } catch (NamingException ne) {
      // We can't log here
    }

    if (contextName == null) {
      // We return the default context
      return defaultContext;
    } else {
      // Let's see if we already know such a context
      LoggerContext loggerContext = contextMap.get(contextName);

      if (loggerContext == null) {
        // We have to create a new LoggerContext
        loggerContext = new LoggerContext();
        loggerContext.setName(contextName);
        contextMap.put(contextName, loggerContext);

        // Do we have a dedicated configuration file?
        String configFilePath = JNDIUtil.lookup(ctx,
            JNDI_CONFIGURATION_RESOURCE);
        if (configFilePath != null) {
          configureLoggerContextByResource(loggerContext, configFilePath);
        } else {
          ContextInitializer.autoConfig(loggerContext);
        }
      }
      return loggerContext;
    }
  }

  public LoggerContext getDefaultLoggerContext() {
    return defaultContext;
  }

  public LoggerContext detachLoggerContext(String loggerContextName) {
    return contextMap.remove(loggerContextName);
  }

  private void configureLoggerContextByResource(LoggerContext context,
      String configFilePath) {
    URL url = Loader.getResourceByTCL(configFilePath);
    if (url != null) {
      try {
        JoranConfigurator configurator = new JoranConfigurator();
        context.shutdownAndReset();
        configurator.setContext(context);
        configurator.doConfigure(url);
      } catch (JoranException e) {
        StatusPrinter.print(context);
      }
    } else {
      Logger logger = defaultContext.getLogger(LoggerContext.ROOT_NAME);
      logger.warn("The provided URL for context" + context.getName()
          + " does not lead to a valid file");
    }
  }

}
