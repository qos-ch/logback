package ch.qos.logback.core.issue.LOGBACK_849;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import org.junit.Ignore;
import org.junit.Test;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.ContextBase;
import ch.qos.logback.core.util.ExecutorServiceUtil;


public class Basic {

  ExecutorService executor = ExecutorServiceUtil.newExecutorService();
  Context context = new ContextBase();

  @Test(timeout = 100)
  public void withNoSubmittedTasksShutdownNowShouldReturnImmediately() throws InterruptedException {
    executor.shutdownNow();
    executor.awaitTermination(5000, TimeUnit.MILLISECONDS);
  }

  @Ignore
  @Test
  public void withOneSlowTask() throws InterruptedException {
    executor.execute(new InterruptIgnoring(1000));
    Thread.sleep(100);
    ExecutorServiceUtil.shutdown(executor);
  }

  //  InterruptIgnoring ===========================================
  static class InterruptIgnoring implements Runnable {

    int delay;

    InterruptIgnoring(int delay) {
      this.delay = delay;
    }

    public void run() {
      long runUntil = System.currentTimeMillis() + delay;

      while (true) {
        try {
          long sleep = runUntil - System.currentTimeMillis();
          System.out.println("will sleep " + sleep);
          if (sleep > 0) {
            Thread.currentThread().sleep(delay);
          } else {
            return;
          }
        } catch (InterruptedException e) {
          // ignore the exception
        }
      }
    }
  }


}
