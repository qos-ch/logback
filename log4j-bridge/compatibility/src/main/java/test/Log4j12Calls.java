package test;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.apache.log4j.MDC;

public class Log4j12Calls extends TestCase {
  public static final Logger logger = Logger.getLogger(Log4j12Calls.class);
  
  public void testLog() {
    MDC.put("key", "value1");
    
    logger.debug("Entering application");
    logger.info("Violets are blue");
    logger.warn("Here is a warning");
    
    logger.error("Exiting application", new Exception("just testing"));
    
    MDC.remove("key");
  }
}
