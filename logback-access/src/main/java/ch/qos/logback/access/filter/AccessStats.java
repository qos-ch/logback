package ch.qos.logback.access.filter;

public interface AccessStats {

  
  long getTotal();
  
  long getLastDaysCount();
  double getDailyAverage();
 
  
  long getLastWeeksCount();
  double getWeeklyAverage();
  
  long getLastMonthsCount();
  double getMonthlyAverage();
  
}
