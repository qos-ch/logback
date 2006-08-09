package ch.qos.logback.access.pattern;

import ch.qos.logback.access.spi.AccessEvent;

public class RequestMethodConverter extends AccessConverter {

   
  protected String convert(AccessEvent accessEvent) {    
    return accessEvent.getMethod();
  }
}
