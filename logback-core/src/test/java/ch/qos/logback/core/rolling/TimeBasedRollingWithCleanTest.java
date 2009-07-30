package ch.qos.logback.core.rolling;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FilenameFilter;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.ContextBase;
import ch.qos.logback.core.layout.EchoLayout;
import ch.qos.logback.core.util.CoreTestConstants;

public class TimeBasedRollingWithCleanTest {

  Context context = new ContextBase();
  EchoLayout<Object> layout = new EchoLayout<Object>();

  static final String MONTHLY_DATE_PATTERN = "yyyy-MM";
  static final String DAILY_DATE_PATTERN = "yyyy-MM-dd";

  static final long MILLIS_IN_MINUTE = 60 * 1000;
  static final long MILLIS_IN_HOUR = 60 * MILLIS_IN_MINUTE;
  static final long MILLIS_IN_DAY = 24 * MILLIS_IN_HOUR;
  static final long MILLIS_IN_MONTH = 30 * MILLIS_IN_DAY;

  @Before
  public void setUp() throws Exception {
    context.setName("test");

    // remove all files containing the string 'clean'
    File dir = new File(CoreTestConstants.OUTPUT_DIR_PREFIX);
    if (dir.isDirectory()) {
      File[] toDelete = dir.listFiles(new FilenameFilter() {
        public boolean accept(File dir, String name) {
          return name.contains("clean");
        }
      });
      for (File f : toDelete) {
        System.out.println(f);
        f.delete();
      }
    }
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void montlyRollover() throws Exception {
    doRollover(CoreTestConstants.OUTPUT_DIR_PREFIX + "clean-%d{" + MONTHLY_DATE_PATTERN
        + "}.txt", MILLIS_IN_MONTH, 20);

  }

  @Test
  public void dailyRollover() throws Exception {
    doRollover(CoreTestConstants.OUTPUT_DIR_PREFIX + "clean-%d{" + DAILY_DATE_PATTERN
        + "}.txt.zip", MILLIS_IN_DAY, 5);
  }

  void doRollover(String fileNamePattern, long delay, int maxHistory)
      throws Exception {
    long currentTime = System.currentTimeMillis();

    RollingFileAppender<Object> rfa = new RollingFileAppender<Object>();
    rfa.setContext(context);
    rfa.setLayout(layout);
    // rfa.setFile(Constants.OUTPUT_DIR_PREFIX + "clean.txt");
    TimeBasedRollingPolicy<Object> tbrp = new TimeBasedRollingPolicy<Object>();
    tbrp.setContext(context);
    tbrp.setFileNamePattern(fileNamePattern);

    tbrp.setMaxHistory(maxHistory);
    tbrp.setParent(rfa);
    tbrp.timeBasedTriggering = new DefaultTimeBasedFileNamingAndTriggeringPolicy<Object>();
    tbrp.timeBasedTriggering.setCurrentTime(currentTime);
    tbrp.start();
    rfa.setRollingPolicy(tbrp);
    rfa.start();

    for (int i = 0; i < maxHistory * 3; i++) {
      rfa.doAppend("Hello---" + i);
      tbrp.timeBasedTriggering.setCurrentTime(addTime(tbrp.timeBasedTriggering.getCurrentTime(), delay / 2));
      if (tbrp.future != null) {
        tbrp.future.get(200, TimeUnit.MILLISECONDS);
      }
    }
    rfa.stop();
    check(maxHistory + 1);
  }

  void check(int expectedCount) {
    // remove all files containing the string 'clean'
    File dir = new File(CoreTestConstants.OUTPUT_DIR_PREFIX);
    if (dir.isDirectory()) {
      File[] match = dir.listFiles(new FilenameFilter() {
        public boolean accept(File dir, String name) {
          return name.contains("clean");
        }
      });
      //System.out.println(Arrays.toString(match));
      assertEquals(expectedCount, match.length);
    }

  }

  static long addTime(long currentTime, long timeToWait) {
    return currentTime + timeToWait;
  }

}
