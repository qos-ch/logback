package ch.qos.logback.access.filter;

public interface AccessStats {

  
  long getTotal();
  
  long getDailyTotal();
  long getDailyAverage();
 
  
  long getWeeklyTotal();
  long getWeeklyAverage();
  
  long getMonthlyTotal();
  long getMonthlyAverage();
  
}
