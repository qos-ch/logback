package ch.qos.logback.access.filter;

public class AccessStatsImpl implements AccessStats {

  final CountingFilter countingFilter;
  
  AccessStatsImpl(CountingFilter countingFilter) {
    this.countingFilter = countingFilter;
  }
  
  public long getDailyAverage() {
    // TODO Auto-generated method stub
    return 0;
  }

  public long getDailyTotal() {
    // TODO Auto-generated method stub
    return 0;
  }

  public long getMonthlyAverage() {
    // TODO Auto-generated method stub
    return 0;
  }

  public long getMonthlyTotal() {
    // TODO Auto-generated method stub
    return 0;
  }

  public long getTotal() {
    return countingFilter.getTotal();
  }

  public long getWeeklyAverage() {
    // TODO Auto-generated method stub
    return 0;
  }

  public long getWeeklyTotal() {
    // TODO Auto-generated method stub
    return 0;
  }

}
