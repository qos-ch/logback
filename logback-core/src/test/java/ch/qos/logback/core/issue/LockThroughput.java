package ch.qos.logback.core.issue;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Short sample code testing the throughput of a fair lock.
 * 
 * @author Joern Huxhorn
 * @author Ceki Gulcu
 */
public class LockThroughput implements Runnable {

  enum LockingModel {
    SYNC, FAIR, UNFAIR;
  }

  static int THREAD_COUNT = 2;
  static long OVERALL_DURATION_IN_MILLIS = 5000;
  static LockThroughput[] RUNNABLE_ARRAY = new LockThroughput[THREAD_COUNT];
  static Thread[] THREAD_ARRAY = new Thread[THREAD_COUNT];
  
  static Object LOCK = new Object();
  static Lock FAIR_LOCK = new ReentrantLock(true);
  static Lock UNFAIR_LOCK = new ReentrantLock(false);
  
  static private int COUNTER = 0;

  LockingModel model;
  boolean done = false;

  public static void main(String args[]) throws InterruptedException {
    printEnvironmentInfo();
    execute(LockingModel.SYNC);
    execute(LockingModel.UNFAIR);
    execute(LockingModel.FAIR);

    COUNTER = 0;
    execute(LockingModel.SYNC);
    cleanUpAndPrintResults("Sync:   ");
    execute(LockingModel.UNFAIR);
    cleanUpAndPrintResults("Unfair: ");
    execute(LockingModel.FAIR);
    cleanUpAndPrintResults("Fair:   ");

  }

  public static void printEnvironmentInfo() {
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

  public static void execute(LockingModel model) throws InterruptedException {
    for (int i = 0; i < THREAD_COUNT; i++) {
      RUNNABLE_ARRAY[i] = new LockThroughput(model);
      THREAD_ARRAY[i] = new Thread(RUNNABLE_ARRAY[i]);
    }
    for (Thread t : THREAD_ARRAY) {
      t.start();
    }
    // let the threads run for a while
    Thread.sleep(OVERALL_DURATION_IN_MILLIS);

    for (int i = 0; i < THREAD_COUNT; i++) {
      RUNNABLE_ARRAY[i].done = true;
    }
  }

  public static void cleanUpAndPrintResults(String model) throws InterruptedException {
    for (int i = 0; i < THREAD_COUNT; i++) {
      THREAD_ARRAY[i].join();
    }
    System.out.println(model + COUNTER+", or "+ ((OVERALL_DURATION_IN_MILLIS*1000*1000L)/COUNTER) +" nanos per cycle");
    COUNTER = 0;
  }

  LockThroughput(LockingModel model) {
    this.model = model;
  }


  void fairLockRun() {
    for (;;) {
      FAIR_LOCK.lock();
      COUNTER++;
      FAIR_LOCK.unlock();
      if (done) {
        return;
      }
    }
  }

  void unfairLockRun() {
    for (;;) {
      UNFAIR_LOCK.lock();
      COUNTER++;
      UNFAIR_LOCK.unlock();
      if (done) {
        return;
      }
    }
  }

  void synchronizedRUn() {
    for (;;) {
      synchronized (LOCK) {
        COUNTER++;
        if (done) {
          return;
        }
      }
    }
  }

  public void run() {
    switch (model) {
    case SYNC:
      synchronizedRUn();
      break;
    case FAIR:
      fairLockRun();
      break;
    case UNFAIR:
      unfairLockRun();
      break;
    }
  }

  public String toString() {
    return "counter=" + COUNTER;
  }

}
