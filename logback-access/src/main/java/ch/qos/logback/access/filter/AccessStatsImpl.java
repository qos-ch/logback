package ch.qos.logback.access.filter;

import ch.qos.logback.core.spi.LifeCycle;

public class AccessStatsImpl implements AccessStats, LifeCycle {

  final CountingFilter countingFilter;
  boolean started;

  StatsByDay statsByDay = new StatsByDay();
  StatsByWeek statsByWeek = new StatsByWeek();
  StatsByMonth statsByMonth = new StatsByMonth();
  
  AccessStatsImpl(CountingFilter countingFilter) {
    this.countingFilter = countingFilter;
  }

  public double getDailyAverage() {
    return statsByDay.getAverage();
  }

  public long getLastDaysCount() {
    return statsByDay.getLastCount();
  }

  public double getMonthlyAverage() {
    return  statsByMonth.getAverage();
  }

  public long getLastMonthsCount() {
    return statsByMonth.getLastCount();
  }

  public long getTotal() {
    return countingFilter.getTotal();
  }

  public double getWeeklyAverage() {
    return statsByWeek.getAverage();
  }

  public long getLastWeeksCount() {
    return statsByWeek.getLastCount();
  }

  void refresh(long now) {
    long total = getTotal();
    statsByDay.update(now, total);
    statsByWeek.update(now, total);
    statsByMonth.update(now, total);
    
  }

  void refresh() {
    long now = System.currentTimeMillis();
    refresh(now);
  }

  public void start() {
    started = true;
    long now = System.currentTimeMillis();
    statsByDay = new StatsByDay(now);
    statsByWeek = new StatsByWeek(now);
    statsByMonth = new StatsByMonth(now);
  }

  public boolean isStarted() {
    return started;
  }

  public void stop() {
    started = false;
    statsByDay.reset();
    statsByWeek.reset();
    statsByMonth.reset();
  }

}
