package ch.qos.logback.classic.issue.lbclassic135.lbclassic139;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.core.contention.RunnableWithCounterAndDone;

/**
 * 
 * @author Olivier Cailloux
 * 
 */
public class Accessor extends RunnableWithCounterAndDone {
  private Logger logger = LoggerFactory.getLogger(Accessor.class);

  final Worker worker;

  Accessor(Worker worker) {
    this.worker = worker;
  }

  public void run() {
    System.out.println("enter Accessor.run");
    while (!isDone()) {
      logger.info("Current worker status is: {}.", worker);
    }
    System.out.println("leaving Accessor.run");
  }
}
