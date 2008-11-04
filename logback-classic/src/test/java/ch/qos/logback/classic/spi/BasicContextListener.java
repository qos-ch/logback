package ch.qos.logback.classic.spi;

import ch.qos.logback.classic.LoggerContext;

public class BasicContextListener implements LoggerContextListener {

  boolean updated = false;
  LoggerContext context;
  
  public void onReset(LoggerContext context) {
    updated = true;
    this.context = context;
    
  }
  public void onStart(LoggerContext context) {
    updated = true;
    this.context = context;
  }
}
