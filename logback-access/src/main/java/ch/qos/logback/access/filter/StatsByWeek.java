package ch.qos.logback.access.filter;

import ch.qos.logback.core.util.TimeUtil;

public class StatsByWeek extends PeriodicStats {

  StatsByWeek() {
    super();
  }
  
  StatsByWeek(long now) {
    super(now);
  }
  @Override
  long computeStartOfNextPeriod(long now) {
    return TimeUtil.computeStartOfNextWeek(now);
  }
  
}
