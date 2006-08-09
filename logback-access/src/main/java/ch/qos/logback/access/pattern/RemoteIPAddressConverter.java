package ch.qos.logback.access.pattern;

import ch.qos.logback.access.spi.AccessEvent;

public class RemoteIPAddressConverter extends AccessConverter {

  protected String convert(AccessEvent accessEvent) {
    return accessEvent.getRemoteAddr();
  }

}
