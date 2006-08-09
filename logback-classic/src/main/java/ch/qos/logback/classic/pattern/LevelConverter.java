package ch.qos.logback.classic.pattern;

import ch.qos.logback.classic.spi.LoggingEvent;

/**
 * Return the event's level.
 * 
 * @author Ceki G&uuml;lc&uuml;
 */
public class LevelConverter extends ClassicConverter {

  public String convert(Object event) {
    LoggingEvent le = (LoggingEvent) event;
    return le.getLevel().toString();
  }

}
