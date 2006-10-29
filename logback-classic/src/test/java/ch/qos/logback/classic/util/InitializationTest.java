package ch.qos.logback.classic.util;

import junit.framework.TestCase;


import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.ConsoleAppender;

public class InitializationTest extends TestCase {

  protected void setUp() throws Exception {
    super.setUp();
  }

  protected void tearDown() throws Exception {
    super.tearDown();
  }
 
  public void test() {
    org.slf4j.Logger logger = LoggerFactory.getLogger(InitializationTest.class);
    logger.debug("Hello-didily-odily");
    
    Logger root = (Logger) LoggerFactory.getLogger("root");
    Appender appender = root.getAppender("STDOUT");
    assertNotNull(appender);
    assertTrue(appender instanceof ConsoleAppender);
  }
}
