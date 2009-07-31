package ch.qos.logback.classic.rolling;

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

public class TimeBasedRollingWithConfigFileTest extends
    ScaffoldingForRollingTests {

  LoggerContext lc = new LoggerContext();
  Logger logger = lc.getLogger(this.getClass());

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
    loadConfig(ClassicTestConstants.JORAN_INPUT_PREFIX + "/rolling/basic.xml");
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
  
  @Override
  protected void addExpectedFileNamedIfItsTime_ByDate(String testId, boolean gzExtension) {
    if (passThresholdTime(nextRolloverThreshold)) {
      addExpectedFileName_ByDate(testId, getDateOfPreviousPeriodsStart(),
          gzExtension);
      recomputeRolloverThreshold(currentTime);
    }
  }
}
