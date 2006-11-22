package ch.qos.logback.access.filter;

import ch.qos.logback.core.util.TimeUtil;

public class StatsByHour extends PeriodicStats {

  StatsByHour() {
    super();
  }

  StatsByHour(long now) {
    super(now);
  }

  @Override
  long computeStartOfNextPeriod(long now) {
    return TimeUtil.computeStartOfNextHour(now);
  }

}
