package ch.qos.logback.classic.issue.lbclassic135.lbclassic139;

import org.junit.Test;

public class DeadlockTest {

  @Test(timeout=2000)
  public void test() throws Exception {
    Worker worker = new Worker();
    Accessor accessor = new Accessor(worker);
    
    Thread workerThread = new Thread(worker, "WorkerThread");
    Thread accessorThread = new Thread(accessor, "AccessorThread");
    
    workerThread.start();
    accessorThread.start();

    Thread.sleep(1500);
    
    worker.setDone(true);
    accessor.setDone(true);
    
    workerThread.join();
    accessorThread.join();
  }
}
