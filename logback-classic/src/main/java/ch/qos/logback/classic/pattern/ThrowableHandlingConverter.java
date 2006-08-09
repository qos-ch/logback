package ch.qos.logback.classic.pattern;

import ch.qos.logback.classic.pattern.ClassicConverter;


/**
 * Converter which handle throwables should be derived from this class.
 *
 */
public abstract class ThrowableHandlingConverter extends ClassicConverter {
  
  boolean handlesThrowable() {
    return true;
  }
}
