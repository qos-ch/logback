package ch.qos.logback.access.pattern;

import ch.qos.logback.access.spi.AccessEvent;

/**
 * The first line of the request.
 * 
 * @author Ceki G&uumllc&uuml;
 */
public class RequestURLConverter extends AccessConverter {

  protected String convert(AccessEvent accessEvent) {
    return accessEvent.getRequestURL();
  }
}
