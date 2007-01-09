package ch.qos.logback.classic.pattern;

import ch.qos.logback.classic.spi.LoggingEvent;

/**
 * Return the event's level.
 * 
 * @author Ceki G&uuml;lc&uuml;
 */
public class LevelConverter extends ClassicConverter {

  public String convert(LoggingEvent le) {
    return le.getLevel().toString();
  }

}
