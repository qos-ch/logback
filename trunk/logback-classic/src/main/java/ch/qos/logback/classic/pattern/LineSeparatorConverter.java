package ch.qos.logback.classic.pattern;

import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.CoreGlobal;

public class LineSeparatorConverter extends ClassicConverter {

  public String convert(LoggingEvent event) {
    return CoreGlobal.LINE_SEPARATOR;
  }

}
