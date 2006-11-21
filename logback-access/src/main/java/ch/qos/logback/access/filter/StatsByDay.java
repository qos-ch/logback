package ch.qos.logback.access.filter;

import ch.qos.logback.core.util.TimeUtil;

public class StatsByDay extends PeriodicStats {
  
  StatsByDay(long now) {
    super(now);
  }
  @Override
  long computeStartOfNextPeriod(long now) {
    return TimeUtil.computeStartOfNextDay(now);
  }
  
}
