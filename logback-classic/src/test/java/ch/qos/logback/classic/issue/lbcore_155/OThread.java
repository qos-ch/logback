package ch.qos.logback.classic.issue.lbcore_155;


import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.util.StatusPrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Ceki G&uuml;c&uuml;
 */
public class OThread extends Thread {


  static int NANOS_IN_MILLI = 1000 * 1000;

  static int WAIT_MILLIS = 10;

  Logger logger = LoggerFactory.getLogger(this.getClass());

  public void run() {

    while (!isInterrupted()) {
      long start = System.nanoTime();
      for (long now = System.nanoTime(); now < start + 2*WAIT_MILLIS*NANOS_IN_MILLI; now = System.nanoTime()) {
        logger.info("in time loop");
      }

      logger.info("before 2nd sleep");
     
      try {
        sleep(1000);
      } catch (InterruptedException e) {
        logger.info("While sleeping", e);
        e.printStackTrace();
        break;
      }
      logger.info("after sleep");
    }
    logger.info("exiting WHILE");

  }
}
