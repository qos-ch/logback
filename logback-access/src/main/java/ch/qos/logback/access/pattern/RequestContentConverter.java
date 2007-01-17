package ch.qos.logback.access.pattern;

import ch.qos.logback.access.spi.AccessEvent;

/**
 * This class is tied to the <code>requestContent</code> conversion word.
 * <p>
 * It has been removed from the {@link ch.qos.logback.access.PatternLayout} since
 * it needs further testing before wide use.
 * <p>
 * @author Ceki G&uuml;lc&uuml;
 * @author S&eacute;bastien Pennec
 */
public class RequestContentConverter extends AccessConverter {

  @Override
  public String convert(AccessEvent accessEvent) {
    return accessEvent.getRequestContent();
  }

}
