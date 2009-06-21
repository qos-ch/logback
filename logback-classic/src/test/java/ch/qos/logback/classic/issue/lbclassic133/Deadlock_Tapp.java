package ch.qos.logback.classic.issue.lbclassic133;

import static org.junit.Assert.assertFalse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Deadlock_Tapp {
  private static Logger s_logger = LoggerFactory.getLogger(Deadlock_Tapp.class);

  public static void main(String[] args) throws Exception {
    s_logger.debug("Starting test.");

    final Worker worker = new Worker();
    final Thread workerThread = new Thread(new Runnable() {
      public void run() {
        while (true) {
          worker.work();
        }
      }
    });
    workerThread.setName("WorkerThread");

    final Thread accessorThread = new Thread(new Runnable() {
      public void run() {
        Accessor a = new Accessor();
        while (true) {
          a.access(worker);
        }
      }
    });
    accessorThread.setName("AccessorThread");

    workerThread.start();
    accessorThread.start();

    workerThread.join(50 * 1000);
    assertFalse("Worker thread seems locked.", workerThread.isAlive());
  }
}
