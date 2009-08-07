package ch.qos.logback.core.rolling;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.ContextBase;
import ch.qos.logback.core.layout.EchoLayout;
import ch.qos.logback.core.testUtil.RandomUtil;
import ch.qos.logback.core.util.CoreTestConstants;

public class TimeBasedRollingWithArchiveRemovalTest {

  Context context = new ContextBase();
  EchoLayout<Object> layout = new EchoLayout<Object>();

  static final String MONTHLY_DATE_PATTERN = "yyyy-MM";
  static final String MONTHLY_CROLOLOG_DATE_PATTERN = "yyyy/MM";

  static final String DAILY_DATE_PATTERN = "yyyy-MM-dd";
  static final String DAILY_CROLOLOG_DATE_PATTERN = "yyyy/MM/dd";

  static final long MILLIS_IN_MINUTE = 60 * 1000;
  static final long MILLIS_IN_HOUR = 60 * MILLIS_IN_MINUTE;
  static final long MILLIS_IN_DAY = 24 * MILLIS_IN_HOUR;
  static final long MILLIS_IN_MONTH = 30 * MILLIS_IN_DAY;

  int diff = RandomUtil.getPositiveInt();
  protected String randomOutputDir = CoreTestConstants.OUTPUT_DIR_PREFIX + diff
      + "/";
  int slashCount;

  // by default tbfnatp is an instance of
  // DefaultTimeBasedFileNamingAndTriggeringPolicy
  TimeBasedFileNamingAndTriggeringPolicy<Object> tbfnatp = new DefaultTimeBasedFileNamingAndTriggeringPolicy<Object>();

  @Before
  public void setUp() throws Exception {
    context.setName("test");
  }

  @After
  public void tearDown() throws Exception {
  }

  int computeSlashCount(String datePattern) {
    int fromIndex = 0;
    int count = 0;
    while (true) {
      int i = datePattern.indexOf('/', fromIndex);
      if (i == -1) {
        break;
      } else {
        count++;
        fromIndex = i + 1;
        if (fromIndex >= datePattern.length()) {
          break;
        }
      }
    }
    return count;
  }

  @Test
  public void montlyRollover() throws Exception {
    slashCount = computeSlashCount(MONTHLY_DATE_PATTERN);
    // large maxPeriod, a 3 times as many number of periods to simulate
    doRollover(randomOutputDir + "clean-%d{" + MONTHLY_DATE_PATTERN + "}.txt",
        MILLIS_IN_MONTH, 20, 20 * 3, expectedCountWithoutDirs(20));
  }

  @Test
  public void montlyRolloverOverManyPeriods() throws Exception {
    // small maxHistory, many periods
    slashCount = computeSlashCount(MONTHLY_CROLOLOG_DATE_PATTERN);
    doRollover(randomOutputDir + "/%d{" + MONTHLY_CROLOLOG_DATE_PATTERN
        + "}/clean.txt.zip", MILLIS_IN_MONTH, 5, 40, expectedCountWithDirs(5));
  }

  @Test
  public void dailyRollover() throws Exception {
    slashCount = computeSlashCount(DAILY_DATE_PATTERN);
    doRollover(
        randomOutputDir + "clean-%d{" + DAILY_DATE_PATTERN + "}.txt.zip",
        MILLIS_IN_DAY, 5, 5 * 3, expectedCountWithoutDirs(5));
  }

  @Test
  public void dailyCronologRollover() throws Exception {
    slashCount = computeSlashCount(DAILY_CROLOLOG_DATE_PATTERN);
    doRollover(randomOutputDir + "/%d{" + DAILY_CROLOLOG_DATE_PATTERN
        + "}/clean.txt.zip", MILLIS_IN_DAY, 8, 8 * 3, expectedCountWithDirs(8));
  }

  @Test
  public void dailySizeBasedRollover() throws Exception {
    SizeAndTimeBasedFNATP<Object> sizeAndTimeBasedFNATP = new SizeAndTimeBasedFNATP<Object>();
    sizeAndTimeBasedFNATP.setMaxFileSize("1");
    tbfnatp = sizeAndTimeBasedFNATP;

    slashCount = computeSlashCount(DAILY_DATE_PATTERN);
    doRollover(randomOutputDir + "/%d{" + DAILY_DATE_PATTERN
        + "}-clean.%i.zip", MILLIS_IN_DAY, 5, 5 * 4, expectedCountWithoutDirs(5));
  }

  
  @Test
  public void dailyChronologSizeBasedRollover() throws Exception {
    SizeAndTimeBasedFNATP<Object> sizeAndTimeBasedFNATP = new SizeAndTimeBasedFNATP<Object>();
    sizeAndTimeBasedFNATP.setMaxFileSize("1");
    tbfnatp = sizeAndTimeBasedFNATP;

    slashCount = computeSlashCount(DAILY_CROLOLOG_DATE_PATTERN);
    doRollover(randomOutputDir + "/%d{" + DAILY_DATE_PATTERN
        + "}/clean.%i.zip", MILLIS_IN_DAY, 5, 5 * 4, xexpectedCountWithDirs_NoSlash(5));
  }

  void doRollover(String fileNamePattern, long periodDurationInMillis,
      int maxHistory, int simulatedNumberOfPeriods, int expectedCount) throws Exception {
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
    tbrp.timeBasedTriggering = tbfnatp;
    tbrp.timeBasedTriggering.setCurrentTime(currentTime);
    tbrp.start();
    rfa.setRollingPolicy(tbrp);
    rfa.start();

    int ticksPerPeriod = 64;
    long runLength = simulatedNumberOfPeriods * ticksPerPeriod;

    for (long i = 0; i < runLength; i++) {
      rfa.doAppend("Hello---" + i);
      tbrp.timeBasedTriggering.setCurrentTime(addTime(tbrp.timeBasedTriggering
          .getCurrentTime(), periodDurationInMillis / ticksPerPeriod));

      if (tbrp.future != null) {
        tbrp.future.get(200, TimeUnit.MILLISECONDS);
      }
    }
    rfa.stop();
    check(expectedCount);
  }

  void recursiveDirectoryDescent(File dir, List<File> fileList,
      final String pattern) {
    if (dir.isDirectory()) {
      File[] match = dir.listFiles(new FileFilter() {
        public boolean accept(File f) {
          if (f.isDirectory()) {
            return true;
          } else {
            return f.getName().contains(pattern);
          }
        }
      });
      for (File f : match) {
        fileList.add(f);
        if (f.isDirectory()) {
          recursiveDirectoryDescent(f, fileList, pattern);
        }
      }
    }
  }

  int expectedCountWithoutDirs(int maxHistory) {
    // maxHistory plus the currently active file
    return maxHistory+1;
  }
  
  int expectedCountWithDirs(int maxHistory) {
    // each slash adds a new directory
    // + one file and one directory per archived log file
    return (maxHistory + 1) * 2 + slashCount;
  }

  int xexpectedCountWithDirs_NoSlash(int maxHistory) {
    // each slash adds a new directory
    // + one file and one directory per archived log file
    return (maxHistory + 1) * 2;
  }
  
  void check(int expectedCount) {
    File dir = new File(randomOutputDir);
    List<File> fileList = new ArrayList<File>();
    recursiveDirectoryDescent(dir, fileList, "clean");
    assertEquals(expectedCount, fileList.size());
  }

  static long addTime(long currentTime, long timeToWait) {
    return currentTime + timeToWait;
  }

}
