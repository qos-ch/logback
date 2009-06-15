package ch.qos.logback.core.issue;

import ch.qos.logback.core.issue.SelectiveLockRunnable.LockingModel;

/**
 * Short sample code testing the throughput of a fair lock.
 * 
 * @author Joern Huxhorn
 * @author Ceki Gulcu
 */
public class NoLockThroughput {

  static int THREAD_COUNT = 3;
  static long OVERALL_DURATION_IN_MILLIS = 2000;

  public static void main(String args[]) throws InterruptedException {

    ThreadedThroughputCalculator tp = new ThreadedThroughputCalculator(
        OVERALL_DURATION_IN_MILLIS);
    tp.printEnvironmentInfo("NoLockThroughput");

    for (int i = 0; i < 2; i++) {
      tp.execute(buildArray(LockingModel.NOLOCK));
    }

    tp.execute(buildArray(LockingModel.NOLOCK));
    tp.printThroughput("No lock:   ", true);
  }

  static SelectiveLockRunnable[] buildArray(LockingModel model) {
    SelectiveLockRunnable[] array = new SelectiveLockRunnable[THREAD_COUNT];
    for (int i = 0; i < THREAD_COUNT; i++) {
      array[i] = new SelectiveLockRunnable(model);
    }
    return array;
  }

}
