package ch.qos.logback.core.issue;

import ch.qos.logback.core.issue.SelectiveLockRunnable.LockingModel;

/**
 * Short sample code testing the throughput of a fair lock.
 * 
 * @author Joern Huxhorn
 * @author Ceki Gulcu
 */
public class LockThroughput {

  static int THREAD_COUNT = 10;
  static long OVERALL_DURATION_IN_MILLIS = 5000;

  public static void main(String args[]) throws InterruptedException {

    ThreadedThroughputCalculator tp = new ThreadedThroughputCalculator(
        OVERALL_DURATION_IN_MILLIS);
    tp.printEnvironmentInfo("LockThroughput");

    for (int i = 0; i < 2; i++) {
      tp.execute(buildArray(LockingModel.SYNC));
      tp.execute(buildArray(LockingModel.UNFAIR));
      tp.execute(buildArray(LockingModel.FAIR));
    }
    
    tp.execute(buildArray(LockingModel.SYNC));
    tp.printThroughput("Sync:   ");

    tp.execute(buildArray(LockingModel.UNFAIR));
    tp.printThroughput("Unfair: ");

    tp.execute(buildArray(LockingModel.FAIR));
    tp.printThroughput("Fair:   ");
  }

  static SelectiveLockRunnable[] buildArray(LockingModel model) {
    SelectiveLockRunnable[] array = new SelectiveLockRunnable[THREAD_COUNT];
    for (int i = 0; i < THREAD_COUNT; i++) {
      array[i] = new SelectiveLockRunnable(model);
    }
    return array;
  }

}
