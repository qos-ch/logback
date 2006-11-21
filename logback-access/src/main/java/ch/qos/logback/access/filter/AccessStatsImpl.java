package ch.qos.logback.access.filter;

import ch.qos.logback.core.spi.LifeCycle;

public class AccessStatsImpl implements AccessStats, LifeCycle {

  final CountingFilter countingFilter;
  boolean started;

  StatsByDay statsByDay = new StatsByDay(System.currentTimeMillis());
  
  AccessStatsImpl(CountingFilter countingFilter) {
    this.countingFilter = countingFilter;
  }

  public double getDailyAverage() {
    return statsByDay.getAverage();
  }

  public long getLastDaysCount() {
    return statsByDay.getLastCount();
  }

  public long getMonthlyAverage() {
    return 0;
  }

  public long getMonthlyTotal() {
    return 0;
  }

  public long getTotal() {
    return countingFilter.getTotal();
  }

  public long getWeeklyAverage() {
    return 0;
  }

  public long getWeeklyTotal() {
    return 0;
  }

  void refresh(long now) {
    statsByDay.refresh(now, getTotal());
  }

  void refresh() {
    long now = System.currentTimeMillis();
    refresh(now);
  }

  public void start() {
    started = true;

  }

  public boolean isStarted() {
    return started;
  }

  public void stop() {
    started = false;
  }

}
