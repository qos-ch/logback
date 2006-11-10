package ch.qos.logback.classic.util;

import junit.framework.TestCase;

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.ConsoleAppender;

public class InitializationTest extends TestCase {

  org.slf4j.Logger logger = LoggerFactory.getLogger(InitializationTest.class);
  LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
  Logger root = (Logger) LoggerFactory.getLogger("root");

  protected void setUp() throws Exception {
    super.setUp();
    logger.debug("Hello-didily-odily");

  }

  protected void tearDown() throws Exception {
    super.tearDown();
  }

  public void testAutoconfig() {
    Appender appender = root.getAppender("STDOUT");
    assertNotNull(appender);
    assertTrue(appender instanceof ConsoleAppender);
  }

  public void testReset() {
    {
      Appender appender = root.getAppender("STDOUT");
      assertNotNull(appender);
      assertTrue(appender instanceof ConsoleAppender);
    }
    {
      lc.shutdownAndReset();
      Appender appender = root.getAppender("STDOUT");
      assertNull(appender);
    }

  }
}
