package ch.qos.logback.classic.turbo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.issue.lbclassic135.LoggingRunnable;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.classic.util.TeztConstants;

import ch.qos.logback.core.contention.MultiThreadedHarness;
import ch.qos.logback.core.contention.RunnableWithCounterAndDone;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.status.StatusChecker;
import ch.qos.logback.core.util.StatusPrinter;

public class ReconfigureOnChangeTest {
  LoggerContext loggerContext = new LoggerContext();
  Logger logger = loggerContext.getLogger(this.getClass());
  final static int THREAD_COUNT = 3;

  static int TOTAL_TEST_DURATION = 10000;
  static int SLEEP_BETWEEN_UPDATES = 1000;
  
  MultiThreadedHarness harness = new MultiThreadedHarness(TOTAL_TEST_DURATION);
  
  void configure(String file) throws JoranException {
    JoranConfigurator jc = new JoranConfigurator();
    jc.setContext(loggerContext);
    jc.doConfigure(file);
  }

  RunnableWithCounterAndDone[] buildRunnableArray(File configFile) {

    RunnableWithCounterAndDone[] rArray = new RunnableWithCounterAndDone[THREAD_COUNT];
    rArray[0] = new Updater(configFile);
    for (int i = 1; i < THREAD_COUNT; i++) {
      rArray[i] = new LoggingRunnable(logger);
    }
    return rArray;
  }

  // Tests whether ConfigurationAction is installing ReconfigureOnChangeFilter
  @Test
  public void scan1() throws JoranException, IOException, InterruptedException {

    String configFileAsStr = TeztConstants.TEST_DIR_PREFIX
        + "input/turbo/scan1.xml";
    configure(configFileAsStr);

    File file = new File(configFileAsStr);
    RunnableWithCounterAndDone[] runnableArray = buildRunnableArray(file);
    harness.execute(runnableArray);
    
    long expectedRreconfigurations = runnableArray[0].getCounter();
    
    StatusChecker checker = new StatusChecker(loggerContext);
    StatusPrinter.print(loggerContext);
    assertTrue(checker.isErrorFree());
    int result = checker.matchCount("Resetting and reconfiguring context");
    assertEquals(expectedRreconfigurations, result);
   
  }

  class Updater extends RunnableWithCounterAndDone {
    File configFile;
    Updater(File configFile) {
      this.configFile = configFile;
    }
    public void run() {
      while (!isDone()) {
        try {
          Thread.sleep(SLEEP_BETWEEN_UPDATES);
        } catch (InterruptedException e) {
        }
        if(isDone()) {
          return;
        }
        counter++;
        configFile.setLastModified(System.currentTimeMillis());
      }
    }

  }

}
