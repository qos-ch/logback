package ch.qos.logback.access.pattern;

import ch.qos.logback.access.spi.AccessEvent;

/**
 * Always returns the NA (not available) string which is "-" in the case
 * of access conversions. 
 *
 * @author Ceki G&uumllc&uuml;
 */
public class NAConverter extends AccessConverter {
  
  public String convert(AccessEvent accessEvent) {    
      return AccessEvent.NA;
  }

}
