package ch.qos.logback.access.filter;


abstract public class PeriodicStats {
  
  private long nextPeriodBegins = 0;
  private long lastTotal = 0;
  private long lastCount = 0;

  private double average;
  private int n;
 
  PeriodicStats(long now) {
    nextPeriodBegins = computeStartOfNextPeriod(now);
  }
 
  void refresh(long now, long total) {
    if (now > nextPeriodBegins) {     
      lastCount = total - lastTotal;
      lastTotal = total;
      average = (average * n + lastCount) / (++n);
      nextPeriodBegins = computeStartOfNextPeriod(now);
    }
  }

  public double getAverage() {
    return average;
  }

  public long getLastCount() {
    return lastCount;
  }
  
  abstract long computeStartOfNextPeriod(long now);

}
