package ch.qos.logback.access.pattern;

import ch.qos.logback.access.spi.AccessEvent;
import ch.qos.logback.core.CoreConstants;


public class LineSeparatorConverter extends AccessConverter {

  public String convert(AccessEvent event) {
    return CoreConstants.LINE_SEPARATOR;
  }
}
