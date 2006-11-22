package ch.qos.logback.access.filter;

import ch.qos.logback.core.util.TimeUtil;

public class StatsByMinute extends PeriodicStats {

  StatsByMinute() {
    super();
  }

  StatsByMinute(long now) {
    super(now);
  }

  @Override
  long computeStartOfNextPeriod(long now) {
    return TimeUtil.computeStartOfNextDay(now);
  }

}
