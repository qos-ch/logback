package ch.qos.logback.core;

import java.text.DecimalFormat;

public class StopWatch {

  private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("0.000");
  static final long NANOS_IN_ONE_MICROSECOND = 1000;
  static final long NANOS_IN_ONE_MILLISECOND = NANOS_IN_ONE_MICROSECOND * 1000;
  static final long NANOS_IN_ONE_SECOND =NANOS_IN_ONE_MILLISECOND * 1000;
  

  enum Status {
    STARTED, STOPPED;
  }
  
  enum DurationUnit {
    NANOSECOND, MICROSECOND, MILLISSECOND, SECOND;
  }

  final String name;
  final long startTime;
  long stopTime;
  Status status;

  public StopWatch(String name) {
    this.name = name;
    this.startTime = System.nanoTime();
    this.status = Status.STARTED;
  }

  public StopWatch stop() {
    return stop(System.nanoTime());
  }

  public StopWatch stop(long stopTime) {
    this.status = Status.STOPPED;
    this.stopTime = stopTime;
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
      switch (selectDurationUnitForDisplay(getResultInNanos())) {
      case NANOSECOND:
        buf.append(getResultInNanos());
        buf.append(" nanoseconds.");
        break;
      case MICROSECOND:
        buf.append(getResultInMicros());
        buf.append(" microseconds.");
        break;
      case MILLISSECOND:
        buf.append(getResultInMillis());
        buf.append(" milliseconds.");
        break;
      case SECOND:
        double seconds = getResultInSeconds();
        buf.append(DECIMAL_FORMAT.format(seconds));
        buf.append(" seconds.");
        break;
      }
      break;
    default:
      new IllegalStateException("Status " + status + " is not expected");
    }
    return buf.toString();
  }

  DurationUnit selectDurationUnitForDisplay(long durationInNanos) {
    if (durationInNanos < 10L * NANOS_IN_ONE_MICROSECOND) {
      return DurationUnit.NANOSECOND;
    } else if (durationInNanos < 10L * NANOS_IN_ONE_MILLISECOND) {
      return DurationUnit.MICROSECOND;
    } else if (durationInNanos < 5L * NANOS_IN_ONE_SECOND) {
      return DurationUnit.MILLISSECOND;
    } else {
      return DurationUnit.SECOND;
    }
  }

  public final long getResultInNanos() {
    if (status == Status.STARTED) {
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
