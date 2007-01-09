package ch.qos.logback.access.pattern;

import ch.qos.logback.access.spi.AccessEvent;

/**
 * The request URI.
 * 
 * @author Ceki G&uumllc&uuml;
 */
public class RequestURIConverter extends AccessConverter {

  public String convert(AccessEvent accessEvent) {
    return accessEvent.getRequestURI();
  }

}
