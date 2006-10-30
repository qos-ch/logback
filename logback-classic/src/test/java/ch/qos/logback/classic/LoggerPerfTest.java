package ch.qos.logback.classic;


import java.net.InetAddress;

import junit.framework.TestCase;
import ch.qos.logback.core.appender.NOPAppender;

public class LoggerPerfTest extends TestCase {

  final static String KAL = "kal";
  String localhostName = null;
  
  public void setUp() throws Exception {
    localhostName = InetAddress.getLocalHost().getCanonicalHostName();
  }
  public void testSpeed() {
    long len = 1000*1000*10;
    loop(len);
    double avg = loop(len);
    
    System.out.println("Running on "+localhostName);
    // check for performance on KAL only
    if(KAL.equals(localhostName)) {
      assertTrue(30 > avg);
    }
    System.out.println("Average log time for disabled statements: "+avg+" nanos.");
  }
  
  double loop(long len) {
    LoggerContext lc = new LoggerContext();
    NOPAppender mopAppender = new NOPAppender();
    mopAppender.start();
    Logger logger = lc.getLogger(this.getClass());
    logger.setLevel(Level.OFF);
    long start = System.nanoTime();
    for(long i = 0; i < len; i++) {
      logger.debug("Toto");
    }
    long end = System.nanoTime();
    return (end-start)/len;
  }
  
}
