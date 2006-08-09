package ch.qos.logback.access.pattern;

import ch.qos.logback.access.spi.AccessEvent;

public class ServerNameConverter extends AccessConverter {

  protected String convert(AccessEvent accessEvent) {    
    return accessEvent.getServerName();
  }
}
