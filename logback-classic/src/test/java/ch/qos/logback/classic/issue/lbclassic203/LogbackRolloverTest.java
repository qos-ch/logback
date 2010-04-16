package ch.qos.logback.classic.issue.lbclassic203;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogbackRolloverTest extends Thread {
  private static Logger log = LoggerFactory
      .getLogger(LogbackRolloverTest.class);
  private static final int DEFAULT_THREAD_COUNT = 2;
  private static int threadcount = DEFAULT_THREAD_COUNT;

  public static void main(String[] args) {
    if (args.length > 0) {
      try {
        threadcount = Integer.parseInt(args[0]);
      } catch (Exception e) {
        System.out
            .println("Usage: LogbackRolloverTest [<thread count>] (defaults to "
                + DEFAULT_THREAD_COUNT + " threads)");
        System.exit(1);
      }
    }
    System.out.println("Logging from " + threadcount
        + (threadcount == 1 ? " thread" : " threads")
        + " (optional thread count parameter defaults to "
        + DEFAULT_THREAD_COUNT + ")");

    for (int i = 0; i < threadcount; i++) {
      new LogbackRolloverTest().start();
    }
  }

  public void run() {
    int count = 0;
    while (true) {
      doLog(count++);
    }
  }

  private void doLog(int count) {
    log.debug("The current date is " + new Date() + " the count is " + count);
    log.info("Info Message");
    log.warn("Warn Message");
    log.trace("Trace  message");
    log.error("Error message");
    log.info("Info Message");
    log.warn("Warn Message");
    log.trace("Trace  message");
    log.error("Error message");
    try {
      throw new Exception("throwing exception " + count
          + " to create a stack trace");
    } catch (Exception e) {
      log.error("caught " + e.toString(), e);
    }
    if (count % 10 == 0) {
      try {
        Thread.sleep(500);
      } catch (InterruptedException e) {
        log.error("Thread interrupted: " + e.toString(), e);
      }
    }

  }
}
