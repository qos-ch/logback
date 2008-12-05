package chapter4.sub.sample;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Bar {
  Logger logger = LoggerFactory.getLogger(Bar.class);
  
  public String toString() {
    return "test 123";
  }
  
  public void createLoggingRequest() {
    subMethodToCreateRequest();
  }
  
  //this is done to create a stacktrace of more than one line
  private void subMethodToCreateRequest() {
    logger.error("error-level request", new Exception("test exception"));
  }
}
