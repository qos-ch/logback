package ch.qos.logback.classic.spi.special;

import ch.qos.logback.classic.spi.CPDCSpecial;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.PackagingDataCalculator;
import ch.qos.logback.classic.spi.ThrowableProxy;


public class CPDCSpecialImpl implements CPDCSpecial {

  
  Throwable throwable;
  IThrowableProxy throwableProxy;
  
  public void doTest() {
    nesting();
  }
  
  private void nesting() {
    throwable = new Throwable("x");
    throwableProxy = new ThrowableProxy(throwable);
    PackagingDataCalculator pdc = new PackagingDataCalculator();
    pdc.calculate(throwableProxy.getThrowableDataPointArray());
  }
  
  public Throwable getThrowable() {
    return throwable;
  }
  public IThrowableProxy getThrowableProxy() {
    return throwableProxy;
  }
}
