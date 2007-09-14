package ch.qos.logback.core;

import java.text.DecimalFormat;

public class StopWatch {

  private static final int STARTED = 1;
  private static final int STOPPED = 2;
  private static final int NANOS_IN_ONE_MICROSECOND = 1000;
  private static final int NANOS_IN_ONE_MILLISECOND = 1000*1000;
  private static final int NANOS_IN_ONE_SECOND = 1000*1000*1000;
  private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("0.000");
  
  final String name;
  final long startTime;
  long stopTime;
  int status;

  StopWatch(String name) {
    this.name = name;
    this.startTime = System.nanoTime();
    this.status = STARTED;
  }

  StopWatch stop() {
    this.status = STOPPED;
    this.stopTime = System.nanoTime();
    return this;
  }

  @Override
  public String toString() {
    StringBuffer buf = new StringBuffer();
    buf.append("StopWatch [");
    buf.append(name);
    buf.append("] ");

    switch (status) {
    case STARTED:
      buf.append("STARTED");
      break;
    case STOPPED:
      buf.append("STOPPED at ");
      if(getResultInNanos() < 10*NANOS_IN_ONE_MICROSECOND) {
        buf.append(getResultInNanos());
        buf.append(" nanoseconds.");
      }
      else if (getResultInNanos() < 10*NANOS_IN_ONE_MILLISECOND) {
        buf.append(getResultInMicros());
        buf.append(" microseconds.");
      } else if (getResultInNanos() < 5*NANOS_IN_ONE_SECOND)  {
        
        buf.append(getResultInMillis());
        buf.append(" milliseconds.");
      } else {
        double seconds = getResultInSeconds();
        buf.append(DECIMAL_FORMAT.format(seconds));
        buf.append(" seconds.");
        
      }
      break;
    default:
      new IllegalStateException("Status " + status + " is not expected");
    }
    return buf.toString();
  }

  public final long getResultInNanos() {
    if (status == STARTED) {
      return 0;
    } else {
      return stopTime - startTime;
    }
  }

  public final long getResultInMicros() {
    return getResultInNanos() / NANOS_IN_ONE_MICROSECOND;
  }

  public final long getResultInMillis() {
    return getResultInNanos() / NANOS_IN_ONE_MILLISECOND;
  }
  
  public final double getResultInSeconds() {
    return ((double) getResultInNanos() / NANOS_IN_ONE_SECOND);
  }
}
