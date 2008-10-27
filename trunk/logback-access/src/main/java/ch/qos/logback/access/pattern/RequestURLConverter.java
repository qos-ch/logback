package ch.qos.logback.access.pattern;

import ch.qos.logback.access.spi.AccessEvent;

/**
 * The first line of the request.
 * 
 * @author Ceki G&uumllc&uuml;
 */
public class RequestURLConverter extends AccessConverter {

  public String convert(AccessEvent accessEvent) {
    return accessEvent.getRequestURL();
  }
}
