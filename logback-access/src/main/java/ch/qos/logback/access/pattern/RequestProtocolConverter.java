package ch.qos.logback.access.pattern;

import ch.qos.logback.access.spi.AccessEvent;

public class RequestProtocolConverter extends AccessConverter {

  
  public String convert(AccessEvent accessEvent) {    
    return accessEvent.getProtocol();
  }
}
