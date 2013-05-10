package ch.qos.logback.core.testUtil;

import ch.qos.logback.core.status.StatusChecker;

import java.util.concurrent.ThreadPoolExecutor;

public class Wait {

  static public void forActiveCountToEqual(ThreadPoolExecutor executorService, int i) {
    while (executorService.getActiveCount() != i) {
      try {
        Thread.sleep(10);
        System.out.print(".");
      } catch (InterruptedException e) {
      }
    }
  }
  static public void forStatusMessage(StatusChecker statusChecker, String regex) {
    while (!statusChecker.containsMatch(regex)) {
      try {
        Thread.sleep(10);
        System.out.print(".");
      } catch (InterruptedException e) {
      }
    }
  }

}
