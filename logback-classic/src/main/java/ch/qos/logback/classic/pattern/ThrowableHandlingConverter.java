package ch.qos.logback.classic.pattern;

import ch.qos.logback.classic.pattern.ClassicConverter;
import ch.qos.logback.classic.spi.LoggingEvent;


/**
 * Converter which handle throwables should be derived from this class.
 *
 */
public abstract class ThrowableHandlingConverter extends ClassicConverter {
  
  boolean handlesThrowable() {
    return true;
  }
  
  // tentatively...
  public boolean onNewLine(LoggingEvent le) {
    return le.getThrowableInformation() != null;
  }
}
