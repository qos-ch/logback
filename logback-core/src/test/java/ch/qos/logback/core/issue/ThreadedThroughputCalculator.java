package ch.qos.logback.core.issue;

/**
 * 
 * Useful scaffolding to measure the throughput of certain operations when
 * invoked by multiple threads.
 * 
 * @author Joern Huxhorn
 * @author Ralph Goers
 * @author Ceki Gulcu
 */
public class ThreadedThroughputCalculator {

  RunnableForThrougputComputation[] runnableArray;
  Thread[] threadArray;
  final long overallDurationInMillis;

  public ThreadedThroughputCalculator(long overallDurationInMillis) {
    this.overallDurationInMillis = overallDurationInMillis;
  }

  public void printEnvironmentInfo(String msg) {
    System.out.println("=== "+ msg +" ===");
    System.out.println("java.runtime.version = "
        + System.getProperty("java.runtime.version"));
    System.out.println("java.vendor          = "
        + System.getProperty("java.vendor"));
    System.out.println("java.version         = "
        + System.getProperty("java.version"));
    System.out.println("os.name              = "
        + System.getProperty("os.name"));
    System.out.println("os.version           = "
        + System.getProperty("os.version"));
  }

  public void execute(RunnableForThrougputComputation[] runnableArray)
      throws InterruptedException {
    this.runnableArray = runnableArray;
    Thread[] threadArray = new Thread[runnableArray.length];

    for (int i = 0; i < runnableArray.length; i++) {
      threadArray[i] = new Thread(runnableArray[i]);
    }
    for (Thread t : threadArray) {
      t.start();
    }
    // let the threads run for a while
    Thread.sleep(overallDurationInMillis);

    for (RunnableForThrougputComputation r : runnableArray) {
      r.setDone(true);
    }
    for (Thread t : threadArray) {
      t.join();
    }
  }

  public void printThroughput(String msg) throws InterruptedException {
    int sum = 0;
    for (RunnableForThrougputComputation r : runnableArray) {
      sum += r.getCounter();
    }
    System.out.println(msg + "total of " + sum + " operations, or "
        + (sum / overallDurationInMillis) + " operations per millisecond");
  }

}
