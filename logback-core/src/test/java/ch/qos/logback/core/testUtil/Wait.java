package ch.qos.logback.core.testUtil;

import ch.qos.logback.core.status.StatusChecker;

import java.util.concurrent.ThreadPoolExecutor;

public class Wait {

  static long MAX_WAIT = 10000;

  static public void forActiveCountToEqual(ThreadPoolExecutor executorService, int i) {
    long start = System.currentTimeMillis();
    while (executorService.getActiveCount() != i && withinTimeLimits(start)) {
      try {
        Thread.sleep(10);
        System.out.print(".");
      } catch (InterruptedException e) {
      }
    }
  }
  static public void forStatusMessage(StatusChecker statusChecker, String regex) {
    long start = System.currentTimeMillis();
    while (!statusChecker.containsMatch(regex) && withinTimeLimits(start)) {
      try {
        Thread.sleep(10);

      } catch (InterruptedException e) {
      }
    }
  }

  private static boolean withinTimeLimits(long start) {
    long now = System.currentTimeMillis();
    return (now - start) < MAX_WAIT;
  }

}
