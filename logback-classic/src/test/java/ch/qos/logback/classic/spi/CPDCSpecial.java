package ch.qos.logback.classic.spi;

public interface CPDCSpecial {

  public abstract void doTest();

  public abstract Throwable getThrowable();

  public abstract IThrowableProxy getThrowableProxy();

}