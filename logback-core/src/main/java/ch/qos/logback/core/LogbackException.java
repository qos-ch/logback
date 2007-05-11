package ch.qos.logback.core;

public class LogbackException extends RuntimeException {

  private static final long serialVersionUID = -799956346239073266L;

  public LogbackException(String msg) {
    super(msg);
  }
  
  
  public LogbackException(String msg, Throwable nested) {
    super(msg, nested);
  }
  
}
