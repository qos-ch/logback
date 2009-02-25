package ch.qos.logback.classic.pattern;

import ch.qos.logback.classic.spi.ILoggingEvent;

/**
 * Return the events thread (usually the current thread).
 * 
 * @author Ceki G&uuml;lc&uuml;
 */
public class ThreadConverter extends ClassicConverter {

  public String convert(ILoggingEvent event) {
    return event.getThreadName();
  }

}
