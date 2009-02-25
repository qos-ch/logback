package chapter5;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.pattern.ClassicConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;

public class MySampleConverter extends ClassicConverter {

  private static final String END_COLOR = "\u001b[m";

  private static final String ERROR_COLOR = "\u001b[0;31m";
  private static final String WARN_COLOR = "\u001b[0;33m";

  @Override
  public String convert(ILoggingEvent event) {
    StringBuffer sbuf = new StringBuffer();
    sbuf.append(getColor(event.getLevel()));
    sbuf.append(event.getLevel());
    sbuf.append(END_COLOR);
    return sbuf.toString();
  }

  /**
   * Returns the appropriate characters to change the color for the specified
   * logging level.
   */
  private String getColor(Level level) {
    switch (level.toInt()) {
    case Level.ERROR_INT:
      return ERROR_COLOR;
    case Level.WARN_INT:
      return WARN_COLOR;
    default:
      return "";
    }
  }
}
