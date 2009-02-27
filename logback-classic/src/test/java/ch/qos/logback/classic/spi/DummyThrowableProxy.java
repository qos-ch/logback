package ch.qos.logback.classic.spi;

public class DummyThrowableProxy implements IThrowableProxy {
  
  private String className;
  private String message;
  private int commonFramesCount;
  private ThrowableDataPoint[] throwableDataPointArray;
  private IThrowableProxy cause;
  

  public String getClassName() {
    return className;
  }
  public void setClassName(String className) {
    this.className = className;
  }
  public String getMessage() {
    return message;
  }
  public void setMessage(String message) {
    this.message = message;
  }
  public int getCommonFrames() {
    return commonFramesCount;
  }
  public void setCommonFramesCount(int commonFramesCount) {
    this.commonFramesCount = commonFramesCount;
  }

  public ThrowableDataPoint[] getThrowableDataPointArray() {
    return throwableDataPointArray;
  }
  public void setThrowableDataPointArray(
      ThrowableDataPoint[] throwableDataPointArray) {
    this.throwableDataPointArray = throwableDataPointArray;
  }
  
  public IThrowableProxy getCause() {
    return cause;
  }
  public void setCause(IThrowableProxy cause) {
    this.cause = cause;
  }
  
  


}
