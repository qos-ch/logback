package ch.qos.logback.classic.filter;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterReply;

/**
 * A class that filters events depending on their level.
 * 
 * All events with a level equal or above the specified
 * level will be accepted, while all events with a level
 * under the specified level will be denied.
 *
 * @author S&eacute;bastien Pennec
 */
public class ThresholdFilter extends Filter {

  Level level;
  
  @Override
  public FilterReply decide(Object eventObject) {
    if (!isStarted()) {
      return FilterReply.NEUTRAL;
    }
    
    LoggingEvent event = (LoggingEvent)eventObject;
    
    if (event.getLevel().isGreaterOrEqual(level)) {
      return FilterReply.ACCEPT;
    } else {
      return FilterReply.DENY;
    }
  }
  
  public void setLevel(String level) {
    this.level = Level.toLevel(level);
  }
  
  public void start() {
    if (this.level != null) {
      super.start();
    }
  }
}
