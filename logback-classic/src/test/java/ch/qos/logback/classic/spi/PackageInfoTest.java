package ch.qos.logback.classic.spi;

import java.util.Random;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetup;

public class PackageInfoTest {

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
      PackageInfoCalculator pic = new PackageInfoCalculator();
      
      ThrowableDataPoint[] tdpa = ThrowableToDataPointArray.convert(e);
      pic.computePackageInfo(tdpa);
      
      
      //for (ThrowableDataPoint ste : stea) {
///
   //   }
    }
  }

  public void doPerf(boolean withPI) {
    try {
      ServerSetup serverSetup = new ServerSetup(-1, "localhost",
          ServerSetup.PROTOCOL_SMTP);
      GreenMail greenMail = new GreenMail((ServerSetup) null);
      // greenMail.start();
    } catch (Throwable e) {
      StackTraceElement[] stea = e.getStackTrace();
      
      if (withPI) {
        PackageInfoCalculator pic = new PackageInfoCalculator();
        for (StackTraceElement ste : stea) {
          String className = ste.getClassName();
          //PackageInfo pi = pic.compute(className);
        }
      }
    }
  }

  double loop(int len, boolean withPI) {
    long start = System.nanoTime();
    for (int i = 0; i < len; i++) {
      doPerf(withPI);
    }
    return (1.0*System.nanoTime() - start)/len/1000;
  }

  @Test
  public void perfTest() {
    int len = 10000;
    loop(len, false);
    loop(len, true);
    
    double d0 = loop(len, false);
    System.out.println("without package info " + d0+" microseconds");

    double d1 = loop(len, true);
    System.out.println("with    package info " + d1 +" microseconds");
  }
}
