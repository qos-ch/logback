package ch.qos.logback.classic.spi;

import java.util.EventObject;

public class LogbackEvent extends EventObject {

  private static final long serialVersionUID = -2688530978380786446L;

  private final EventType type;
  
  public LogbackEvent(Object source, EventType type) {
    super(source);
    this.type = type;
  }
  
  public EventType getType() {
    return type;
  }
}
