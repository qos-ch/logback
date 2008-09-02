package ch.qos.logback.classic.pattern;

import java.util.Random;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetup;

public class UtilTest {

  int diff = 1024 + new Random().nextInt(10000);

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void withGreenMail() {
    try {
      ServerSetup serverSetup = new ServerSetup(-1, "localhost",
          ServerSetup.PROTOCOL_SMTP);
      GreenMail greenMail = new GreenMail((ServerSetup) null);
      // greenMail.start();
    } catch (Throwable e) {
      // e.printStackTrace();
      StackTraceElement[] stea = e.getStackTrace();
      for (StackTraceElement ste : stea) {
        String className = ste.getClassName();
        PackageInfo pi = Util.getPackageInfo(className);
        System.out.println("  at " + className + "." + ste.getMethodName()
            + "(" + ste.getFileName() + ":" + ste.getLineNumber() + ") ["
            + pi.jarName + ":" + pi.version + "]");
      }
    }
  }

  public void doPerf(boolean versionExtraction) {
    try {
      ServerSetup serverSetup = new ServerSetup(-1, "localhost",
          ServerSetup.PROTOCOL_SMTP);
      GreenMail greenMail = new GreenMail((ServerSetup) null);
      // greenMail.start();
    } catch (Throwable e) {
      StackTraceElement[] stea = e.getStackTrace();
      if (versionExtraction) {
        for (StackTraceElement ste : stea) {
          String className = ste.getClassName();
          PackageInfo pi = Util.getPackageInfo(className);
        }
      }
    }
  }

  double loop(int len, boolean ve) {
    long start = System.nanoTime();
    for (int i = 0; i < len; i++) {
      doPerf(ve);
    }
    return (1.0*System.nanoTime() - start)/len/1000;
  }

  @Test
  @Ignore
  public void perfTest() {
    int len = 1000;
    loop(len, false);
    double d0 = loop(len, false);

    System.out.println("false " + d0);

    loop(len, true);
    double d1 = loop(len, true);

    System.out.println("false " + d1);
  }
}
