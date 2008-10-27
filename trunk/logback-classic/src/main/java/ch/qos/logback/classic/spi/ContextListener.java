package ch.qos.logback.classic.spi;

import ch.qos.logback.classic.LoggerContext;

public interface ContextListener {
  
  public void onReset(LoggerContext context);
  public void onStart(LoggerContext context);

}
