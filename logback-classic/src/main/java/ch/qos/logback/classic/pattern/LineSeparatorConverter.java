package ch.qos.logback.classic.pattern;

import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.CoreConstants;

public class LineSeparatorConverter extends ClassicConverter {

  public String convert(LoggingEvent event) {
    return CoreConstants.LINE_SEPARATOR;
  }

}
