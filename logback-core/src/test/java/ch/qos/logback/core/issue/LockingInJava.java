package ch.qos.logback.core.issue;

/**
 * Short sample code illustrating locking policies in the JDK.
 * See http://jira.qos.ch/browse/LBCORE-97 for a discussion.
 * 
 * @author Joern Huxhorn
 * @author Ceki Gulcu
 */
public class LockingInJava implements Runnable {

  static int THREAD_COUNT = 5;
  static Object LOCK = new Object();
  static Runnable[] RUNNABLE_ARRAY = new Runnable[THREAD_COUNT];
  static Thread[] THREAD_ARRAY = new Thread[THREAD_COUNT];

  private int counter = 0;

  public static void main(String args[]) throws InterruptedException {
    printEnvironmentInfo();
    execute();
    printResults();
  }

  public static void printEnvironmentInfo()  {
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

  public static void execute() throws InterruptedException {
    for (int i = 0; i < THREAD_COUNT; i++) {
      RUNNABLE_ARRAY[i] = new LockingInJava();
      THREAD_ARRAY[i] = new Thread(RUNNABLE_ARRAY[i]);
    }
    for(Thread t: THREAD_ARRAY) {
      t.start();
    }
    // let the threads run for a while
    Thread.sleep(10000);
    for(Thread t: THREAD_ARRAY) {
      t.interrupt();
    }
    Thread.sleep(100); // wait a moment for termination, to lazy for join ;)
  }

  public static void printResults() {
    for (int i = 0; i < RUNNABLE_ARRAY.length; i++) {
      System.out.println("runnable[" + i + "]: " + RUNNABLE_ARRAY[i]);
    }
  }

  public void run() {
    for (;;) {
      synchronized (LOCK) {
        counter++;
        try {
          Thread.sleep(10);
        } catch (InterruptedException ex) {
          break;
        }
      }
    }
  }

  public String toString() {
    return "counter=" + counter;
  }

}
