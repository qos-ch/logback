package ch.qos.logback.classic.pattern;

import ch.qos.logback.classic.spi.ILoggingEvent;

/**
 * Return the event's level.
 * 
 * @author Ceki G&uuml;lc&uuml;
 */
public class LevelConverter extends ClassicConverter {

  public String convert(ILoggingEvent le) {
    return le.getLevel().toString();
  }

}
