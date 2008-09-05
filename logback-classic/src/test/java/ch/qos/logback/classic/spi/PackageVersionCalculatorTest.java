package ch.qos.logback.classic.spi;


import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ch.qos.logback.core.CoreGlobal;

public class PackageVersionCalculatorTest {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void test() throws Exception {
    Throwable t = new Throwable("x");
    //t.printStackTrace();
    ThrowableProxy tp = new ThrowableProxy(t);
    PackageInfoCalculator pic = new PackageInfoCalculator();
    pic.computePackageInfo(tp.getThrowableDataPointArray());
    StringBuilder builder = new StringBuilder();
    for(ThrowableDataPoint tdp: tp.getThrowableDataPointArray()) {
      String string = tdp.toString();
      builder.append(string);
      extraData(builder, tdp);
      builder.append(CoreGlobal.LINE_SEPARATOR);
    }
    System.out.println(builder.toString());
  }
  
  protected void extraData(StringBuilder builder, ThrowableDataPoint tdp) {
    StackTraceElementProxy step = tdp.getStackTraceElementProxy();
    if(step != null) {
      PackageInfo pi = step.getPackageInfo();
      if(pi != null) {
        builder.append(" [").append(pi.getJarName()).append(':').append(pi.getVersion()).append(']');
      }
    }
  }
  
}
