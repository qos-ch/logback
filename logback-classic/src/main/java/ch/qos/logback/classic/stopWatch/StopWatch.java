package ch.qos.logback.classic.stopwatch;


public class StopWatch {


  enum Status {
    STARTED, STOPPED;
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

  public String getName() {
    return name;
  }

  public StopWatch stop() {
    if(status == Status.STOPPED) {
      return this;
    } 
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
      buf.append("elapsed time: ");
      buf.append(Util.nanosInDurationUnitAsStr(this));
      break;
    default:
      new IllegalStateException("Status " + status + " is not expected");
    }
    return buf.toString();
  }

  public final long getResultInNanos() {
    if (status == Status.STARTED) {
      return 0;
    } else {
      return stopTime - startTime;
    }
  }

}
