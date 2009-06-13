package ch.qos.logback.classic.issue.lbclassic133;

import static org.junit.Assert.assertFalse;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeadlockTest {
  private static Logger s_logger = LoggerFactory.getLogger(DeadlockTest.class);

  @Test
  public void deadlockTest() throws Exception {
    s_logger.debug("Starting test.");

    final Worker worker = new Worker();
    final Thread workerThread = new Thread(new Runnable() {
      public void run() {
        worker.work();
      }
    });
    workerThread.setName("WorkerThread");

    final Thread accessorThread = new Thread(new Runnable() {
      public void run() {
        new Accessor().access(worker);
      }
    });
    accessorThread.setName("AccessorThread");

    workerThread.start();
    accessorThread.start();

    workerThread.join(5 * 1000);
    assertFalse("Worker thread seems locked.", workerThread.isAlive());
  }
}
