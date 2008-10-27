package ch.qos.logback.access.filter;


abstract public class PeriodicStats {
  
  private long nextPeriodBegins = 0;
  private long lastTotal = 0;
  private long lastCount = 0;

  private double average;
  private int n;
 
  PeriodicStats() {
    this(System.currentTimeMillis());
  }
  
  PeriodicStats(long now) {
    nextPeriodBegins = computeStartOfNextPeriod(now);
  }
 
  void update(long now, long total) {
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
  
  void reset(long now) {
    nextPeriodBegins = computeStartOfNextPeriod(now);
    lastTotal = 0;
    lastCount = 0;
    average = 0.0;
    n = 0;
  }
  
  void reset() {
    reset(System.currentTimeMillis());
  }
  
  abstract long computeStartOfNextPeriod(long now);

}
