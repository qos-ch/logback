package ch.qos.logback.classic.pattern;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.CoreConstants;

public class LineSeparatorConverter extends ClassicConverter {

  public String convert(ILoggingEvent event) {
    return CoreConstants.LINE_SEPARATOR;
  }

}
