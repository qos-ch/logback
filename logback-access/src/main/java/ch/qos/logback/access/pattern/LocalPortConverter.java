package ch.qos.logback.access.pattern;

import ch.qos.logback.access.spi.AccessEvent;

public class LocalPortConverter extends AccessConverter {

  
  protected String convert(AccessEvent accessEvent) {    
    return Integer.toString(accessEvent.getLocalPort());
  }
}
