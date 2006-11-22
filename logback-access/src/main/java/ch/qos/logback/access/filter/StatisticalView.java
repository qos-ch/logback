package ch.qos.logback.access.filter;

public interface StatisticalView {

  
  long getTotal();
  
  long getLastMinuteCount();
  double getMinuteAverage();
  
  
  long getLastHoursCount();
  double getHourlyAverage();
  
  
  long getLastDaysCount();
  double getDailyAverage();
 
  
  long getLastWeeksCount();
  double getWeeklyAverage();
  
  long getLastMonthsCount();
  double getMonthlyAverage();
  
}
