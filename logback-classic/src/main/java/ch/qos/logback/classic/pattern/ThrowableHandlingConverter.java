package ch.qos.logback.classic.pattern;



/**
 * Converter which handle throwables should be derived from this class.
 *
 */
public abstract class ThrowableHandlingConverter extends ClassicConverter {
  
  boolean handlesThrowable() {
    return true;
  }
}
