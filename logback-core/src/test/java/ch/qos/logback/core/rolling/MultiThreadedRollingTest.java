package ch.qos.logback.core.rolling;

import static org.junit.Assert.fail;

import java.io.File;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.ContextBase;
import ch.qos.logback.core.Layout;
import ch.qos.logback.core.contention.MultiThreadedHarness;
import ch.qos.logback.core.contention.RunnableWithCounterAndDone;
import ch.qos.logback.core.layout.EchoLayout;
import ch.qos.logback.core.status.StatusChecker;
import ch.qos.logback.core.testUtil.RandomUtil;
import ch.qos.logback.core.util.CoreTestConstants;
import ch.qos.logback.core.util.StatusPrinter;

public class MultiThreadedRollingTest {

  final static int NUM_THREADS = 5;
  final static int TOTAL_DURATION = 2000;
  
  Layout<Object> layout;
  Context context = new ContextBase();

  int diff = RandomUtil.getPositiveInt();
  String outputDirStr = CoreTestConstants.OUTPUT_DIR_PREFIX + "multi-" + diff
      + "/";

  RollingFileAppender<Object> rfa = new RollingFileAppender<Object>();

  @Before
  public void setUp() throws Exception {
    layout = new EchoLayout<Object>();
    File outputDir = new File(outputDirStr);
    outputDir.mkdirs();
    
    rfa.setName("rolling");
    rfa.setLayout(layout);
    rfa.setContext(context);
    rfa.setFile(outputDirStr + "output.log");
    String datePattern = "yyyy-MM-dd'T'HH_mm_ss_SSS";

    TimeBasedRollingPolicy tbrp = new TimeBasedRollingPolicy();
    tbrp.setFileNamePattern(outputDirStr + "test-%d{" + datePattern + "}");
    tbrp.setContext(context);
    tbrp.setParent(rfa);
    tbrp.start();

    rfa.setRollingPolicy(tbrp);
    rfa.start();
  }

  @After
  public void tearDown() throws Exception {
    rfa.stop();
  }
  
  RunnableWithCounterAndDone[] buildRunnableArray() {
    RunnableWithCounterAndDone[] runnableArray = new RunnableWithCounterAndDone[NUM_THREADS];
    for (int i = 0; i < NUM_THREADS; i++) {
      runnableArray[i] = new RFARunnable(i, rfa);
    }
    return runnableArray;
  }

  @Test
  public void executeHarness() throws InterruptedException {
    MultiThreadedHarness multiThreadedHarness = new MultiThreadedHarness(TOTAL_DURATION);
    RunnableWithCounterAndDone[] runnableArray = buildRunnableArray();
    multiThreadedHarness.execute(runnableArray);
    StatusPrinter.print(context);
    
    StatusChecker checker = new StatusChecker(context.getStatusManager());
    if(!checker.isErrorFree()) {
      fail("errors reported");
      StatusPrinter.print(context);
    }
    
  }

  long diff(long start) {
    return System.currentTimeMillis() - start;
  }

  static class RFARunnable extends RunnableWithCounterAndDone {
    RollingFileAppender<Object> rfa;
    int id;
    RFARunnable(int id, RollingFileAppender<Object> rfa) {
      this.id = id;
      this.rfa = rfa;
    }

    public void run() {
      while (!isDone()) {
        counter++;
        rfa.doAppend(id + " " + counter);
      }
      System.out.println("id="+id + ", counter="+counter + " on exit");
    }
    
  }

}
