package ch.qos.logback.classic.issue.lbclassic135.lbclassic139;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.core.contention.RunnableWithCounterAndDone;

/**
 * 
 * @author Olivier Cailloux
 * 
 */
public class Worker extends RunnableWithCounterAndDone {
  private Logger logger = LoggerFactory.getLogger(Worker.class);

  private final Object lock = new Object();

  public void run() {
    System.out.println("enter Worker.run");
    while (!isDone()) {
      synchronized (lock) {
        try {
          Thread.sleep(500);
        } catch (InterruptedException exc) {
        }
        logger.info("lock the logger");
      }
    }
    System.out.println("leaving done");
  }

  @Override
  public String toString() {
    synchronized (lock) {
      final StringBuffer buf = new StringBuffer("STATUS");
      return buf.toString();
    }
  }
}