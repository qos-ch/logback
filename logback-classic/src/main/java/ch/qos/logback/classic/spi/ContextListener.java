package ch.qos.logback.classic.spi;

import java.util.EventListener;

public interface ContextListener extends EventListener {
  
  public void update(LogbackEvent logbackEvent);

}
