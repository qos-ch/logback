package ch.qos.logback.access.pattern;

import ch.qos.logback.access.spi.AccessEvent;

public class StatusCodeConverter extends AccessConverter {

  public String convert(AccessEvent accessEvent) {
    return Integer.toString(accessEvent.getStatusCode());
  }

}
