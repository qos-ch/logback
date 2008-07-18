package ch.qos.logback.classic.turbo;

import ch.qos.logback.classic.Level;


/**
 * Bean pairing an MDC value with a log level.
 * 
 * @author Raplh Goers
 * @author Ceki Gulcu 
 */
public class MDCValueLevelPair {
  private String value;
  private Level level;

  public String getValue() {
    return value;
  }

  public void setValue(String name) {
    this.value = name;
  }

  public Level getLevel() {
    return level;
  }

  public void setLevel(Level level) {
    this.level = level;
  }
}
