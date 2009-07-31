package ch.qos.logback.classic.rolling;

import static org.junit.Assert.assertTrue;

import java.util.Date;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ch.qos.logback.classic.ClassicTestConstants;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.ScaffoldingForRollingTests;
import ch.qos.logback.core.rolling.TimeBasedFileNamingAndTriggeringPolicy;
import ch.qos.logback.core.rolling.TimeBasedRollingPolicy;
import ch.qos.logback.core.status.StatusChecker;
import ch.qos.logback.core.util.StatusPrinter;

public class TimeBasedRollingWithConfigFileTest extends
    ScaffoldingForRollingTests {

  LoggerContext lc = new LoggerContext();
  Logger logger = lc.getLogger(this.getClass());
  int fileSize = 0;
  int fileIndexCounter = 0;
  int sizeThreshold;
  
  @Before
  @Override
  public void setUp() {
    lc.setName("test");
    super.setUp();
    lc.putProperty("randomOutputDir", randomOutputDir);
  }

  @After
  public void tearDown() throws Exception {
  }

  void loadConfig(String confifFile) throws JoranException {
    JoranConfigurator jc = new JoranConfigurator();
    jc.setContext(lc);
    jc.doConfigure(confifFile);
    currentTime = System.currentTimeMillis();
    recomputeRolloverThreshold(currentTime);
  }

  @Test
  public void basic() throws Exception {
    String testId = "basic";
    lc.putProperty("testId", testId);
    loadConfig(ClassicTestConstants.JORAN_INPUT_PREFIX + "/rolling/"+testId+".xml");
    StatusChecker sc = new StatusChecker(lc);
    assertTrue(sc.isErrorFree());
    
    Logger root = lc.getLogger(Logger.ROOT_LOGGER_NAME);
    
    expectedFilenameList.add(randomOutputDir+"z"+testId);
    
    RollingFileAppender<ILoggingEvent> rfa = (RollingFileAppender<ILoggingEvent>) root
        .getAppender("ROLLING");

    TimeBasedRollingPolicy tprp = (TimeBasedRollingPolicy<ILoggingEvent>) rfa
        .getTriggeringPolicy();
    TimeBasedFileNamingAndTriggeringPolicy tbnatp = tprp.getTimeBasedFileNamingAndTriggeringPolicy();
    

    
    String prefix = "Hello---";
    int runLength = 4;
    for (int i = 0; i < runLength; i++) {
      logger.debug(prefix + i);
      addExpectedFileNamedIfItsTime_ByDate(testId, false);
      incCurrentTime(500);
      tbnatp.setCurrentTime(currentTime);
    }

    existenceCheck(expectedFilenameList);
    sortedContentCheck(randomOutputDir, runLength, prefix); 
  }
  
  @Test
  public void timeAndSize() throws Exception {
    String testId = "timeAndSize";
    lc.putProperty("testId", testId);
    loadConfig(ClassicTestConstants.JORAN_INPUT_PREFIX + "/rolling/"+testId+".xml");
    Logger root = lc.getLogger(Logger.ROOT_LOGGER_NAME);
    
    expectedFilenameList.add(randomOutputDir+"z"+testId);
    
    RollingFileAppender<ILoggingEvent> rfa = (RollingFileAppender<ILoggingEvent>) root
        .getAppender("ROLLING");

    StatusPrinter.print(lc);
    StatusChecker sc = new StatusChecker(lc);
    assertTrue(sc.isErrorFree());

    
    TimeBasedRollingPolicy tprp = (TimeBasedRollingPolicy<ILoggingEvent>) rfa
        .getTriggeringPolicy();
    TimeBasedFileNamingAndTriggeringPolicy tbnatp = tprp.getTimeBasedFileNamingAndTriggeringPolicy();
    

    
    String prefix = "Hello---";
    int runLength = 4;
    for (int i = 0; i < runLength; i++) {
      String msg = prefix + i;
      logger.debug(msg);
      addExpectedFileNamedIfItsTime(testId, msg, false);
      incCurrentTime(500);
      tbnatp.setCurrentTime(currentTime);
    }

    System.out.println(expectedFilenameList);
    existenceCheck(expectedFilenameList);
    sortedContentCheck(randomOutputDir, runLength, prefix); 
  }
 
 
  void addExpectedFileNamedIfItsTime(String testId, String msg,
      boolean gzExtension) {
    fileSize += msg.getBytes().length;

    if (passThresholdTime(nextRolloverThreshold)) {
      fileIndexCounter = 0;
      fileSize = 0;
      addExpectedFileName(testId, getDateOfCurrentPeriodsStart(),
          fileIndexCounter, gzExtension);
      recomputeRolloverThreshold(currentTime);
      return;
    }

    // windows can delay file size changes, so we only allow for
    // fileIndexCounter 0 and 1
    if ((fileIndexCounter < 1) && fileSize > sizeThreshold) {
      addExpectedFileName(testId, getDateOfCurrentPeriodsStart(),
          ++fileIndexCounter, gzExtension);
      fileSize = 0;
      return;
    }
  }

  void addExpectedFileName(String testId, Date date, int fileIndexCounter,
      boolean gzExtension) {

    String fn = randomOutputDir + testId + "-" + SDF.format(date) + "-"
        + fileIndexCounter;
    if (gzExtension) {
      fn += ".gz";
    }
    expectedFilenameList.add(fn);
  }
  
  @Override
  protected void addExpectedFileNamedIfItsTime_ByDate(String testId, boolean gzExtension) {
    if (passThresholdTime(nextRolloverThreshold)) {
      addExpectedFileName_ByDate(testId, getDateOfPreviousPeriodsStart(),
          gzExtension);
      recomputeRolloverThreshold(currentTime);
    }
  }
}
