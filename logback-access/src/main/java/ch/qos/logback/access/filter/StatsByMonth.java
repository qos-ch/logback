package ch.qos.logback.access.filter;

import ch.qos.logback.core.util.TimeUtil;

public class StatsByMonth extends PeriodicStats {

  StatsByMonth() {
    super();
  }
  
  StatsByMonth(long now) {
    super(now);
  }
  
  @Override
  long computeStartOfNextPeriod(long now) {
    return TimeUtil.computeStartOfNextMonth(now);
  }
  
}
