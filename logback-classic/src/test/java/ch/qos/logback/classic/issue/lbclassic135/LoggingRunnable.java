package ch.qos.logback.classic.issue.lbclassic135;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.core.contention.RunnableWithCounterAndDone;

public class LoggingRunnable extends RunnableWithCounterAndDone {

  Logger logger;
  
  LoggingRunnable(Logger logger) {
    this.logger = logger;
  }
  
  public void run() {
    while(!isDone()) {
      logger.info("hello world ABCDEFGHI");
      counter++;
    }
  }

}
