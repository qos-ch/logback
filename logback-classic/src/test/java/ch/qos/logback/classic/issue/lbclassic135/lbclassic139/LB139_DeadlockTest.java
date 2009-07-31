package ch.qos.logback.classic.issue.lbclassic135.lbclassic139;

import org.junit.Before;
import org.junit.Test;

import ch.qos.logback.classic.BasicConfigurator;
import ch.qos.logback.classic.LoggerContext;

public class LB139_DeadlockTest {

  LoggerContext loggerContext = new LoggerContext();
  
  @Before
  public void setUp() {
    loggerContext.setName("LB139");
    BasicConfigurator.configure(loggerContext);
  }
  
  @Test(timeout=3000)
  public void test() throws Exception {
    Worker worker = new Worker(loggerContext);
    Accessor accessor = new Accessor(worker, loggerContext);
    
    Thread workerThread = new Thread(worker, "WorkerThread");
    Thread accessorThread = new Thread(accessor, "AccessorThread");
    
    workerThread.start();
    accessorThread.start();

    int sleep = Worker.SLEEP_DUIRATION*10;
    
    System.out.println("Will sleep for "+sleep+" millis");
    Thread.sleep(sleep);
    System.out.println("Done sleeping ("+sleep+" millis)");
    worker.setDone(true);
    accessor.setDone(true);
    
    workerThread.join();
    accessorThread.join();
  }
}
