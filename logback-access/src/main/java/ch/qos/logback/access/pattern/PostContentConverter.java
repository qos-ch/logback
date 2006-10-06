package ch.qos.logback.access.pattern;

import ch.qos.logback.access.spi.AccessEvent;

/**
 * This class is tied to the <code>postContent</code> conversion pattern.
 * <p>
 * It has been removed from the {@link ch.qos.logback.access.PatternLayout} since
 * it needs further testing before being used widely.
 * <p>
 * <strong>Please handle this class with caution if you wish to use it anyway!</strong>
 * 
 * @author Ceki G&uuml;lc&uuml;
 * @author S&eacute;bastien Pennec
 */
public class PostContentConverter extends AccessConverter {

  @Override
  protected String convert(AccessEvent accessEvent) {
    return accessEvent.getPostContent();
  }

}
