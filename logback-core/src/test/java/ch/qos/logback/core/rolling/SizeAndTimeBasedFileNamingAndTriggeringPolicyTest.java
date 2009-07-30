package ch.qos.logback.core.rolling;

import static org.junit.Assert.assertTrue;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.ContextBase;
import ch.qos.logback.core.layout.EchoLayout;
import ch.qos.logback.core.testUtil.RandomUtil;
import ch.qos.logback.core.util.Compare;
import ch.qos.logback.core.util.CoreTestConstants;

public class SizeAndTimeBasedFileNamingAndTriggeringPolicyTest {
  static final String DATE_PATTERN = "yyyy-MM-dd_HH_mm_ss";

  int diff = RandomUtil.getPositiveInt();
  String randomOutputDir = CoreTestConstants.OUTPUT_DIR_PREFIX + "/" + diff
      + "/";

  SizeAndTimeBasedFileNamingAndTriggeringPolicy<Object> satbfnatPolicy = new SizeAndTimeBasedFileNamingAndTriggeringPolicy<Object>();

  SimpleDateFormat sdf = new SimpleDateFormat(DATE_PATTERN);

  EchoLayout<Object> layout = new EchoLayout<Object>();
  Context context = new ContextBase();

  RollingFileAppender<Object> rfa1 = new RollingFileAppender<Object>();
  TimeBasedRollingPolicy<Object> tbrp1 = new TimeBasedRollingPolicy<Object>();
  Calendar cal = Calendar.getInstance();
  long currentTime; // initialized in setUp()
  long nextRolloverThreshold; // initialized in setUp()
  List<String> expectedFilenameList = new ArrayList<String>();

  int fileSize = 0;
  int fileIndexCounter = 0;
  int sizeThreshold;
  
  @Before
  public void setUp() {
    context.setName("test");
    cal.set(Calendar.MILLISECOND, 333);
    currentTime = cal.getTimeInMillis();
    recomputeRolloverThreshold(currentTime);
    System.out.println("at setUp() currentTime="
        + sdf.format(new Date(currentTime)));

  }

  // assuming rollover every second
  void recomputeRolloverThreshold(long ct) {
    long delta = ct % 1000;
    nextRolloverThreshold = (ct - delta) + 1000;
  }

  void initRFA(RollingFileAppender<Object> rfa, String filename) {
    rfa.setContext(context);
    rfa.setLayout(layout);
    if (filename != null) {
      rfa.setFile(filename);
    }
  }

  void initTRBP(RollingFileAppender<Object> rfa, TimeBasedRollingPolicy<Object> tbrp,
      String filenamePattern, int sizeThreshold, long givenTime, long lastCheck) {

    tbrp.setContext(context);
    satbfnatPolicy.setMaxFileSize(""+sizeThreshold);
    tbrp.setTimeBasedTriggering(satbfnatPolicy);
    tbrp.setFileNamePattern(filenamePattern);
    tbrp.setParent(rfa);
    tbrp.timeBasedTriggering.setCurrentTime(givenTime);
    if (lastCheck != 0) {
      tbrp.timeBasedTriggering.setDateInCurrentPeriod(new Date(lastCheck));
    }
    rfa.setRollingPolicy(tbrp);
    tbrp.start();
    rfa.start();
  }

  @Test
  public void noCompression_FileBSet_NoRestart_1() throws Exception {
    String testId = "test1";
    System.out.println(randomOutputDir);
    String file = randomOutputDir + "toto.log";
    initRFA(rfa1, file);
    sizeThreshold = 300;
    initTRBP(rfa1, tbrp1, randomOutputDir + testId + "-%d{" + DATE_PATTERN
        + "}-%i.txt", sizeThreshold, currentTime, 0);

    addExpectedFileName(testId, getDateOfCurrentPeriodsStart(), fileIndexCounter, false);

    incCurrentTime(100);
    tbrp1.timeBasedTriggering.setCurrentTime(currentTime);

    for (int i = 0; i < 100; i++) {
      String msg = "Hello -----------------" + i;
      rfa1.doAppend(msg);
      addExpectedFileNamedIfItsTime(testId, msg, false);
      incCurrentTime(20);
      tbrp1.timeBasedTriggering.setCurrentTime(currentTime);
    }
    

    massageExpectedFilesToCorresponToCurrentTarget(file);
    int i = 0;
    for (String fn : expectedFilenameList) {
      System.out.println(fn);
      //assertTrue(Compare.compare(fn, CoreTestConstants.TEST_DIR_PREFIX
      //    + "witness/rolling/satb-test1." + i++));
    }
  }
  
  void massageExpectedFilesToCorresponToCurrentTarget(String file) {
    // we added one too many files by date
    expectedFilenameList.remove(expectedFilenameList.size() - 1);
    expectedFilenameList.add(file);
  }
  
  boolean passThresholdTime(long nextRolloverThreshold) {
    return currentTime >= nextRolloverThreshold;
  }
  
  void addExpectedFileNamedIfItsTime(String testId, String msg, boolean gzExtension) {
    fileSize += msg.getBytes().length;
    
    if (passThresholdTime(nextRolloverThreshold)) {
      fileIndexCounter = 0;
      fileSize = 0;
      addExpectedFileName(testId, getDateOfCurrentPeriodsStart(), fileIndexCounter,
          gzExtension);
      recomputeRolloverThreshold(currentTime);
      return;
    }
    
    // windows can delay file size changes
    if((fileIndexCounter <= 1) && fileSize > sizeThreshold) {
      addExpectedFileName(testId, getDateOfCurrentPeriodsStart(), ++fileIndexCounter,
          gzExtension);
      fileSize = 0;
      return;
    }
    
  }

  void addExpectedFileName(String testId, Date date, int fileIndexCounter, boolean gzExtension) {
    String fn = CoreTestConstants.OUTPUT_DIR_PREFIX + testId + "-" + sdf.format(date)+"-"+fileIndexCounter+".txt";
    if (gzExtension) {
      fn += ".gz";
    }
    expectedFilenameList.add(fn);
  }
  
  Date getDateOfCurrentPeriodsStart() {
    long delta = currentTime % 1000;
    return new Date(currentTime - delta);
  }
  
  void incCurrentTime(long increment) {
    currentTime += increment;
  }
}
