package ch.qos.logback.classic.selector;

import ch.qos.logback.classic.LoggerContext;

public class DefaultContextSelector implements ContextSelector {

  private LoggerContext context;
  
  public DefaultContextSelector(LoggerContext context) {
    this.context = context;
  }
  
  public LoggerContext getLoggerContext() {
    return getDefaultLoggerContext();
  }

  public LoggerContext getDefaultLoggerContext() {
    return context;
  }

  public LoggerContext detachLoggerContext(String loggerContextName) {
    return context;
  }
}
