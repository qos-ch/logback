package ch.qos.logback.access.pattern;

import ch.qos.logback.access.spi.AccessEvent;

public class RequestProtocolConverter extends AccessConverter {

  
  protected String convert(AccessEvent accessEvent) {    
    return accessEvent.getProtocol();
  }
}
