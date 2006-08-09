package ch.qos.logback.access.pattern;

import ch.qos.logback.access.spi.AccessEvent;
import ch.qos.logback.core.CoreGlobal;


public class LineSeparatorConverter extends AccessConverter {

  public String convert(AccessEvent event) {
    return CoreGlobal.LINE_SEPARATOR;
  }
}
