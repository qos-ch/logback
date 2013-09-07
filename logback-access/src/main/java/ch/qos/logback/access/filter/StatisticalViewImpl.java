/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2013, QOS.ch. All rights reserved.
 *
 * This program and the accompanying materials are dual-licensed under
 * either the terms of the Eclipse Public License v1.0 as published by
 * the Eclipse Foundation
 *
 *   or (per the licensee's choosing)
 *
 * under the terms of the GNU Lesser General Public License version 2.1
 * as published by the Free Software Foundation.
 */
package ch.qos.logback.access.filter;

import ch.qos.logback.core.spi.LifeCycle;

public class StatisticalViewImpl implements StatisticalView, LifeCycle {

  final CountingFilter countingFilter;
  boolean started;

  StatsByMinute statsByMinute = new StatsByMinute();
  StatsByHour statsByHour = new StatsByHour();
  StatsByDay statsByDay = new StatsByDay();
  StatsByWeek statsByWeek = new StatsByWeek();
  StatsByMonth statsByMonth = new StatsByMonth();
  
  StatisticalViewImpl(CountingFilter countingFilter) {
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

  void update(long now) {
    long total = getTotal();
    statsByMinute.update(now, total);
    statsByHour.update(now, total);
    statsByDay.update(now, total);
    statsByWeek.update(now, total);
    statsByMonth.update(now, total);
    
  }

  void update() {
    long now = System.currentTimeMillis();
    update(now);
  }

  public void start() {
    System.out.println("StatisticalViewImpl start called");
    started = true;
    long now = System.currentTimeMillis();
    statsByMinute = new StatsByMinute(now);
    statsByHour = new StatsByHour(now);
    statsByDay = new StatsByDay(now);
    statsByWeek = new StatsByWeek(now);
    statsByMonth = new StatsByMonth(now);
  }

  public boolean isStarted() {
    return started;
  }

  public void stop() {
    started = false;
    statsByMinute.reset();
    statsByHour.reset();
    statsByDay.reset();
    statsByWeek.reset();
    statsByMonth.reset();
  }

  public long getLastMinuteCount() {
    return statsByMinute.getLastCount();
  }

  public double getMinuteAverage() {
    return statsByMinute.getAverage();
  }

  public double getHourlyAverage() {
    return statsByHour.getAverage();
  }

  public long getLastHoursCount() {
    return  statsByHour.getLastCount();
  }

}
