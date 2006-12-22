package ch.qos.logback.classic.filter;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.filter.AbstractMatcherFilter;
import ch.qos.logback.core.spi.FilterReply;

public class LevelFilter extends AbstractMatcherFilter {

  Level level;
  
  @Override
  public FilterReply decide(Object eventObject) {
    if (!isStarted()) {
      return FilterReply.NEUTRAL;
    }
    
    LoggingEvent event = (LoggingEvent)eventObject;
    
    if (event.getLevel().isGreaterOrEqual(level)) {
      return onMatch;
    } else {
      return onMismatch;
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
