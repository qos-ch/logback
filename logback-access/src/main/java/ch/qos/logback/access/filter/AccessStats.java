package ch.qos.logback.access.filter;

public interface AccessStats {

  
  long getTotal();
  
  long getLastDaysCount();
  double getDailyAverage();
 
  
  long getWeeklyTotal();
  long getWeeklyAverage();
  
  long getMonthlyTotal();
  long getMonthlyAverage();
  
}
