package ch.qos.logback.classic.spi;

public class BasicContextListener implements ContextListener {

  boolean updated = false;
  LogbackEvent lastEvent;
  
  public void update(LogbackEvent logbackEvent) {
    updated = true;
    lastEvent = logbackEvent;
  }
}
