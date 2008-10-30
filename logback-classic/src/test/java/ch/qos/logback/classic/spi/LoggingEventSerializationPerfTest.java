package ch.qos.logback.classic.spi;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.ObjectOutputStream;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.helpers.BogoPerf;

import ch.qos.logback.classic.net.NOPOutputStream;
import ch.qos.logback.classic.net.testObjectBuilders.Builder;
import ch.qos.logback.classic.net.testObjectBuilders.LoggingEventWithParametersBuilder;
import ch.qos.logback.classic.net.testObjectBuilders.TrivialLoggingEventBuilder;
import ch.qos.logback.core.CoreConstants;

public class LoggingEventSerializationPerfTest {

  static int LOOP_LEN = 10 * 1000;

  NOPOutputStream noos = new NOPOutputStream();
  ObjectOutputStream oos;

  @Before
  public void setUp() throws Exception {
    oos = new ObjectOutputStream(noos);
   
  }

  @After
  public void tearDown() throws Exception {
  }

  double doLoop(Builder builder, int loopLen) {
    long start = System.nanoTime();
    int resetCounter = 0;
    for (int i = 0; i < loopLen; i++) {
      try {
        oos.writeObject(builder.build(i));
        oos.flush();
        if (++resetCounter >= CoreConstants.OOS_RESET_FREQUENCY) {
          oos.reset();
          resetCounter = 0;
        }

      } catch (IOException ex) {
        fail(ex.getMessage());
      }
    }
    long end = System.nanoTime();
    return (end - start) / (1.0d * loopLen);
  }

  @Test
  public void testPerformance() {
    TrivialLoggingEventBuilder builder = new TrivialLoggingEventBuilder();

    doLoop(builder, LOOP_LEN);
    noos.reset();
    double avg = doLoop(builder, LOOP_LEN);

                         
    long actualSize = (long) (noos.size()/(1024*1.1d));
    double baosSizeLimit = 500;

    assertTrue("baos size" + actualSize + " should be less than "
        + baosSizeLimit, baosSizeLimit > actualSize);

    // the reference was computed on Orion (Ceki's computer)
    long referencePerf = 5000;
    BogoPerf.assertDuration(avg, referencePerf, CoreConstants.REFERENCE_BIPS);
  }
  
  
  @Test
  public void testPerformanceWithParameters() {
    LoggingEventWithParametersBuilder builder = new LoggingEventWithParametersBuilder();

    doLoop(builder, LOOP_LEN);
    noos.reset();
    double avg = doLoop(builder, LOOP_LEN);

    long actualSize = (long) (noos.size()/(1024*1.1d));
    
    double baosSizeLimit = 1300;
    assertTrue("actualSize " + actualSize + " should be less than "
        + baosSizeLimit, baosSizeLimit > actualSize);

    // the reference was computed on Orion (Ceki's computer)
    long referencePerf = 7000;
    BogoPerf.assertDuration(avg, referencePerf, CoreConstants.REFERENCE_BIPS);
  }
}
