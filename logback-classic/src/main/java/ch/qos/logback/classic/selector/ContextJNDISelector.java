/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * 
 * Copyright (C) 1999-2006, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
 */
package ch.qos.logback.classic.selector;

import static ch.qos.logback.classic.ClassicGlobal.JNDI_CONFIGURATION_RESOURCE;
import static ch.qos.logback.classic.ClassicGlobal.JNDI_CONTEXT_NAME;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
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

  private static final ThreadLocal<LoggerContext> threadLocal = new ThreadLocal<LoggerContext>();

  public ContextJNDISelector(LoggerContext context) {
    contextMap = Collections
        .synchronizedMap(new HashMap<String, LoggerContext>());
    defaultContext = context;
  }

  public LoggerContext getLoggerContext() {
    String contextName = null;
    Context ctx = null;

    // First check if ThreadLocal has been set already
    LoggerContext lc = threadLocal.get();
    if (lc != null) {
      return lc;
    }

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
          try {
            new ContextInitializer(loggerContext).autoConfig();
          } catch(JoranException je) {
            StatusPrinter.print(loggerContext);
          }
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
    URL url = Loader.getResourceBySelfClassLoader(configFilePath);
    if (url != null) {
      try {
        JoranConfigurator configurator = new JoranConfigurator();
        context.reset();
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

  public List<String> getContextNames() {
    List<String> list = new ArrayList<String>();
    list.addAll(contextMap.keySet());
    return list;
  }

  public LoggerContext getLoggerContext(String name) {
    return contextMap.get(name);
  }

  /**
   * Returns the number of managed contexts Used for testing purposes
   * 
   * @return the number of managed contexts
   */
  public int getCount() {
    return contextMap.size();
  }

  /**
   * These methods are used by the LoggerContextFilter.
   * 
   * They provide a way to tell the selector which context to use, thus saving
   * the cost of a JNDI call at each new request.
   * 
   * @param context
   */
  public void setLocalContext(LoggerContext context) {
    threadLocal.set(context);
  }

  public void removeLocalContext() {
    threadLocal.remove();
  }

}
