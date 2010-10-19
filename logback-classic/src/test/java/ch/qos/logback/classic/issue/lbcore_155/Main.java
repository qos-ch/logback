package ch.qos.logback.classic.issue.lbcore_155;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.util.StatusPrinter;
import org.slf4j.LoggerFactory;

/**
 * @author Ceki G&uuml;c&uuml;
 */
public class Main {

  public static void main(String[] args) throws InterruptedException {

    Logger logger = (Logger) LoggerFactory.getLogger(Main.class);
    StatusPrinter.print((LoggerContext) LoggerFactory.getILoggerFactory());
    OThread ot = new OThread();
    ot.start();
    Thread.sleep(OThread.WAIT_MILLIS-500);
    logger.info("About to interrupt");
    ot.interrupt();
    logger.info("After interrupt");
    logger.info("Leaving main");

  }
}
