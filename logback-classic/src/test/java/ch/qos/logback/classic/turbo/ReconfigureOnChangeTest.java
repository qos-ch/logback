package ch.qos.logback.classic.turbo;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;

import org.junit.Test;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.issue.lbclassic135.LoggingRunnable;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.classic.util.TeztConstants;
import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.contention.MultiThreadedHarness;
import ch.qos.logback.core.contention.RunnableWithCounterAndDone;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.status.StatusChecker;

public class ReconfigureOnChangeTest {
  LoggerContext loggerContext = new LoggerContext();
  Logger logger = loggerContext.getLogger(this.getClass());
  final static int THREAD_COUNT = 5;

  String SCAN1_FILE_AS_STR = TeztConstants.TEST_DIR_PREFIX
      + "input/turbo/scan1.xml";

  static int TOTAL_TEST_DURATION = 2000;
  // it actually takes time for Windows to propagate file modification changes
  // values below 100 milliseconds can be problematic
  static int SLEEP_BETWEEN_UPDATES = 300;

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
    configure(SCAN1_FILE_AS_STR);
    File file = new File(SCAN1_FILE_AS_STR);
    RunnableWithCounterAndDone[] runnableArray = buildRunnableArray(file);
    harness.execute(runnableArray);

    long expectedRreconfigurations = runnableArray[0].getCounter();

    StatusChecker checker = new StatusChecker(loggerContext);
    //StatusPrinter.print(loggerContext);
    assertTrue(checker.isErrorFree());
    int effectiveResets = checker.matchCount("Resetting and reconfiguring context");
    // the number of effective resets must be equal or less than expectedRreconfigurations
    assertTrue(effectiveResets <=  expectedRreconfigurations);
    // however, there should be some effective resets
    assertTrue((effectiveResets * 1.1) >= (expectedRreconfigurations * 1.0));
  }

  @Test
  public void perfTest() throws MalformedURLException {
    ReconfigureOnChangeFilter rocf = new ReconfigureOnChangeFilter();
    rocf.setContext(loggerContext);
    File file = new File(SCAN1_FILE_AS_STR);
    loggerContext.putObject(CoreConstants.URL_OF_LAST_CONFIGURATION_VIA_JORAN,
        file.toURI().toURL());
    rocf.start();
    assertTrue(rocf.isStarted());
    loggerContext.addTurboFilter(rocf);
    
    final int loopLen = 1000*1000;
    
    loop(loopLen, rocf);
    loop(loopLen, rocf);
    double avg = loop(loopLen, rocf);
    System.out.println(avg);
    // the reference was computed on Orion (Ceki's computer)
    //long referencePerf = 5000;
    //BogoPerf.assertDuration(avg, referencePerf, CoreConstants.REFERENCE_BIPS); 
  }

  public double loop(int loopLen, ReconfigureOnChangeFilter rocf) {
    long start = System.nanoTime();
    for (int i = 0; i < loopLen; i++) {
        //logger.debug("hello");
      rocf.decide(null, logger, Level.DEBUG, " ", null, null);
    }
    long end = System.nanoTime();
    return (end - start) / (1.0d * loopLen);
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
        if (isDone()) {
          return;
        }
        counter++;
        configFile.setLastModified(System.currentTimeMillis());
      }
    }
  }

}
