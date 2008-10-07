package ch.qos.logback.core.rolling;




import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.ContextBase;
import ch.qos.logback.core.layout.EchoLayout;
import ch.qos.logback.core.util.Constants;

public class TimeBasedRollingWithCleanTest {

  Context context = new ContextBase();
  EchoLayout<Object> layout = new EchoLayout<Object>();
  
  static final String DATE_PATTERN = "yyyy-MM-dd_HH_mm_ss";
  
  @Before
  public void setUp() throws Exception {
    context.setName("test");
  }

  @After
  public void tearDown() throws Exception {
  }

  
  @Test
  public void smoke() {
    long currentTime = System.currentTimeMillis();
    
    RollingFileAppender<Object> rfa = new RollingFileAppender<Object>();
    rfa.setContext(context);
    rfa.setLayout(layout);
    rfa.setFile(Constants.OUTPUT_DIR_PREFIX + "clean.txt");
    TimeBasedRollingPolicy tbrp = new TimeBasedRollingPolicy();
    tbrp.setContext(context);
    tbrp.setFileNamePattern(Constants.OUTPUT_DIR_PREFIX + "clean-%d{"
        + DATE_PATTERN + "}.txt");
    tbrp.setParent(rfa);
    tbrp.setCurrentTime(currentTime);
    tbrp.start();
    rfa.setRollingPolicy(tbrp);
    rfa.start();
   
    for (int i = 0; i < 10; i++) {
      rfa.doAppend("Hello---" + i);
      tbrp.setCurrentTime(addTime(tbrp.getCurrentTime(), 500));      
    }
   
  }
  
  static long addTime(long currentTime, long timeToWait) {
    return currentTime + timeToWait;
  }

}
