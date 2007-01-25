package ch.qos.logback.classic.selector;

import ch.qos.logback.classic.LoggerContext;

/**
 * An interface that provides access to different contexts.
 * 
 * It is used by the LoggerFactory to access the context
 * it will use to retrieve loggers.
 *
 * @author Ceki G&uuml;lc&uuml;
 * @author S&eacute;bastien Pennec
 */
public interface ContextSelector {

  public LoggerContext getLoggerContext();
  
  public LoggerContext getDefaultLoggerContext();
  
  public LoggerContext detachLoggerContext(String loggerContextName);
  
}
